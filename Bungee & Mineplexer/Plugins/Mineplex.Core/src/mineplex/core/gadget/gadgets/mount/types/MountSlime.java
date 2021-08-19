package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.Mount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountSlime extends Mount<SingleEntityMountData<Slime>>
{

	public MountSlime(GadgetManager manager)
	{
		super(manager,
				"Slime Mount",
				UtilText.splitLineToArray(C.cGray + "Bounce around on your very own slime friend!", LineFormat.LORE),
				15000,
				Material.SLIME_BALL,
				(byte) 0
		);

		BouncyCollisions = true;
	}

	@Override
	public SingleEntityMountData<Slime> spawnMount(Player player)
	{
		Slime slime = player.getWorld().spawn(player.getLocation(), Slime.class);
		slime.setSize(2);
		slime.setCustomName(player.getName() + "'s " + getName());
		slime.setCustomNameVisible(true);
		UtilEnt.removeTargetSelectors(slime);

		return new SingleEntityMountData<>(player, slime);
	}

	@Override
	@EventHandler
	public void updateBounce(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		//Bounce
		for (SingleEntityMountData<Slime> slimeData : getActiveMounts().values())
		{
			Slime slime = slimeData.getEntity();

			if (slime.getPassenger() == null)
			{
				UtilEnt.setFakeHead(slime, false);
				continue;
			}

			if (!UtilEnt.isGrounded(slime) || !(slime.getPassenger() instanceof  Player))
			{
				continue;
			}

			Player player = (Player) slime.getPassenger();

			if (!Recharge.Instance.use(player, getName(), 200, false, false))
			{
				continue;
			}

			Vector dir = slime.getPassenger().getLocation().getDirection();

			UtilAction.velocity(slime, dir, 1, true, 0, 0.4, 1, true);
			UtilEnt.CreatureForceLook(slime, 0, UtilAlg.GetYaw(dir));

			slime.getWorld().playSound(slime.getLocation(), Sound.SLIME_WALK, 1f, 0.75f);
		}

		super.updateBounce(event);
	}
}
