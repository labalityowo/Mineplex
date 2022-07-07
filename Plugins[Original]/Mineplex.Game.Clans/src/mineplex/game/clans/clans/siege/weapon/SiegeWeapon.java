package mineplex.game.clans.clans.siege.weapon;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.spigotmc.event.entity.EntityDismountEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautArrayList;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilCollections;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.hologram.Hologram;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClanDeleteEvent;
import mineplex.game.clans.clans.siege.SiegeManager;
import mineplex.game.clans.clans.siege.events.LoadSiegeWeaponEvent;
import mineplex.game.clans.clans.siege.events.MountSiegeWeaponEvent;
import mineplex.game.clans.clans.siege.events.SiegeWeaponExplodeEvent;
import mineplex.game.clans.clans.siege.repository.tokens.SiegeWeaponToken;
import mineplex.game.clans.clans.siege.weapon.projectile.WeaponProjectile;
import mineplex.game.clans.clans.siege.weapon.util.AccessRule;
import mineplex.game.clans.clans.siege.weapon.util.AccessType;
import mineplex.game.clans.clans.siege.weapon.util.BarrierCollisionBox;
import mineplex.game.clans.clans.siege.weapon.util.WeaponStateInfo;
import mineplex.game.clans.items.PlayerGear;
import mineplex.game.clans.items.legendaries.AlligatorsTooth;
import mineplex.game.clans.items.legendaries.DemonicScythe;
import mineplex.game.clans.items.legendaries.GiantsBroadsword;
import mineplex.game.clans.items.legendaries.HyperAxe;
import mineplex.game.clans.items.legendaries.KnightLance;
import mineplex.game.clans.items.legendaries.LegendaryItem;
import mineplex.game.clans.items.legendaries.MagneticMaul;
import mineplex.game.clans.items.legendaries.WindBlade;

public abstract class SiegeWeapon implements Listener
{
	// Constants
	protected final int _maxHealth;
	protected final byte _weaponTypeIdentifier;
	
	protected SiegeWeaponToken _loadedToken;
	
	// Managers
	protected ClansManager _clans;
	protected SiegeManager _siegeManager;
	
	// Info
	protected final int _uniqueId;
	
	protected final ClanInfo _ownerClan;
	protected final Location _location;
	protected final String _name;
	
	protected int _health;
	protected String _currentState;
	protected boolean _alive = true;
	protected boolean _invincible = false;

	// Mechanics
	protected final NautArrayList<Entity> _comprisedOf;
	
	protected Player _rider;
	protected Inventory _inventory;
	protected AccessRule _inventoryAccess;
	protected BarrierCollisionBox _collisionBox;
	
	protected AccessRule _fireAccess;
	
	protected int _boundingBoxSize;
	
	protected double _yaw;
	
	protected long _lastRight = -1;
	protected long _lastLeft = -1;
	protected long _lastFired = -1;
	
	// Display
	protected Hologram _infoHologram;
	
	// Utility
	protected final NautHashMap<String, WeaponStateInfo> _registeredStates;
	protected final NautHashMap<String, Entity> _entityMapping;
	
	protected boolean _syncWithDb;
	
	// Customizability
	private double[] _forcedVelocity;
	
	private float[] _lockedYaw;
	
	protected boolean _isRideable;
	protected boolean _invertRotation;
	protected AccessRule _mountAccess;
	
	protected float _rotSpeed = 20.f;
	
	protected int _ammunitionSlot;
	protected Material _ammunitionType;
	protected int _maxAmmunition;
	
	protected NautArrayList<Integer> _firepowerSlots = new NautArrayList<>();
	protected Material _firepowerType;
	protected int _maxFirepowerPerSlot;
	
	protected int _baseDamage;
	
	protected WeaponProjectile _projectile;
	
