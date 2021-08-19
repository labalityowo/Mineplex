package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
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
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkSeismicSlamHG extends Perk
{	
	public PerkSeismicSlamHG() 
	{
		super("Ground Pound", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with " + "Sword/Axe" + " to " + C.cGreen + "Ground Pound"
				});
	}

	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		Player player = event.getPlayer();

		if (!UtilItem.isAxe(player.getItemInHand()) && !UtilItem.isSword(player.getItemInHand()))
		{
			return;
		}

		if (!Kit.HasKit(player) || !Recharge.Instance.use(player, GetName(), 14000, true, true))
		{
			return;
		}

		//Action
		double range = 6;

		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), range);
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(player))
				continue;

			cur.playEffect(EntityEffect.HURT);

			//Velocity
			UtilAction.velocity(cur, 
					UtilAlg.getTrajectory2d(player.getLocation().toVector(), cur.getLocation().toVector()), 
					1.8 * targets.get(cur), true, 0, 0.3 + 0.9 * targets.get(cur), 1.2, true);

			//Condition
			Manager.GetCondition().Factory().Falling(GetName(), cur, player, 10, false, true);

			//Inform
			UtilPlayer.message(cur, F.main("Game", F.name(player.getName()) +" hit you with " + F.skill(GetName()) + "."));
		}

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);
		for (Block cur : UtilBlock.getInRadius(player.getLocation(), 4d).keySet())
			if (UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)) && !UtilBlock.airFoliage(cur))
				cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, cur.getTypeId());


		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}
}
