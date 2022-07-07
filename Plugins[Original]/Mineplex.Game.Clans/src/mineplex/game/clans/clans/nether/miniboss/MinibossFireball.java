package mineplex.game.clans.clans.nether.miniboss;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.game.clans.clans.ClansManager;

/**
 * Manager class for managing boss fireballs
 */
public class MinibossFireball implements Listener
{
	private static final double FIREBALL_EXPLOSION_RANGE = 5;
	private static final float SOUND_VOLUME = 1f;
	private static final float SOUND_PITCH = 0.8f;
	private static final double STRENGTH_MULTIPLIER = 1.6;
	private static final double VERT_MULTIPLIER = 0.8;
	
	public MinibossFireball()
	{
		Bukkit.getPluginManager().registerEvents(this, ClansManager.getInstance().getPlugin());
	}
	
	@EventHandler
	public void onHit(ProjectileHitEvent event)
	{
		Projectile proj = event.getEntity();

		if (!(proj instanceof LargeFireball))
		{
			return;
		}
		if (!proj.hasMetadata("MINIBOSS_FIREBALL"))
		{
			return;
		}

		Map<LivingEntity, Double> hitMap = UtilEnt.getInRadius(proj.getLocation(), FIREBALL_EXPLOSION_RANGE);
		for (LivingEntity cur : hitMap.keySet())
		{	
			double range = hitMap.get(cur);
			
			ClansManager.getInstance().getCondition().Factory().Ignite("Fireball", cur, ((LivingEntity)proj.getMetadata("MINIBOSS_FIREBALL").get(0).value()), 7 * range, false, false);
			ClansManager.getInstance().getCondition().Factory().Falling("Fireball", cur, ((LivingEntity)proj.getMetadata("MINIBOSS_FIREBALL").get(0).value()), 10, false, true);
			UtilAction.velocity(cur, UtilAlg.getTrajectory(proj.getLocation().add(0, -0.5, 0), cur.getEyeLocation()), 
					STRENGTH_MULTIPLIER * range, false, 0, VERT_MULTIPLIER * range, 1.2, true);
		}
	}
	
	/**
	 * Checks if the given projectile is a boss fireball
	 * @param entity The projectile to check
	 * @return Whether the given projectile is a boss fireball
	 */
	public static boolean isFireball(Projectile entity)
	{
		return entity.hasMetadata("MINIBOSS_FIREBALL");
	}
	
	/**
	 * Makes an entity shoot a fireball
	 * @param shooter The entity to shoot from
	 */
	public static void launchFireball(LivingEntity shooter)
	{
		LargeFireball ball = shooter.launchProjectile(LargeFireball.class);
		ball.setShooter(shooter);
		ball.setIsIncendiary(false);		
		ball.setYield(0);
		ball.setBounce(false);
		ball.teleport(shooter.getEyeLocation().add(shooter.getLocation().getDirection().multiply(1)));
		ball.setVelocity(new Vector(0,0,0));
		ball.setMetadata("MINIBOSS_FIREBALL", new FixedMetadataValue(ClansManager.getInstance().getPlugin(), shooter));
		shooter.getWorld().playSound(shooter.getLocation(), Sound.GHAST_FIREBALL, SOUND_VOLUME, SOUND_PITCH);
	}
}