package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkLifestealArrows extends Perk
{

	private double _health;
	
	public PerkLifestealArrows(double health)
	{
		super("Lifesteal Arrows", new String[] {});
		
		_health = health;
	}
	
	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.PROJECTILE)
		{
			return;
		}
		
		Player player = event.GetDamagerPlayer(true);
		
		if (!hasPerk(player))
		{
			return;
		}
		
		player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + _health));
		UtilParticle.PlayParticleToAll(ParticleType.HEART, player.getLocation().add(0, 1, 0), 1, 1, 1, 1, 5, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.HEART, event.GetDamageeEntity().getLocation().add(0, 1, 0), 1, 1, 1, 1, 5, ViewDist.NORMAL);

	}

}
