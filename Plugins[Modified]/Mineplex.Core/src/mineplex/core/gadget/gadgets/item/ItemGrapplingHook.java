package mineplex.core.gadget.gadgets.item;

import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemGrapplingHook extends ItemGadget
{

	private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(5);

	private final Map<Player, Pair<Location, Long>> _targets;

	public ItemGrapplingHook(GadgetManager manager)
	{
		super(manager, "Grappling Hook", new String[]
				{
						C.cGray + "Fly through the skies like",
						C.cGray + "a kite!"
				}, CostConstants.POWERPLAY_BONUS, Material.IRON_BARDING, (byte) 0, 1000, null);

		_targets = new HashMap<>();

		Free = false;
		setPPCYearMonth(YearMonth.of(2018, Month.SEPTEMBER));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Block block = UtilPlayer.getTarget(player, null, 75);

		if (block == null)
		{
			player.sendMessage(F.main(Manager.getName(), "You missed your " + F.name(getName()) + "."));
			return;
		}

		player.getWorld().playSound(player.getLocation(), Sound.SHOOT_ARROW, 0.5F, 1);
		player.sendMessage(F.main(Manager.getName(), "You shot your " + F.name(getName()) + ". Sneak to cancel."));
		attachHook(player, block.getLocation().add(0.5, 0.5, 0.5));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		detachHook(player);
	}

	private void attachHook(Player player, Location target)
	{
		detachHook(player);

		Location start = player.getLocation().add(0, 1, 0);

		LineParticle particle = new LineParticle(start, target, null, 0.5, UtilMath.offset(start, target),ParticleType.CLOUD, UtilServer.getPlayers());

		while (!particle.update())
		{
		}

		_targets.put(player, Pair.create(target, System.currentTimeMillis()));
	}

	private void detachHook(Player player)
	{
		if (_targets.remove(player) != null)
		{
			player.getWorld().playSound(player.getLocation(), Sound.SKELETON_IDLE, 0.5F, 1);
		}
	}

	@EventHandler
	public void updateHooks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_targets.entrySet().removeIf(entry ->
		{
			Player player = entry.getKey();
			Pair<Location, Long> pair = entry.getValue();

			Location target = pair.getLeft();
			Location location = player.getLocation().add(0, 0.3, 0);

			if (UtilTime.elapsed(pair.getRight(), TIMEOUT) || UtilMath.offsetSquared(location, target) < 9)
			{
				return true;
			}

			Vector direction = UtilAlg.getTrajectory(location, target).multiply(1.2);

			player.setVelocity(direction.setY(direction.getY() + 0.1));
			UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, null, 0, 2, ViewDist.NORMAL);
			return false;
		});
	}

	@EventHandler(ignoreCancelled = true)
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		if (event.isSneaking())
		{
			detachHook(event.getPlayer());
		}
	}
}
