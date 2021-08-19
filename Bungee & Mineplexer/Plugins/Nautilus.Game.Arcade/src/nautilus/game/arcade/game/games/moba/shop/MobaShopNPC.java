package nautilus.game.arcade.game.games.moba.shop;

import java.util.UUID;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.disguise.disguises.DisguiseLiving;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph.ShopMorphGadget;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph.ShopMorphType;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;

public class MobaShopNPC implements IPacketHandler
{

	private final MobaShop _shop;
	private final Player _player;
	private final ArmorStand _stand;
	private final ShopMorphGadget _gadget;

	public MobaShopNPC(MobaShop shop, Player player, Location location, ShopMorphGadget gadget)
	{
		_shop = shop;
		_player = player;
		_gadget = gadget;

		shop.getHost().getArcadeManager().getPacketHandler().addPacketHandler(this, PacketPlayInUseEntity.class, PacketPlayOutSpawnEntityLiving.class);

		_stand = location.getWorld().spawn(location, ArmorStand.class);

		_stand.setRemoveWhenFarAway(false);
		_stand.setCustomName(MobaShop.getNPCName());
		_stand.setCustomNameVisible(true);
		UtilEnt.vegetate(_stand);
		UtilEnt.silence(_stand, true);
		UtilEnt.ghost(_stand, true, false);

		disguise();
	}

	private void disguise()
	{
		DisguiseLiving disguise;

		if (_gadget == null)
		{
			DisguiseVillager villager = new DisguiseVillager(_stand);
			villager.setName(MobaShop.getNPCName());
			villager.setCustomNameVisible(true);
			disguise = villager;
		}
		else if (_gadget.getType().getSkinData() != null)
		{
			GameProfile profile = new GameProfile(UUID.randomUUID(), SkinData.getUnusedSkullName());
			profile.getProperties().clear();
			profile.getProperties().put("textures", _gadget.getType().getSkinData().getProperty());

			DisguisePlayer player = new DisguisePlayer(_stand, profile);
			player.getHologram()
					.setHologramTarget(HologramTarget.WHITELIST)
					.addPlayer(_player)
					.setText(
							MobaShop.getNPCName()
					);
			disguise = player;

			Location location = _stand.getLocation();
			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _shop.getHost().GetSpectatorLocation())));

			_shop.getHost().getArcadeManager().runSyncLater(() -> _stand.teleport(location), 20);
		}
		else
		{
			DisguiseInsentient insentient = (DisguiseInsentient) _gadget.getType().createInstance(_stand);

			if (insentient == null)
			{
				return;
			}

			insentient.setName(MobaShop.getNPCName());
			insentient.setCustomNameVisible(true);
			disguise = insentient;

			if (_gadget.getType() == ShopMorphType.WITHER_SKELETON)
			{
				((DisguiseSkeleton) insentient).SetSkeletonType(SkeletonType.WITHER);
			}
		}

		_shop.getHost().getArcadeManager().GetDisguise().disguise(disguise);
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		Player player = packetInfo.getPlayer();

		if (packetInfo.getPacket() instanceof PacketPlayOutSpawnEntityLiving && !player.equals(_player))
		{
			PacketPlayOutSpawnEntityLiving packet = (PacketPlayOutSpawnEntityLiving) packetInfo.getPacket();

			if (packet.a == _stand.getEntityId())
			{
				packetInfo.setCancelled(true);
			}
		}
		else if (packetInfo.getPacket() instanceof PacketPlayInUseEntity)
		{
			PacketPlayInUseEntity packet = (PacketPlayInUseEntity) packetInfo.getPacket();

			if (packet.a == _stand.getEntityId())
			{
				_shop.openShop(packetInfo.getPlayer());
			}
		}
	}

	public ArmorStand getStand()
	{
		return _stand;
	}
}