	public SiegeWeapon(int maxHealth, String name, SiegeWeaponToken token, ClansManager clansManager, SiegeManager siegeManager)
	{
		_weaponTypeIdentifier = token.WeaponType;
		
		_loadedToken = token;
		
		_uniqueId = token.UniqueId;
		
		_siegeManager = siegeManager;
		_location = token.Location;
		_name = name;
		_health = _maxHealth = maxHealth;
		_ownerClan = token.OwnerClan;
		
		_comprisedOf = new NautArrayList<>();
		_registeredStates = new NautHashMap<>();
		_entityMapping = new NautHashMap<>();
		
		_infoHologram = new Hologram(ClansManager.getInstance().getHologramManager(), _location.clone().add(.5, 3, .5), _name + " Health", getDisplayHealth());
		_infoHologram.start();
		
		_infoHologram.setInteraction((player, type) ->
		{
			if (type.equals(ClickType.LEFT))
			{
				handleLeftClick(player);
			}
			else if (type.equals(ClickType.RIGHT))
			{
				handleRightClick(player);
			}
		});
		
		UtilServer.RegisterEvents(this);
		
		_clans = clansManager;
		
		_yaw = token.Yaw;
		_lastFired = token.LastFired;
		
		_health = token.Health;
		
		FindEntities();
	}
	
	public SiegeWeapon(int typeId, Location location, int maxHealth, String name, ClanInfo owner, ClansManager clansManager, SiegeManager siegeManager)
	{
		_uniqueId = siegeManager.randomId();
		_weaponTypeIdentifier = (byte) typeId;
		
		_siegeManager = siegeManager;
		_location = location;
		_name = name;
		_health = _maxHealth = maxHealth;
		_ownerClan = owner;
		
		_comprisedOf = new NautArrayList<>();
		_registeredStates = new NautHashMap<>();
		_entityMapping = new NautHashMap<>();
		
		_infoHologram = new Hologram(ClansManager.getInstance().getHologramManager(), _location.clone().add(.5, 3, .5), _name + " Health", getDisplayHealth());
		_infoHologram.start();
		
		UtilServer.getPluginManager().registerEvents(this, clansManager.getPlugin());
		
		_clans = clansManager;
	}
	
	protected void insert()
	{
		if (_syncWithDb)
			_siegeManager.getRepository().insertWeapon(toToken());
	}
	
	protected int calculateDamage(Player player)
	{
		ItemStack stack = player.getItemInHand();
		PlayerGear gear = _clans.getGearManager().getPlayerGear(player);
		
		if (stack == null)
			return 1;
		
		if (gear.getWeapon() != null && gear.getWeapon() instanceof LegendaryItem)
		{
			if (gear.getWeapon() instanceof AlligatorsTooth)
				return 8;
			else if (gear.getWeapon() instanceof GiantsBroadsword)
				return 10;
			else if (gear.getWeapon() instanceof HyperAxe)
				return 6;
			else if (gear.getWeapon() instanceof MagneticMaul)
				return 8;
			else if (gear.getWeapon() instanceof WindBlade)
				return 7;
			else if (gear.getWeapon() instanceof DemonicScythe)
				return 8;
			else if (gear.getWeapon() instanceof KnightLance)
				return 8;
		}
		
		return (int) UtilItem.getAttackDamage(stack.getType());
	}
	
	protected void enableInventory(Inventory inventory, AccessRule accessRule)
	{
		_inventory = inventory;
		_inventoryAccess = accessRule;
	}
	
	protected void setBoundingBox(int size)
	{
		Validate.isTrue(size > 0, "Size must be a positive number.");
		Validate.isTrue(UtilMath.isOdd(size), "Size must be an odd number.");
		
		_boundingBoxSize = size;
		
		_collisionBox = size == 1 ? BarrierCollisionBox.single(_location.clone()) : BarrierCollisionBox.all(_location.clone().subtract((size - 1) / 2, 0, (size - 1) / 2), _location.clone().add(((size - 1) / 2) + .2, size - 1, ((size - 1) / 2) + .2));
		_collisionBox.Construct();
		_collisionBox.registerRight((block, player) -> handleRightClick(player));
		_collisionBox.registerLeft((block, player) -> handleLeftClick(player));
	}
	
