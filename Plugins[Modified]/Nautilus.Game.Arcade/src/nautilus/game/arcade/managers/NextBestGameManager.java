package nautilus.game.arcade.managers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.status.GameInfo;
import mineplex.core.game.status.GameInfo.GameJoinStatus;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.party.Lang;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.servers.ServerManager;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.minestrike.Minestrike;
import nautilus.game.arcade.game.games.paintball.Paintball;
import nautilus.game.arcade.game.games.wither.WitherGame;

/**
 * Controls a speed-up feature designed to keep players playing.
 * Using the current best resources we have (would be ideal to have instant, instead of delayed, server responses),
 * this will attempt to find the best server based off the group that he is currently playing on (I.E MicroBattles - MICRO).
 * If no server is found, a random one will be selected based off of other *good* choices, but not ideal.
 * For the sake of sanity, there is not point re-filtering the populated list of possible other servers, as it most likely won't have been updated
 * in redis, as it takes a second or two to update. When this changes, re-filtering, and not stopping until the *best* server is found, is feasible.
 * <p>
 * Player's can control how they are sent between games, whether it be clicking text, clicking an item, or automatically going into the next game.
 * Player's can cancel either of them
 * <p>
 * Player's will receive the option to go, or have their countdown started upon being out of the game.
 * Parties will not have the option to go, or have their countdown started, until all members of the party are dead.
 * <p>
 * Parties will also be sent together.
 */
public class NextBestGameManager implements Listener
{

	/**
	 * ItemStack the representation of cancelling sending to the next game
	 */
	private static final ItemStack CANCEL_ITEM = new ItemBuilder(Material.REDSTONE_BLOCK)
			.setTitle(C.cDRed + "Cancel Sending")
			.build();

	/**
	 * ItemStack representation of the "Go to Next Game" command
	 */
	private static final ItemStack GO_TO_NEXT_ITEM = new ItemBuilder(Material.EMERALD_BLOCK)
			.setTitle(C.cGreenB + "Join another game!")
			.build();

	/**
	 * What slot in the inventory the item's go in.
	 */
	private static final int INVENTORY_SLOT = 2;

	/**
	 * This is the lowest a countdown may be in order to be taken into account in the case of the "best" server not being found for non partied players.
	 */
	private static final int ACCEPTABLE_SOLO_COUNTDOWN = 5;

	/**
	 * This is the lowest a countdown may be in order to be taken into account in the case of the "best" server not being found for partied players.
	 */
	private static final int ACCEPTABLE_PARTY_COUNTDOWN = 20;


	/*
	 * Text Components
	 */

	private static final TextComponent DONT_WORRY = new TextComponent("You are out of the game! ");
	private static final TextComponent CLICK_HERE = new TextComponent("Click Here");
	private static final TextComponent GO_TO_NEW_GAME = new TextComponent("Go to a new game!");
	private static final String COMMAND_NEXT = "/gotonextbestgame";
	private static final String COMMAND_CANCEL = "/cancelsendingtonextbestgame";
	private static final TextComponent OR = new TextComponent(" or ");
	private static final TextComponent CLICK_HERE_CANCEL = new TextComponent("Click Here");
	private static final TextComponent TO_STAY = new TextComponent(" to stay in your current one.");
	private static final TextComponent OR_THE = new TextComponent(" or the ");
	private static final TextComponent EMERALD_BLOCK = new TextComponent("Emerald Block");
	private static final TextComponent TO_GO = new TextComponent(" to go to a new one.");

	private static final TextComponent YOU_HAVE = new TextComponent("You have ");
	private static final TextComponent AUTO_JOIN_PREF = new TextComponent("Auto-Join ");
	private static final TextComponent ON_PLUS_EXTRA = new TextComponent("on! If you don't want to be sent to another game automatically, ");
	private static final String COMMAND_PREFS = "/prefs";
	private static final TextComponent CLICK_PREFS = new TextComponent("click here ");
	private static final TextComponent TO_CHANGE = new TextComponent("to disable it in your preferences.");

	private static final List<TextComponent> AUTO_JOIN_OFF_COMPONENTS;
	private static final List<TextComponent> AUTO_JOIN_ON_COMPONENTS;
	private static final List<TextComponent> WARNING_COMPONENTS;

