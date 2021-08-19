package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkLeapTackleHG extends Perk
{
	private HashMap<LivingEntity, Long> _live = new HashMap<LivingEntity, Long>();

	public PerkLeapTackleHG() 
	{
		super("Leap Attack", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Sword/Axe to " + C.cGreen + "Leap Tackle"
				});
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE") && !event.getPlayer().getItemInHand().getType().toString().contains("_SWORD"))
			return; 

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		UtilAction.velocity(player, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 1.2, true);

		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void End(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Collide
		for (Player player : Manager.GetGame().GetPlayers(true))
			if (_live.containsKey(player))
				for (Player other : Manager.GetGame().GetPlayers(true))
					if (!Manager.isSpectator(other))
						if (!other.equals(player))
							if (UtilMath.offset(player, other) < 2)
							{
								Hit(player, other);
								_live.remove(player);
								return;
							}

		//End
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!UtilEnt.isGrounded(player))
				continue;

			if (!_live.containsKey(player))
				continue;

			if (!UtilTime.elapsed(_live.get(player), 1000))  
				continue;

			_live.remove(player);			
		}	
	}
	
	public void Hit(Player damager, LivingEntity damagee)
	{
		damagee.playEffect(EntityEffect.HURT);
		
		//Slow
		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 1, 3, false, false, true, false);
		Manager.GetCondition().Factory().Slow(GetName(), damager, damagee, 1, 3, false, false, true, false);

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
	}
}
