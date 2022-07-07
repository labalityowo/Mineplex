package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkTakedown extends Perk
{
	private HashMap<LivingEntity, Long> _live = new HashMap<LivingEntity, Long>();

	public PerkTakedown() 
	{
		super("Takedown", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Sword to " + C.cGreen + "Takedown"
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

		if (!UtilGear.isSword(event.getPlayer().getItemInHand()))
			return; 

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 12000, true, true))
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
								collide(player, other);
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
	
	public void collide(Player damager, LivingEntity damagee)
	{
		//Damage Event
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, 8, true, true, false,
				damager.getName(), GetName());	
		
		UtilAction.zeroVelocity(damager);

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;
		
		event.AddKnockback(GetName(), 2);
	}
}