	static
	{
		DONT_WORRY.setColor(ChatColor.GRAY);

		GO_TO_NEW_GAME.setColor(ChatColor.GREEN);

		CLICK_HERE.setColor(ChatColor.GOLD);
		CLICK_HERE.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{GO_TO_NEW_GAME}));
		CLICK_HERE.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND_NEXT));

		OR.setColor(ChatColor.GRAY);

		CLICK_HERE_CANCEL.setColor(ChatColor.RED);
		CLICK_HERE_CANCEL.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND_CANCEL));

		TO_STAY.setColor(ChatColor.GRAY);

		OR_THE.setColor(ChatColor.GRAY);

		EMERALD_BLOCK.setColor(ChatColor.GOLD);

		TO_GO.setColor(ChatColor.GRAY);

		AUTO_JOIN_OFF_COMPONENTS = Lists.newArrayList(DONT_WORRY, CLICK_HERE, OR_THE, EMERALD_BLOCK, TO_GO);

		AUTO_JOIN_ON_COMPONENTS = Lists.newArrayList(DONT_WORRY, CLICK_HERE, TO_GO, OR, CLICK_HERE_CANCEL, TO_STAY);

		YOU_HAVE.setColor(ChatColor.GRAY);

		AUTO_JOIN_PREF.setColor(ChatColor.GREEN);

		ON_PLUS_EXTRA.setColor(ChatColor.GRAY);

		CLICK_PREFS.setColor(ChatColor.GOLD);
		CLICK_PREFS.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND_PREFS));

		TO_CHANGE.setColor(ChatColor.GRAY);

		WARNING_COMPONENTS = Lists.newArrayList(YOU_HAVE, AUTO_JOIN_PREF, ON_PLUS_EXTRA, CLICK_PREFS, TO_CHANGE);

	}

	/**
	 * The group of this specific arcade instance
	 */
	private final String _serverGroup;

	/**
	 * The region of this specific arcade instance
	 */
	private final Region _region;

	/**
	 * A reference to parties
	 */
	private final PartyManager _partyManager;

	/**
	 * The current game being played
	 */
	private Game _game;

	private final Map<UUID, CountdownRunnable> _tasks = Maps.newHashMap();

	public NextBestGameManager(String serverGroup, Region region, PartyManager partyManager)
	{
		_serverGroup = serverGroup;
		_region = region;
		_partyManager = partyManager;
		UtilServer.getPlugin().getServer().getPluginManager().registerEvents(this, UtilServer.getPlugin());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_tasks.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onDeath(PlayerStateChangeEvent event)
	{
		if (event.GetGame() instanceof Minestrike
				|| event.GetGame() instanceof WitherGame
				|| event.GetGame() instanceof Paintball
				|| event.GetGame() instanceof Halloween2016
				|| event.GetGame().Manager.GetHost() != null)
		{
			return;
		}

		new BukkitRunnable()
		{

			@Override
			public void run()
			{
				if (event.GetGame().GetState() != GameState.Live || event.GetState() == PlayerState.IN)
				{
					return;
				}

				Player player = event.GetPlayer();
				Party party = _partyManager.getPartyByPlayer(player);

				if (party == null)
				{
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);

					player.sendMessage(" ");
					player.sendMessage(" ");

					if (_partyManager.getPreferencesManager().get(player).isActive(Preference.AUTO_JOIN_NEXT_GAME))
					{
						if (!_partyManager.getPreferencesManager().get(player).isActive(Preference.DISABLE_WARNING))
						{
							sendWarning(player);
							player.sendMessage(" ");
						}

						_tasks.put(player.getUniqueId(), new CountdownRunnable(player));

						giveItem(player);

						sendMessage(player, true);

						player.sendMessage(" ");

					}
					else
					{
						sendMessage(player, false);

						getGame().getArcadeManager().runSyncLater(() -> giveItem(player), GameSpectatorManager.ITEM_GIVE_DELAY);
					}

					player.sendMessage(" ");
					player.updateInventory();
					return;
				}

				List<Player> players = party.getMembers();
				boolean countDown = true;

				for (Player player1 : players)
				{
					if (_game.IsAlive(player1))
					{
						countDown = false;
					}
				}

				if (!countDown)
				{
					return;
				}

				Player owner = Bukkit.getPlayer(party.getOwnerName());
				owner.sendMessage(F.main("Game", "All party members are dead!"));
				if (_partyManager.getPreferencesManager().get(owner).isActive(Preference.AUTO_JOIN_NEXT_GAME))
				{

					owner.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);

					owner.sendMessage(" ");
					owner.sendMessage(" ");

					if (!_partyManager.getPreferencesManager().get(owner).isActive(Preference.DISABLE_WARNING))
					{
						sendWarning(owner);
						owner.sendMessage(" ");
					}

					sendMessage(owner, true);

					owner.sendMessage(" ");

					_tasks.put(player.getUniqueId(), new CountdownRunnable(party));
				}
				else
				{
					owner.sendMessage(" ");
					owner.sendMessage(" ");

					sendMessage(owner, false);

					owner.sendMessage(" ");
					owner.sendMessage(" ");

					giveItem(owner);
					owner.updateInventory();
				}
			}
		}.runTaskLater(_partyManager.getPlugin(), 5L);
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack inHand = player.getItemInHand();

		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		if (getGame() == null)
		{
			return;
		}

		if (getGame().IsAlive(player))
		{
			return;
		}

		if (inHand == null || (inHand.getType() != Material.REDSTONE_BLOCK && inHand.getType() != Material.EMERALD_BLOCK))
		{
			return;
		}

		boolean cancel = inHand.isSimilar(CANCEL_ITEM);
		Party party = _partyManager.getPartyByPlayer(player);

		if (cancel)
		{
			cancel(player, party);
			return;
		}

		handle(player);
	}

	public void onCommand(Player player)
	{
		handle(player);
	}

	private void handle(Player player)
	{
		if (getGame() != null && getGame().IsAlive(player))
		{
			return;
		}

		Party party = _partyManager.getPartyByPlayer(player);

		if (party != null)
		{
			if (!party.getOwnerName().equalsIgnoreCase(player.getName()))
			{
				Lang.NOT_OWNER_SERVER.send(player);
				return;
			}

			Player owner = Bukkit.getPlayer(party.getOwnerName());

			if (_partyManager.getPreferencesManager().get(owner).isActive(Preference.COUNTDOWN_ON_CLICK))
			{
				_tasks.put(player.getUniqueId(), new CountdownRunnable(owner));
				giveItem(player);
			}
			else
			{
				MinecraftServer server = findBestGame(_partyManager.getClientManager().Get(player.getUniqueId()).hasPermission(ArcadeManager.Perm.JOIN_FULL), party);

				if (server == null)
				{
					owner.sendMessage(F.main("Game", "There are no good servers to send you to!"));
					return;
				}

				Portal.getInstance().sendPlayerToServer(party.getOwnerAsPlayer().get(), server.getName(), Intent.PLAYER_REQUEST);
			}
			return;
		}

		if (_partyManager.getPreferencesManager().get(player).isActive(Preference.COUNTDOWN_ON_CLICK))
		{
			_tasks.put(player.getUniqueId(), new CountdownRunnable(player));
			giveItem(player);
		}
		else
		{
			MinecraftServer server = findBestGame(_partyManager.getClientManager().Get(player.getUniqueId()).hasPermission(ArcadeManager.Perm.JOIN_FULL), null);
			sendToServer(player, server);
		}
	}

	private MinecraftServer findBestGame(boolean joinFull, Party party)
	{
		List<MinecraftServer> servers = Lists.newArrayList(ServerManager.getServerRepository(_region).getServersByGroup(_serverGroup));
		List<MinecraftServer> possible = Lists.newArrayList();

		int acceptableCountdown = party == null ? ACCEPTABLE_SOLO_COUNTDOWN : ACCEPTABLE_PARTY_COUNTDOWN;

		for (MinecraftServer other : servers)
		{
			GameInfo info;

			try
			{
				info = GameInfo.fromString(other.getMotd());
			}
			catch (JsonSyntaxException ex)
			{
				continue;
			}

			if (info.getJoinable() != GameJoinStatus.OPEN)
			{
				if (!joinFull || info.getJoinable() != GameJoinStatus.RANKS_ONLY)
				{
					continue;
				}
			}

			switch (info.getStatus())
			{
				case WAITING:
				case VOTING:
					break;
				case STARTING:
					if (info.getTimer() < acceptableCountdown)
					{
						continue;
					}
					break;
				default:
					continue;
			}

			int required = party == null ? 1 : party.getSize();

			if (other.getPlayerCount() + required <= other.getMaxPlayerCount())
			{
				possible.add(other);
			}
		}

		return possible.stream()
				.max(Comparator.comparingInt(MinecraftServer::getPlayerCount))
				.orElse(null);
	}

	private void sendToServer(Player player, MinecraftServer server)
	{
		if (server == null)
		{
			player.sendMessage(F.main("Game", "There are no good servers to send you to!"));
			return;
		}
		player.sendMessage(F.main("Server", "Sending you to " + F.name(server.getName()) + "..."));
		_partyManager.getPortal().sendPlayer(player, server.getName());
	}

	public Game getGame()
	{
		return _game;
	}

	public void setGame(Game game)
	{
		_game = game;
	}

	private void giveItem(Player player)
	{
		if (getGame().IsAlive(player))
		{
			return;
		}

		player.getInventory().setItem(INVENTORY_SLOT, GO_TO_NEXT_ITEM);
	}

	private void sendWarning(Player player)
	{
		TextComponent message = new TextComponent("Game> ");
		message.setColor(ChatColor.BLUE);

		List<TextComponent> comps = Lists.newArrayList(WARNING_COMPONENTS);

		comps.forEach(message::addExtra);
		player.spigot().sendMessage(message);
	}

	private void sendMessage(Player player, boolean autoJoin)
	{
		TextComponent message = new TextComponent("Game> ");
		message.setColor(ChatColor.BLUE);

		List<TextComponent> comps = autoJoin ? AUTO_JOIN_ON_COMPONENTS : AUTO_JOIN_OFF_COMPONENTS;

		comps.forEach(message::addExtra);
		player.spigot().sendMessage(message);
	}

	public void cancel(Player player, Party party)
	{
		CountdownRunnable task = _tasks.remove(player.getUniqueId());

		if (task == null)
		{
			return;
		}

		task.cancel();

		if (party != null)
		{
			party.getMembers().forEach(partyMember ->
			{
				partyMember.sendMessage(F.main("Game", "Cancelled sending your party to a new game!"));
				partyMember.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1.0F, 1.0F);

				giveItem(partyMember);
			});
		}
		else
		{
			player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1.0F, 1.0F);
			player.sendMessage(F.main("Game", "Cancelled sending you to a new game!"));

			giveItem(player);
		}
	}

	private final class CountdownRunnable extends BukkitRunnable
	{
		private int _ticks = 15;
		private Player _player;
		private Party _party;

		private CountdownRunnable(Player player)
		{
			_player = player;
			runTaskTimer(_partyManager.getPlugin(), 0L, 20L);
		}

		private CountdownRunnable(Party party)
		{
			_party = party;
			_player = Bukkit.getPlayerExact(party.getOwnerName());
			runTaskTimer(_partyManager.getPlugin(), 0L, 20L);
		}

		@Override
		public void run()
		{
			if (_game.GetState() != GameState.Live)
			{
				cancel();
				if (_party != null)
				{
					_party.sendMessage(F.main("Game", "The game has ended! Stopping countdown to keep you in this lobby."));
					return;
				}
				if (_player != null)
				{
					_player.sendMessage(F.main("Game", "The game has ended! Stopping countdown to keep you in this lobby."));
					return;
				}
				return;
			}

			if (_ticks == 0)
			{
				cancel();
				_tasks.remove(_player.getUniqueId());

				if (_party != null)
				{
					MinecraftServer server = findBestGame(false, _party);

					if (server == null)
					{
						_party.sendMessage(F.main("Game", "There are no good servers to send you to!"));
						return;
					}

					Portal.getInstance().sendPlayerToServer(_party.getOwnerAsPlayer().get(), server.getName(), Intent.PLAYER_REQUEST);
					return;
				}

				if (Bukkit.getPlayer(_player.getUniqueId()) == null)
				{
					return;
				}

				MinecraftServer server = findBestGame(_partyManager.getClientManager().Get(_player.getUniqueId()).hasPermission(ArcadeManager.Perm.JOIN_FULL), null);

				if (server == null)
				{
					_player.sendMessage(F.main("Game", "There are no good servers to send you to!"));
					return;
				}

				sendToServer(_player, server);

				return;
			}

			if (_ticks == 10 || _ticks <= 5)
			{
				if (_party == null)
				{
					_player.sendMessage(F.main("Game", "Sending you to your next game in " + F.greenElem(String.valueOf(_ticks)) + " " + (_ticks == 1 ? "second" : "seconds")));
				}
				else
				{
					_party.sendMessage(F.main("Game", "Sending you to your next game in " + F.greenElem(String.valueOf(_ticks)) + " " + (_ticks == 1 ? "second" : "seconds")));
				}
			}
			_ticks--;
		}
	}
}