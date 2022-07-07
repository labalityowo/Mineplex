package mineplex.core.gadget.gadgets.item;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemPartyPopper extends ItemGadget implements IThrown
{
	HashSet<Item> _items = new HashSet<>();

	public ItemPartyPopper(GadgetManager manager)
	{
		super(manager, "Party Popper", 
				UtilText.splitLineToArray(C.cWhite + "Celebrate by blasting confetti into peoples' eyes!", LineFormat.LORE),
				1, Material.GOLDEN_CARROT, (byte) 0, 1000, new Ammo("Party Popper", "1 Party Popper", Material.GOLDEN_CARROT,
				(byte) 0, new String[]
					{
						C.cWhite + "100 Party Poppers for you to shoot!"
					}, -2, 1));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		/*
//		Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), 
//				ItemStackFactory.Instance.CreateStack(Material.REDSTONE_LAMP_OFF, (byte)0, 1, "Lamp" + Math.random()));
//		
//		UtilAction.velocity(ent, player.getLocation().getDirection().normalize().multiply(0.1), 1, false, 0, 0.2, 10, false);
//		
//		Manager.getProjectileManager().AddThrow(ent, player, this, 3000, false, false, true, true, 0.5f);
		
		*/
		
		
		for(int data : new int[]{1,2,4,5,6,9,10,11,12,13,14,15})
		{
//			UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, data), player.getEyeLocation(), null, 0.4f, 50, ViewDist.LONG);
			
			for(int i = 0; i < 10; i++)
			{	
				Vector v = new Vector(Math.random() - 0.5, Math.random() - 0.3, Math.random() - 0.5);
				v.normalize();
				v.multiply(0.2);
				v.add(player.getLocation().getDirection());
				v.normalize().multiply(Math.random()*0.4 + 0.4);
				
				UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, data), player.getEyeLocation(), v, 1, 0, ViewDist.LONG);
			}
			
			/*
			Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), 
					ItemStackFactory.Instance.CreateStack(Material.INK_SACK, (byte)data, 1, "Ink" + Math.random()));
			_items.add(ent);
			
			Vector random = new Vector(Math.random() - 0.5, Math.random() - 0.3, Math.random() - 0.5);
			random.normalize();
			random.multiply(0.05);

			UtilAction.velocity(ent, player.getLocation().getDirection().normalize().multiply(0.3).add(random), 1 + 0.4 * Math.random(), false, 0, 0.2, 10, false);	
			
			Manager.getProjectileManager().AddThrow(ent, player, this, 3000, false, false, true, true, 0.5f);
			*/
		}
		// Sound
		for(int i = 0; i < 3; i++)
		{
			player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 2.0f, 0.8f);
		}
	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Explode(data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Explode(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Explode(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	@EventHandler
	public void particleTrail(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		
		for(Item item : _items)
		{
			byte data = item.getItemStack().getData().getData();
			UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, data), item.getLocation(), 0, 0, 0, 0.0f, 3, ViewDist.LONG);
		}
	}

	public void Explode(ProjectileUser data)
	{
		for(int type : new int[]{1,2,4,5,6,9,10,11,12,13,14,15})
		{
			UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, type), data.getThrown().getLocation(), null, 0.4f, 50, ViewDist.LONG);
		}
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.LAVA_POP, 0.75f, 1.25f);
		data.getThrown().remove();
		/*
		if (data.getThrown() instanceof Item)
		{
			Item item = (Item) data.getThrown();
			byte b = item.getItemStack().getData().getData();
			UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, b), item.getLocation(), 0, 0, 0, 0.2f, 80, ViewDist.LONG);
			_items.remove(item);
		}
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.LAVA_POP, 0.75f, 1.25f);
		data.getThrown().remove();
		*/
	}
}