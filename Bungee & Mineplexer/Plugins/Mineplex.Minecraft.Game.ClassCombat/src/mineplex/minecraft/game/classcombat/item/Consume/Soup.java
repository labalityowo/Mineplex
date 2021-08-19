package mineplex.minecraft.game.classcombat.item.Consume;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.core.projectile.ProjectileUser;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.item.ItemUsable;

public class Soup extends ItemUsable
{
	public Soup(ItemFactory factory, Material type,
			int amount, boolean canDamage, int gemCost, int tokenCost,
			ActionType useAction, boolean useStock, long useDelay,
			int useEnergy, ActionType throwAction, boolean throwStock,
			long throwDelay, int throwEnergy, float throwPower, 
			long throwExpire, boolean throwPlayer, boolean throwBlock, boolean throwIdle, boolean throwPickup) 
	{
		super(factory, "Mushroom Soup", new String[] { "Consume:", "Heals two hunger points.", "Gives Regen II boost for 4 seconds" }, type, amount, canDamage, gemCost, tokenCost,
				useAction, useStock, useDelay, useEnergy, throwAction, throwStock,
				throwDelay, throwEnergy, throwPower, 
				throwExpire, throwPlayer, throwBlock, throwIdle, throwPickup);
		
		setFree(true);
	}

	@Override
	public void UseAction(PlayerInteractEvent event) 
	{
		Player player = event.getPlayer();

		//Food
		UtilPlayer.hunger(player, 4);

		//Condition
		Factory.Condition().Factory().Custom(GetName(), player, player, 
				ConditionType.REGENERATION, 4, 1, false, 
				Material.MUSHROOM_SOUP, (byte)0, true);
		
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.EAT, 1f, 1f);
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 39);
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 40);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{

	}

	@Override
	public void Idle(ProjectileUser data) 
	{

	}

	@Override
	public void Expire(ProjectileUser data) 
	{

	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}