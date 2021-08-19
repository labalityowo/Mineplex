package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkSlam extends Perk
{
	private String _name = "Leap";
	private double _vertical;
	private double _horizontal;
	private long _recharge;
	
	public PerkSlam(String name, double power, double heightLimit, long recharge) 
	{
		super("Slam", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + name
				});
		
		_name = name;
		_vertical = power;
		_horizontal = heightLimit;
		_recharge = recharge;
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
		
		if (!Recharge.Instance.use(player, _name, _recharge, true, true))
			return;
		
		//Slam
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), 6);
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(player))
				continue;

			//Velocity
			UtilAction.velocity(cur, 
					UtilAlg.getTrajectory2d(player.getLocation().toVector(), cur.getLocation().toVector()), 
					_horizontal * targets.get(cur), true, 0, 0.4 + _vertical * targets.get(cur), 0.4 + _vertical, true);
			
			//Condition
			Manager.GetCondition().Factory().Falling(GetName(), cur, player, 10, false, true);

			//Inform
			if (cur instanceof Player)
				UtilPlayer.message((Player)cur, F.main("Game", F.name(player.getName()) +" hit you with " + F.skill(_name) + "."));	
		}
		
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);
		for (Block cur : UtilBlock.getInRadius(player.getLocation(), 4d).keySet())
			if (UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)) && !UtilBlock.airFoliage(cur))
				cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, cur.getTypeId());
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(_name) + "."));
	}
}
