package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkHammerThrow extends Perk implements IThrown
{	
	private HashMap<Item, Player> _thrown = new HashMap<Item, Player>();
	
	public PerkHammerThrow() 
	{
		super("Hammer Throw", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Diamond Axe to " + C.cGreen + "Hammer Throw"
				});
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilInv.IsItem(event.getItem(), Material.DIAMOND_AXE, (byte) -1))
			return;
		
		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		player.setItemInHand(null);
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		
		//Throw
		Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.DIAMOND_AXE));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 10, true);
		
		//Projectile
		Manager.GetProjectile().AddThrow(item, player, this, -1, true, true, true, false, 0.6f);
		
		//Store
		_thrown.put(item, player);
	}
	
	@EventHandler
	public void Pickup(PlayerPickupItemEvent event)
	{
		if (!event.getPlayer().equals(_thrown.get(event.getItem())))
			return;
		
		event.setCancelled(true);
		event.getItem().remove();
		
		Player player = _thrown.remove(event.getItem());
		
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_AXE, (byte)0, 1, F.item("Thor Hammer")));
	}
	
	@EventHandler
	public void Timeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		Iterator<Item> itemIterator = _thrown.keySet().iterator();
		
		while (itemIterator.hasNext())
		{
			Item item = itemIterator.next();
			
			if (item == null || item.getTicksLived() > 200 || !item.isValid() || item.isDead())
			{
				if (item != null)
					item.remove();
				
				Player player = _thrown.get(item);

				itemIterator.remove();
								
				if (!Manager.IsAlive(player))
					continue;
				
				player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_AXE, (byte)0, 1, F.item("Thor Hammer")));
			}
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Rebound(data.getThrower(), data.getThrown());
		
		if (target == null)
			return;
		
		double damage = 16;
		if (target instanceof Giant)
			damage = 8;
		
		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.LIGHTNING, damage, true, true, false,
				UtilEnt.getName(data.getThrower()), GetName());
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Rebound(data.getThrower(), data.getThrown());
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Rebound(data.getThrower(), data.getThrown());
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	public void Rebound(LivingEntity player, Entity ent)
	{
		ent.getWorld().playSound(ent.getLocation(), Sound.ZOMBIE_METAL, 0.6f, 0.5f);
		
		double mult = 0.5 + (0.6 * (UtilMath.offset(player.getLocation(), ent.getLocation())/16d));
		
		//Velocity
		ent.setVelocity(player.getLocation().toVector().subtract(ent.getLocation().toVector()).normalize().add(new Vector(0, 0.4, 0)).multiply(mult));
		
		//Ticks
		if (ent instanceof Item)
			((Item)ent).setPickupDelay(5);
	}
	
	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;
		
		event.AddKnockback(GetName(), 2);
	}
}
