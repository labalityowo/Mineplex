package nautilus.game.arcade.kit.perks;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkShadowmeld extends Perk
{
	private HashSet<Player> _active = new HashSet<Player>();
	
	public PerkShadowmeld() 
	{
		super("Shadowmeld", new String[] 
				{ 
				"Hold Crouch to become invisible.",
				"",
				"Shadowmeld ends if you attack or an",
				"enemy comes within 4 blocks of you."
				});
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void ChargeBlock(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;

		for (Player cur : Manager.GetGame().GetPlayers(true))
		{	
			if (!Kit.HasKit(cur))
				continue;
			
			//Sneak
			if (!_active.contains(cur) && cur.isSneaking())
			{
				cur.setExp(Math.min(0.999f, cur.getExp() + (1f/60f)));
				
				if (cur.getExp() >= 0.99f)
				{
					Manager.GetCondition().Factory().Cloak(GetName(), cur, cur, 2.9, false, false);
				}
					
			}
			//End
			else 
			{
				end(cur);
			}
		}
	}
	
	private void end(Player cur)
	{
		_active.remove(cur);
		cur.setExp(0f);
		Manager.GetCondition().EndCondition(cur, null, GetName());
	}

	@EventHandler
	public void endProximity(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (Manager.GetGame() == null)
			return;
		
		for (Player cur : Manager.GetGame().GetPlayers(true))
		{
			if (!_active.contains(cur)) 	
				continue;
			
			//Proximity Decloak
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (other.equals(cur))
					continue;

				if (UtilMath.offset(cur, other) > 4)
					continue;

				end(cur);
				break;
			}

			Manager.GetCondition().Factory().Cloak(GetName(), cur, cur, 2.9, false, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void endDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		end(damagee);
	}

	@EventHandler
	public void EndInteract(PlayerInteractEvent event)
	{
		end(event.getPlayer());
	}
	
	@EventHandler
	public void EndBow(EntityShootBowEvent event)
	{
		end((Player)event.getEntity());
	}
}
