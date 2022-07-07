package nautilus.game.arcade.game.games.castleassault.kits;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkBloodlust extends Perk
{
	private Map<Player, Integer> _lusting = new WeakHashMap<>();
	
	private double _damageBoost;
	private int _duration;
	
	public PerkBloodlust(double damageBoost, int duration) 
	{
		super("Bloodlust",
			new String[]
			{ 
				C.cGray + "Deal an extra " + (damageBoost / 2) + " hearts of damage for " + duration + " seconds after a kill.",
			}
		);
		
		_damageBoost = damageBoost;
		_duration = duration;
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		Integer id = _lusting.remove(event.getEntity());
		if (id != null)
		{
			Bukkit.getScheduler().cancelTask(id);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (event.GetLog().GetKiller() == null)
		{
			return;
		}

		if (!event.GetLog().GetKiller().IsPlayer())
		{
			return;
		}

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (player == null)
		{
			return;
		}
		
		if (!Kit.HasKit(player))
		{
			return;
		}
		
		if (!Manager.GetGame().IsAlive(player))
		{
			return;
		}
		
		if (UtilPlayer.isSpectator(player))
		{
			return;
		}
		
		if (!hasPerk(player))
		{
			return;
		}
		
		Integer id = _lusting.remove(player);
		if (id != null)
		{
			Bukkit.getScheduler().cancelTask(id);
		}
		
		player.sendMessage(C.cRed + "You are now channeling bloodlust for " + _duration + " seconds!");
		_lusting.put(player, Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), () -> _lusting.remove(player), _duration * 20).getTaskId());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void damageIncrease(EntityDamageByEntityEvent event)
	{
		if (event.getCause() == DamageCause.FIRE_TICK)
			return;
		
		if (event.getDamage() <= 1)
			return;
		
		if (!(event.getDamager() instanceof Player))
			return;
		
		Player damager = (Player) event.getDamager();
		
		if (!Kit.HasKit(damager))
		{
			return;
		}
		
		if (!hasPerk(damager))
		{
			return;
		}
		
		if (!Manager.IsAlive(damager))
		{
			return;
		}
		
		if (!_lusting.containsKey(damager))
		{
			return;
		}
		
		event.setDamage(event.getDamage() + _damageBoost);
	}
}