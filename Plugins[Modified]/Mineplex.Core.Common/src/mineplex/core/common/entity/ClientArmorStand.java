package mineplex.core.common.entity;

import mineplex.core.common.util.UtilPlayer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class ClientArmorStand implements ArmorStand
{

	private static final int HAND = 0;
	private static final int HELMET = 4;
	private static final int CHESTPLATE = 3;
	private static final int LEGGINGS = 2;
	private static final int BOOTS = 1;

	public static ClientArmorStand spawn(Location location)
	{
		EntityArmorStand entityArmorStand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle());
		entityArmorStand.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		ClientArmorStand clientArmorStand = new ClientArmorStand(entityArmorStand);

		for (Player other : clientArmorStand.getObservers())
		{
			UtilPlayer.sendPacket(other, new PacketPlayOutSpawnEntityLiving(entityArmorStand));
		}

		return clientArmorStand;
	}

	public static ClientArmorStand spawn(Location loc, Player... players)
	{
		EntityArmorStand entityArmorStand = new EntityArmorStand(((CraftWorld) loc.getWorld()).getHandle());
		entityArmorStand.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		ClientArmorStand clientArmorStand = new ClientArmorStand(entityArmorStand, players);
		Packet<?> packet = new PacketPlayOutSpawnEntityLiving(entityArmorStand);

		for (Player observer : players)
		{
			UtilPlayer.sendPacket(observer, packet);
		}

		return clientArmorStand;
	}

	private final EntityArmorStand _armorStand;
	private ItemStack _itemInHand, _helmet, _chestplate, _leggings, _boots;
	private boolean _visible;
	private final DataWatcher _dataWatcher;

	private Player[] _observers;

	private ClientArmorStand(EntityArmorStand armorStand, Player... players)
	{
		_armorStand = armorStand;
		_dataWatcher = armorStand.getDataWatcher();
		_observers = players;
	}

	public EntityArmorStand getHandle()
	{
		return _armorStand;
	}

	@Override
	public ItemStack getItemInHand()
	{
		return _itemInHand;
	}

	@Override
	public void setItemInHand(ItemStack itemStack)
	{
		_itemInHand = itemStack;
		sendPacket(new PacketPlayOutEntityEquipment(getEntityId(), HAND, CraftItemStack.asNMSCopy(itemStack)));
	}

	@Override
	public ItemStack getHelmet()
	{
		return _helmet;
	}

	@Override
	public void setHelmet(ItemStack itemStack)
	{
		_helmet = itemStack;
		sendPacket(new PacketPlayOutEntityEquipment(getEntityId(), HELMET, CraftItemStack.asNMSCopy(itemStack)));
	}

	@Override
	public ItemStack getChestplate()
	{
		return _chestplate;
	}

	@Override
	public void setChestplate(ItemStack itemStack)
	{
		_chestplate = itemStack;
		sendPacket(new PacketPlayOutEntityEquipment(getEntityId(), CHESTPLATE, CraftItemStack.asNMSCopy(itemStack)));
	}

	@Override
	public ItemStack getLeggings()
	{
		return _leggings;
	}

	@Override
	public void setLeggings(ItemStack itemStack)
	{
		_leggings = itemStack;
		sendPacket(new PacketPlayOutEntityEquipment(getEntityId(), LEGGINGS, CraftItemStack.asNMSCopy(itemStack)));
	}

	@Override
	public ItemStack getBoots()
	{
		return _boots;
	}

	@Override
	public void setBoots(ItemStack itemStack)
	{
		_boots = itemStack;
		sendPacket(new PacketPlayOutEntityEquipment(getEntityId(), BOOTS, CraftItemStack.asNMSCopy(itemStack)));
	}

	@Override
	public Location getEyeLocation()
	{
		return getLocation().add(0, getEyeHeight(), 0);
	}

	@Override
	public double getEyeHeight()
	{
		return 1.62;
	}

	@Override
	public double getEyeHeight(boolean sneaking)
	{
		return getEyeHeight();
	}

	@Override
	public String getCustomName()
	{
		return _armorStand.getCustomName();
	}

	@Override
	public Location getLocation()
	{
		return new Location(getWorld(), _armorStand.locX, _armorStand.locY, _armorStand.locZ, _armorStand.yaw, _armorStand.pitch);
	}

	@Override
	public boolean isVisible()
	{
		return _visible;
	}

	@Override
	public void setVisible(boolean visible)
	{
		_visible = visible;
		_armorStand.setInvisible(!visible);
		sendMetaPacket();
	}

	private void sendMetaPacket()
	{
		sendPacket(new PacketPlayOutEntityMetadata(getEntityId(), _dataWatcher, true), _observers);
	}

	@Override
	public Entity getPassenger()
	{
		return null;
	}

	@Override
	public Entity getVehicle()
	{
		return null;
	}

	@Override
	public org.bukkit.World getWorld()
	{
		return _armorStand.getWorld().getWorld();
	}

	@Override
	public void remove()
	{
		sendPacket(new PacketPlayOutEntityDestroy(new int[]{_armorStand.getId()}));
	}

	public void remove(Player... player)
	{
		sendPacket(new PacketPlayOutEntityDestroy(new int[]{_armorStand.getId()}), player);
	}

	@Override
	public void setCustomName(String arg0)
	{
		_armorStand.setCustomName(arg0);
		sendMetaPacket();
	}

	@Override
	public boolean setPassenger(Entity arg0)
	{
		return false;
	}

	public boolean teleport(Location location, Player player)
	{
		double pX = _armorStand.locX;
		double pY = _armorStand.locY;
		double pZ = _armorStand.locZ;
		float pYaw = _armorStand.yaw;
		float pPitch = _armorStand.pitch;

		_armorStand.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		UtilPlayer.sendPacket(player, new PacketPlayOutEntityTeleport(_armorStand));
		_armorStand.setPositionRotation(pX, pY, pZ, pYaw, pPitch);
		return false;
	}

	@Override
	public boolean teleport(Location loc)
	{
		_armorStand.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		sendPacket(new PacketPlayOutEntityTeleport(_armorStand));
		return false;
	}

	@Override
	public EulerAngle getBodyPose()
	{
		return null;
	}

	@Override
	public EulerAngle getHeadPose()
	{
		return fromNMS(_armorStand.headPose);
	}

	@Override
	public void setHeadPose(EulerAngle pose)
	{
		_armorStand.setHeadPose(toNMS(pose));
		sendMetaPacket();
	}

	@Override
	public EulerAngle getLeftArmPose()
	{
		return null;
	}

	@Override
	public EulerAngle getLeftLegPose()
	{
		return null;
	}

	@Override
	public EulerAngle getRightArmPose()
	{
		return null;
	}

	@Override
	public EulerAngle getRightLegPose()
	{
		return null;
	}

	@Override
	public boolean hasArms()
	{
		return false;
	}

	@Override
	public boolean hasBasePlate()
	{
		return false;
	}

	@Override
	public boolean hasGravity()
	{
		return false;
	}

	@Override
	public boolean isMarker()
	{
		return false;
	}

	@Override
	public boolean isSmall()
	{
		return false;
	}

	@Override
	public void setArms(boolean arg0)
	{
		_armorStand.setArms(arg0);
		sendMetaPacket();
	}

	@Override
	public void setBasePlate(boolean arg0)
	{
		_armorStand.setBasePlate(arg0);
		sendMetaPacket();
	}

	@Override
	public void setBodyPose(EulerAngle arg0)
	{
	}

	@Override
	public void setLeftArmPose(EulerAngle arg0)
	{
	}

	@Override
	public void setLeftLegPose(EulerAngle arg0)
	{
	}

	@Override
	public void setRightArmPose(EulerAngle arg0)
	{
	}

	@Override
	public void setRightLegPose(EulerAngle arg0)
	{
	}

	@Override
	public void setSmall(boolean arg0)
	{
		_armorStand.setSmall(arg0);
		sendMetaPacket();
	}

	@Override
	public int getEntityId()
	{
		return _armorStand.getId();
	}

	public Player[] getObservers()
	{
		return _observers;
	}

	public void sendPacket(Packet<?> packet)
	{
		sendPacket(packet, getObservers());
	}

	public void sendPacket(Packet<?> packet, Collection<Player> observers)
	{
		for (Player player : observers)
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	public void sendPacket(Packet<?> packet, Player... observers)
	{
		for (Player player : observers)
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	// Not needed

	@Override
	public void setGravity(boolean b)
	{

	}

	@Override
	public void setMarker(boolean b)
	{

	}

	@Override
	public List<Block> getLineOfSight(HashSet<Byte> hashSet, int i)
	{
		return null;
	}

	@Override
	public List<Block> getLineOfSight(Set<Material> set, int i)
	{
		return null;
	}

	@Override
	public Block getTargetBlock(HashSet<Byte> hashSet, int i)
	{
		return null;
	}

	@Override
	public Block getTargetBlock(Set<Material> set, int i)
	{
		return null;
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hashSet, int i)
	{
		return null;
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i)
	{
		return null;
	}

	@Override
	public Egg throwEgg()
	{
		return null;
	}

	@Override
	public Snowball throwSnowball()
	{
		return null;
	}

	@Override
	public Arrow shootArrow()
	{
		return null;
	}

	@Override
	public int getRemainingAir()
	{
		return 0;
	}

	@Override
	public void setRemainingAir(int i)
	{

	}

	@Override
	public int getMaximumAir()
	{
		return 0;
	}

	@Override
	public void setMaximumAir(int i)
	{

	}

	@Override
	public int getMaximumNoDamageTicks()
	{
		return 0;
	}

	@Override
	public void setMaximumNoDamageTicks(int i)
	{

	}

	@Override
	public double getLastDamage()
	{
		return 0;
	}

	@Override
	public void setLastDamage(double v)
	{

	}

	@Override
	public int getNoDamageTicks()
	{
		return 0;
	}

	@Override
	public void setNoDamageTicks(int i)
	{

	}

	@Override
	public Player getKiller()
	{
		return null;
	}

	@Override
	public boolean addPotionEffect(PotionEffect potionEffect)
	{
		return false;
	}

	@Override
	public boolean addPotionEffect(PotionEffect potionEffect, boolean b)
	{
		return false;
	}

	@Override
	public boolean addPotionEffects(Collection<PotionEffect> collection)
	{
		return false;
	}

	@Override
	public boolean hasPotionEffect(PotionEffectType potionEffectType)
	{
		return false;
	}

	@Override
	public void removePotionEffect(PotionEffectType potionEffectType)
	{

	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects()
	{
		return null;
	}

	@Override
	public boolean hasLineOfSight(org.bukkit.entity.Entity entity)
	{
		return false;
	}

	@Override
	public boolean getRemoveWhenFarAway()
	{
		return false;
	}

	@Override
	public void setRemoveWhenFarAway(boolean b)
	{

	}

	@Override
	public EntityEquipment getEquipment()
	{
		return null;
	}

	@Override
	public void setCanPickupItems(boolean b)
	{

	}

	@Override
	public boolean getCanPickupItems()
	{
		return false;
	}

	@Override
	public boolean isLeashed()
	{
		return false;
	}

	@Override
	public org.bukkit.entity.Entity getLeashHolder() throws IllegalStateException
	{
		return null;
	}

	@Override
	public boolean setLeashHolder(org.bukkit.entity.Entity entity)
	{
		return false;
	}

	@Override
	public boolean shouldBreakLeash()
	{
		return false;
	}

	@Override
	public void setShouldBreakLeash(boolean b)
	{

	}

	@Override
	public boolean shouldPullWhileLeashed()
	{
		return false;
	}

	@Override
	public void setPullWhileLeashed(boolean b)
	{

	}

	@Override
	public boolean isVegetated()
	{
		return false;
	}

	@Override
	public void setVegetated(boolean b)
	{

	}

	@Override
	public boolean isGhost()
	{
		return false;
	}

	@Override
	public void setGhost(boolean b)
	{

	}

	@Override
	public void damage(double v)
	{

	}

	@Override
	public void damage(double v, org.bukkit.entity.Entity entity)
	{

	}

	@Override
	public double getHealth()
	{
		return 0;
	}

	@Override
	public void setHealth(double v)
	{

	}

	@Override
	public double getMaxHealth()
	{
		return 0;
	}

	@Override
	public void setMaxHealth(double v)
	{

	}

	@Override
	public void resetMaxHealth()
	{

	}

	@Override
	public Location getLocation(Location location)
	{
		return null;
	}

	@Override
	public void setVelocity(Vector vector)
	{

	}

	@Override
	public Vector getVelocity()
	{
		return null;
	}

	@Override
	public boolean isOnGround()
	{
		return false;
	}

	@Override
	public boolean teleport(Location location, TeleportCause teleportCause)
	{
		return false;
	}

	@Override
	public boolean teleport(org.bukkit.entity.Entity entity)
	{
		return false;
	}

	@Override
	public boolean teleport(org.bukkit.entity.Entity entity, TeleportCause teleportCause)
	{
		return false;
	}

	@Override
	public List<org.bukkit.entity.Entity> getNearbyEntities(double v, double v1, double v2)
	{
		return null;
	}

	@Override
	public int getFireTicks()
	{
		return 0;
	}

	@Override
	public int getMaxFireTicks()
	{
		return 0;
	}

	@Override
	public void setFireTicks(int i)
	{

	}

	@Override
	public boolean isDead()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return false;
	}

	@Override
	public Server getServer()
	{
		return null;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public boolean eject()
	{
		return false;
	}

	@Override
	public float getFallDistance()
	{
		return 0;
	}

	@Override
	public void setFallDistance(float v)
	{

	}

	@Override
	public void setLastDamageCause(EntityDamageEvent entityDamageEvent)
	{

	}

	@Override
	public EntityDamageEvent getLastDamageCause()
	{
		return null;
	}

	@Override
	public UUID getUniqueId()
	{
		return null;
	}

	@Override
	public int getTicksLived()
	{
		return 0;
	}

	@Override
	public void setTicksLived(int i)
	{

	}

	@Override
	public void playEffect(EntityEffect entityEffect)
	{

	}

	@Override
	public EntityType getType()
	{
		return null;
	}

	@Override
	public boolean isInsideVehicle()
	{
		return false;
	}

	@Override
	public boolean leaveVehicle()
	{
		return false;
	}

	@Override
	public void setCustomNameVisible(boolean b)
	{
		_armorStand.setCustomNameVisible(b);
	}

	@Override
	public boolean isCustomNameVisible()
	{
		return false;
	}

	@Override
	public Spigot spigot()
	{
		return null;
	}

	@Override
	public void sendMessage(String s)
	{

	}

	@Override
	public void sendMessage(String[] strings)
	{

	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public void setMetadata(String s, MetadataValue metadataValue)
	{

	}

	@Override
	public List<MetadataValue> getMetadata(String s)
	{
		return null;
	}

	@Override
	public boolean hasMetadata(String s)
	{
		return false;
	}

	@Override
	public void removeMetadata(String s, Plugin plugin)
	{

	}

	@Override
	public boolean isPermissionSet(String s)
	{
		return false;
	}

	@Override
	public boolean isPermissionSet(Permission permission)
	{
		return false;
	}

	@Override
	public boolean hasPermission(String s)
	{
		return false;
	}

	@Override
	public boolean hasPermission(Permission permission)
	{
		return false;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b)
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin)
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i)
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int i)
	{
		return null;
	}

	@Override
	public void removeAttachment(PermissionAttachment permissionAttachment)
	{

	}

	@Override
	public void recalculatePermissions()
	{

	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return null;
	}

	@Override
	public boolean isOp()
	{
		return false;
	}

	@Override
	public void setOp(boolean b)
	{

	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> aClass)
	{
		return null;
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> aClass, Vector vector)
	{
		return null;
	}

	private static EulerAngle fromNMS(Vector3f old)
	{
		return new EulerAngle(Math.toRadians(old.getX()), Math.toRadians(old.getY()), Math.toRadians(old.getZ()));
	}

	private static Vector3f toNMS(EulerAngle old)
	{
		return new Vector3f((float) Math.toDegrees(old.getX()), (float) Math.toDegrees(old.getY()), (float) Math.toDegrees(old.getZ()));
	}

}