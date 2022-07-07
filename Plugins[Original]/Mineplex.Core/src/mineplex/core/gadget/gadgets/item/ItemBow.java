package mineplex.core.gadget.gadgets.item;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;

public class ItemBow extends ItemGadget
{
	/**
	 * Created by: Mysticate
	 * Timestamp: February 4, 2016
	 */
	
	private HashSet<Snowball> _arrows = new HashSet<Snowball>();
	private boolean _spawning = false;
	
	private String[] _responses = new String[]
			{
					"Do you feel the love?",
					"Are you in love?",
					"(I think they like you!)",
					"Ahhh. Young love!",
					"Maybe they'll bring you a rose!",
					"You should go say Hi to them.",
					"They look nice.",
					"I love love!",
					"Go get 'em, tiger!",
					"Don't be TOO flirty, now.",
					"That was one of my good arrows!",
					"Do you believe in love at first sight?",
					"Never gunna give you up, never gunna let you down.",
					"I approve this message!",
			};
	
	private IPacketHandler _packetHandler = new IPacketHandler()
	{
		@Override
		public void handle(PacketInfo packetInfo)
		{
			if (_spawning)
			{
				packetInfo.setCancelled(true);
				return;
			}
			
			PacketPlayOutSpawnEntity packet = (PacketPlayOutSpawnEntity) packetInfo.getPacket();
			
			for (Snowball arrow : _arrows)
			{
				if (arrow.getEntityId() == packet.a)
				{
					packetInfo.setCancelled(true);
					return;
				}
			}
		}
	};
	
	@SuppressWarnings("unchecked")
	public ItemBow(GadgetManager manager)
	{
		super(manager, "Cupid's Arrows", 
				UtilText.splitLineToArray(C.cGray + "This symbol of love will live on with you forever! Mainly because we couldn't attach the cupid wings to it. I guess duct tape can't fix everything!", LineFormat.LORE), 
				-6, Material.BOW, (byte) 0, 1000, new Ammo("Cupid Arrow", "Cupid's Arrows", Material.ARROW, (byte) 0, UtilText.splitLineToArray(C.cGray + "Use these arrows to shoot love into people's hearts!", LineFormat.LORE), -6, 15));
		
		Manager.getPacketManager().addPacketHandler(_packetHandler, PacketPlayOutSpawnEntity.class);
	}

	@Override
	public void ActivateCustom(Player player)
	{
		_spawning = true;
		Snowball arrow = player.launchProjectile(Snowball.class);
		_spawning = false;
		
		_arrows.add(arrow);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			for (Snowball arrow : new HashSet<Snowball>(_arrows))
			{
				if (!arrow.isValid())
				{
					_arrows.remove(arrow);
					continue;
				}
				
				UtilParticle.PlayParticleToAll(ParticleType.HEART, arrow.getLocation(), 0F, 0F, 0F, 0F, 1, ViewDist.LONGER);
			}
		}
	}
	
	@EventHandler
	public void onHit(ProjectileHitEvent event)
	{
		if (!(event.getEntity() instanceof Snowball))
			return;
		
		Snowball arrow = (Snowball) event.getEntity();
		
		if (!_arrows.contains(arrow))
			return;
		
		Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				if (_arrows.remove(arrow))
				{
					arrow.remove();
					UtilPlayer.message((Player) arrow.getShooter(), F.main(C.cRed + "Cupid", "You missed the shot!"));
				}
			}
		}, 1);
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Snowball))
			return;
		
		Snowball arrow = (Snowball) event.getDamager();
		
		if (!_arrows.contains(arrow))
			return;
		
		_arrows.remove(arrow);
		arrow.remove();
		
		Player shooter = (Player) arrow.getShooter();

		if (event.getEntity() instanceof Creeper && event.getEntity().getCustomName().equalsIgnoreCase(C.cGreenB + "Carl the Creeper"))
		{
			UtilPlayer.message(shooter, F.main(C.cRed + "Cupid", "Nobody will ever replace Carla."));
			return;
		}
		
		// Effects
		if (event.getEntity() instanceof Player)
		{
			if (event.getEntity() == shooter)
			{
				UtilPlayer.message(shooter, F.main(C.cRed + "Cupid", "We've all been there."));
				return;
			}
			
			UtilPlayer.message(shooter, F.main(C.cRed + "Cupid", "You hit " + F.name(UtilEnt.getName(event.getEntity())) + " with an arrow! I wonder how they're feeling..."));

			((Player) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 40, false, false));
			UtilPlayer.message(event.getEntity(), F.main(C.cRed + "Cupid", F.name(UtilEnt.getName(shooter)) + " hit you with an arrow! " + UtilMath.randomElement(_responses)));
		}

		UtilParticle.PlayParticleToAll(ParticleType.HEART, event.getEntity().getLocation(), .5F, .5F, .5F, 0F, 4, ViewDist.LONGER);
		
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.WOOL, 14), event.getEntity().getLocation(), .5F, .5F, .5F, 0.0f, 5, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.WOOL, 6), event.getEntity().getLocation(), .5F, .5F, .5F, 0.0f, 5, ViewDist.NORMAL);
	
		shooter.playSound(shooter.getLocation(), Sound.ORB_PICKUP, 0.5f, 0.5f);

//		Entity damaged = event.getEntity();	
//		AxisAlignedBB bb = ((CraftEntity) damaged).getHandle().getBoundingBox();
//
//		final Location loc = damaged.getLocation().clone();
//		final AxisAlignedBB bounding = new AxisAlignedBB(bb.a, bb.b, bb.c, bb.d, bb.e, bb.f); // Clone it
//
//		int i = 0;
//		for (double rise = bounding.b ; rise < bounding.e ; rise += .2)
//		{	
//			i++;
//
//			final float y = (float) (rise - bounding.b);
//			final float x = (float) (Math.sin(y) * 2);
//			final float z = (float) (Math.cos(y) * 2);
//
//			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					UtilParticle.PlayParticleToAll(ParticleType.HEART, loc.clone().add(x, y, z), 0F, 0F, 0F, 0F, 1, ViewDist.LONGER);
//				}
//			}, i);
//		}
	}
}
