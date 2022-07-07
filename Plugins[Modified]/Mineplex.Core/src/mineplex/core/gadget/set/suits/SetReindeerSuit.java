package mineplex.core.gadget.set.suits;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerAntlers;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerChest;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerHooves;
import mineplex.core.gadget.gadgets.outfit.reindeer.OutfitReindeerLegs;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.recharge.Recharge;

public class SetReindeerSuit extends GadgetSet
{

	private static final Vector VELOCITY = new Vector(0, 2, 0);
	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(5);

	public SetReindeerSuit(GadgetManager manager)
	{
		super(manager, "Rudolf", "Sneak to leap up into the air!",
				manager.getGadget(OutfitReindeerAntlers.class),
				manager.getGadget(OutfitReindeerChest.class),
				manager.getGadget(OutfitReindeerLegs.class),
				manager.getGadget(OutfitReindeerHooves.class)
		);
	}

	@EventHandler
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if (event.isSneaking() || !isActive(player) || !Recharge.Instance.use(player, getName() + " Leap", COOLDOWN, true, false))
		{
			return;
		}

		player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
		UtilAction.velocity(player, VELOCITY);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int iterations = 0;

			@Override
			public void run()
			{
				if (++iterations == 30)
				{
					cancel();
				}

				Location location = player.getEyeLocation();
				Vector velocity = location.getDirection();
				velocity.setY(Math.min(0.1, velocity.getY()));

				UtilAction.velocity(player, velocity);

				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location.subtract(0, 0.5, 0), 0, 0, 0, 0.1F, 3, ViewDist.NORMAL);

				if (iterations % 4 == 0)
				{
					player.getWorld().playSound(location, Sound.ORB_PICKUP, 1, 1);
				}
			}
		}, 20, 1);
	}
}
