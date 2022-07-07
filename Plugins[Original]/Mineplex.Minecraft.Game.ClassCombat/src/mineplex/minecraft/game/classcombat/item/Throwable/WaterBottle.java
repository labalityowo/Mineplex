package mineplex.minecraft.game.classcombat.item.Throwable;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.projectile.ProjectileUser;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.item.ItemUsable;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;

public class WaterBottle extends ItemUsable
{
	private static final ConditionType[] NEGATIVE_TYPES = 
	{
		ConditionType.BLINDNESS,
		ConditionType.CONFUSION,
		ConditionType.HARM,
		ConditionType.HUNGER,
		ConditionType.POISON,
		ConditionType.POISON_SHOCK,
		ConditionType.SHOCK,
		ConditionType.SLOW,
		ConditionType.SLOW_DIGGING,
		ConditionType.WEAKNESS,
		ConditionType.WITHER,
		ConditionType.SILENCE
	};
	
	public WaterBottle(ItemFactory factory, Material type,
			int amount, boolean canDamage, int gemCost, int tokenCost,
			ActionType useAction, boolean useStock, long useDelay,
			int useEnergy, ActionType throwAction, boolean throwStock,
			long throwDelay, int throwEnergy, float throwPower, 
			long throwExpire, boolean throwPlayer, boolean throwBlock, boolean throwIdle, boolean throwPickup) 
	{
		super(factory, "Water Bottle", new String[] { 
				"Thrown, giving AoE effect;", 
				"* 3 Range",
				"* Douses Players",
				"* Extinguishes Fires",
				"Used, giving personal effect;", 
				"* Douses Player",
				"* Fire Resistance for 4 Seconds"
				}, type, amount, canDamage, gemCost, tokenCost,
				useAction, useStock, useDelay, useEnergy, throwAction, throwStock,
				throwDelay, throwEnergy, throwPower, 
				throwExpire, throwPlayer, throwBlock, throwIdle, throwPickup);
		
		setFree(true);
	}

	@Override
	public void UseAction(PlayerInteractEvent event) 
	{
		Player player = event.getPlayer();
		
		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		//Extinguish
		player.setFireTicks(-20);
		
		//Resist
		Factory.Condition().Factory().FireResist(GetName(), player, player, 4, 0, false, true, true);
		
		BukkitTask task = UtilServer.runSyncTimer(() ->
		{
			for (ConditionType type : NEGATIVE_TYPES)
			{
				Factory.Condition().EndCondition(player, type, null);
			}
		}, 0, 10);
		UtilServer.runSyncLater(task::cancel, 20 * 4);

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 1f, 1.4f);
		player.getWorld().playEffect(player.getEyeLocation(), Effect.STEP_SOUND, 8);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Break(data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Break(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Break(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	public void Break(ProjectileUser data)
	{
		if (!(data.getThrower() instanceof Player))
		{
			return;
		}

		Player thrower = (Player) data.getThrower();
		Location location = data.getThrown().getLocation();

		//Splash
		data.getThrown().getWorld().playEffect(location, Effect.STEP_SOUND, 20);
		data.getThrown().getWorld().playEffect(location, Effect.STEP_SOUND, 8);
		data.getThrown().getWorld().playSound(location, Sound.SPLASH, 1f, 1.4f);

		//Extinguish
		Factory.Fire().RemoveNear(data.getThrown().getLocation(), 3);
		
		//Remove
		data.getThrown().remove();

		for (Player player : UtilPlayer.getNearby(data.getThrown().getLocation(), 3))
		{
			if (Factory.getRelation().canHurt(player, thrower))
			{
				continue;
			}

			//Extinguish
			player.setFireTicks(-20);
			
			for (ConditionType type : NEGATIVE_TYPES)
			{
				Factory.Condition().EndCondition(player, type, null);
			}
		}
	}
}