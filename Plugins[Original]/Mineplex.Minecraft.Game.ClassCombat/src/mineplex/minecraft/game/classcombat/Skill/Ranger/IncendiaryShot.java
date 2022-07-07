//package mineplex.minecraft.game.classcombat.Skill.Ranger;
//
//import java.util.HashSet;
//import java.util.Iterator;
//
//import org.bukkit.Material;
//import org.bukkit.Sound;
//import org.bukkit.entity.Arrow;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;
//import org.bukkit.entity.Projectile;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.block.Action;
//import org.bukkit.event.entity.EntityShootBowEvent;
//import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
//
//import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
//import mineplex.minecraft.game.core.damage.CustomDamageEvent;
//import mineplex.core.common.util.F;
//import mineplex.core.common.util.UtilParticle;
//import mineplex.core.common.util.UtilServer;
//import mineplex.core.updater.event.UpdateEvent;
//import mineplex.core.updater.UpdateType;
//import mineplex.core.common.util.UtilPlayer;
//import mineplex.core.common.util.UtilParticle.ParticleType;
//import mineplex.core.common.util.UtilParticle.ViewDist;
//import mineplex.minecraft.game.classcombat.Skill.SkillActive;
//import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
//
//public class IncendiaryShot extends SkillActive
//{
//	private HashSet<Entity> _arrows = new HashSet<Entity>();
//	private HashSet<Player> _active = new HashSet<Player>();
//
//	public IncendiaryShot(SkillFactory skills, String name, ClassType classType, SkillType skillType,
//			int cost, int levels,
//			int energy, int energyMod, 
//			long recharge, long rechargeMod, boolean rechargeInform, 
//			Material[] itemArray, 
//			Action[] actionArray) 
//	{
//		super(skills, name, classType, skillType, 
//				cost, levels,
//				energy, energyMod, 
//				recharge, rechargeMod, rechargeInform, 
//				itemArray,
//				actionArray);
//
//		SetDesc(new String[] 
//				{
//				"Prepare an incendiary shot;",
//				"Your next arrow will ignite",
//				"its target for #2#1 seconds."
//				});
//	}
//
//	@Override
//	public boolean CustomCheck(Player player, int level) 
//	{
//		if (isInWater(player))
//		{
//			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(getName()) + " in water."));
//			return false;
//		}
//
//		return true;
//	}
//
//	@Override
//	public void Skill(Player player, int level) 
//	{
//		//Action
//		_active.add(player);
//
//		//Inform
//		UtilPlayer.message(player, F.main(GetClassType().name(), "You prepared " + F.skill(getName(level)) + "."));
//
//		//Effect
//		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);
//	}
//
//	@EventHandler
//	public void ShootBow(EntityShootBowEvent event)
//	{
//		if (!(event.getEntity() instanceof Player))
//			return;
//
//		if (!(event.getProjectile() instanceof Arrow))
//			return;
//
//		Player player = (Player)event.getEntity();
//
//		if (!_active.remove(player))
//			return;
//
//		//Inform
//		UtilPlayer.message(player, F.main(GetClassType().name(), "You fired " + F.skill(getName(getLevel(player))) + "."));
//
//		_arrows.add(event.getProjectile());
//		event.getProjectile().setFireTicks(200);
//	}
//
//	@EventHandler(priority = EventPriority.HIGH)
//	public void ArrowHit(CustomDamageEvent event)
//	{
//		if (event.IsCancelled())
//			return;
//
//		if (event.GetCause() != DamageCause.PROJECTILE)
//			return;
//
//		Projectile projectile = event.GetProjectile();
//		if (projectile == null)	return;
//
//		if (!_arrows.contains((Entity)projectile))
//			return;
//
//		LivingEntity damagee = event.GetDamageeEntity();
//		if (damagee == null)	return;
//
//		Player damager = event.GetDamagerPlayer(true);
//		if (damager == null)	return;
//
//		//Level
//		int level = getLevel(damager);
//		if (level == 0)			return;
//
//		//Ignite
//		Factory.Condition().Factory().Ignite(getName(), damagee, damager, 2 + level, true, true);
//
//		//Damage
//		event.AddMod(damager.getName(), getName(), 0, true);
//
//		//Effect
//		damagee.getWorld().playSound(damagee.getLocation(), Sound.FIZZ, 2f, 1.5f);
//		
//		//Remove
//		projectile.remove();
//	}
//	
//	@EventHandler
//	public void Particle(UpdateEvent event)
//	{
//		if (event.getType() != UpdateType.TICK)
//			return;
//		
//		for (Entity ent : _arrows)
//		{
//			UtilParticle.PlayParticle(ParticleType.FLAME, ent.getLocation(), 0, 0, 0, 0, 1,
//					ViewDist.MAX, UtilServer.getPlayers());
//		}
//	}
//
//	@EventHandler
//	public void Clean(UpdateEvent event)
//	{
//		if (event.getType() != UpdateType.SEC)
//			return;
//		
//		for (Iterator<Entity> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
//		{
//			Entity arrow = arrowIterator.next();
//			
//			if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround())
//			{
//				arrowIterator.remove();
//			}
//		}
//	}
//
//	@Override
//	public void Reset(Player player) 
//	{
//		_active.remove(player);
//	}
//}
