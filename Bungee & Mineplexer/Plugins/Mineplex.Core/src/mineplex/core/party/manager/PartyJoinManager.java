package mineplex.core.party.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.chat.ChatChannel;
import mineplex.core.chat.event.FormatPlayerChatEvent;
import mineplex.core.chat.format.ChatFormatComponent;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.party.Lang;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.constants.PartyRemoveReason;
import mineplex.core.party.rediscommands.PartyTransferRequest;
import mineplex.core.party.rediscommands.PartyTransferResponse;
import mineplex.core.portal.Intent;
import mineplex.core.portal.events.GenericServerTransferEvent;
import mineplex.core.portal.events.ServerTransferEvent;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.utils.UtilGameProfile;
import mineplex.serverdata.commands.ServerCommandManager;
import mineplex.serverdata.data.MinecraftServer;

/**
 * Manages allocating slots for parties to join
 */
public class PartyJoinManager implements Listener
{

	private final TwoFactorAuth _twofactor = Managers.require(TwoFactorAuth.class);
	private final PartyManager _plugin;
	private final int _maxPlayers;
	private final ChatFormatComponent[] _partyComponents =
			{
					sender ->
					{
						TextComponent component = new TextComponent("PARTY");
						component.setColor(ChatColor.DARK_PURPLE);
						component.setBold(true);
						return component;
					},
					sender ->
					{
						TextComponent component = new TextComponent(sender.getName());
						component.setColor(ChatColor.WHITE);
						component.setBold(true);
						return component;
					}
			};
	private final Map<UUID, BukkitTask> _pendingTransfers = new ConcurrentHashMap<>();

	public PartyJoinManager(PartyManager plugin)
	{
		_plugin = plugin;
		_maxPlayers = plugin.getPlugin().getServer().getMaxPlayers();

		UtilServer.RegisterEvents(this);

		ServerCommandManager.getInstance().registerCommandType(PartyTransferRequest.class, command ->
		{
			if (!command.isSentToThisServer())
				return;

			_plugin.runSync(() ->
			{
				int currentPlayers = UtilServer.getPlayersCollection().size();
				if (currentPlayers + command.getUnrankedMembers().size() > _maxPlayers && !command.isCanJoinFullServers())
				{
					ServerCommandManager.getInstance().publishCommand(new PartyTransferResponse(command, PartyTransferResponse.Result.NOT_ENOUGH_ROOM));
					return;
				}

				for (UUID uuid : command.getUnrankedMembers())
				{
					_plugin.getClientManager().reserveFor(uuid);
				}

				Party newParty = new Party(command);

				_plugin.addParty(newParty);

				for (UUID uuid : command.getAllMembers())
				{
					_plugin.putIntoPendingJoin(uuid, newParty.getUniqueId());
				}

				ServerCommandManager.getInstance().publishCommand(new PartyTransferResponse(command, PartyTransferResponse.Result.SUCCESS));
			});
		});

		ServerCommandManager.getInstance().registerCommandType(PartyTransferResponse.class, command ->
		{
			if (!command.getOrigin().wasSentFromThisServer())
				return;

			_plugin.runSync(() ->
			{
				BukkitTask task = _pendingTransfers.get(command.getOrigin().getPartyUUID());
				if (task == null)
				{
					return;
				}

				task.cancel();

				Party party = _plugin.getPartyById(command.getOrigin().getPartyUUID());

				switch (command.getResult())
				{
					case NOT_ENOUGH_ROOM:
						_pendingTransfers.remove(command.getOrigin().getPartyUUID());
						party.sendMessage(F.main("Party", "Sorry, there wasn't enough room for your party in " + F.elem(command.getFromServer()) + "."));
						break;
					case SUCCESS:
						party.sendMessage(F.main("Party", "Success! I've reserved some room for you in " + F.elem(command.getFromServer()) + "."));
						party.sendMessage(F.main("Party", "You will be transferred shortly."));
						List<Player> members = new ArrayList<>(party.getMembers());

						// Clear the party first.
						// We've already got a duplicate on the remote server so let's get rid of this one ASAP
						party.clear();
						_plugin.removeParty(party);


						for (Player player : members)
						{
							_plugin.getPortal().sendPlayer(player, command.getFromServer());
						}
						break;
					case UNKNOWN:
						_pendingTransfers.remove(command.getOrigin().getPartyUUID());
						party.sendMessage(F.main("Party", "Uh... something went wrong?"));
						break;
				}
			});
		});
	}

	/**
	 * Initiates a request to join a server for a specific party
	 *
	 * @param server The desired server
	 * @param party  The requesting party
	 */
	private void requestServerJoin(String server, Party party)
	{
		if (_pendingTransfers.containsKey(party.getUniqueId()))
		{
			party.sendMessage(F.main("Party", "Please wait until your current transfer is complete"));
			return;
		}

		List<UUID> unranked = new ArrayList<>();
		List<UUID> all = new ArrayList<>();
		for (Player player : party.getMembers())
		{
			if (!_plugin.getClientManager().Get(player).hasPermission(CoreClientManager.Perm.JOIN_FULL))
			{
				unranked.add(player.getUniqueId());
			}
			all.add(player.getUniqueId());
		}

		_pendingTransfers.put(party.getUniqueId(), _plugin.runSyncLater(() ->
		{
			_pendingTransfers.remove(party.getUniqueId());

			if (party.getMembers().size() == 0)
			{
				return;
			}

			party.sendMessage(F.main("Party", "Aww, the destination server didn't respond :("));
		}, 20 * 5L));

		System.out.println("Sending transfer request to " + server + " for " + party.getUniqueId() + " " + party.getMembers());

		party.sendMessage(F.main("Party", "Please wait while I check whether " + F.elem(server) + " has enough room for this party."));
		ServerCommandManager.getInstance().publishCommand(new PartyTransferRequest(party.getUniqueId(), UtilGameProfile.getGameProfile(party.getOwnerAsPlayer().get()), all, unranked, unranked.size() == 0, server));
	}

