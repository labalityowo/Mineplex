package nautilus.game.arcade.game.games.evolution.mobs.perks;

import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.C;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class PerkFlamingSwordEVO extends Perk
{
	/**
	 * @author Mysticate
	 */
	
	private NautHashMap<Player, Long> _active = new NautHashMap<Player, Long>();
		
	public PerkFlamingSwordEVO() 
	{
		super("Flamethrower", new String[] 
				{ 
				C.cYellow + "Block" + C.cGray + " to use " + C.cGreen + "Flamethrower"
				});
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!UtilInv.IsItem(event.getItem(), Material.BLAZE_ROD, (byte) 0))
			return;
		
		if (!Manager.IsAlive(event.getPlayer()))
			return;
		
		if (_active.containsKey(event.getPlayer()))
			return;
		
		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(event.getPlayer(), GetName(), 5000);
		Bukkit.getServer().getPluginManager().callEvent(useEvent);
		
		if (useEvent.isCancelled())
			return;
		
		if (!Recharge.Instance.use(event.getPlayer(), useEvent.getAbility(), useEvent.getCooldown(), true, true))
			return;
		
		_active.put(event.getPlayer(), System.currentTimeMillis());
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		Iterator<Entry<Player, Long>> iterator = _active.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<Player, Long> entry = iterator.next();
			
			if (entry.getKey() == null || !entry.getKey().isOnline())
			{
				iterator.remove();
				continue;
			}
			
			if (!Manager.IsAlive(entry.getKey()))
			{
				iterator.remove();
				continue;
			}
			
			if (UtilPlayer.isSpectator(entry.getKey()))
			{
				iterator.remove();
				continue;
			}
			
			if (UtilTime.elapsed(entry.getValue(), 1000))
			{
				iterator.remove();
				continue;
			}
			
			EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(entry.getKey(), GetName(), 0);
			Bukkit.getServer().getPluginManager().callEvent(useEvent);
			
			if (useEvent.isCancelled())
				continue;
			
			flame(useEvent.getPlayer());
		}
	}
	
	private void flame(Player player)
	{
		//Fire
		Item fire = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER));
		Manager.GetFire().Add(fire, player, 0.7, 0, 0.5, 1, "Inferno", false);

		fire.teleport(player.getEyeLocation());
		double x = 0.07 - (UtilMath.r(14)/100d);
		double y = 0.07 - (UtilMath.r(14)/100d);
		double z = 0.07 - (UtilMath.r(14)/100d);
		fire.setVelocity(player.getLocation().getDirection().add(new Vector(x,y,z)).multiply(1.6));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.GHAST_FIREBALL, 0.1f, 1f);
	}
}
