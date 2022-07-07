package mineplex.game.clans.clans.siege.weapon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilCollections;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansGame;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.siege.SiegeManager;
import mineplex.game.clans.clans.siege.repository.tokens.SiegeWeaponToken;
import mineplex.game.clans.clans.siege.weapon.projectile.WeaponProjectile;
import mineplex.game.clans.clans.siege.weapon.util.AccessRule;
import mineplex.game.clans.clans.siege.weapon.util.AccessType;
import mineplex.game.clans.clans.siege.weapon.util.WeaponStateInfo;

public class Cannon extends SiegeWeapon
{
	public static final ItemStack CANNON_ITEM = new ItemBuilder(Material.SPONGE, 1).setData((byte) 1).setRawTitle(C.Reset + C.cBlue + "Cannon").build();
	
	private int _firepower = 1;
	
	public Cannon(SiegeManager siegeManager, SiegeWeaponToken token)
	{
		super(300, "Cannon", token, siegeManager.getClansManager(), siegeManager);
		
		if (_ownerClan == null)
		{
			System.out.println("[cannon] owner clan null, killing");
			kill();
			return;
		}
		
		System.out.println("Siege> Loading Cannon from token " + token.UniqueId);
		
		setBoundingBox(1);
		
		setStateInfo("Unloaded", new WeaponStateInfo(Material.SPONGE, (byte) 1));
		setStateInfo("Loaded", new WeaponStateInfo(Material.SPONGE, (byte) 0));
		
		setAmmunitionType(Material.TNT);
		
		setAmmunitionSlot(4);
		setMaximumAmmunitionPerSlot(1);
		
		_baseDamage = 650;
		
		setFireRule(new AccessRule(AccessType.LCLICK_BB, player ->
		{
			if (!isRiding(player))
			{
				return false;
			}
			
//			if (!_ownerClan.isMember(player))
//			{
//				UtilPlayer.message(player, F.main("Clans", "This cannon is not owned by your Clan."));
//				return false;
//			}

			if (_clans.hasTimer(player))
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot fire a cannon whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				return false;
			}
			
			if (!canBeFired())
			{
				UtilPlayer.message(player, F.main("Clans", "Cannon is not loaded correctly."));
				return false;
			}
			
			if (System.currentTimeMillis() - _lastFired < 20000)
			{
				UtilPlayer.message(player, F.main("Clans", "Cannon is cooling down (" + F.time(UtilTime.MakeStr(20000 - (System.currentTimeMillis() - _lastFired))) + ")"));
				return false;
			}
			
			return true;
		}));
		
		enableInventory(UtilServer.getServer().createInventory(null, InventoryType.DISPENSER, C.cDAquaB + _name), new AccessRule(AccessType.RCLICK_BB, player -> player.equals(getRider())));
		
