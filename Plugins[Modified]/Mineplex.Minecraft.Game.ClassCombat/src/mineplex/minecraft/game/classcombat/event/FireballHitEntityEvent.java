package mineplex.minecraft.game.classcombat.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FireballHitEntityEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    
    private Projectile _projectile;
    private LivingEntity _hitEntity;
    
    private boolean _cancelled;
    
    public FireballHitEntityEvent(Projectile proj, LivingEntity entity)
    {
    	_projectile = proj;
    	_hitEntity = entity;
	}

    public Projectile getProjectile()
    {
    	return _projectile;
    }
    
    public LivingEntity getHitEntity()
    {
    	return _hitEntity;
    }
    
    public void setCancelled(boolean cancelled)
    {
    	_cancelled = cancelled;
    }
    
    public boolean isCancelled()
    {
    	return _cancelled;
    }
    
	public HandlerList getHandlers()
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
