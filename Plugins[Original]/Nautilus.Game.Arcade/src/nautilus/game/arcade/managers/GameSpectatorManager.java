package nautilus.game.arcade.managers;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketPlayOutCamera;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameSpectatorManager implements Listener, IPacketHandler
{
	// Common delay for giving items when a spectator dies,
	// to prevent them from accidentally switching servers.
	public final static long ITEM_GIVE_DELAY = 30L;

	// A map of a player UUID to the UUID of the entity they want to spectate
	private final Map<UUID, UUID> _pendingSpectate = Collections.synchronizedMap(new HashMap<>());

	private final ArcadeManager _manager;

	public GameSpectatorManager(ArcadeManager manager)
	{
		_manager = manager;

		_manager.getPluginManager().registerEvents(this, _manager.getPlugin());

		_manager.getPacketHandler().addPacketHandler(this, PacketHandler.ListenerPriority.HIGH, PacketPlayOutNamedEntitySpawn.class);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void interactCancel(PlayerInteractEvent event)
	{
		if (_manager.GetGame() == null)
			return;

		Player player = event.getPlayer();

		if (!_manager.GetGame().IsAlive(player))
			event.setCancelled(true);

		if (_manager.GetGame() == null)
			return;

		if (!_manager.GetGame().AllowEntitySpectate)
			return;

		if (!_manager.GetGame().IsLive())
			return;

		if (player.getGameMode() != GameMode.SPECTATOR)
			return;

		if (player.getSpectatorTarget() == null)
			return;

		if (!(player.getSpectatorTarget() instanceof Player))
			return;

		List<Player> players = _manager.GetGame().GetPlayers(true);
		int currentPlayer = 0;
		for (Player otherPlayer : players)
		{
			currentPlayer++;
			if (player.getSpectatorTarget() == otherPlayer)
				break;
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
			currentPlayer = currentPlayer - 2;
		else
			return;

		if (currentPlayer < 0)
			currentPlayer = players.size() - 1;

		if (currentPlayer >= players.size())
			currentPlayer = 0;

		if (players.get(currentPlayer) == null)
			return;

		Player specPlayer = players.get(currentPlayer);

		setSpectating(player, specPlayer);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void interactEntityCancel(PlayerInteractEntityEvent event)
	{
		if (_manager.GetGame() == null)
			return;

		Player player = event.getPlayer();

		if (UtilEnt.hasFlag(event.getRightClicked(), UtilEnt.FLAG_ENTITY_COMPONENT))
			return;

		if (!_manager.GetGame().IsAlive(player))
		{
			event.setCancelled(true);
			if (_manager.GetGame().IsLive())
			{
				if (_manager.GetGame().AllowEntitySpectate)
				{
					setSpectating(player, event.getRightClicked());
				}
			}
		}
	}

	@EventHandler
	public void updateSpecEntitys(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		if (_manager.GetGame() == null)
			return;

		if (_manager.GetGame().IsLive() || _manager.GetGame().GetState() == GameState.End)
		{
			if (_manager.GetGame().AllowEntitySpectate)
			{
				for (Player player : UtilServer.getPlayers())
				{
					if (!_manager.GetGame().IsAlive(player))
					{
						if (player.getGameMode() == GameMode.SPECTATOR)
						{
							if (player.getSpectatorTarget() == null)
							{
								despectate(player);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void spectatedEntityDeath(PlayerDeathEvent event)
	{
		if (_manager.GetGame() == null)
			return;

		if (_manager.GetGame().IsLive() || _manager.GetGame().GetState() == GameState.End)
		{
			if (_manager.GetGame().AllowEntitySpectate)
			{
				for (Player player : UtilServer.getPlayers())
				{
					if (!_manager.GetGame().IsAlive(player))
					{
						if (player.getGameMode() == GameMode.SPECTATOR)
						{
							if (player.getSpectatorTarget() == event.getEntity())
							{
								if (_manager.GetGame().GetPlayers(true).size() >= 1)
								{
									setSpectating(player, _manager.GetGame().GetPlayers(true).get(UtilMath.r(_manager.GetGame().GetPlayers(true).size())));
									return;
								}
								despectate(player);
							}
						}
					}
				}
			}
		}
	}

	// todo what if we cancel this event?
	@EventHandler(priority = EventPriority.LOW)
	public void dismountEntity(PlayerToggleSneakEvent event)
	{
		if (_manager.GetGame() == null)
			return;

		if (_manager.GetGame().IsLive() || _manager.GetGame().GetState() == GameState.End)
		{
			if (_manager.GetGame().AllowEntitySpectate)
			{
				if (!_manager.GetGame().IsAlive(event.getPlayer()))
				{
					if (event.getPlayer().getGameMode() == GameMode.SPECTATOR)
					{
						despectate(event.getPlayer());
					}
				}
			}
		}
	}

	/*
	 * There's a reason this code is so complicated.
	 *
	 * Basically, when we want to set someone to spectate another entity, we send a PacketPlayOutCamera
	 * However, on the client side if the entity with the id as specified in PacketPlayOutCamera doesn't exist,
	 *   the client simply ignores it
	 * This is alright if we're talking about small games like SSM or whatever, but in bigger games like SG and UHC
	 *   the client does not get to keep the entire map loaded in memory
	 * Therefore, there is a chance that when we call Player#setSpectatorTarget, it has no effect because the client
	 *   has not yet loaded the target
	 *
	 * To remedy this, we take a two pronged approach.
	 *
	 * First, we use Minecraft's internal EntityTracker to determine whether the client should have the entity loaded
	 *   or not (if that's out of sync we're screwed anyways so oh well)
	 *
	 * If the client does have it loaded, then we simply use the Bukkit API to set the spectator target
	 *
	 * If the client doesn't have it loaded, we add it to a map of pending spectates and let the packet handler do the rest
	 *
	 * If at any point the client wants to stop spectating, we immediately replace the value in the map which notifies
	 *   the packet handler to stop doing whatever it's doing.
	 *
	 *   This is because the despectation code will work no matter what step of the spectating process is currently
	 *   being executed
	 */
	public void setSpectating(Player player, Entity target)
	{
		if (_manager.GetGame().IsAlive(player))
		{
			return;
		}

		Player playerTarget = null;

		if (target instanceof Player)
		{
			playerTarget = (Player) target;
			if (!_manager.GetGame().IsAlive(playerTarget))
			{
				return;
			}
		}

		// Not finished last spectate
		if (_pendingSpectate.containsKey(player.getUniqueId()))
			return;

		_pendingSpectate.put(player.getUniqueId(), target.getUniqueId());

		EntityPlayer ep = ((CraftPlayer) player).getHandle();

		EntityTracker tracker = ep.u().getTracker();
		EntityTrackerEntry entry = tracker.trackedEntities.get(target.getEntityId());

		// If the server is tracking this entity (eg the player should have it loaded) we can spectate right away
		if (entry.trackedPlayers.contains(ep))
		{
			player.teleport(target.getLocation().add(0, 1, 0));

			// If the player already has the entity loaded, we have to set it now
			player.setGameMode(GameMode.SPECTATOR);
			player.setSpectatorTarget(target);
			if (playerTarget != null)
				UtilTextBottom.display(C.cGray + "You are spectating " + F.elem(_manager.GetGame().GetTeam(playerTarget).GetColor() + playerTarget.getName()) + ".", player);

			UtilPlayer.message(player, F.main("Game", "Sneak to stop spectating."));
			_pendingSpectate.remove(player.getUniqueId());
			return;
		}

		// We still set spectating here even though it's pointless because of updateSpecEntites() above
		player.teleport(target.getLocation().add(0, 1, 0));
		player.setGameMode(GameMode.SPECTATOR);
		player.setSpectatorTarget(target);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void vehicleDamage(VehicleDamageEvent event)
	{
		if (_manager.GetGame() == null)
			return;

		if (!(event.getAttacker() instanceof Player))
			return;

		Player player = (Player) event.getAttacker();

		if (!_manager.GetGame().IsAlive(player))
			event.setCancelled(true);
	}

	// Yes, there are a lot of checks for _pendingSpectate
	// This is needed because if at any point (keeping in mind this code is not executed sequentially, but with delays)
	// spectation needs to be aborted (signaled by replacing the value in _pendingSpectate)
	// Then we need to stop spectating right away
	// Otherwise the client will be out of sync
	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayOutNamedEntitySpawn)
		{
			PacketPlayOutNamedEntitySpawn packet = (PacketPlayOutNamedEntitySpawn) packetInfo.getPacket();
			if (_pendingSpectate.get(packetInfo.getPlayer().getUniqueId()) == packet.b)
			{
				// Handle Minestrike spam race condition
				if (_manager.GetGame().IsAlive(packetInfo.getPlayer()))
				{
					_manager.runSync(() ->
					{
						_pendingSpectate.remove(packetInfo.getPlayer().getUniqueId());
					});
					return;
				}

				packetInfo.setCancelled(true);

				EntityPlayer ep = ((CraftPlayer) packetInfo.getPlayer()).getHandle();
				NetworkManager manager = ep.playerConnection.networkManager;

				if (_pendingSpectate.get(packetInfo.getPlayer().getUniqueId()) != packet.b)
					return;

				manager.a(packet, future ->
				{
					if (_pendingSpectate.get(packetInfo.getPlayer().getUniqueId()) != packet.b)
						return;

					_manager.runSync(() ->
					{
						PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(packetInfo.getPlayer(), GameMode.SPECTATOR);
						UtilServer.CallEvent(event);
						if (event.isCancelled())
						{
							_manager.runSync(() ->
							{
								_pendingSpectate.remove(packetInfo.getPlayer().getUniqueId());
							});
							return;
						}

						ep.playerInteractManager.setGameMode(WorldSettings.EnumGamemode.getById(GameMode.SPECTATOR.getValue()));
						ep.fallDistance = 0.0F;

						if (_pendingSpectate.get(packetInfo.getPlayer().getUniqueId()) != packet.b)
							return;

						manager.a(new PacketPlayOutCamera(ep), future1 ->
						{
							if (_pendingSpectate.get(packetInfo.getPlayer().getUniqueId()) != packet.b)
								return;

							manager.a(new PacketPlayOutGameStateChange(3, (float) GameMode.SPECTATOR.getValue()), future2 ->
							{
								if (_pendingSpectate.get(packetInfo.getPlayer().getUniqueId()) != packet.b)
									return;

								_manager.runSync(() ->
								{
									PacketPlayOutCamera p1 = new PacketPlayOutCamera();
									p1.a = packet.a;

									if (_pendingSpectate.get(packetInfo.getPlayer().getUniqueId()) != packet.b)
										return;

									manager.a(p1, future3 ->
									{
										_manager.runSync(() ->
										{
											Player playerTarget = Bukkit.getPlayer(packet.b);

											if (playerTarget != null)
												UtilTextBottom.display(C.cGray + "You are spectating " + F.elem(_manager.GetGame().GetTeam(playerTarget).GetColor() + playerTarget.getName()) + ".", packetInfo.getPlayer());

											UtilPlayer.message(packetInfo.getPlayer(), F.main("Game", "Sneak to stop spectating."));
											_pendingSpectate.remove(packetInfo.getPlayer().getUniqueId());
										});
									});
								});
							});
						});
					});
				});
			}
		}
	}

	public void despectate(Player player)
	{
		// We want to override and tell any pending spectates that we are despectating now
		_pendingSpectate.put(player.getUniqueId(), player.getUniqueId());

		PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(player, GameMode.SURVIVAL);
		UtilServer.CallEvent(event);
		if (event.isCancelled())
		{
			_pendingSpectate.remove(player.getUniqueId());
			return;
		}

		EntityPlayer ep = ((CraftPlayer) player).getHandle();
		ep.playerInteractManager.setGameMode(WorldSettings.EnumGamemode.getById(GameMode.SURVIVAL.getValue()));
		ep.fallDistance = 0.0F;
		NetworkManager manager = ep.playerConnection.networkManager;
		manager.a(new PacketPlayOutCamera(ep), future1 ->
		{
			manager.a(new PacketPlayOutGameStateChange(3, (float) GameMode.SURVIVAL.getValue()), future2 ->
			{
				_manager.runSync(() ->
				{
					player.setAllowFlight(true);
					_pendingSpectate.remove(player.getUniqueId());
				});
			});
		});
	}
}
