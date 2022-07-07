package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on tnt.
 */
public class ChallengeDeadlyTnt extends Challenge
{
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_HEIGHT = 1;
	private static final int STAINED_CLAY_DATA_RANGE = 16;

	private static final int KNOCKBACK_APPLY_RADIUS_MIN = 4;
	private static final double KNOCKBACK_APPLY_RADIUS_NORMALIZER = 10000;
	private static final int KNOCKBACK_DIVIDER = 2;
	private static final double KNOCKBACK_NORMALIZER = 20000;
	private static final int KNOCKBACK_MULTIPLIER = 3;
	private static final int KNOCKBACK_HEIGHT_MAX = 6;
	private static final double KNOCKBACK_HEIGHT_MIN = 0.5;
	private static final int KNOCKBACK_HEIGHT_MULTIPLIER = 2;

	private static final int TNT_USE_COOLDOWN = 3000;
	private static final double TNT_VELOCITY_POWER = 0.6;
	private static final double TNT_VELOCICTY_HEIGHT = 0.2;
	private static final double TNT_VELOCITY_HEIGHT_MAX = 1;
	private static final int TNT_FUSE_TICKS_MULTIPLIER = 60;
	private static final double TNT_FUSE_TICKS_DIVIDER = 70000;

	public ChallengeDeadlyTnt(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Deadly TNT",
			"Throw TNT at other players.",
			"Do not get knocked off!");

		Settings.setUseMapHeight();
		Settings.setCanCruble();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (Location location : circle(getCenter(), size, 1, true, false, 0))
		{
			spawns.add(location.add(0, MAP_HEIGHT, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (Location location : circle(getCenter(), getArenaSize(), 1, false, false, 0))
		{
			Block block = location.getBlock();
			setBlock(block, Material.STAINED_CLAY, (byte) UtilMath.r(STAINED_CLAY_DATA_RANGE));
			addBlock(block);
		}
	}

	@Override
	public void onStart()
	{
		setItem(Settings.getLockedSlot(), new ItemStack(Material.TNT));
	}

	@Override
	public void onEnd()
	{
		remove(EntityType.PRIMED_TNT);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!(event.getEntity() instanceof TNTPrimed))
			return;

		event.blockList().clear();
		dealKnockbackToNearbyPlayers(event.getLocation());
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.GetCause() != DamageCause.ENTITY_EXPLOSION)
			return;

		event.SetCancelled("No TNT damage");
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (!Recharge.Instance.use(player, "TNT", TNT_USE_COOLDOWN, false, true))
			return;

		throwTnt(player);
	}

	private void dealKnockbackToNearbyPlayers(Location center)
	{
		HashMap<Player, Double> nearby = UtilPlayer.getInRadius(center, KNOCKBACK_APPLY_RADIUS_MIN + ((System.currentTimeMillis() - Settings.getStartTime()) / KNOCKBACK_APPLY_RADIUS_NORMALIZER));

		for (Player player : nearby.keySet())
		{
			if (!isPlayerValid(player))
				continue;

			double mult = nearby.get(player) / KNOCKBACK_DIVIDER;
			mult += (System.currentTimeMillis() - Settings.getStartTime()) / KNOCKBACK_NORMALIZER;

			UtilAction.velocity(player, UtilAlg.getTrajectory(center, player.getLocation()), KNOCKBACK_MULTIPLIER * mult, false, 0, KNOCKBACK_HEIGHT_MIN + KNOCKBACK_HEIGHT_MULTIPLIER * mult, KNOCKBACK_HEIGHT_MAX, true);
		}
	}

	private void throwTnt(Player player)
	{
		Location dropsite = player.getEyeLocation().add(player.getLocation().getDirection());
		TNTPrimed tnt = player.getWorld().spawn(dropsite, TNTPrimed.class);

		UtilAction.velocity(tnt, player.getLocation().getDirection(), TNT_VELOCITY_POWER, false, 0, TNT_VELOCICTY_HEIGHT, TNT_VELOCITY_HEIGHT_MAX, false);

		int ticks = (int) (TNT_FUSE_TICKS_MULTIPLIER * (1 - ((System.currentTimeMillis() - Settings.getStartTime()) / TNT_FUSE_TICKS_DIVIDER)));
		tnt.setFuseTicks(ticks);

		player.playSound(player.getLocation(), Sound.FUSE, 1.0F, 1.0F);
	}
}
