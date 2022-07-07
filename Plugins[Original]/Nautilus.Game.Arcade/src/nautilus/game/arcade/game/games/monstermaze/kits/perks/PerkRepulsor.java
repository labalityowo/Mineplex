package nautilus.game.arcade.game.games.monstermaze.kits.perks;

import java.util.HashMap;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.monstermaze.events.AbilityUseEvent;
import nautilus.game.arcade.game.games.monstermaze.events.EntityLaunchEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkRepulsor extends Perk
{
	private HashMap<Entity, Long> _launched = new HashMap<Entity, Long>();
	
	public PerkRepulsor()
	{
		super("Repulsor", new String[]
				{
				F.elem("Click") + " with Coal to use " + F.skill("Repulse") + ".",
				});
	}

	@EventHandler
	public void onRepulse(PlayerInteractEvent event)
	{	
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R)) 
			return;
		
		if (!UtilInv.IsItem(event.getItem(), Material.COAL, (byte) 0))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player)) 
			return;
		
		event.setCancelled(true);
		
		UtilInv.remove(player, Material.COAL, (byte)0, 1);
		UtilInv.Update(player);
		
		UtilFirework.playFirework(player.getLocation(), Type.BALL_LARGE, Color.AQUA, false, false);
		
		for (Entity ent : UtilEnt.getInRadius(player.getLocation(), 6).keySet())
		{
			if (ent instanceof Player || !(ent instanceof LivingEntity))
				continue;
				
			ent.playEffect(EntityEffect.HURT);
			UtilAction.velocity(ent, UtilAlg.getTrajectory2d(player, ent), 1, true, 0, 0.8, 2, true);

			_launched.put(ent, System.currentTimeMillis());
			
			Bukkit.getPluginManager().callEvent(new EntityLaunchEvent(ent));
		}
		
		Bukkit.getPluginManager().callEvent(new AbilityUseEvent(player));
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		HashMap<Entity, Long> copy = new HashMap<Entity, Long>();
		copy.putAll(_launched);
		
		for (Entity en : copy.keySet())
		{
			if (en == null || !en.isValid())
			{
				remove(en);
				continue;
			}
			
			if (en.isOnGround() && UtilTime.elapsed(copy.get(en), 500))
			{
				remove(en);
				continue;
			}
			
			//If there are blocks surrounding the block it's on top of (if it's on the side of a block)
			if (!UtilBlock.getInBoundingBox(en.getLocation().clone().add(1, -1, 1), en.getLocation().clone().subtract(1, 1, 1), true).isEmpty() && UtilTime.elapsed(copy.get(en), 500))
			{
				remove(en);
				continue;
			}
			
			if (UtilTime.elapsed(copy.get(en), 1500))
			{
				remove(en);
				continue;
			}
		}
	}
	
	private void remove(Entity en)
	{
		_launched.remove(en);
		en.remove();
		
		UtilFirework.playFirework(en.getLocation(), Type.BALL, Color.BLACK, false, false);
	}
}
