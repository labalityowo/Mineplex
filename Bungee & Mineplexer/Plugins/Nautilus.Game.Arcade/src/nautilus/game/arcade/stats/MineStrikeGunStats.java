package nautilus.game.arcade.stats;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.minestrike.CustomGunDamageEvent;

public class MineStrikeGunStats extends StatTracker<Game>
{

	public MineStrikeGunStats(Game game)
	{
		super(game);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onGunDamageEvent(CustomGunDamageEvent event)
	{
		if(event.getDamageEvent().isCancelled()) return;
		
		if(event.getBullet().Shooter == null) return;
		if(!event.getBullet().Shooter.isOnline()) return;
		
		if(!event.getBullet().Gun.getOwnerName().equals(event.getBullet().Shooter.getName())) return;
		
		// Check if the damage will result in killing the player
		double dmg = event.getDamageEvent().GetDamage();
		
		if ((float) event.getPlayer().getNoDamageTicks() > (float) event.getPlayer().getMaximumNoDamageTicks() / 2.0F)
		{
			if (dmg <= event.getPlayer().getLastDamage()) return;
		}
		
		if(!event.getDamageEvent().IgnoreArmor())
		{
			int j = 25 - ((CraftLivingEntity)event.getPlayer()).getHandle().br();
			double k = dmg * (float)j;

			dmg = k / 25.0f;	
		}
				
		double newHealth = event.getPlayer().getHealth() - dmg;
		if(newHealth > 0) return;
		
		String stat = event.getBullet().Gun.getStatNameKills(false); 
		addStat(event.getBullet().Shooter, stat, 1, false, false);
		
		event.getBullet().Gun.incrementKill();
		event.getBullet().Gun.updateWeaponName(event.getBullet().Shooter, event.getGame());
		
	}

	

}
