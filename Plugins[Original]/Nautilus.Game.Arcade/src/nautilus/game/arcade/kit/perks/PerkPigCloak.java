package nautilus.game.arcade.kit.perks;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkPigCloak extends Perk
{
	public PerkPigCloak() 
	{
		super("Cloak", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Cloak"
				});
	}
	
	@EventHandler
	public void Use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (!UtilGear.isAxe(event.getItem()))
			return; 
		
		if (!Kit.HasKit(player))
			return;
		
		event.setCancelled(true);

		if (!Recharge.Instance.use(player, GetName(), GetName(), 10000, true, true))
			return;

		//Action
		Manager.GetCondition().Factory().Cloak(GetName(), player, player, 5, false, false);

		for (int i=0 ; i<3 ; i++)
		{
			player.getWorld().playSound(player.getLocation(), Sound.SHEEP_SHEAR, 2f, 0.5f);
			player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 80);
		}
		
		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void EndDamagee(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		if (!Kit.HasKit(damagee))
			return;

		//End
		Manager.GetCondition().EndCondition(damagee, null, GetName());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void EndDamager(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		if (!Kit.HasKit(damager))
			return;

		//End
		Manager.GetCondition().EndCondition(damager, null, GetName());
	}
	
	@EventHandler
	public void EndInteract(PlayerInteractEvent event)
	{
		if (!Kit.HasKit(event.getPlayer()))
			return;
		
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
			return;

		Manager.GetCondition().EndCondition(event.getPlayer(), null, GetName());
	}

	@EventHandler
	public void Reset(PlayerDeathEvent event) 
	{
		//Remove Condition
		Manager.GetCondition().EndCondition(event.getEntity(), null, GetName());
	}
}
