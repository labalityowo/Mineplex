package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

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
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkBodySlam extends Perk
{
	private HashMap<LivingEntity, Long> _live = new HashMap<LivingEntity, Long>();

	private int _damage;
	private double _knockback;
	
	public PerkBodySlam(int damage, double knockback) 
	{
		super("Body Slam", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Body Slam"
				});
		
		_damage = damage;
		_knockback = knockback;
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

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return; 

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		UtilAction.velocity(player, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 0.8, true);

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
								DoSlam(player, other);
								_live.remove(player);
								return;
							}

		//End
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!_live.containsKey(player))
				continue;
			
			if (UtilEnt.isGrounded(player) || UtilTime.elapsed(_live.get(player), 1000))
				_live.remove(player);			
		}	
	}
	
	public void DoSlam(Player damager, LivingEntity damagee)
	{
		//Damage Event
		Manager.GetDamage().NewDamageEvent(damager, damagee, null, 
				DamageCause.CUSTOM, _damage/3d, true, true, false,
				damager.getName(), GetName() + " Recoil");	

		//Damage Event
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, _damage, true, true, false,
				damager.getName(), GetName());	

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;
		
		event.AddKnockback(GetName(), _knockback);
	}
}