	protected void setBoundingBox(int size, int y)
	{
		Validate.isTrue(size > 0, "Size must be a positive number.");
		Validate.isTrue(UtilMath.isOdd(size), "Size must be an odd number.");
		
		_boundingBoxSize = size;
		
		_collisionBox = size == 1 ? BarrierCollisionBox.single(_location.clone()) : BarrierCollisionBox.all(_location.clone().subtract((size - 1) / 2, 0, (size - 1) / 2), _location.clone().add(((size - 1) / 2) + .2, y, ((size - 1) / 2) + .2));
		_collisionBox.Construct();
		_collisionBox.registerRight((block, player) -> handleRightClick(player));
		_collisionBox.registerLeft((block, player) -> handleLeftClick(player));
	}
	
	private void update()
	{
		if (getEntity("WEAPON") == null || getEntity("PLAYERMOUNT") == null)
		{
			kill();
			return;
		}
		
		if (_inventory != null)
			checkInventory();
		
		_rider = (Player) getEntity("PLAYERMOUNT").getPassenger();
		
		if (!GetNextState().equals(_currentState))
			setState(GetNextState());
		
		if (_projectile != null && _projectile.hasDied())
			_projectile = null;
		
		ArmorStand armorStand = (ArmorStand) getEntity("WEAPON");
		double standYaw = _yaw % 360;
		
		if (getRider() != null)
		{
			double riderYaw = (getRider().getLocation().getYaw() + (_invertRotation ? 180 : 0)) % 360;

			double dif = riderYaw - standYaw;
			if (dif > 180) dif -= 360;
			if (dif < -180) dif += 360;

			double yaw = (float) ((float)standYaw + Math.min(dif / _rotSpeed, 4f));
			
			if (_lockedYaw != null)
			{
				float min = _lockedYaw[0];
				float max = _lockedYaw[1];
				
				if (yaw < min)
				{
					yaw = min;
				}
				
				if (yaw > max)
				{
					yaw = max;
				}
			}
			
			armorStand.setHeadPose(new EulerAngle(0, Math.toRadians(CustomRotate(_yaw = yaw)), 0));
		}
	}
	
	protected boolean canBeFired()
	{
		return getPowerLevel() > 0 && getAmmunition() > 0 && _projectile == null;
	}
	
	public void SetForcedVelocity(double vertical, double horizontal)
	{
		_forcedVelocity = new double[] { vertical, horizontal };
	}
	
	public void LockYaw(float minYaw, float maxYaw)
	{
		_lockedYaw = new float[] { minYaw, maxYaw };
	}
	
	private void fire(Player player)
	{
		_lastFired = System.currentTimeMillis();
		
		double[] vel = GetProjectileVelocity();
		
		if (_forcedVelocity != null)
		{
			vel = _forcedVelocity;
		}
		
		_inventory.clear();
		
		_projectile = CustomFire(((ArmorStand) getEntity("WEAPON")).getHeadPose().getY(), vel[0], vel[1]);
	}
	
	protected void setFireRule(AccessRule rule)
	{
		_fireAccess = rule;
	}
	
	protected void setFirepowerType(Material type)
	{
		_firepowerType = type;
	}
	
	protected void setAmmunitionType(Material type)
	{
		_ammunitionType = type;
	}
	
	protected void setFirepowerSlots(Integer... slots)
	{
		_firepowerSlots = UtilCollections.newNautList(slots);
	}
	
	protected void setMaximumFirepowerPerSlot(int maxFirepowerPerSlot)
	{
		_maxFirepowerPerSlot = maxFirepowerPerSlot;
	}
	