	@EventHandler
	public void onTransferToGenericServer(GenericServerTransferEvent event)
	{
		Player player = event.getPlayer();
		Party party = _plugin.getPartyByPlayer(player);

		if (party == null)
		{
			return;
		}

		// If the server wants to kick the player, let's not stop it
		if (event.getIntent() == Intent.KICK || event.getIntent() == Intent.FORCE_TRANSFER)
		{
			_plugin.removeFromParty(event.getPlayer(), PartyRemoveReason.KICKED);

			return;
		}

		event.setCancelled(true);

		if (!party.getOwnerName().equalsIgnoreCase(player.getName()))
		{
			Lang.NOT_OWNER_SERVER.send(player);
			return;
		}

		MinecraftServer best = null;
		int lowest = Integer.MAX_VALUE;
		List<MinecraftServer> serverList = Lists.newArrayList(_plugin.getPortal().getRepository().getServersByGroup(event.getServer().getName()));
		for (MinecraftServer server : serverList)
		{
			int playercount = server.getPlayerCount();
			if (playercount < 20)
			{
				continue;
			}
			if (playercount < lowest)
			{
				lowest = playercount;
				if (best == null)
				{
					best = server;
				}
			}
		}
		if (best == null)
		{
			best = serverList.get(new Random().nextInt(serverList.size()));
		}

		party.sendMessage(F.main("Party", F.elem(player.getName()) + " is moving the party to " + F.elem(best.getName())));

		requestServerJoin(best.getName(), party);
	}

	@EventHandler(ignoreCancelled = true)
	public void onTransfer(ServerTransferEvent event)
	{
		Player player = event.getPlayer();
		Party party = _plugin.getPartyByPlayer(player);

		if (party == null)
		{
			return;
		}

		// If the server wants to kick the player, let's not stop it
		if (event.getIntent() == Intent.KICK || event.getIntent() == Intent.FORCE_TRANSFER)
		{
			_plugin.removeFromParty(event.getPlayer(), PartyRemoveReason.KICKED);

			return;
		}

		if (_pendingTransfers.containsKey(party.getUniqueId()))
		{
			return;
		}

		event.setCancelled(true);

		if (event.getServer().toUpperCase().startsWith("CLANS-"))
		{
			party.sendMessage(F.main("Party", "You cannot join a Clans server while in a party!"));
			return;
		}

		if (!party.getOwnerName().equalsIgnoreCase(player.getName()))
		{
			Lang.NOT_OWNER_SERVER.send(player);
			return;
		}

		party.sendMessage(F.main("Party", F.elem(player.getName()) + " is moving the party to " + F.elem(event.getServer())));

		requestServerJoin(event.getServer(), party);
	}

	public void removePendingJoin(Party party)
	{
		_pendingTransfers.remove(party.getUniqueId());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (_plugin.hasPendingJoin(player))
		{
			Party pendingParty = _plugin.getPendingParty(player);
			_plugin.removePendingJoin(player);

			if (pendingParty == null)
			{
				UtilPlayer.message(player, F.main("Party", "Uh oh. It seems that in the time it took for you to join this server, your inviter has disbanded their party"));
				return;
			}

			if (pendingParty.getOwner().getId().equals(event.getPlayer().getUniqueId()))
			{
				UtilPlayer.message(player, F.main("Party", "Welcome back! Your party is just as you've left it!"));
			}
			else
			{
				UtilPlayer.message(player, F.main("Party", "Welcome! Adding you to " + F.elem(pendingParty.getOwnerName()) + "'s party!"));
			}

			_plugin.addToParty(player, pendingParty);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		Party party = _plugin.getPartyByPlayer(player);

		_plugin.getInviteManager().removeAll(player);

		if (party == null)
		{
			return;
		}

		PartyRemoveReason reason = PartyRemoveReason.LEFT;

		_plugin.removeFromParty(player, reason);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEntityEvent event)
	{
		if (UtilItem.isSimilar(event.getPlayer().getItemInHand(), PartyManager.INTERFACE_ITEM, UtilItem.ItemAttribute.NAME))
		{
			event.setCancelled(true);
			event.getPlayer().updateInventory();
			CommandCenter.getCommands().get("party").Execute(event.getPlayer(), new String[0]);
		}
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event)
	{
		if (_twofactor.isAuthenticating(event.getPlayer()))
		{
			return;
		}
		if (UtilItem.isSimilar(event.getItem(), PartyManager.INTERFACE_ITEM, UtilItem.ItemAttribute.NAME))
		{
			event.setCancelled(true);
			CommandCenter.getCommands().get("party").Execute(event.getPlayer(), new String[0]);
		}
	}

	@EventHandler
	public void onChat(FormatPlayerChatEvent event)
	{
		if (event.getChatChannel() != ChatChannel.PARTY)
		{
			return;
		}

		Party party = _plugin.getPartyByPlayer(event.getPlayer());

		if (party == null)
		{
			event.getPlayer().sendMessage(F.main(_plugin.getName(), "You are not in a Party!"));
			event.setCancelled(true);
			return;
		}

		event.setMessageColour(ChatColor.LIGHT_PURPLE);
		event.getRecipients().removeIf(other -> !party.isMember(other));
		event.getFormatComponents().clear();
		Collections.addAll(event.getFormatComponents(), _partyComponents);
	}
}