package nautilus.game.arcade.game.games.milkcow.kits.perk;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkCharge extends Perk
{

	public PerkCharge()
	{
		super("Cow Charge", new String[]
				{
						C.cYellow + "Sprint" + C.cGray + " to use " + C.cGreen + "Cow Charge"
				});
	}

	@EventHandler
	public void collide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!player.isSprinting() || !hasPerk(player))
			{
				continue;
			}

			Location location = player.getLocation();
			location.setYaw(0);

			player.getWorld().playSound(location, Sound.COW_WALK, 0.8f, 1f);

			Manager.GetCondition().Factory().Speed(GetName(), player, player, 0.9, 2, false, false, false);

			UtilEnt.getInRadius(location.clone().add(location.getDirection()), 2).forEach((entity, scale) ->
			{
				if (entity.equals(player) || entity instanceof Cow)
				{
					return;
				}

				if (entity instanceof Player)
				{
					Player hit = (Player) entity;

					if (!Recharge.Instance.use(hit, "Hit By " + GetName(), 500, false, false))
					{
						return;
					}

					for (ItemStack itemStack : hit.getInventory().getContents())
					{
						if (itemStack == null || itemStack.getType() != Material.MILK_BUCKET)
						{
							continue;
						}

						itemStack.setType(Material.BUCKET);
					}
				}

				Location entityLocation = entity.getLocation();

				Vector dir = UtilAlg.getTrajectory2d(location, entityLocation);
				dir.add(location.getDirection());
				dir.setY(0);

				Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, 3, false, false, false, player.getName(), GetName());
				Manager.GetCondition().Factory().Falling(GetName(), entity, player, 10, false, true);

				UtilAction.velocity(entity, dir, 1 + 0.8 * scale, true, 0, 0.8 + 0.4 * scale, 1.6, true);

				player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 0.75f, 1f);
			});
		}
	}

	@EventHandler
	public void charge(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
			{
				continue;
			}

			if (player.isSprinting())
			{
				UtilPlayer.hunger(player, -1);

				if (player.getFoodLevel() <= 0)
				{
					player.setSprinting(false);
				}
			}
			else
			{
				UtilPlayer.hunger(player, 1);
			}
		}
	}
}
