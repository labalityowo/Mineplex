package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkShockingStrike extends Perk
{
	public PerkShockingStrike() 
	{
		super("Shocking Strikes", new String[] 
				{
				C.cGray + "Your attacks Shock/Blind/Slow opponents.",
				});
	}
		
	@EventHandler(priority = EventPriority.MONITOR)
	public void Effect(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;
		
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		Manager.GetCondition().Factory().Slow(GetName(), event.GetDamageeEntity(), damager, 2, 1, false, false, false, false);
		Manager.GetCondition().Factory().Blind(GetName(), event.GetDamageeEntity(), damager, 1, 0, false, false, false);
		Manager.GetCondition().Factory().Shock(GetName(), event.GetDamageeEntity(), damager, 1, false, false);
	}
}
