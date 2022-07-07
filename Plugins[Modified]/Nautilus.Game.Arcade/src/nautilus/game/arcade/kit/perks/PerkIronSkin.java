package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.Managers;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.core.damage.DamageManager;
import nautilus.game.arcade.kit.Perk;

public class PerkIronSkin extends Perk
{
	private double _reduction;
	private boolean _percentage;
	
	public PerkIronSkin(double d)
	{
		this(d, false);
	}
	
	public PerkIronSkin(double d, boolean percentage) 
	{
		super("Iron Skin", new String[] 
				{ 
				"You take " + (d * (percentage ? 100 : 1)) + (percentage ? "%" : "") + " less damage from attacks",
				});
		
		_reduction = d;
		_percentage = percentage;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void damageDecrease(CustomDamageEvent event)
	{
		if (event.IsCancelled() || event.GetCause() == DamageCause.FIRE_TICK || event.GetDamage() <= 1)
		{
			return;
		}
		
		Player damagee = event.GetDamageePlayer();

		if (damagee == null || !hasPerk(damagee))
		{
			return;
		}
		
		if (_percentage)
		{
			event.AddMod(damagee.getName(), GetName(), -_reduction * event.GetDamage(), false);
		}
		else
		{
			event.AddMod(damagee.getName(), GetName(), -_reduction, false);
		}
	}
}