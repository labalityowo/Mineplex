package mineplex.minecraft.game.core.condition;

import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.conditions.*;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ConditionFactory
{
	public ConditionManager Manager;

	public ConditionFactory(ConditionManager manager) 
	{
		Manager = manager;
	}

	public Condition Custom(String reason, LivingEntity ent, LivingEntity source, 
			ConditionType type, double duration, int mult, boolean extend, 
			Material indMat, byte indData, boolean showIndicator)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source,  
				type, mult, (int)(20 * duration), extend, 
				indMat, indData, showIndicator, false));
	}

	public Condition Invulnerable(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend, boolean showIndicator)
	{
		showIndicator = false;

		return Manager.AddCondition(new Condition(Manager, reason, ent, source,  
				ConditionType.INVULNERABLE, 0, (int)(20 * duration), extend, 
				Material.GHAST_TEAR, (byte)0, showIndicator, false));
	}
	
	public Condition FireItemImmunity(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend)
	{
		return Manager.AddCondition(new FireItemImmunity(Manager, reason, ent, source,  
				ConditionType.FIRE_ITEM_IMMUNITY, 0, (int)(20 * duration), extend, 
				Material.GHAST_TEAR, (byte)0, false));
	}

	public Condition Cloak(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend, boolean inform)
	{
		return Manager.AddCondition(new Cloak(Manager, reason, ent, source,  
				ConditionType.CLOAK, 0, (int)(20 * duration), extend, 
				Material.GHAST_TEAR, (byte)0, false));
	}

	public Condition untrueCloak(String reason, LivingEntity ent, LivingEntity source, double duration, boolean extend)
	{
		return Manager.AddCondition(new UntrueCloak(Manager, reason, ent, source, ConditionType.UNTRUE_CLOAK, 0, (int)(20 * duration), extend, Material.GHAST_TEAR, (byte)0, false));
	}

	public Condition Explosion(String reason, LivingEntity ent, LivingEntity source, 
			int mult, double duration, boolean extend, boolean showIndicator)
	{
		//Never Show
		showIndicator = false;

		return Manager.AddCondition(new Condition(Manager, reason, ent, source,  
				ConditionType.EXPLOSION, mult, (int)(20 * duration), extend, 
				Material.GHAST_TEAR, (byte)0, showIndicator, false));
	}

	public Condition Lightning(String reason, LivingEntity ent, LivingEntity source, 
			int mult, double duration, boolean extend, boolean showIndicator)
	{
		showIndicator = false;

		return Manager.AddCondition(new Condition(Manager, reason, ent, source,  
				ConditionType.LIGHTNING, mult, (int)(20 * duration), extend, 
				Material.GHAST_TEAR, (byte)0, showIndicator, false));
	}

	public Condition Falling(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend, boolean showIndicator)
	{
		showIndicator = false;

		return Manager.AddCondition(new Condition(Manager, reason, ent, source,  
				ConditionType.FALLING, 0, (int)(20 * duration), extend, 
				Material.GHAST_TEAR, (byte)0, showIndicator, false));
	}

	public Condition Silence(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend, boolean showIndicator)
	{
		return Manager.AddCondition(new Silence(Manager, reason, ent, source,  
				ConditionType.SILENCE, 0, (int)(20 * duration), extend, 
				Material.WATCH, (byte)0, showIndicator));
	}

	public Condition Speed(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.SPEED, mult, (int)(20 * duration), extend, 
				Material.FEATHER, (byte)0, showIndicator, ambient));
	}

	public Condition Strength(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.INCREASE_DAMAGE, mult, (int)(20 * duration), extend, 
				Material.IRON_SWORD, (byte)0, showIndicator, ambient));
	}

	public Condition Hunger(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		showIndicator = false;

		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.HUNGER, mult, (int)(20 * duration), extend, 
				Material.ROTTEN_FLESH, (byte)0, showIndicator, ambient));
	}

	public Condition Regen(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.REGENERATION, mult, (int)(20 * duration), extend, 
				Material.INK_SACK, (byte)1, showIndicator, ambient)); 
	}

	public Condition Weakness(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{	
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.WEAKNESS, mult, (int)(20 * duration), extend, 
				Material.INK_SACK, (byte)15, showIndicator, ambient)); 
	}

	public Condition Protection(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.DAMAGE_RESISTANCE, mult, (int)(20 * duration), extend, 
				Material.IRON_CHESTPLATE, (byte)0, showIndicator, ambient)); 
	}

	public Condition FireResist(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.FIRE_RESISTANCE, mult, (int)(20 * duration), extend, 
				Material.BLAZE_POWDER, (byte)0, showIndicator, ambient)); 
	}

	public Condition Breath(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.WATER_BREATHING, mult, (int)(20 * duration), extend, 
				Material.INK_SACK, (byte)4, showIndicator, ambient)); 
	}

	public Condition DigFast(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.FAST_DIGGING, mult, (int)(20 * duration), extend, 
				Material.GLOWSTONE_DUST, (byte)0, showIndicator, ambient));
	}

	public Condition DigSlow(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.SLOW_DIGGING, mult, (int)(20 * duration), extend, 
				Material.WOOD_PICKAXE, (byte)0, showIndicator, ambient)); 
	}

	public Condition Jump(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.JUMP, mult, (int)(20 * duration), extend, 
				Material.CARROT_ITEM, (byte)0, showIndicator, ambient)); 
	}

	public Condition Invisible(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		showIndicator = false;

		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.INVISIBILITY, mult, (int)(20 * duration), extend, 
				Material.SNOW_BALL, (byte)0, showIndicator, ambient)); 
	}

	/*
	public Condition Vulnerable(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Vulnerability(Manager, reason, ent, source, 
				ConditionType.WITHER, mult, (int)(20 * duration), extend, 
				Material.BONE, (byte)0, showIndicator, ambient)); 
	}
	*/
	
	public Condition Shock(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend, boolean showIndicator)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source,  
				ConditionType.SHOCK, 0, (int)(20 * duration), extend, 
				Material.DEAD_BUSH, (byte)0, showIndicator, false));
	}
	
	public Condition Ignite(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend, boolean showIndicator)
	{
		showIndicator = false;

		return Manager.AddCondition(new Burning(Manager, reason, ent, source,  
				ConditionType.BURNING, 0, (int)(20 * duration), extend, 
				Material.GHAST_TEAR, (byte)0, showIndicator));
	}

	public Condition Slow(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean stun, boolean ambient)
	{
		if (stun)
			ent.setVelocity(new Vector(0,0,0));

		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.SLOW, mult, (int)(20 * duration), extend, 
				Material.WEB, (byte)0, showIndicator, ambient));
	}
	
	public Condition Wither(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{		
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.WITHER, mult, (int)(20 * duration), extend, 
				Material.SKULL_ITEM, (byte)1, showIndicator, ambient)); 
	}
	
	public Condition Poison(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{		
		return Poison(reason, ent, source, duration, mult, extend, showIndicator, ambient, false);
	}
	
	public Condition Poison(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient, boolean cancelPotion)
	{		
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.POISON, mult, (int)(20 * duration), extend, 
				Material.SLIME_BALL, (byte)14, showIndicator, ambient, cancelPotion)); 
	}
	
	public Condition PoisonShock(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend)
	{		
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.POISON_SHOCK, 0, (int)(20 * duration), extend, 
				Material.SLIME_BALL, (byte)14, false, false)); 
	}
	
	public Condition Confuse(String reason, LivingEntity ent, LivingEntity source,
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.CONFUSION, mult, (int)(20 * duration), extend, 
				Material.ENDER_PEARL, (byte)0, showIndicator, ambient)); 
	}
	
	public Condition Blind(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.BLINDNESS, mult, (int)(20 * duration), extend, 
				Material.EYE_OF_ENDER, (byte)0, showIndicator, ambient)); 
	}
	
	public Condition NightVision(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.NIGHT_VISION, mult, (int)(20 * duration), extend, 
				Material.EYE_OF_ENDER, (byte)0, showIndicator, ambient)); 
	}
	
	public Condition HealthBoost(String reason, LivingEntity ent, LivingEntity source, 
			double duration, int mult, boolean extend, boolean showIndicator, boolean ambient)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.HEALTH_BOOST, mult, (int)(20 * duration), extend, 
				Material.APPLE, (byte)0, showIndicator, ambient)); 
	}
	
	public Condition ArcadeHungerDisable(String reason, LivingEntity ent, LivingEntity source, 
			double duration, boolean extend)
	{
		return Manager.AddCondition(new Condition(Manager, reason, ent, source, 
				ConditionType.ARCADE_HUNGER_DISABLE, 0, (int)(20 * duration), extend, 
				Material.COAL, (byte)0, false, false)); 
	}
}
