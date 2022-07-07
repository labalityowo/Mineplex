package mineplex.core.gadget.gadgets.mount.types;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseSpider;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountSpider extends HorseMount
{

	public MountSpider(GadgetManager manager)
	{
		super(manager,
				"Spider Mount",
				UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "Why ride a horse when you can ride a cute and cuddly spider!",
								C.blankLine,
								"#" + C.cWhite + "Look Up to use Wall Climb",
								"#" + C.cWhite + "Jump to use Leap",
								"",
								C.cBlue + "Only buyable during Halloween 2015"
						}, LineFormat.LORE),
				CostConstants.NO_LORE,
				Material.WEB,
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

		DisguiseSpider spider = new DisguiseSpider(horse);
		spider.setName(player.getName() + "'s Spider Mount");
		spider.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(spider);

		return data;
	}

	@EventHandler
	public void sounds(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		for (Player player : getActiveMounts().keySet())
		{
			Horse horse = getActiveMounts().get(player).getEntity();

			if (!Recharge.Instance.use(player, "Spider Mount Sounds", (1000 * UtilMath.r(3)) + 500, false, false))
			{
				continue;
			}

			//Moving
			if (horse.getVelocity().length() != 0 && UtilEnt.isGrounded(horse))
			{
				horse.getWorld().playSound(horse.getLocation(), Sound.SPIDER_WALK, .4F, 1.0F);
			}
		}
	}

	@EventHandler
	public void wallClimb(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Entry<Player, SingleEntityMountData<Horse>> entry : getActiveMounts().entrySet())
		{
			Player player = entry.getKey();
			Horse horse = entry.getValue().getEntity();

			if (horse.getPassenger() == null || !horse.getPassenger().equals(player))
			{
				continue;
			}

			//If player is looking up
			if (player.getEyeLocation().getPitch() > -45)
			{
				continue;
			}

			Vector direction = new Vector(horse.getVelocity().getX(), 0.2, horse.getVelocity().getZ());
			for (Block block : UtilBlock.getSurrounding(horse.getLocation().getBlock(), true))
			{
				if (UtilBlock.airFoliage(block) || block.isLiquid())
				{
					continue;
				}

				UtilAction.velocity(horse, direction);
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

			if (horse.getPassenger() == null || !horse.getPassenger().equals(player) || !UtilEnt.isGrounded(horse))
			{
				continue;
			}

			if (!Recharge.Instance.use(player, "Spider Mount Jump", 100, false, false))
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
				player.playSound(horse.getLocation(), Sound.SPIDER_IDLE, .4F, 1.0F);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}