package mineplex.core.gadget.gadgets.morph;

import static mineplex.core.common.util.UtilServer.runSync;

import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.DummyEntity;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.utils.UtilGameProfile;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutNewAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.World;

public class MorphDinnerbone extends MorphGadget implements IPacketHandler
{
	private static final String NAME = "Dinnerbone";

	private final CoreClientManager _coreClientManager = Managers.require(CoreClientManager.class);

	// Maps player to map of player and the id for the armorstand nametag
	private final Map<Integer, Map<UUID, Integer>> _armorStandIds = new HashMap<>();
	// Maps player to map of player and all ids that it owns
	private final Map<Integer, Map<UUID, List<Integer>>> _allIds = new HashMap<>();

	public MorphDinnerbone(GadgetManager manager)
	{
		super(manager, "Over Easy Morph", UtilText.splitLinesToArray(new String[]{
						C.cGray + "This morph lets you walk around on your head. But be careful, all the blood might go to your head!",
				}, LineFormat.LORE),
				-14, Material.EGG, (byte) 0);

		Managers.require(PacketHandler.class).addPacketHandler(this, PacketHandler.ListenerPriority.LOW, PacketPlayOutNamedEntitySpawn.class, PacketPlayOutEntityDestroy.class);

		setPPCYearMonth(YearMonth.of(2017, Month.JANUARY));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile profile = UtilGameProfile.getGameProfile(player);
		try
		{
			UtilGameProfile.changeName(profile, "Dinnerbone");
		}
		catch (ReflectiveOperationException e)
		{
			// Literally should never happen
			e.printStackTrace();
		}

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, profile);
		disguisePlayer.setSendSkinDataToSelf(false);
		disguisePlayer.setReplaceOriginalName(false, 10);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.isCancelled())
			return;

		if (packetInfo.getPacket() instanceof PacketPlayOutNamedEntitySpawn)
		{
			PacketPlayOutNamedEntitySpawn packet = (PacketPlayOutNamedEntitySpawn) packetInfo.getPacket();
			Entity entity = UtilEnt.getEntityById(packet.a);
			
			if (!(entity instanceof Player))
			{
				return;
			}
			
			Player owner = (Player) entity;
			if (Manager.getActive(owner, GadgetType.MORPH) == this)
			{
				summonForEntity(packetInfo.getPlayer(), owner);
			}
		}
		else if (packetInfo.getPacket() instanceof PacketPlayOutEntityDestroy)
		{
			PacketPlayOutEntityDestroy packet = (PacketPlayOutEntityDestroy) packetInfo.getPacket();
			for (int id : packet.a)
			{
				destroyForEntity(packetInfo.getPlayer(), id);
			}
		}
	}

	private void summonForEntity(Player receiver, Player player)
	{
		switch (UtilPlayer.getVersion(receiver))
		{
			case Version1_9:
				summonForEntity19(receiver, player);
				break;
			case Version1_8:
				summonForEntity18(receiver, player);
				break;
		}
	}

	private void summonForEntity19(Player receiver, Player player)
	{
		World world = ((CraftWorld) receiver.getWorld()).getHandle();

		DataWatcher armorStandWatcher = getArmorStandWatcher(player);
		armorStandWatcher.a(10, (byte) 0x10, EntityArmorStand.META_ARMOR_OPTION, (byte) 0x10); // Small

		DataWatcher squidWatcher = new DataWatcher(new DummyEntity(world));
		squidWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);

		PacketPlayOutSpawnEntityLiving spawnSquid = new PacketPlayOutSpawnEntityLiving();
		spawnSquid.a = UtilEnt.getNewEntityId();
		spawnSquid.b = EntityType.SQUID.getTypeId();
		spawnSquid.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnSquid.d = -150;
		spawnSquid.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnSquid.i = 0;
		spawnSquid.j = 0;
		spawnSquid.k = 0;
		spawnSquid.f = 0;
		spawnSquid.g = 0;
		spawnSquid.h = 0;
		spawnSquid.uuid = UUID.randomUUID();
		spawnSquid.l = squidWatcher;

		PacketPlayOutSpawnEntityLiving spawnArmorStand = new PacketPlayOutSpawnEntityLiving();
		spawnArmorStand.a = UtilEnt.getNewEntityId();
		spawnArmorStand.b = EntityType.ARMOR_STAND.getTypeId();
		spawnArmorStand.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnArmorStand.d = -150;
		spawnArmorStand.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnArmorStand.i = 0;
		spawnArmorStand.j = 0;
		spawnArmorStand.k = 0;
		spawnArmorStand.f = 0;
		spawnArmorStand.g = 0;
		spawnArmorStand.h = 0;
		spawnArmorStand.uuid = UUID.randomUUID();
		spawnArmorStand.l = armorStandWatcher;

		PacketPlayOutNewAttachEntity attachSquidtoPlayer = new PacketPlayOutNewAttachEntity(player.getEntityId(), new int[]{spawnSquid.a});
		PacketPlayOutNewAttachEntity attachArmorStandToSquid = new PacketPlayOutNewAttachEntity(spawnSquid.a, new int[]{spawnArmorStand.a});

		_armorStandIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), spawnArmorStand.a);
		_allIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), Arrays.asList(spawnSquid.a, spawnArmorStand.a));

		runSync(() ->
		{
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnSquid);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnArmorStand);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachSquidtoPlayer);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachArmorStandToSquid);
		});
	}

	private void summonForEntity18(Player receiver, Player player)
	{
		World world = ((CraftWorld) receiver.getWorld()).getHandle();

		DataWatcher armorStandWatcher = getArmorStandWatcher(player);
		armorStandWatcher.a(10, (byte) 0x10, EntityArmorStand.META_ARMOR_OPTION, (byte) 0x10); // Small

		DataWatcher squidWatcher = new DataWatcher(new DummyEntity(world));
		squidWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);

		PacketPlayOutSpawnEntityLiving spawnSquid = new PacketPlayOutSpawnEntityLiving();
		spawnSquid.a = UtilEnt.getNewEntityId();
		spawnSquid.b = EntityType.WOLF.getTypeId();
		spawnSquid.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnSquid.d = -150;
		spawnSquid.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnSquid.i = 0;
		spawnSquid.j = 0;
		spawnSquid.k = 0;
		spawnSquid.f = 0;
		spawnSquid.g = 0;
		spawnSquid.h = 0;
		spawnSquid.uuid = UUID.randomUUID();
		spawnSquid.l = squidWatcher;

		PacketPlayOutSpawnEntityLiving spawnArmorStand = new PacketPlayOutSpawnEntityLiving();
		spawnArmorStand.a = UtilEnt.getNewEntityId();
		spawnArmorStand.b = EntityType.ARMOR_STAND.getTypeId();
		spawnArmorStand.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnArmorStand.d = -150;
		spawnArmorStand.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnArmorStand.i = 0;
		spawnArmorStand.j = 0;
		spawnArmorStand.k = 0;
		spawnArmorStand.f = 0;
		spawnArmorStand.g = 0;
		spawnArmorStand.h = 0;
		spawnArmorStand.uuid = UUID.randomUUID();
		spawnArmorStand.l = armorStandWatcher;

		PacketPlayOutAttachEntity attachSquidtoPlayer = new PacketPlayOutAttachEntity();
		attachSquidtoPlayer.a = 0;
		attachSquidtoPlayer.b = spawnSquid.a;
		attachSquidtoPlayer.c = player.getEntityId();

		PacketPlayOutAttachEntity attachArmorStandToSquid = new PacketPlayOutAttachEntity();
		attachArmorStandToSquid.a = 0;
		attachArmorStandToSquid.b = spawnArmorStand.a;
		attachArmorStandToSquid.c = spawnSquid.a;

		_armorStandIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), spawnArmorStand.a);
		_allIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), Arrays.asList(spawnSquid.a, spawnArmorStand.a));

		runSync(() ->
		{
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnSquid);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnArmorStand);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachSquidtoPlayer);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachArmorStandToSquid);
		});
	}

	private void destroyForEntity(Player receiver, int id)
	{
		Map<UUID, Integer> innerMap = _armorStandIds.get(id);
		if (innerMap != null)
		{
			innerMap.remove(receiver.getUniqueId());

			if (innerMap.isEmpty())
			{
				_armorStandIds.remove(id);
			}
		}

		Map<UUID, List<Integer>> allIdsMap = _allIds.get(id);

		if (allIdsMap != null)
		{
			List<Integer> ids = allIdsMap.remove(receiver.getUniqueId());
			if (ids != null)
			{
				int[] idsArr = ids.stream().mapToInt(Integer::intValue).toArray();

				PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(idsArr);
				((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(destroy);
			}

			if (allIdsMap.isEmpty())
			{
				_allIds.remove(id);
			}
		}
	}

	private DataWatcher getArmorStandWatcher(Player ownerOfTrack)
	{
		PermissionGroup group = _coreClientManager.Get(ownerOfTrack).getRealOrDisguisedPrimaryGroup();
		String name = ownerOfTrack.getName();

		if (group != null)
		{
			if (!group.getDisplay(false, false, false, false).isEmpty())
			{
				name = group.getDisplay(true, true, true, false) + " " + ChatColor.RESET + name;
			}
		}

		World world = ((CraftWorld) ownerOfTrack.getWorld()).getHandle();

		DataWatcher armorStandWatcher = new DataWatcher(new DummyEntity(world));
		armorStandWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);
		armorStandWatcher.a(1, (short) 300, net.minecraft.server.v1_8_R3.Entity.META_AIR, 0);

		armorStandWatcher.a(2, name, net.minecraft.server.v1_8_R3.Entity.META_CUSTOMNAME, name);
		armorStandWatcher.a(3, (byte) 1, net.minecraft.server.v1_8_R3.Entity.META_CUSTOMNAME_VISIBLE, true);

		return armorStandWatcher;
	}
}