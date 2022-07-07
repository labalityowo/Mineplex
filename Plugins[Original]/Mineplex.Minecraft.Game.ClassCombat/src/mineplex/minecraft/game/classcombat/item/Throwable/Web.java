package mineplex.minecraft.game.classcombat.item.Throwable;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.projectile.ProjectileUser;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.item.ItemUsable;
import mineplex.minecraft.game.classcombat.item.event.WebTossEvent;

public class Web extends ItemUsable
{

	private final Map<Block, LivingEntity> _owner = new HashMap<>();

	public Web(ItemFactory factory, Material type,
			int amount, boolean canDamage, int gemCost, int tokenCost,
			ActionType useAction, boolean useStock, long useDelay,
			int useEnergy, ActionType throwAction, boolean throwStock,
			long throwDelay, int throwEnergy, float throwPower, 
			long throwExpire, boolean throwPlayer, boolean throwBlock, boolean throwIdle, boolean throwPickup) 
	{
		super(factory, "Web", new String[] { "Thrown:", "Used to trap enemies." }, type, amount, canDamage, gemCost, tokenCost,
				useAction, useStock, useDelay, useEnergy, throwAction, throwStock,
				throwDelay, throwEnergy, throwPower, 
				throwExpire, throwPlayer, throwBlock, throwIdle, throwPickup);
		
		setFree(true);
	}

	@Override
	public void UseAction(PlayerInteractEvent event) 
	{

	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (target != null)
		{
			double distance = UtilMath.offset(target.getLocation(), data.getThrown().getLocation());
			
			if (distance > .75)
			{
				data.getThrown().teleport(data.getThrown().getLocation().add(new Vector(0, -distance / 2, 0)));
			}
		}
		
		createWeb(data.getThrown());
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		createWeb(data.getThrown());
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		createWeb(data.getThrown());
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	private void createWeb(Entity ent)
	{
		LivingEntity thrower = Factory.Throw().getThrower(ent);
		
		//Effect
		ent.getWorld().playEffect(ent.getLocation(), Effect.STEP_SOUND, 30);
		ent.remove();
		
		if (canWeb(thrower, ent.getLocation()))
		{
			Block block = ent.getLocation().getBlock();
			Factory.BlockRestore().add(block, 30, (byte) 0, 6000);

			_owner.put(block, thrower);
			Factory.runSyncLater(() -> _owner.remove(block), 120);
		}
	}
	
	private boolean canWeb(LivingEntity thrower, Location location)
	{
		WebTossEvent webEvent = new WebTossEvent(thrower, location);
		Bukkit.getPluginManager().callEvent(webEvent);
		
		return !webEvent.isCancelled() && UtilBlock.airFoliage(location.getBlock()) && !location.getBlock().getType().toString().contains("BANNER");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRightClick(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Block block = event.getClickedBlock();
		LivingEntity owner = _owner.remove(block);

		if (event.getPlayer().equals(owner))
		{
			Factory.BlockRestore().restore(block);
		}
	}
}