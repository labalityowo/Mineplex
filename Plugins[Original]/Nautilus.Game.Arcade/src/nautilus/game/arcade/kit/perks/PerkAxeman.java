package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilItem;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkAxeman extends Perk
{
	public PerkAxeman() 
	{
		super("Axe Master", new String[] 
				{
				C.cGray + "Deals +1 Damage with Axes",
				});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void AxeDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (!UtilItem.isAxe(damager.getItemInHand()))
		{
			return;
		}

		if (!Kit.HasKit(damager) || !Recharge.Instance.usable(damager, GetName()))
		{
			return;
		}

		event.AddMod(damager.getName(), GetName(), 1, false);
	}
}
