package nautilus.game.arcade.game.games.evolution.evolve;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.hologram.HologramManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.modules.compass.CompassAttemptTargetEvent;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.Evolution;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.kit.perks.event.PerkConstructorEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EvolveManager implements Listener
{
	/**
	 * @author Mysticate
	 */
	
	public final Evolution Host;
	
	private final NautHashMap<Location, SimpleEntry<Location, Location>> _evolveLocs;
	
	private NautHashMap<String, EvolveData> _data = new NautHashMap<String, EvolveData>();
	
	public EvolveManager(Evolution evolution, NautHashMap<Location, SimpleEntry<Location, Location>> evolveLocs)
	{
		Host = evolution;
		_evolveLocs = evolveLocs;
		
		Bukkit.getPluginManager().registerEvents(this,  Host.Manager.getPlugin());
	}
	
	private PlatformToken getLocation()
	{
		ArrayList<Entry<Location, SimpleEntry<Location, Location>>> locs = new ArrayList<Entry<Location, SimpleEntry<Location, Location>>>(_evolveLocs.entrySet());
		Entry<Location, SimpleEntry<Location, Location>> entry = locs.get(UtilMath.r(locs.size()));
		
		return new PlatformToken(entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue());
	}

	public boolean isEvolving(Player player)
	{		
		return _data.containsKey(player.getName());
	}
	
	public void addEvolve(HologramManager holo, Player player, EvoKit from, EvoKit to)
	{
		if (_data.containsKey(player.getName()))
			return;
		
		_data.put(player.getName(), new EvolveData(this, holo, getLocation(), player, from, to));
	}
	
	public EvolveData getEvolve(Player player)
	{
		return _data.get(player.getName());
	}
	
	@EventHandler
	public void tick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!Host.IsLive())
			return;
		
		Iterator<Entry<String, EvolveData>> iterator = _data.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, EvolveData> entry = iterator.next();
			
			Player player = UtilPlayer.searchExact(entry.getKey());
			
			if (player == null || !player.isOnline())
			{
				try
				{
					entry.getValue().end();
				}
				catch (NullPointerException ex) {}
				
				iterator.remove();
				continue;
			}
			
			if (!Host.IsAlive(player))
			{
				try
				{
					entry.getValue().end();
				}
				catch (NullPointerException ex) {}
				
				iterator.remove();
				continue;
			}
				
			if (entry.getValue().tick())
			{			
				try
				{
					entry.getValue().end();
				}
				catch (NullPointerException ex) {}
				
				iterator.remove();
				continue;
			}
		}
	}
	
	public void end()
	{
		for (EvolveData data : _data.values())
		{
			data.end();
		}
		
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEvolveDamageRecieve(CustomDamageEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (!(event.GetDamageeEntity() instanceof Player))
			return;
		
		if (isEvolving(event.GetDamageePlayer()))
			event.SetCancelled("Player is evolving!");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEvolveDamageDeal(CustomDamageEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (!(event.GetDamagerEntity(true) instanceof Player))
			return;
		
		if (isEvolving(event.GetDamagerPlayer(true)))
			event.SetCancelled("Player is evolving!");
	}
	
	@EventHandler
	public void onEvolveAbility(EvolutionAbilityUseEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (isEvolving(event.getPlayer()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEvolveCompass(CompassAttemptTargetEvent event)
	{
		if (!Host.IsLive())
			return;

		if (!(event.getTarget() instanceof Player))
			return;

		if (isEvolving((Player) event.getTarget()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEvolveConstruct(PerkConstructorEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (isEvolving(event.getPlayer()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEvolveMove(PlayerMoveEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (isEvolving(event.getPlayer()))
			event.setTo(event.getFrom());
	}

	@EventHandler
	public void onEggFall(BlockPhysicsEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (event.getChangedType() == Material.DRAGON_EGG || event.getBlock().getType() == Material.DRAGON_EGG)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEggSolidify(EntityChangeBlockEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (!(event.getEntity() instanceof FallingBlock))
			return;
		
		if (((FallingBlock) event.getEntity()).getMaterial() != Material.DRAGON_EGG)
			return;
		
		event.setCancelled(true);
		event.getEntity().remove();
	}
	
}