	protected void setAmmunitionSlot(int ammunitionSlot)
	{
		_ammunitionSlot = ammunitionSlot;
	}
	
	protected void setMaximumAmmunitionPerSlot(int maxAmmunition)
	{
		_maxAmmunition = maxAmmunition;
	}
	
	protected boolean isRiding(Player player)
	{
		return player.equals(getRider());
	}
	
	protected void setRideable(AccessRule accessRule)
	{
		_isRideable = true;
		_mountAccess = accessRule;
	}
	
	public void kill()
	{
		System.out.println("Killing: " + this.getClass().getSimpleName() + " [" + _uniqueId + "]");
		
		_siegeManager.runSync(() ->
		{
			CustomCleanup();
			
			_comprisedOf.forEach(Entity::remove);
			
			_entityMapping.clear();
			_comprisedOf.clear();
			_infoHologram.stop();
			
			if (_collisionBox != null) _collisionBox.Destruct();
			
			_siegeManager.dead(this);
			
			_alive = false;
		});
		
		HandlerList.unregisterAll(this);
	}
	
	private void handleMount(Player player)
	{
		UtilServer.CallEvent(new MountSiegeWeaponEvent(player, this));
		
		if (!CustomMount(player))
		{
			getEntity("PLAYERMOUNT").setPassenger(player);
		}
		
		CustomOnMount(player);
	}
	
	private void handleInventoryOpen(Player player)
	{
		player.openInventory(_inventory);
	}
	
	private void handleRightClick(Player player)
	{
		if (_lastRight == -1)
		{
			_lastRight = System.currentTimeMillis();
		}
		else
		{
			if (System.currentTimeMillis() - _lastRight <= 40)
			{
				return;
			}
		}
		
		_lastRight = System.currentTimeMillis();
		
		CustomRightClick(player);
		
		if (_isRideable && _mountAccess.allow(AccessType.RCLICK_BB, player))
		{
			handleMount(player);
			return;
		}
		
		if (_inventory != null && _inventoryAccess.allow(AccessType.RCLICK_BB, player))
		{
			handleInventoryOpen(player);
			return;
		}
		
		if (_fireAccess.allow(AccessType.RCLICK_BB, player))
		{
			fire(player);
		}
	}
	
	protected boolean CustomInventoryValid(int slot, ItemStack item)
	{
		return false;
	}
	
	private void dismount(Player player)
	{
		_clans.runSync(() -> player.teleport(player.getLocation().add(0, 1, 0)));
	}
	
	protected void handleLeftClick(Player player)
	{
		if (player.getGameMode() == GameMode.CREATIVE && player.isSneaking())
		{
			removeHealth(getHealth());
			return;
		}
		
		if (_lastLeft == -1)
		{
			_lastLeft = System.currentTimeMillis();
		}
		else
		{
			if (System.currentTimeMillis() - _lastLeft <= 40)
			{
				return;
			}
		}
		
		_lastLeft = System.currentTimeMillis();
		
		CustomLeftClick(player);
		
		if (_isRideable && _mountAccess.allow(AccessType.LCLICK_BB, player))
		{
			handleMount(player);
			return;
		}
		
		if (_inventory != null && _inventoryAccess.allow(AccessType.LCLICK_BB, player))
		{
			handleInventoryOpen(player);
			return;
		}
		
		if (_fireAccess.allow(AccessType.LCLICK_BB, player))
		{
			fire(player);
			return;
		}
		
		if (!player.equals(_rider) && Recharge.Instance.use(player, "Damage Cannon", 200, false, false))
		{
			int health = calculateDamage(player);
			
			removeHealth(health);
			
			new Hologram(
					_siegeManager.getClansManager().getHologramManager(),
					_location.clone().add(UtilMath.random(-1, 1),1.4, UtilMath.random(-1, 1)),
					false,
					3500l,
				C.cRed + "-" + health)
			.start();
		}
	}
	
	
	protected abstract double[] GetProjectileVelocity();
	protected abstract String GetNextState();
	protected abstract void FindEntities();
	protected abstract WeaponProjectile CustomFire(double yawRot, double verticalVel, double horizontalVel);
	protected void CustomTick() { return; }
	protected void CustomOnMount(Player player) { return; }
	protected void CustomLeftClick(Player player) { return; }
	protected void CustomRightClick(Player player) { return; }
	protected void CustomCleanup() { return; }
	protected void CustomUpdateState(String state) { return; }
	
