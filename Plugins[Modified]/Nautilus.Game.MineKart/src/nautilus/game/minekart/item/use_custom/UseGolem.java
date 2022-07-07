package nautilus.game.minekart.item.use_custom;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.item.use_default.ItemUse;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartUtil;
import nautilus.game.minekart.kart.condition.ConditionType;
import nautilus.game.minekart.kart.crash.Crash_Explode;

public class UseGolem extends ItemUse
{
	@Override
	public void Use(KartItemManager manager, Kart kart) 
	{
		kart.SetItemStored(null);
		
		kart.GetGP().Announce(F.main("MK", F.elem(UtilEnt.getName(kart.GetDriver())) + " used " + F.item("Earthquake") + "."));
		
		for (Kart other : manager.KartManager.GetKarts().values())
		{
			if (other.equals(kart))
				continue;
			
			if (!KartUtil.IsGrounded(other))
				continue;
			
			if (other.HasCondition(ConditionType.Star) || other.HasCondition(ConditionType.Ghost))
				continue;
			
			double offset = UtilMath.offset(kart.GetDriver(), other.GetDriver());

			//MiniBump Crash
			if (offset < 100)
				new Crash_Explode(other, 0.4 + ((100 - offset)/100), false);
			
			//Half Velocity
			other.GetVelocity().multiply(0.50);
			
			//Sound
			other.GetDriver().getWorld().playSound(other.GetDriver().getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.5f);
		}	
		
		//Sound
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.5f);
		kart.GetDriver().getWorld().playSound(kart.GetDriver().getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.5f);
		
		//Effect
		for (Block cur : UtilBlock.getInRadius(kart.GetDriver().getLocation(), 4d).keySet())
			if (UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)) && !UtilBlock.airFoliage(cur))
				cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, cur.getTypeId());
	}
}
