package mineplex.minecraft.game.core.condition;

import java.util.ArrayList;
import java.util.HashMap;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConditionApplicator
{
	//Types
	private HashMap<String, PotionEffectType> _effectMap;
	
	public ConditionApplicator() 
	{
		_effectMap = new HashMap<String, PotionEffectType>();
		_effectMap.put("Blindness", PotionEffectType.BLINDNESS);
		_effectMap.put("Confusion", PotionEffectType.CONFUSION);
		_effectMap.put("DamageResist", PotionEffectType.DAMAGE_RESISTANCE);
		_effectMap.put("FastDig", PotionEffectType.FAST_DIGGING);
		_effectMap.put("FireResist", PotionEffectType.FIRE_RESISTANCE);
		_effectMap.put("Harm", PotionEffectType.HARM);
		_effectMap.put("Heal", PotionEffectType.HEAL);
		_effectMap.put("Hunger", PotionEffectType.HUNGER);
		_effectMap.put("Strength", PotionEffectType.INCREASE_DAMAGE);
		_effectMap.put("Jump", PotionEffectType.JUMP);
		_effectMap.put("Poison", PotionEffectType.POISON);
		_effectMap.put("Regeneration", PotionEffectType.REGENERATION);
		_effectMap.put("Slow", PotionEffectType.SLOW);
		_effectMap.put("SlowDig", PotionEffectType.SLOW_DIGGING);
		_effectMap.put("Speed", PotionEffectType.SPEED);
		_effectMap.put("Breathing", PotionEffectType.WATER_BREATHING);
		_effectMap.put("Weakness", PotionEffectType.WEAKNESS);
		_effectMap.put("Invisibility", PotionEffectType.INVISIBILITY);
		_effectMap.put("NightVision", PotionEffectType.NIGHT_VISION);
	}
	
	public void clearEffects(Player player)
	{
		for (PotionEffectType cur : _effectMap.values())
			player.removePotionEffect(cur);
	}

	public void listEffect(Player caller)
	{
		caller.sendMessage(ChatColor.RED + "[C] " + ChatColor.YELLOW + "Listing Potion Effects;");

		caller.sendMessage(ChatColor.DARK_GREEN + "Health Effects: " + ChatColor.AQUA +
				"Harm, Heal, Poison, Regeneration, Hunger");

		caller.sendMessage(ChatColor.DARK_GREEN + "Damage Effects: " + ChatColor.AQUA +
				"Strength, Weakness, DamageResist, FireResist");

		caller.sendMessage(ChatColor.DARK_GREEN + "Movement Effects: " + ChatColor.AQUA +
				"Slow, Speed, Jump, FireResist");

		caller.sendMessage(ChatColor.DARK_GREEN + "Vision Effects: " + ChatColor.AQUA +
				"Blindness, Confusion, NightVision");

		caller.sendMessage(ChatColor.DARK_GREEN + "Misc Effects: " + ChatColor.AQUA +
				"FastDig, SlowDig, Breathing, Invisibility");
	}

	public HashMap<String, PotionEffectType> readEffect(Player caller, String eString)
	{
		HashMap<String, PotionEffectType> eList = new HashMap<String, PotionEffectType>();
		ArrayList<String> errorList = new ArrayList<String>();

		String[] eToken = eString.split(",");

		for (String eCur : eToken)
		{
			for (String cur : _effectMap.keySet())
			{
				if (cur.equalsIgnoreCase(eCur))
				{
					eList.put(cur, _effectMap.get(cur));
				}
				else
				{
					errorList.add(eCur);
				}
			}
		}

		if (!errorList.isEmpty())
		{
			String out = ChatColor.RED + "[C] " + ChatColor.YELLOW + "Invalid Effects:" + ChatColor.AQUA;

			for (String cur : errorList)
			{
				out += " '" + cur + "'";
			}

			caller.sendMessage(out);
		}

		return eList;
	}

	public void doEffect(Player caller, String name, HashMap<String, PotionEffectType> eMap, String durationString, String strengthString, boolean extend)
	{
		//All Players
		ArrayList<Player> targetList = new ArrayList<Player>();
		ArrayList<String> invalidList = new ArrayList<String>();
		if (name.equalsIgnoreCase("all"))
		{
			for (Player cur : UtilServer.getPlayers())
				targetList.add(cur);
		}
		//Listed Players
		else
		{
			String[] playerTokens = name.split(",");

			for (String curName : playerTokens)
			{
				Player target = (Player)UtilPlayer.matchOnline(null, name, false);

				if (target != null)
				{
					targetList.add(target);
				}
				else
				{
					invalidList.add(curName);
				}	
			}
		}

		if (!invalidList.isEmpty())
		{
			String out = ChatColor.RED + "[C] " + ChatColor.YELLOW + "Invalid Targets:";
			for (String cur : invalidList)
			{
				out += " '" + cur + "'";
			}
			caller.sendMessage(out);
		}


		if (targetList.isEmpty())
		{
			caller.sendMessage(ChatColor.RED + "[C] " + ChatColor.YELLOW + "No Valid Targets Listed.");
			return;
		}

		//Parse Effects
		if (eMap.isEmpty())
		{
			caller.sendMessage(ChatColor.RED + "[C] " + ChatColor.YELLOW + "No Valid Effects Listed.");
			return;
		}	

		//Parse Duration and Strength
		double duration;
		int strength;
		try
		{
			duration = Double.parseDouble(durationString);
			strength = Integer.parseInt(strengthString);

			if (duration <= 0)		
			{
				caller.sendMessage(ChatColor.RED + "[C] " + ChatColor.YELLOW + "Invalid Effect Duration.");
				return;
			}

			if (strength < 0)		
			{
				caller.sendMessage(ChatColor.RED + "[C] " + ChatColor.YELLOW + "Invalid Effect Strength.");
				return;
			}
		}
		catch (Exception ex)
		{
			caller.sendMessage(ChatColor.RED + "[C] " + ChatColor.YELLOW + "Invalid Effect Duration/Strength.");
			return;
		}


		caller.sendMessage(ChatColor.RED + "[C] " + ChatColor.YELLOW + "Applying Effect(s) to Target(s).");
		for (Player curPlayer : targetList)
		{
			for (String cur : _effectMap.keySet())
			{
				addEffect(curPlayer, cur, _effectMap.get(cur), duration, strength, true, extend);
			}
		}
	}

	public boolean addEffect(LivingEntity target, String effectName, PotionEffectType type, double duration, int strength, boolean inform, boolean extend)
	{	
		//Duration of Previous
		int oldDur = 0;

		if (target.hasPotionEffect(type))
		{
			for (PotionEffect cur : target.getActivePotionEffects())
			{
				if (cur.getType().equals(type))
				{
					if (cur.getAmplifier() > strength)
						return true;

					else if (extend)
						oldDur += cur.getDuration();
				}
			}

			//Remove Old
			target.removePotionEffect(type);
		}
		
		//Create & Add
		target.addPotionEffect(new PotionEffect(type, (int)(duration*20) + oldDur, strength), true);

		if (inform && target instanceof Player)
		{
			Player tPlayer = (Player)target;
			UtilPlayer.message(tPlayer, F.main("Condition", "You received " + 
					F.elem(effectName + " " + (strength+1)) +
					" for " + F.time(""+UtilMath.trim(1, duration * 20d)) + " Seconds."));

		}

		return false;
	}
}