	protected double CustomRotate(double yaw)
	{
		return yaw;
	}
	
	protected boolean CustomDismount(Player player, Entity entity) { return false; }
	protected boolean CustomMount(Player player) { return false; }
	
	protected final void addEntity(Entity entity, String uniqueName)
	{
		entity.setCustomName(Integer.toString(_uniqueId));
		entity.setCustomNameVisible(false);
		entity.setMetadata("Creature.DoNotDrop", new FixedMetadataValue(_clans.getPlugin(), true));
		UtilEnt.addFlag(entity, "LegendaryAbility.IgnoreMe");
		
		_comprisedOf.add(entity);
		
		_entityMapping.put(uniqueName, entity);
	}
	
	protected final void removeEntity(String uniqueName)
	{
		Entity entity = _entityMapping.get(uniqueName);
		
		entity.removeMetadata("Creature.DoNotDrop", _clans.getPlugin());
		UtilEnt.removeFlag(entity, "LegendaryAbility.IgnoreMe");
		
		_entityMapping.remove(uniqueName);
		_comprisedOf.remove(entity);
		
		entity.remove();
	}
	
	protected final Entity getEntity(String uniqueName)
	{
		return _entityMapping.get(uniqueName);
	}
	
	public final int getHealth()
	{
		return _health;
	}
	
	public final String getDisplayHealth()
	{
		return UtilText.getProgress(null, ((double) _health) / ((double) _maxHealth), null, false, 12);
	}
	
	public final void setHealth(int health)
	{
		_health = UtilMath.clamp(health, 0, _maxHealth);

		_infoHologram.setText(_name + " Health", getDisplayHealth());
		
		if (_health == 0 && !_invincible)
			kill();
	}
	
	public final void removeHealth(int health)
	{
		if (_invincible) return;
		setHealth(_health - health);
	}
	
	public final void addHealth(int health)
	{
		setHealth(_health + health);
	}
	
	public final void setState(String state)
	{
		Validate.isTrue(_registeredStates.containsKey(state), "Provided state has not yet been registered.");
		
		((ArmorStand) getEntity("WEAPON")).setHelmet(new ItemStack(_registeredStates.get(state).getType(), 1, (short) 0, (byte) _registeredStates.get(state).getData()));
		
		CustomUpdateState(_currentState = state);
	}
	
	public final void setStateInfo(String state, WeaponStateInfo info)
	{
		_registeredStates.put(state, info);
	}
	
	public final WeaponStateInfo getStateInfo(String state)
	{
		if (!_registeredStates.containsKey(state))
			_registeredStates.put(state, new WeaponStateInfo(Material.STONE, (byte) 101));
		
		return _registeredStates.get(state);
	}
	