		setRideable(new AccessRule(AccessType.RCLICK_BB, player ->
		{
			if (!_ownerClan.isMember(player))
			{
				UtilPlayer.message(player, F.main("Clans", "This Cannon is not owned by your Clan."));
				return false;
			}
			
			if (getRider() != null && !getRider().equals(player))
			{
				UtilPlayer.message(player, F.main("Clans", "Someone is already riding this cannon."));
				return false;
			}

			if (_clans.hasTimer(player))
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot ride on a Cannon whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				return false;
			}
			
			return !player.equals(getRider());
		}));
	}
	
	public Cannon(Location location, ClanInfo clan, SiegeManager siegeManager, boolean syncWithDb)
	{
		super(2, location.clone().add(0.5, 0, 0.5), 1400, "Cannon", clan, clan.Clans, siegeManager);
		
		_syncWithDb = syncWithDb;
		
		setBoundingBox(1);
		
		setStateInfo("Unloaded", new WeaponStateInfo(Material.SPONGE, (byte) 1));
		setStateInfo("Loaded", new WeaponStateInfo(Material.SPONGE, (byte) 0));
		
		loadEntities(true);
		
		setFirepowerType(Material.SULPHUR);
		setAmmunitionType(Material.TNT);
		
		setFirepowerSlots(1, 3, 5, 7);
		setMaximumFirepowerPerSlot(3);
		
		setAmmunitionSlot(4);
		setMaximumAmmunitionPerSlot(1);
		
		_baseDamage = 650;
		
		setFireRule(new AccessRule(AccessType.LCLICK_BB, player ->
		{
			if (!isRiding(player))
			{
				return false;
			}
			
//			if (!_ownerClan.isMember(player))
//			{
//				UtilPlayer.message(player, F.main("Clans", "This Cannon is not owned by your Clan."));
//				return false;
//			}

			if (_clans.hasTimer(player))
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot fire a Cannon whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				return false;
			}
			
			if (!canBeFired())
			{
				UtilPlayer.message(player, F.main("Clans", "Cannon is not loaded correctly."));
				return false;
			}
			
			if (System.currentTimeMillis() - _lastFired < 30000)
			{
				UtilPlayer.message(player, F.main("Clans", "Cannon is cooling down (" + F.time(UtilTime.MakeStr(30000 - (System.currentTimeMillis() - _lastFired))) + ")"));
				return false;
			}
			
			return true;
		}));
		
		enableInventory(UtilServer.getServer().createInventory(null, InventoryType.DISPENSER, C.cDAquaB + _name), new AccessRule(AccessType.RCLICK_BB, player -> player.equals(getRider())));
		
		setRideable(new AccessRule(AccessType.RCLICK_BB, player ->
		{
			if (!_ownerClan.isMember(player))
			{
				UtilPlayer.message(player, F.main("Clans", "This Cannon is not owned by your Clan."));
				return false;
			}
			
			if (getRider() != null && !getRider().equals(player))
			{
				UtilPlayer.message(player, F.main("Clans", "Someone is already riding this Cannon."));
				return false;
			}

			if (_clans.hasTimer(player))
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot ride on a Cannon whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				return false;
			}
			
			return !player.equals(getRider());
		}));
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	protected void InventoryClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null)
		{
			return;
		}

		if (event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT)
		{
			if(_inventory.getViewers().contains(event.getWhoClicked()))
			{
				event.setCancelled(true); //block shift right clicking tnt into this inventory
				getClans().runSyncLater(() -> ((Player) event.getWhoClicked()).updateInventory(), 1L);
			}
			
			return;
		}

		if (!event.getClickedInventory().equals(_inventory))
		{
			return;
		}
		
		if (event.getSlot() == 0)
		{
			int oldFirepower = _firepower;
			
			_firepower = UtilMath.clamp(--_firepower, 1, 3);
			
			if (oldFirepower != _firepower)
			{
				((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
			}
			else
			{
				((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
			}
			
			event.setCancelled(true);
		}
		else if (event.getSlot() == 1)
		{
			event.setCancelled(true);
		}
		else if (event.getSlot() == 2)
		{
			int oldFirepower = _firepower;
			
			_firepower = UtilMath.clamp(++_firepower, 1, 3);
			
			if (oldFirepower != _firepower)
			{
				((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
			}
			else
			{
				((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
			}
			
			event.setCancelled(true);
		}
		else if (event.getSlot() != _ammunitionSlot)
		{
			event.setCancelled(true);
		}
		else if (event.getSlot() == _ammunitionSlot && ClansGame.isDupedFromClassShop(event.getCursor()))
		{
			event.setCancelled(true);
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (ClansManager.getInstance().getClientManager().Get(p).hasPermission(ClansGame.Perm.DUPE_ALERT))
				{
					UtilPlayer.message(p, F.elem("[" + C.cRedB + "!" + C.cGray + "] ") + event.getWhoClicked().getName() + " just tried to use a duped item/block!");
				}
			}
			event.setCursor(new ItemStack(Material.AIR));
			((Player)event.getWhoClicked()).updateInventory();
		}
	}
	
	private void updateInventory()
	{
		_inventory.setItem(0, new ItemBuilder(Material.LEVER).setTitle(C.cRed + "-1 Firepower").build());
		_inventory.setItem(1, new ItemBuilder(Material.SULPHUR).setTitle(C.cWhiteB + "Firepower: " + C.cYellow + _firepower).setAmount(_firepower).build());
		_inventory.setItem(2, new ItemBuilder(Material.LEVER).setTitle(C.cGreen + "+1 Firepower").build());
		
		for (int slot : UtilCollections.newList(3, 5, 6, 7, 8))
		{
			_inventory.setItem(slot, new ItemBuilder(Material.COBBLESTONE).setTitle(C.cGray + "Cannon Wall").build());
		}
	}
	
	protected boolean CustomInventoryValid(int slot, ItemStack item)
	{
		return true; // all slots are now filled; slot == 0 || slot == 1 || slot == 2;
	}
	
	private void loadEntities(boolean insert)
	{
		Slime filler = _location.getWorld().spawn(_location.clone(), Slime.class);
		
		UtilEnt.silence(filler, true);
		UtilEnt.vegetate(filler);
		
		filler.setSize(-1);
		filler.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 1, true, false));
		
		addEntity(filler, "Filler_1");
		
		Slime playerMount = _location.getWorld().spawn(_location.clone(), Slime.class);
		
		UtilEnt.silence(playerMount, true);
		UtilEnt.vegetate(playerMount);
		
		playerMount.setSize(-1);
		playerMount.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 1, true, false));
		
		getEntity("Filler_1").setPassenger(playerMount);
		addEntity(playerMount, "PLAYERMOUNT");
		
		ArmorStand weapon = _location.getWorld().spawn(_location.clone(), ArmorStand.class);
		
		UtilEnt.setFakeHead(weapon, true);
		weapon.teleport(_location);
		weapon.setVisible(false);
		weapon.setGravity(false);
		
		weapon.setPassenger(getEntity("Filler_1"));
		
		addEntity(weapon, "WEAPON");
		
		if (insert)
		{
			insert();
		}
	}
	
	@Override
	public void FindEntities()
	{
		Lists.newArrayList(_location.getWorld().getEntities())
				.forEach(entity ->
				{
					if (Integer.toString(_uniqueId).equals(entity.getCustomName()))
					{
						entity.remove();
					}
				});
		
		loadEntities(false);
	}
	
	@Override
	protected WeaponProjectile CustomFire(double yawRot, double verticalVel, double horizontalVel)
	{
		Location location = UtilAlg.moveForward(new Location(_location.getWorld(), _location.getX(), _location.getY() + .2, _location.getZ(), (float) yawRot, (float) 0), 0.35, (float) yawRot, false);
		
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, location, new Vector(0, 0, 0), .1f, 2, ViewDist.MAX);
		UtilServer.getServer().getOnlinePlayers().forEach(player -> player.playSound(location, Sound.EXPLODE, 1.f, 1.f));
		
		return new TntProjectile(this, location, yawRot, verticalVel, horizontalVel);
	}
	
	@Override
	public String GetNextState()
	{
		if (getAmmunition() > 0)
		{
			return "Loaded";
		}
		
		return "Unloaded";
	}
	
	protected int getPowerLevel()
	{
		return _firepower;
	}
	
	@Override
	protected void CustomTick()
	{
		updateInventory();
		
		if (getProjectile() != null)
		{
			UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, getProjectile().getLocation().add(0, .5, 0), new Vector(0, 0, 0), .1f, 3, ViewDist.MAX);
		}
	}
	
	protected void handleLeftClick(Player player)
	{
		super.handleLeftClick(player);
	}
	
	@Override
	protected double[] GetProjectileVelocity()
	{
		int firepower = getPowerLevel();
		
		double hMult = 0;
		double yAdd = 0;
		
		if (firepower == 1)
		{
			hMult = 1.2;
			yAdd = 0.5;
		}
		else if (firepower == 2)
		{
			hMult = 1.7;
			yAdd = 0.55;
		}
		else if (firepower >= 3)
		{
			hMult = 2.35;
			yAdd = 0.6;
		}
		
		return new double[] { yAdd, hMult };
	}

	/*@EventHandler
	public void explosionEffects(SiegeWeaponExplodeEvent event)
	{
		List<Block> blocks = Stream.generate(() -> UtilAlg.getRandomLocation(event.getProjectile().getLocation(), 4 * getPowerLevel()).getBlock())
			.limit(30) // Generate up to 30
			.distinct() // distinct blocks,
			.filter(block -> block.getType() != Material.AIR) // filter for non-air
			.filter(block -> // and blocks whose locations aren't blacklisted,
			{
				ClanTerritory claim = _siegeManager.getClansManager().getClanUtility().getClaim(block.getLocation());
				return claim == null || _siegeManager.getClansManager().getBlacklist().allowed(claim.Owner);
			})
			.limit(10) // and take up to 10 of them.
			.collect(Collectors.toList());

		_clans.getExplosion().BlockExplosion(
				blocks,
				event.getProjectile().getLocation(),
				false,
				false
		);
	}*/
}