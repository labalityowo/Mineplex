package mineplex.core.gadget.gadgets.mount.types;

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R3.EntityLiving;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountChicken extends HorseMount
{

	public MountChicken(GadgetManager manager)
	{
		super(manager,
				"Chicken Mount",
				UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "This isn't flying! It is falling with style.",
								C.cRed + " ",
								C.cWhite + "Left Click to Bawk"
						}, LineFormat.LORE),
				CostConstants.FOUND_IN_THANKFUL_CHESTS,
				Material.FEATHER,
				(byte) 0,
				Color.BLACK,
				Style.NONE,
				Variant.HORSE,
				2,
				null
		);
	}

	@Override
	public SingleEntityMountData<Horse> spawnMount(Player player)
	{
		SingleEntityMountData<Horse> data = super.spawnMount(player);
		Horse horse = data.getEntity();

		UtilEnt.silence(horse, true);

		DisguiseChicken chicken = new DisguiseChicken(horse);
		chicken.setName(player.getName() + "'s Chicken Mount");
		chicken.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(chicken);

		return data;
	}

	@EventHandler
	public void onOrderQuack(PlayerAnimationEvent event)
	{
		if (getActiveMounts().containsKey(event.getPlayer()) && event.getAnimationType() == PlayerAnimationType.ARM_SWING)
		{
			if (Recharge.Instance.use(event.getPlayer(), "Chicken Bawk", 500, false, false))
			{
				event.getPlayer().getWorld().playSound(getActiveMounts().get(event.getPlayer()).getEntity().getEyeLocation(), Sound.CHICKEN_IDLE, .4F, 1.0F);
			}
		}
	}

	@EventHandler
	public void jump(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : getActiveMounts().keySet())
		{
			final Horse horse = getActiveMounts().get(player).getEntity();

			// Slows down falling
			if (!horse.isOnGround() && horse.getVelocity().getY() < 0)
			{
				Vector velocity = horse.getVelocity();
				velocity.setY(velocity.getY() * 0.6);
				horse.setVelocity(velocity);
			}

			if (horse.getPassenger() == null || !horse.getPassenger().equals(player) || !UtilEnt.isGrounded(horse) || !Recharge.Instance.use(player, "Chicken Mount Jump", 100, false, false))
			{
				continue;
			}

			try
			{
				boolean isJumping = JUMP_FIELD.getBoolean(((CraftPlayer) player).getHandle());

				if (!isJumping)
				{
					continue;
				}

				//Not jumping anymore
				((CraftPlayer) player).getHandle().i(false);

				//Velocity
				UtilAction.velocity(horse, 1.4, 0.38, .8, true);

				//Sound
				player.playSound(horse.getLocation(), Sound.CHICKEN_IDLE, .4F, 1.0F);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}