	public void checkInventory()
	{
		for (int slot = 0; slot < _inventory.getSize(); slot++)
		{
			ItemStack item = _inventory.getItem(slot);
			
			if (item == null)
				continue;
			
			if (slot == _ammunitionSlot)
			{
				if (item.getType() != _ammunitionType)
				{
					if (CustomInventoryValid(slot, item))
						continue;
					
					if (getRider() != null)
						getRider().getInventory().addItem(item);
					else
						_location.getWorld().dropItem(_location, item);
					
					_inventory.setItem(slot, null);
				}
				else
				{
					if (item.getAmount() > _maxAmmunition)
					{
						if (getRider() != null)
							getRider().getInventory().addItem(new ItemStack(_ammunitionType, item.getAmount() - _maxAmmunition));
						else
							_location.getWorld().dropItem(_location, item);
						
						_inventory.setItem(slot, new ItemStack(_ammunitionType, _maxAmmunition));
					}
					else
					{
						UtilServer.CallEvent(new LoadSiegeWeaponEvent(getRider(), this));
					}
				}
			}
			else if (_firepowerSlots.contains(slot))
			{
				if (item.getType() != _firepowerType)
				{
					if (CustomInventoryValid(slot, item))
						continue;
					
					if (getRider() != null)
						getRider().getInventory().addItem(item);
					else
						_location.getWorld().dropItem(_location, item);
					
					_inventory.setItem(slot, null);
				}
				else
				{
					if (item.getAmount() > _maxFirepowerPerSlot)
					{
						if (getRider() != null)
							getRider().getInventory().addItem(new ItemStack(_firepowerType, item.getAmount() - _maxFirepowerPerSlot));
						else
							_location.getWorld().dropItem(_location, item);
						
						_inventory.setItem(slot, new ItemStack(_firepowerType, _maxFirepowerPerSlot));
					}
				}
			}
			else
			{
				if (CustomInventoryValid(slot, item))
					continue;
				
				if (getRider() != null)
					getRider().getInventory().addItem(item);
				else
					_location.getWorld().dropItem(_location, item);
				
				_inventory.setItem(slot, null);
			}
		}
	}
	
	@EventHandler
	public void clanDelete(ClanDeleteEvent event)
	{
		if (event.getClanInfo().getName().equals(_ownerClan.getName()))
		{
			System.out.println("Killing Siege weapon " + _uniqueId + " because owner clan has been deleted.");
			kill();
		}
	}
	
