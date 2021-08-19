package nautilus.game.arcade.game.games.rings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.uhc.KitUHC;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class ElytraRings extends SoloGame
{
	private HashMap<Integer, Ring> _rings = new HashMap<Integer, Ring>();
	private HashMap<UUID, Integer> _goneThrough = new HashMap<UUID, Integer>();
	private HashMap<UUID, Location> _lastLocation = new HashMap<UUID, Location>();

	public ElytraRings(ArcadeManager manager)
	{
		super(manager, GameType.ElytraRings, new Kit[]
			{
//					new KitElytraRings(manager)
			}, new String[]
			{
					"Fly through the rings!"
			});

		DeathOut = false;
		DeathMessages = false;

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	public void RespawnPlayer(final Player player)
	{
		player.eject();

		if (_goneThrough.containsKey(player.getUniqueId()) && _rings.containsKey(_goneThrough.get(player.getUniqueId())))
		{
			Ring ring = _rings.get(_goneThrough.get(player.getUniqueId()));

			player.teleport(ring.getCenter());
		}
		else if (_goneThrough.containsKey(player.getUniqueId()) && _rings.containsKey(_goneThrough.get(player.getUniqueId()) + 1))
		{
			Ring ring = _rings.get(_goneThrough.get(player.getUniqueId()) + 1);

			player.teleport(ring.getCenter());
		}
		else
		{
			player.teleport(GetTeam(player).GetSpawn());
		}

		Manager.Clear(player);

		// Event
		PlayerGameRespawnEvent event = new PlayerGameRespawnEvent(this, player);
		UtilServer.getServer().getPluginManager().callEvent(event);

		// Re-Give Kit
		Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				GetKit(player).ApplyKit(player);
			}
		}, 0);
	}

	@Override
	public void ParseData()
	{
		Location loc = UtilAlg.getAverageLocation(GetTeamList().get(0).GetSpawns());
		BlockFace currentDirection = BlockFace.values()[UtilMath.r(4)];

		while (_rings.size() < 30)
		{
			int dist = UtilMath.r(40);

			loc = loc.getBlock().getRelative(currentDirection, 20).getLocation();

			generateRing(loc, currentDirection, 2 + UtilMath.r(2) + UtilMath.r(2));
		}

		loc = loc.getBlock().getRelative(currentDirection, 20).getLocation();

		Ring ring = generateRing(loc, currentDirection, 7);

		for (Block b : ring.getRing())
		{
			b.setType(Material.GOLD_BLOCK);
		}
	}

	@EventHandler
	public void onGameStart(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;

		for (Player player : this.GetPlayers(true))
		{
			player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (event.isCancelled())
			return;

		if (!IsAlive(event.getPlayer()))
			return;

		Player player = event.getPlayer();
		int current = 1;

		if (_goneThrough.containsKey(player.getUniqueId()))
		{
			current = _goneThrough.get(player.getUniqueId()) + 1;
		}

		if (!_rings.containsKey(current))
		{
			return;
		}

		Ring ring = _rings.get(current);

		if (!ring.isMoveThroughRing(event.getFrom(), event.getTo()))
		{
			return;
		}

		_goneThrough.put(player.getUniqueId(), current + 1);

		Announce(player.getName() + " has gone through ring " + current + "!");
	}

	@EventHandler
	public void onSpeedBoost(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetPlayers(true))
		{
			float exp = player.getExp();

			exp += 0.02;

			if (exp > 0.05 && _lastLocation.containsKey(player.getUniqueId()))
			{
				UtilAction.velocity(player, player.getLocation().getDirection().multiply(0.3));

				if (!_goneThrough.containsKey(player.getUniqueId())
						|| _rings.containsKey(_goneThrough.get(player.getUniqueId()) + 1))
				{
					exp -= 0.05;
				}

				for (Location loc : UtilShapes.getLinesDistancedPoints(_lastLocation.get(player.getUniqueId()),
						player.getLocation(), 0.3))
				{
					UtilParticle.PlayParticleToAll(ParticleType.CLOUD, loc, 0.2F, 0.2F, 0.2F, 0, 3, ViewDist.LONGER);
				}

				_lastLocation.put(player.getUniqueId(), player.getLocation());
			}

			player.setExp(Math.min(exp, 1));
		}
	}

	@EventHandler
	public void onToggleSneak(PlayerToggleSneakEvent event)
	{
		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (event.isSneaking() && UtilPlayer.isGliding(player))
		{
			_lastLocation.put(player.getUniqueId(), player.getLocation());
		}
		else
		{
			_lastLocation.remove(player.getUniqueId());
		}
	}

	private Ring generateRing(Location center, BlockFace direction, int size)
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		ArrayList<Block> hole = new ArrayList<Block>();

		for (Location loc : UtilShapes.rotate(UtilShapes.getSphereBlocks(center, size, size, true),
				UtilShapes.getFacing(direction)))
		{
			blocks.add(loc.getBlock());
		}

		size--;

		for (Location loc : UtilShapes.rotate(UtilShapes.getSphereBlocks(center, size, size, false),
				UtilShapes.getFacing(direction)))
		{
			hole.add(loc.getBlock());
		}

		center.setDirection(new Vector(direction.getModX(), direction.getModY(), direction.getModZ()));

		Ring ring = new Ring(blocks, hole, center.clone());

		for (Block b : ring.getRing())
		{
			b.setTypeIdAndData(Material.STAINED_CLAY.getId(), (byte) 4, false);
		}

		_rings.put(_rings.size() + 1, ring);

		return ring;
	}

}
