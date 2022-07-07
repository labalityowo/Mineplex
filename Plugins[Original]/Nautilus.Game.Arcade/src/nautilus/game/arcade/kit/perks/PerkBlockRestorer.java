package nautilus.game.arcade.kit.perks;

import org.bukkit.FireworkEffect.Type;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.data.IBlockRestorer;

public class PerkBlockRestorer extends Perk implements IThrown
{
	public PerkBlockRestorer() 
	{
		super("Repair Master",  new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Throw Repairer",
				});
	}
	
	@EventHandler
	public void Throw(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 12000, true, true))
			return;
		
		event.setCancelled(true);
		
		org.bukkit.entity.Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.COMMAND));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 10, false);
		Manager.GetProjectile().AddThrow(ent, player, this, -1, false, false, true, true, 0.5f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		restore(data.getThrown());
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		restore(data.getThrown());
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		restore(data.getThrown());
	}
	
	public void restore(Entity entity)
	{
		if (Manager.GetGame() != null && Manager.GetGame() instanceof IBlockRestorer)
		{
			((IBlockRestorer)Manager.GetGame()).restoreBlock(entity.getLocation(), 8);
		}
		
		entity.remove();
		
		UtilFirework.playFirework(entity.getLocation(), Type.BALL_LARGE, Color.WHITE, false, true);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}