	@EventHandler
	public void onSiegeWeaponExplode(SiegeWeaponExplodeEvent event)
	{
		if (UtilAlg.inBoundingBox(event.getProjectile().getLocation(), _location.clone().subtract(3, 2, 3), _location.clone().add(3, 2, 3)))
		{
			kill();
			
			_ownerClan.inform("One of your Cannons has been destroyed!", null);
			UtilTextMiddle.display("Damage", "You destroyed " + F.elem(getOwner().getName()) + "'s " + _name + ".", 10, 60, 10, event.getWeapon().getRider());
			UtilPlayer.message(event.getWeapon().getRider(), F.main("Clans", "You destroyed " + F.elem(getOwner().getName()) + "'s " + _name + "."));
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event)
	{
		if (_comprisedOf.contains(event.getEntity()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDmg(EntityDamageByEntityEvent event)
	{
		if (_comprisedOf.contains(event.getEntity()) && event.getDamager() instanceof Player)
		{
			if (!((Player) event.getDamager()).equals(_rider))
			{
				handleLeftClick((Player) event.getDamager());
			}
			
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCloseInv(InventoryCloseEvent event)
	{
		if (!event.getInventory().equals(_inventory))
			return;
		
		ClansManager.getInstance().runSyncLater(() -> {
			if (!event.getPlayer().getInventory().equals(_inventory) && canBeFired())
				UtilTextMiddle.display(_name + " Ready", "Power Level: " + C.cGreen + UtilText.repeat("▌", getPowerLevel()) + C.cRed + UtilText.repeat("▌", _maxFirepowerPerSlot - getPowerLevel()), 20, 100, 20, (Player) event.getPlayer());
		}, 3L);
	}
	
	@EventHandler
	public void onDismount(EntityDismountEvent event)
	{
		if (event.getEntity() instanceof Player && (event.getDismounted().equals(getEntity("PLAYERMOUNT")) || CustomDismount((Player) event.getEntity(), event.getDismounted())))
				dismount((Player) event.getEntity());
	}
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent event)
	{	
		if (_comprisedOf.contains(event.getRightClicked()))
		{
			handleRightClick(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{	
		if (!event.getPlayer().equals(_rider))
		{
			return;
		}
		
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			handleRightClick(event.getPlayer());
			event.setCancelled(true);
		}
		else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			handleLeftClick(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event)
	{
		if (_comprisedOf.contains(event.getRightClicked()))
		{
			handleRightClick(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!event.getBlock().getLocation().getWorld().equals(_location.getWorld()))
		{
			return;
		}
		
		if (event.getBlock().getLocation().distance(_location) < _boundingBoxSize + 1.65 && event.getBlock().getLocation().getY() <= _location.getY())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You may not break blocks near a Siege Weapon"));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (!event.getBlock().getLocation().getWorld().equals(_location.getWorld()))
		{
			return;
		}
		
		if (event.getBlock().getLocation().distance(_location) < _boundingBoxSize + 1.65 && event.getBlock().getLocation().getY() <= _location.getY())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You may not place blocks near a Siege Weapon"));
			event.setCancelled(true);
		}
	}

	public boolean inProtection(Block block)
	{
		if (!block.getLocation().getWorld().equals(_location.getWorld()))
		{
			return false;
		}
		
		return block.getLocation().distance(_location) < _boundingBoxSize + 1.65 && block.getLocation().getY() <= _location.getY() + 2;
	}
	
	@EventHandler
	public void onInteract(PlayerArmorStandManipulateEvent event)
	{
		if (_comprisedOf.contains(event.getRightClicked()))
		{
			handleRightClick(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void chunkUnload(ChunkUnloadEvent event)
	{
		if (_comprisedOf.stream().anyMatch(entity -> entity.getLocation().getChunk().equals(event.getChunk())))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		update();
		
		CustomTick();
	}

	public Location getLocation()
	{
		return _location.clone();
	}

	public double getSize()
	{
		return _boundingBoxSize;
	}
	

	public int getBaseDamage()
	{
		return _baseDamage;
	}
	
	public ClanInfo getOwner()
	{
		return _ownerClan;
	}
	
	public ClansManager getClans()
	{
		return _clans;
	}
	
	protected WeaponProjectile getProjectile()
	{
		return _projectile;
	}
	
	protected int getAmmunition()
	{
		ItemStack item = _inventory.getItem(_ammunitionSlot);
		
		if (item != null && item.getType().equals(_ammunitionType))
			return item.getAmount();
		
		return 0;
	}
	
	protected int getPowerLevel()
	{
		int power = 0;
		
		for (int slot : _firepowerSlots)
		{
			ItemStack item = _inventory.getItem(slot);
			
			if (item == null || !item.getType().equals(_firepowerType))
				continue;
			
			power += _inventory.getItem(slot).getAmount();
		}
		
		return power / 4;
	}
	
	public int getUniqueId()
	{
		return _uniqueId;
	}
	
	public Player getRider()
	{
		return _rider;
	}
	
	public final String getState()
	{
		return _currentState;
	}
	
	public final boolean shouldSyncWithDb()
	{
		return _syncWithDb;
	}
	
	public SiegeWeaponToken toToken()
	{
		SiegeWeaponToken token = new SiegeWeaponToken();
		
		token.UniqueId = _uniqueId;
		token.OwnerClan = _ownerClan;
		token.WeaponType = _weaponTypeIdentifier;
		token.Location = _location;
		token.Health = _health;
		token.Yaw = (int) _yaw;
		
		return token;
	}

	public boolean isPartOf(UUID uniqueId)
	{
		for (Entity entity : _comprisedOf)
		{
			if (entity.getUniqueId().equals(uniqueId))
				return true;
		}
		
		return false;
	}

	public void setInvincible(boolean invincible)
	{
		_invincible = invincible;
	}

	public boolean isInvincible()
	{
		return _invincible;
	}
	
}
