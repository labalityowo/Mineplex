package mineplex.game.clans.clans.worldevent.raid.wither.creature.mage;

import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class KnightPassive extends BossPassive<UndeadKnight, Skeleton>
{
	private long _lastUsed;
	
	public KnightPassive(UndeadKnight creature)
	{
		super(creature);
		
		_lastUsed = System.currentTimeMillis();
	}
	
	@Override
	public int getCooldown()
	{
		return 12;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick() {}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(false) == null)
		{
			return;
		}
		if (event.GetDamagerEntity(false).getEntityId() == getEntity().getEntityId())
		{
			if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
			{
				_lastUsed = System.currentTimeMillis();
				event.AddMod("Hilt Smash", 4 - event.GetDamage());
				getBoss().getEvent().getCondition().Factory().Slow("Hilt Smash", event.GetDamageeEntity(), event.GetDamagerEntity(false), 2, 1, false, true, false, true);
			}
			else
			{
				event.AddMod("Knight Attack", 4 - event.GetDamage());
			}
		}
	}
	
	@EventHandler
	public void onShoot(EntityShootBowEvent event)
	{
		if (event.getEntity().getEntityId() == getEntity().getEntityId())
		{
			event.setCancelled(true);
		}
	}
}