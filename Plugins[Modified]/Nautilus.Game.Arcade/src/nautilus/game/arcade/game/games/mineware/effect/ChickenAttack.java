package nautilus.game.arcade.game.games.mineware.effect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.disguise.disguises.DisguiseChicken;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;

/**
 * This class simulates a chicken attack, that is triggered when a player runs out of lives.
 */
public class ChickenAttack
{
	private static final int CHICKEN_SPAWN_AMOUNT = 12;
	private static final int INFORM_TITLTE_FADE_IN_TICKS = 5;
	private static final int INFORM_TITLTE_STAY_TICKS = 40;
	private static final int INFORM_TITLTE_FADE_OUT_TICKS = 5;
	private static final int ATTACK_EXPIRE_TICKS = 400;
	private static final float FLY_SPEED_RESET = 0.1F;
	private static final double CHICKEN_BABY_SPAWN_CHANCE = 0.1;
	private static final int CHICKEN_LOOK_AT_PLAYER_DISTANCE = 4;

	private BawkBawkBattles _host;
	private JavaPlugin _plugin;

	private Location _center;

	private Location[] _spawnpoints = new Location[4];
	private List<Chicken> _chickens = new ArrayList<>();

	public ChickenAttack(BawkBawkBattles host, Location center)
	{
		_host = host;
		_plugin = host.getArcadeManager().getPlugin();
		_center = center;

		Block block = _center.getBlock();

		_spawnpoints = new Location[] {
			block.getRelative(BlockFace.NORTH).getLocation().add(0, 1, 0),
			block.getRelative(BlockFace.EAST).getLocation().add(0, 1, 0),
			block.getRelative(BlockFace.SOUTH).getLocation().add(0, 1, 0),
			block.getRelative(BlockFace.WEST).getLocation().add(0, 1, 0)
		};

		spawnChickens();
	}

	public void start(Player player)
	{
		_host._beingAttacked.add(player.getUniqueId());

		UtilInv.Clear(player);
		_host.Manager.Clear(player);

		_center.getWorld().strikeLightningEffect(_center);

		player.teleport(_center);

		UtilTextMiddle.display(C.cRed + "Chicken Attack!", "You ran out of lives.", INFORM_TITLTE_FADE_IN_TICKS, INFORM_TITLTE_STAY_TICKS, INFORM_TITLTE_FADE_OUT_TICKS, player);
		UtilPlayer.message(player, F.main("Game", "You failed to follow Bawk Bawk's commands. Now his chickens will devour you!"));
		player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1.0F, 1.0F);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));

		ensurePlayerKill(player);
	}

	private void ensurePlayerKill(Player player)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (_host.IsLive() && _host._beingAttacked.contains(player.getUniqueId()))
				{
					UtilPlayer.message(player, F.main("Game", "You have been moved to spectators."));
					kill(player, false);
				}
				else
				{
					cancel();
				}
			}
		}.runTaskLater(_plugin, ATTACK_EXPIRE_TICKS);
	}

	public void kill(Player player, boolean inform)
	{
		_host.Manager.Clear(player);
		_host._beingAttacked.remove(player.getUniqueId());

		if (inform)
		{
			UtilPlayer.message(player, F.main("Game", "You are now spectating, others can see you as a chicken."));
		}

		new BukkitRunnable()
		{
			@Override
			public void run()
			{

				DisguiseChicken disguise = new DisguiseChicken(player);
				disguise.setBaby();
				_host.Manager.GetDisguise().disguise(disguise);

				player.teleport(_host.GetSpectatorLocation());

				UtilAction.velocity(player, new Vector(0, 1, 0));
				player.setAllowFlight(true);
				player.setFlying(true);
				player.setFlySpeed(FLY_SPEED_RESET);

				player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1.0F, 1.0F);
			}
		}.runTaskLater(_plugin, 1L);
	}

	private void spawnChickens()
	{
		_host.CreatureAllow = true;

		int spawnAmount = CHICKEN_SPAWN_AMOUNT / _spawnpoints.length;

		for (int i = 0; i < _spawnpoints.length; i++)
		{
			Location spawn = _spawnpoints[i];
			World world = spawn.getWorld();

			for (int j = 0; j < spawnAmount; j++)
			{
				Chicken chicken = world.spawn(spawn, Chicken.class);
				setupChicken(chicken);
			}
		}

		_host.CreatureAllow = false;
	}

	private void setupChicken(Chicken chicken)
	{
		_chickens.add(chicken);

		if (Math.random() < CHICKEN_BABY_SPAWN_CHANCE)
		{
			chicken.setBaby();
		}

		UtilEnt.addLookAtPlayerAI(chicken, CHICKEN_LOOK_AT_PLAYER_DISTANCE);

		chicken.setCustomName("Chicken Minion");
		chicken.setCustomNameVisible(true);
	}

	public boolean isGroupMember(Chicken chicken)
	{
		return _chickens.contains(chicken);
	}

	public void reset()
	{
		for (Chicken chicken : _chickens)
		{
			chicken.remove();
		}

		_chickens.clear();
	}

	public List<Chicken> getChickens()
	{
		return _chickens;
	}

	public Location getPlatformCenter()
	{
		return _center;
	}
}
