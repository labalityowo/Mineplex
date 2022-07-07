package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Sharpshooter extends Skill
{
	private WeakHashMap<Player, Integer> _missedCount = new WeakHashMap<Player, Integer>();
	private WeakHashMap<Player, Integer> _hitCount = new WeakHashMap<Player, Integer>();
	private HashMap<Arrow, Player> _arrows = new HashMap<Arrow, Player>();

	public Sharpshooter(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Hitting with arrows increases",
				"arrow damage by 1 for 5 seconds.",
				"",
				"Stacks up to #0#2 times, and each",
				"hit sets duration to 5 seconds."
				});
	}

	@EventHandler
	public void shootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		int level = getLevel((Player)event.getEntity());
		if (level == 0)		return;
		
		if (!(event.getProjectile() instanceof Arrow))
		{
			System.out.println(GetName() + " : " + event.getEntity().getName() + " shot bow but resulting projectile was now Arrow?!?!?!?");
			return;
		}
		
		//Store
		_arrows.put((Arrow) event.getProjectile(), (Player)event.getEntity());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile projectile = event.GetProjectile();
		if (projectile == null)	return;

		if (!_arrows.containsKey(projectile))
			return;

		Player player = _arrows.remove(projectile);
		int level = getLevel(player);
	
		if (event.GetDamagerEntity(true) != null && event.GetDamagerEntity(true).equals(event.GetDamageeEntity()))
			return;
		
		int bonus = 0;
		if (_hitCount.containsKey(player))
			bonus = _hitCount.get(player);
		
		//Increase
		bonus = Math.min(bonus + 1, 2 * level);
		_hitCount.put(player, bonus);
		
		//Damage
		event.AddMod(player.getName(), GetName(), bonus, true);
	
		//Inform
		UtilPlayer.message(event.GetDamagerPlayer(true), F.main(GetClassType().name(), GetName() + ": " + 
				F.elem(bonus + " Consecutive Hits") + C.cGray + " (" + F.skill("+" + bonus + " Damage" ) + C.cGray + ")" ) );
	
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 0.8f + (float)(bonus * 0.2));

		projectile.remove();
		
		_missedCount.remove(player);
		
		Recharge.Instance.useForce(player, GetName() + " Timer", 5000);
	}
	
	@EventHandler
	public void missReset(ProjectileHitEvent event)
	{
		final Projectile projectile = event.getEntity();
		Factory.runSyncLater(() ->
		{
		    if (!projectile.isDead() && _arrows.containsKey(projectile))
		    {
		    	Player shooter = (Player) projectile.getShooter();
		    	
		    	if (!_hitCount.containsKey(shooter))
		    	{
		    		return;
		    	}
		    	
		    	if (!_missedCount.containsKey(shooter))
		    	{
		    		_missedCount.put(shooter, 1);
		    	}
		    	else
		    	{
		    		_missedCount.put(shooter, _missedCount.get(shooter) + 1);
		    		
		    		// Reset
		    		if (_missedCount.get(shooter) >= 2)
		    		{
		    			_hitCount.remove(shooter);
		    			_missedCount.remove(shooter);
						UtilPlayer.message(shooter, F.main(GetClassType().name(), GetName() + " : " + F.elem("Damage Bonus Reset")));
						shooter.playSound(shooter.getLocation(), Sound.NOTE_PLING, 1f, 0.75f);
		    		}
		    	}
		    }
		}, 3l);
	}
	
	@EventHandler
	public void resetViaTime(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Player> playerIter = _hitCount.keySet().iterator();
		
		while (playerIter.hasNext())
		{
			Player player = playerIter.next();
			
			if (Recharge.Instance.usable(player, GetName() + " Timer"))
			{
				playerIter.remove();
				UtilPlayer.message(player, F.main(GetClassType().name(), GetName() + ": " + F.elem("Damage Bonus Reset")));
				player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 0.75f);
			}
		}
	}
	
//	@EventHandler
//	public void resetViaMiss(UpdateEvent event)
//	{
//		if (event.getType() != UpdateType.FAST)
//			return;
//
//		HashSet<Entity> remove = new HashSet<Entity>();
//
//		for (Entity cur : _arrows.keySet())
//			if (cur.isDead() || !cur.isValid() || cur.isOnGround())
//				remove.add(cur);
//
//		for (Entity cur : remove)
//		{
//			Player player = _arrows.remove(cur);
//
//			if (player != null)
//				if (_hitCount.remove(player) != null)
//					UtilPlayer.message(player, F.main(GetClassType().name(), getName() + ": " + F.elem("0 Consecutive Hits")));
//		}		
//	}

	@Override
	public void Reset(Player player) 
	{
		_hitCount.remove(player);
		_missedCount.remove(player);
	}
}