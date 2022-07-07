package mineplex.minecraft.game.classcombat.item.Consume;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.ProjectileUser;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.item.ItemUsable;

public class Apple extends ItemUsable
{
	public Apple(ItemFactory factory, Material type,
			int amount, boolean canDamage, int gemCost, int tokenCost,
			ActionType useAction, boolean useStock, long useDelay,
			int useEnergy, ActionType throwAction, boolean throwStock,
			long throwDelay, int throwEnergy, float throwPower, 
			long throwExpire, boolean throwPlayer, boolean throwBlock, boolean throwIdle, boolean throwPickup) 
	{
		super(factory, "Apple", new String[] { "Consume:", "Heals two hunger points.", " ", "Thrown:", "Does half a heart of damage on hit." }, type, amount, canDamage, gemCost, tokenCost,
				useAction, useStock, useDelay, useEnergy, throwAction, throwStock,
				throwDelay, throwEnergy, throwPower, 
				throwExpire, throwPlayer, throwBlock, throwIdle, throwPickup);
	}

	@Override
	public void UseAction(PlayerInteractEvent event) 
	{
		Player player = event.getPlayer();

		//Food
		UtilPlayer.hunger(player, 4);

		//Energy
		Factory.Energy().ModifyEnergy(player, 10);

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.EAT, 1f, 1f);
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 40);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{	
		if (target == null)
			return;

		//Damage Event
		Factory.Damage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.CUSTOM, 2, true, true, false,
				UtilEnt.getName(data.getThrower()), GetName());

		//Effect
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.CHICKEN_EGG_POP, 1f, 1.6f);

		//Re-Drop
		if (data.getThrown() instanceof Item)
			data.getThrown().getWorld().dropItem(data.getThrown().getLocation(), ItemStackFactory.Instance.CreateStack(((Item)data.getThrown()).getItemStack().getType())).setPickupDelay(60);

		data.getThrown().remove();
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