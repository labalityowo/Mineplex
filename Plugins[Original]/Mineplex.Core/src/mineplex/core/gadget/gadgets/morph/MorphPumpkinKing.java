package mineplex.core.gadget.gadgets.morph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

public class MorphPumpkinKing extends MorphGadget implements IPacketHandler
{
	private static final int CROWN_POINTS = 12;
	
	private List<JackOBomb> _bombs = new ArrayList<>();
	
	public MorphPumpkinKing(GadgetManager manager)
	{
		super(manager, "Pumpkin Kings Head", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Transforms the wearer into the dreaded Pumpkin King!",
						"",
						C.cBlue + "Earned by defeating the Pumpkin King",
						C.cBlue + "in the 2013 Halloween Horror Event",
						C.blankLine,
		                "#" + C.cWhite + "Sneak to use Jack O Bomb"
				}, LineFormat.LORE),
				-1,
				Material.PUMPKIN, (byte)0);

		manager.getPacketManager().addPacketHandler(this, true, PacketPlayInUseEntity.class);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
		disguise.showArmor();
		disguise.SetSkeletonType(SkeletonType.WITHER);
		UtilMorph.disguise(player, disguise, Manager);

		player.getInventory().setHelmet(new ItemStack(Material.JACK_O_LANTERN));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
		player.getInventory().setHelmet(null);
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;
		
		if (!event.isSneaking())
			return;
		
		if (Recharge.Instance.use(event.getPlayer(), "Jack O Bomb", 30000, true, true))
		{
			event.getPlayer().sendMessage(F.main("Recharge", "You used " + F.skill("Jack O Bomb") + "."));
			_bombs.add(new JackOBomb(event.getPlayer(), event.getPlayer().getLocation().add(event.getPlayer().getEyeLocation().getDirection().normalize())));
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			for (Player player : getActive())
			{
				if (Manager.isMoving(player))
				{
					continue;
				}
				for (int i = 0; i < 360; i += 360/CROWN_POINTS)
				{
					double angle = (i * Math.PI / 180);
					double x = 0.5 * Math.cos(angle);
					double z = 0.5 * Math.sin(angle);
					Location loc = player.getEyeLocation().add(x, 1.4, z);
					UtilParticle.PlayParticleToAll(ParticleType.FLAME, loc, null, 0, 1, ViewDist.NORMAL);
				}
			}
			
			_bombs.removeIf(JackOBomb::update);
		}
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayInUseEntity)
		{
			PacketPlayInUseEntity packetPlayInUseEntity = (PacketPlayInUseEntity) packetInfo.getPacket();
			if (packetPlayInUseEntity.action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK)
				for (JackOBomb bomb : _bombs)
					if (bomb._ent.getEntityId() == packetPlayInUseEntity.a)
						packetInfo.setCancelled(true);
		}
	}

	private class JackOBomb
	{
		private Player _user;
		private Item _ent;
		private DisguiseBlock _disguise;
		
		private long _detonation;
		
		private boolean _done;
		
		public JackOBomb(Player user, Location spawnLocation)
		{
			_user = user;
			_ent = spawnLocation.getWorld().dropItem(spawnLocation, new ItemBuilder(Material.APPLE).setTitle(new Random().nextDouble() + "").build());
			_ent.setPickupDelay(Integer.MAX_VALUE);
			_disguise = new DisguiseBlock(_ent, Material.JACK_O_LANTERN.getId(), 0);
			Manager.getDisguiseManager().disguise(_disguise);
			_detonation = System.currentTimeMillis() + 7000;
			_done = false;
		}
		
		public boolean update()
		{
			if (_done)
			{
				return true;
			}
			if (!_user.isOnline())
			{
				Manager.getDisguiseManager().undisguise(_disguise);
				_ent.remove();
				_done = true;
			}
			if (System.currentTimeMillis() >= _detonation)
			{
				UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, _ent.getLocation(), null, 0, 1, ViewDist.NORMAL);
				Map<Player, Double> players = UtilPlayer.getInRadius(_ent.getLocation(), 8);
				for (Player player : players.keySet())
				{
					if (!Manager.selectEntity(MorphPumpkinKing.this, player))
					{
						continue;
					}

					double mult = players.get(player);
							
					//Knockback
					UtilAction.velocity(player, UtilAlg.getTrajectory(_ent.getLocation(), player.getLocation()), 1 * mult, false, 0, 0.5 + 0.5 * mult, 10, true);
				}
				Manager.getDisguiseManager().undisguise(_disguise);
				_ent.remove();
				_done = true;
			}
			
			return false;
		}
	}
}