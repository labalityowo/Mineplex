package mineplex.game.clans.clans.worldevent.raid.wither.creature.mage;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.Managers;
import mineplex.core.blood.Blood;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class MageBoneExplode extends BossPassive<UndeadMage, Skeleton>
{
	private long _lastUsed;
	
	public MageBoneExplode(UndeadMage creature)
	{
		super(creature);
		_lastUsed = System.currentTimeMillis();
	}
	
	private void explode()
	{
		Map<Player, Double> nearby = UtilPlayer.getInRadius(getLocation(), 4);
		for (Player near : nearby.keySet())
		{
			getBoss().getEvent().getDamageManager().NewDamageEvent(near, getEntity(), null, DamageCause.CUSTOM, 3, true, true, false, getEntity().getName(), "Bone Explosion");
		}
		Managers.get(Blood.class).Effects(null, getLocation().add(0, 0.5, 0), 48, 0.8, Sound.SKELETON_HURT, 2f, 1.2f, Material.BONE, (byte) 0, 40, false);
	}
	
	@Override
	public int getCooldown()
	{
		return 8;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
		if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
		{
			_lastUsed = System.currentTimeMillis();
			explode();
		}
	}
	
	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains("Bone Explosion"))
		{
			return;
		}
		
		if (event.GetDamagerEntity(false) == null || event.GetDamagerEntity(false).getEntityId() != getEntity().getEntityId())
		{
			return;
		}
		
		event.AddKnockback("Bone Explosion", 5);
	}
}