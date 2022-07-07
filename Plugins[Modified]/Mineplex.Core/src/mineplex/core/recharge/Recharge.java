package mineplex.core.recharge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.event.ClientUnloadEvent;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class Recharge extends MiniPlugin
{
	public static Recharge Instance;
	
	public HashSet<String> informSet = new HashSet<String>();
	public NautHashMap<String, NautHashMap<String, RechargeData>> _recharge = new NautHashMap<String, NautHashMap<String, RechargeData>>();
	
	//Used to increase cooldown when gadgets are being spammed
	public NautHashMap<String, ArrayList<Long>> _gadgetSlow = new NautHashMap<String, ArrayList<Long>>();
	
	protected Recharge(JavaPlugin plugin)
	{
		super("Recharge", plugin);
	}
	
	public static void Initialize(JavaPlugin plugin)
	{
		Instance = new Recharge(plugin);
	}
	
	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		NautHashMap<String, RechargeData> data = Get(event.getEntity().getName());
		List<RechargeData> rechargeDatas = new ArrayList<RechargeData>(data.values());
		
		// Remove/clear all cooldowns that are flagged as ClearOnDeath
		for (RechargeData rechargeData : rechargeDatas)
		{
			if (rechargeData.ClearOnDeath)
			{
				data.remove(rechargeData.Name);
				System.out.println("Cleared " + rechargeData.Name);
			}
			else
			{
				System.out.println("Didn't clear: " + rechargeData.Name);
			}
		}
	}
	
	public NautHashMap<String, RechargeData> Get(String name)
	{
		if (!_recharge.containsKey(name))
			_recharge.put(name, new NautHashMap<String, RechargeData>());
		
		return _recharge.get(name);
	}

	public NautHashMap<String, RechargeData> Get(Player player)
	{
		return Get(player.getName());
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
			recharge();
		
		if (event.getType() == UpdateType.SLOW)
			for (ArrayList<Long> times : _gadgetSlow.values())
			{
				Iterator<Long> timeIter = times.iterator();
				
				while (timeIter.hasNext())
				{
					long time = timeIter.next();
					
					if (UtilTime.elapsed(time, 20000))
						timeIter.remove();
				}
			}
	}
	
	public void recharge()
	{
		for (Player cur : UtilServer.getPlayers())
		{
			LinkedList<String> rechargeList = new LinkedList<String>();

			//Check Recharged
			for (String ability : Get(cur).keySet())
			{
				if (Get(cur).get(ability).Update())
					rechargeList.add(ability);
			}

			//Inform Recharge
			for (String ability : rechargeList)
			{
				Get(cur).remove(ability);
				
				//Event
				RechargedEvent rechargedEvent = new RechargedEvent(cur, ability);
				UtilServer.getServer().getPluginManager().callEvent(rechargedEvent);
				
				if (informSet.contains(ability))
					UtilPlayer.message(cur, F.main("Recharge", "You can use " + F.skill(ability) + "."));
			}
		}
	}
	
	public boolean use(Player player, String ability, long recharge, boolean inform, boolean attachItem)
	{
		return use(player, ability, ability, recharge, inform, attachItem, false, true, null);
	}
	
	public boolean use(Player player, String ability, long recharge, boolean inform, boolean attachItem, String cooldownPool)
	{
		return use(player, ability, ability, recharge, inform, attachItem, false, true, cooldownPool);
	}
	
	public boolean use(Player player, String ability, String abilityFull, long recharge, boolean inform, boolean attachItem)
	{
		return use(player, ability, abilityFull, recharge, inform, attachItem, false, true, null);
	}
	
	public boolean use(Player player, String ability, long recharge, boolean inform, boolean attachItem, boolean attachDurability)
	{
		return use(player, ability, ability, recharge, inform, attachItem, attachDurability, true, null);
	}
	
	public boolean use(Player player, String ability, String abilityFull, long recharge, boolean inform, 
						boolean attachItem, boolean attachDurability, boolean clearOnDeath, String cooldownPool)
	{
		if (recharge == 0)
			return true;
		
		//Cooldown Pool
		if (cooldownPool != null)
		{
			 if (!_gadgetSlow.containsKey(cooldownPool))
				 _gadgetSlow.put(cooldownPool, new ArrayList<Long>());
			 			 
			 int count = _gadgetSlow.get(cooldownPool).size();
			 
			 recharge += count * 40;
		}

		//Ensure Expirey
		recharge();
		
		//Lodge Recharge Msg
		if (inform && recharge > 1000)
			informSet.add(ability);
	
		//Recharging
		if (Get(player).containsKey(ability))
		{
			if (inform && !Get(player).get(ability).DisplayForce && !Get(player).get(ability).AttachItem)
			{
				UtilPlayer.message(player, F.main("Recharge", "You cannot use " + F.skill(abilityFull) + " for " + 
						F.time(UtilTime.convertString((Get(player).get(ability).GetRemaining()), 1, TimeUnit.FIT)) + "."));
			}

			return false;
		}
		
		//Add Slow
		if (cooldownPool != null)
			_gadgetSlow.get(cooldownPool).add(System.currentTimeMillis());

		//Insert
		UseRecharge(player, ability, recharge, attachItem, attachDurability, clearOnDeath);

		return true;
	}
	
	public void useForce(Player player, String ability, long recharge)
	{
		useForce(player, ability, recharge, false);
	}
	
	public void useForce(Player player, String ability, long recharge, boolean attachItem)
	{
		UseRecharge(player, ability, recharge, attachItem, false, true);
	}
	
	public boolean usable(Player player, String ability)
	{
		return usable(player, ability, false);
	}
	
	public boolean usable(Player player, String ability, boolean inform)
	{
		return usable(player, ability, inform, null);
	}

	/**
	 * Checks if cooldown is over, using a custom message or not
	 * @param player The player to be checked
	 * @param ability The ability to be checked
	 * @param inform Should it inform the player?
	 * @param message The custom message (if NULL, default message will be shown)
	 * @return If the ability is in cooldown or not for that player
	 */
	public boolean usable(Player player, String ability, boolean inform, String message)
	{
		if (!Get(player).containsKey(ability))
			return true;

		if (Get(player).get(ability).GetRemaining() <= 0)
		{
			return true;
		}
		else
		{
			if (inform && !Get(player).get(ability).DisplayForce && !Get(player).get(ability).AttachItem)
			{
				if (message == null)
				{
					UtilPlayer.message(player, F.main("Recharge", "You cannot use " + F.skill(ability) + " for " +
							F.time(UtilTime.convertString((Get(player).get(ability).GetRemaining()), 1, TimeUnit.FIT)) + "."));
				}
				else
				{
					UtilPlayer.message(player, F.main("Recharge", message.replace("%a", F.skill(ability))
							.replace("%t", F.time(UtilTime.convertString(Get(player).get(ability).GetRemaining(),
									1, TimeUnit.FIT)))));
				}
			}

			return false;
		}
	}
	
	public void UseRecharge(Player player, String ability, long recharge, boolean attachItem, boolean attachDurability, boolean clearOnDeath)
	{
		//Event
		RechargeEvent rechargeEvent = new RechargeEvent(player, ability, recharge);
		UtilServer.getServer().getPluginManager().callEvent(rechargeEvent);
		
		Get(player).put(ability, new RechargeData(this, player, ability, player.getItemInHand(), 
				rechargeEvent.GetRecharge(), attachItem, attachDurability, clearOnDeath));
	}
	
	public void recharge(Player player, String ability)
	{
		Get(player).remove(ability);
	}

	@EventHandler
	public void clearPlayer(final ClientUnloadEvent event)
	{
		_recharge.remove(event.GetName());
	}
	
	public void setDisplayForce(Player player, String ability, boolean displayForce)
	{
		if (!_recharge.containsKey(player.getName()))
			return;

		if (!_recharge.get(player.getName()).containsKey(ability))
			return;

		_recharge.get(player.getName()).get(ability).DisplayForce = displayForce;
	}
	
	public void setCountdown(Player player, String ability, boolean countdown)
	{
		if (!_recharge.containsKey(player.getName()))
			return;

		if (!_recharge.get(player.getName()).containsKey(ability))
			return;

		_recharge.get(player.getName()).get(ability).Countdown = countdown; 
	}
	
	public void Reset(Player player) 
	{
		_recharge.put(player.getName(), new NautHashMap<String, RechargeData>());
	}
	
	public void Reset(Player player, String stringContains) 
	{
		NautHashMap<String, RechargeData> data = _recharge.get(player.getName());
		
		if (data == null)
			return;
		
		Iterator<String> rechargeIter = data.keySet().iterator();
		
		while (rechargeIter.hasNext())
		{
			String key = rechargeIter.next();
			
			if (key.toLowerCase().contains(stringContains.toLowerCase()))
			{
				rechargeIter.remove();
			}
		}
	}

	public void debug(Player player, String ability)
	{
		if (!_recharge.containsKey(player.getName()))
		{
			player.sendMessage("No Recharge Map.");
			return;
		}
		
		if (!_recharge.get(player.getName()).containsKey(ability))
		{
			player.sendMessage("Ability Not Found.");
			return;
		}
		
		_recharge.get(player.getName()).get(ability).debug(player);
	}
	
	public void displayExpBar(Player player, String ability)
	{
		if (!_recharge.containsKey(player.getName()))
		{
			player.setExp(1F);
			return;
		}
		
		RechargeData recharge = _recharge.get(player.getName()).get(ability);
		
		if (recharge == null)
		{
			player.setExp(1F);
			return;
		}

		float elasped = (float) (System.currentTimeMillis() - recharge.Time) / recharge.Recharge;

		player.setExp(Math.max(Math.min(1, elasped), 0));
	}
}
