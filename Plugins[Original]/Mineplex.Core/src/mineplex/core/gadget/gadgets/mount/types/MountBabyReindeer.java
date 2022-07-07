package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountBabyReindeer extends HorseMount
{

	public MountBabyReindeer(GadgetManager manager)
	{
		super(manager,
				"Baby Reindeer",
				UtilText.splitLineToArray(C.cGray + "One of Santa's baby reindeers. He's still learning how to fly...", LineFormat.LORE),
				CostConstants.FOUND_IN_WINTER_CHESTS,
				Material.GOLDEN_CARROT,
				(byte) 0,
				Color.CREAMY,
				Style.WHITEFIELD,
				Variant.HORSE,
				0,
				null
		);
	}

	@Override
	public SingleEntityMountData<Horse> spawnMount(Player player)
	{
		SingleEntityMountData<Horse> data = super.spawnMount(player);
		Horse horse = data.getEntity();

		horse.setBaby();
		horse.setMaxHealth(20);

		return data;
	}

	@Override
	protected void setPassenger(Player player, Entity clicked, PlayerInteractEntityEvent event)
	{
		super.setPassenger(player, clicked, event);
		clicked.setPassenger(player);
	}

	@EventHandler
	public void fly(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		for (SingleEntityMountData<Horse> horse : getActiveMounts().values())
		{
			if (horse.getEntity().getPassenger() != horse.getOwner()) continue;

			if (Recharge.Instance.usable(horse.getOwner(), "Reindeer Fly")) continue;

			horse.getEntity().setVelocity(horse.getOwner().getLocation().getDirection().normalize().add(new Vector(0, 0.4, 0)));

			horse.getEntity().getWorld().playSound(horse.getEntity().getLocation(), Sound.HORSE_BREATHE, 0.3f, 0.5f);

			UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, horse.getEntity().getLocation(), 0.2f, 0.0f, 0.2f, 0.0f, 10, ViewDist.NORMAL);
		}
	}

	@EventHandler
	public void onJump(HorseJumpEvent event)
	{
		SingleEntityMountData<Horse> data = getMountData(event.getEntity());
		if (data == null) return;

		event.setCancelled(true);
		if (UtilEnt.isGrounded(event.getEntity()))
		{
			if (Recharge.Instance.use(data.getOwner(), "Reindeer Fly", 1000, false, false))
			{
				event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.HORSE_ANGRY, 1f, 1f);
			}
		}
	}
}
