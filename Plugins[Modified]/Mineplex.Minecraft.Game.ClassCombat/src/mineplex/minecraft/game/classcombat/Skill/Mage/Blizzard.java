
package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Blizzard extends SkillActive
{

	private final Set<Player> _active = new HashSet<>();
	private final Map<Projectile, Player> _snowball = new WeakHashMap<>();

	public Blizzard(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
			int cost, int levels, 
			int energy, int energyMod, 
			long recharge, long rechargeMod, boolean rechargeInform, 
			Material[] itemArray, 
			Action[] actionArray) 
	{
		super(skills, name, classType, skillType,
				cost, levels,
				energy, energyMod, 
				recharge, rechargeMod, rechargeInform, 
				itemArray,
				actionArray);

		SetDesc(new String[] 
				{
				"Hold Block to release a Blizzard.",
				"Releases #1#1 snowballs per wave",
				"which push players away from you."
				});
	}

	@Override
	public String GetEnergyString()
	{
		return "Energy: 31#-1 per Second";
	}
	
	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		return true;
	}
	
	@Override
	public void Skill(Player player, int level) 
	{
		_active.add(player);
	}
	
	@EventHandler
	public void Energy(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.contains(cur))
				continue;
			
			if (!cur.isBlocking())
			{
				_active.remove(cur);
				continue;
			}

			//Level
			int level = getLevel(cur);
			if (level == 0)			
			{
				_active.remove(cur);
				continue;
			}

			//Energy
			if (!Factory.Energy().Use(cur, GetName(), 1.55 - level * 0.05, true, true))
			{
				_active.remove(cur);
			}
		}
	}

	@EventHandler
	public void Snow(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.contains(cur))
				continue;
			
			if (!cur.isBlocking())
			{
				_active.remove(cur);
				continue;
			}

			//Level
			int level = getLevel(cur);
			if (level == 0)			
			{
				_active.remove(cur);
				continue;
			}

			//Target
//			HashSet<Byte> ignore = new HashSet<Byte>();
//			ignore.add((byte) 0);
//			ignore.add((byte) 78);
//			ignore.add((byte) 80);

//			Block target = cur.getTargetBlock(ignore, 7);

			//Snowballs
//			if (target == null || target.getType() == Material.AIR || UtilMath.offset(target.getLocation(), cur.getLocation()) > 5)
//				for (int i=0 ; i<1+level ; i++)
//				{
//					Projectile snow = cur.launchProjectile(Snowball.class);
//					
//					double mult = 0.25 + 0.15 * level;
//					double x = (0.2 - (UtilMath.r(40)/100d)) * mult;
//					double y = (UtilMath.r(20)/100d) * mult;
//					double z = (0.2 - (UtilMath.r(40)/100d)) * mult;
//					
//					
//					
//					snow.setVelocity(cur.getLocation().getDirection().add(new Vector(x,y,z)).multiply(2));
//					_snowball.put(snow, cur);
//				}
//
//			if (target == null || target.getType() == Material.AIR)				
//				continue;
//
//			if (UtilMath.offset(target.getLocation(), cur.getLocation()) > 7)
//				continue;
//
//			HashMap<Block, Double> blocks = UtilBlock.getInRadius(target.getLocation(), 2d, 1);
//			for (Block block : blocks.keySet())
//			{
//				Factory.BlockRestore().Snow(block, (byte)(1 + (int)(2*blocks.get(block))), (byte)(7*level), 2500, 250, 3);
//			}
			
			//Snowballs
			for (int i=0 ; i<1+level ; i++)
			{
				Projectile snow = cur.launchProjectile(Snowball.class);
				
				double mult = 0.25 + 0.15 * level;
				double x = (0.2 - (UtilMath.r(40)/100d)) * mult;
				double y = (UtilMath.r(20)/100d) * mult;
				double z = (0.2 - (UtilMath.r(40)/100d)) * mult;
				
				
				
				snow.setVelocity(cur.getLocation().getDirection().add(new Vector(x,y,z)).multiply(2));
				_snowball.put(snow, cur);
			}

			//Effect
//			target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, 80);
			cur.getWorld().playSound(cur.getLocation(), Sound.STEP_SNOW, 0.1f, 0.5f);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Snowball(CustomDamageEvent event)
	{		
		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile proj = event.GetProjectile();
		if (proj == null)		return;

		if (!(proj instanceof Snowball))
			return;
		
		if (!_snowball.containsKey(proj))
			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		event.SetCancelled(GetName());
		UtilAction.velocity(damagee, proj.getVelocity().multiply(0.1).add(new Vector(0, 0.15, 0)));
	}

//	@EventHandler
//	public void SnowballForm(ProjectileHitEvent event)
//	{
//		if (!(event.getEntity() instanceof Snowball))
//			return;
//		
//		if (_snowball.remove(event.getEntity()) == null)
//			return;
//
//		Factory.BlockRestore().Snow(event.getEntity().getLocation().getBlock(), (byte)1, (byte)7, 2000, 250, 0);
//	}
	
	@EventHandler
	public void clearInvalidSnowballedPlayers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		for (Iterator<Entry<Projectile, Player>> snowBallIterator = _snowball.entrySet().iterator(); snowBallIterator.hasNext();) 
		{
			Entry<Projectile, Player> entry = snowBallIterator.next();
			
			if (!entry.getKey().isValid() || entry.getKey().getTicksLived() > 25 || !entry.getValue().isOnline())
			{
				snowBallIterator.remove();
				entry.getKey().remove();
			}
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}
}
