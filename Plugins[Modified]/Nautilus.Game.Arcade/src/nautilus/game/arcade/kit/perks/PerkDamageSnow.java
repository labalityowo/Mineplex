package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkDamageSnow extends Perk
{
	private int _damage;
	private double _knockback;

	public PerkDamageSnow()
	{
		this(0, 0);
	}

	public PerkDamageSnow(int damage, double knockback) 
	{
		super("Snow Attack", new String[] 
				{
				C.cGray + "+" + damage + " Damage and " + (int)((knockback-1)*100) + "% Knockback to enemies on snow.",
				});
		
		_damage = damage;
		_knockback = knockback;
	}

	@Override
	public void setupValues()
	{
		_damage = getPerkInt("Damage");
		_knockback = getPerkDouble("Knockback");
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getLocation().getBlock().getTypeId() != 78)
			return;
		
		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;
				
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		event.AddMod(damager.getName(), GetName(), _damage, false);
		event.AddKnockback("Knockback Snow", _knockback);
	}
}
