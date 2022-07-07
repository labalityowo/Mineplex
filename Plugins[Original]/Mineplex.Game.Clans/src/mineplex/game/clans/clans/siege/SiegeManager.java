package mineplex.game.clans.clans.siege;

import java.util.Stack;

import net.minecraft.server.v1_8_R3.Material;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import com.google.gson.Gson;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.siege.command.GiveWeaponCommand;
import mineplex.game.clans.clans.siege.outpost.OutpostManager;
import mineplex.game.clans.clans.siege.repository.SiegeWeaponRepository;
import mineplex.game.clans.clans.siege.repository.tokens.SiegeWeaponToken;
import mineplex.game.clans.clans.siege.weapon.Cannon;
import mineplex.game.clans.clans.siege.weapon.SiegeWeapon;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.spawn.Spawn;

public class SiegeManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		GIVE_CANNON_COMMAND,
	}

	private ClansManager _clansManager;
	private OutpostManager _outpostManager;
	
	public NautHashMap<Integer, SiegeWeapon> LiveSiegeWeapons = new NautHashMap<>();
	public NautHashMap<Integer, SiegeWeapon> UnsyncedSiegeWeapons = new NautHashMap<>();
	
	private Gson _gson;
	
	private SiegeWeaponRepository _repository;
	
	private long _lastDatabaseSave = -1L;
	
	public SiegeManager(ClansManager clans)
	{
		super("Siege Manager", clans.getPlugin());
		
		_gson = new Gson();
		
		_clansManager = clans;
		
		_outpostManager = new OutpostManager(clans, this);
		
		_repository = new SiegeWeaponRepository(clans.getPlugin(), this);
		
		_outpostManager.loadOutposts();
		
		getRepository().getWeaponsByServer(getClansManager().getServerId(), tokens ->
			tokens.forEach(token ->
				runSync(() ->
				{
					final SiegeWeapon weapon;
					
					token.Location.getChunk().load();
					
					switch(token.WeaponType)
					{
						case 2:
							weapon = new Cannon(SiegeManager.this, token);
							break;
						default:
							System.out.println("[WEAPONS] ERROR WHILST LOADING WEAPON: INVALID WEAPON TYPE");
							return;
					}
					
					System.out.println("[WEAPONS] LOADED SIEGE WEAPON " + weapon.getClass().getSimpleName() + " [" + token.UniqueId + "]");
					
					LiveSiegeWeapons.put(token.UniqueId, weapon);
				})
			)
		);
		
		addCommand(new GiveWeaponCommand(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GIVE_CANNON_COMMAND, true, true);
	}
	
	@EventHandler
	public void CleanupWeirdos(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC && UtilServer.getPlayers().length != 0)
		{
			Spawn.getSpawnWorld().getEntitiesByClass(Slime.class).stream().filter(slime -> slime.getSize() != -1).forEach(slime -> {
				
				boolean part = false;
				
				for (SiegeWeapon weapon : LiveSiegeWeapons.values())
				{
					if (weapon.isPartOf(slime.getUniqueId()))
					{
						part = true;
						break;
					}
				}
				
				
				if (!part)
					for (SiegeWeapon weapon : UnsyncedSiegeWeapons.values())
					{
						if (weapon.isPartOf(slime.getUniqueId()))
						{
							part = true;
							break;
						}
					}
				
				if (!part)
				{
					System.out.println("Removing slime...");
					slime.remove();
				}
				else
				{
					slime.setSize(-1);
				}
			});
		}
	}
	
	@EventHandler
	public void save(UpdateEvent event)
	{
		if (!UtilTime.elapsed(_initializedTime, 5500) || UtilServer.getPlayers().length == 0)
		{
			return;
		}
		
		if (event.getType() == UpdateType.SLOW)
		{
			if (UtilTime.elapsed(_lastDatabaseSave, 10000))
			{
				_lastDatabaseSave = System.currentTimeMillis();
				_outpostManager.saveOutposts();
				saveSiegeWeapons();
			}
			
			for (Entity entity : Spawn.getSpawnWorld().getEntitiesByClass(ArmorStand.class))
			{
				boolean part = false;
				
				for (SiegeWeapon weapon : LiveSiegeWeapons.values())
				{
					if (weapon.isPartOf(entity.getUniqueId()))
					{
						part = true;
						break;
					}
				}
				
				if (!part)
					for (SiegeWeapon weapon : UnsyncedSiegeWeapons.values())
					{
						if (weapon.isPartOf(entity.getUniqueId()))
						{
							part = true;
							break;
						}
					}
				
				if (!part)
				{
					if (((ArmorStand) entity).getHelmet() != null && ((ArmorStand) entity).getHelmet().getType().equals(Material.SPONGE))
					{
						System.out.println("Removing armor stand");
						entity.remove();
					}
					
					if (entity.getPassenger() != null && entity.getPassenger() instanceof Slime && entity.getPassenger().getPassenger() instanceof Slime)
					{
						System.out.println("Removing armostand + children");
						entity.getPassenger().getPassenger().remove();
						entity.getPassenger().remove();
						entity.remove();
					}
				}
			}
		}
	}
	
	private void saveSiegeWeapons()
	{
		final Stack<Runnable> queue = new Stack<>();
		
		for (final SiegeWeapon weapon : LiveSiegeWeapons.values())
		{
			final SiegeWeaponToken token = weapon.toToken();
			
			if (UnsyncedSiegeWeapons.containsKey(token.UniqueId))
				continue;
			
			queue.push(() -> _repository.updateWeapon(token));
		}
		
		runAsync(() -> {
			while (!queue.isEmpty())
				queue.pop().run();
		});
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (!Clans.HARDCORE)
		{
			return;
		}
		if (event.getItemInHand().isSimilar(Cannon.CANNON_ITEM))
		{
			event.setCancelled(true);
			
			int health = ItemStackFactory.Instance.GetLoreVar(event.getPlayer().getItemInHand(), "Health", 0);
			if (health != 0)
			{
				if (trySpawnCannon(event.getPlayer(), event.getBlock().getLocation()))
				{
					event.getPlayer().setItemInHand(UtilInv.decrement(event.getPlayer().getItemInHand()));
				}
				return;
			}
			
			if (trySpawnCannon(event.getPlayer(), event.getBlock().getLocation()))
			{
				event.getPlayer().setItemInHand(UtilInv.decrement(event.getPlayer().getItemInHand()));
				return;
			}
		}
		
//		if (event.getItemInHand().isSimilar(Catapult.CATAPULT_ITEM))
//		{
//			event.setCancelled(true);
//			
//			if (trySpawnCatapult(event.getPlayer(), event.getBlock().getLocation()))
//			{
//				event.getPlayer().setItemInHand(UtilInv.decrement(event.getPlayer().getItemInHand()));
//				return;
//			}
//		}
	}
	
	public boolean trySpawnCannon(Player player, Location location, int health)
	{
		if (_clansManager.getNetherManager().isInNether(player))
		{
			_clansManager.message(player, "You are not allowed to place this in " + F.clansNether("The Nether") + ".");
			
			return false;
		}
		if (_clansManager.getWorldEvent().getRaidManager().isInRaid(player.getLocation()))
		{
			_clansManager.message(player, "You are not allowed to place this in a raid.");
			
			return false;
		}
		
		if (!_clansManager.isInClan(player))
		{
			UtilPlayer.message(player, F.main("Clans", "You must be in a Clan to place a Cannon."));
			return false;
		}

		if (_clansManager.hasTimer(player))
		{
			UtilPlayer.message(player, F.main("Clans", "You cannot place a Cannon whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			return false;
		}
		
		ClanTerritory claim = _clansManager.getClanUtility().getClaim(location);
		
		if (claim != null && !claim.Owner.equals(_clansManager.getClan(player).getName()))
		{
			UtilPlayer.message(player, F.main("Clans", "You must place a Cannon in the Wilderness or your own Territory."));
			return false;
		}
		
		spawnCannon(player, location).setHealth(health);
		
		return true;
	}
	
	public boolean trySpawnCannon(Player player, Location location)
	{
		if (_clansManager.getNetherManager().isInNether(player))
		{
			_clansManager.message(player, "You are not allowed to place this in " + F.clansNether("The Nether") + ".");
			
			return false;
		}
		if (_clansManager.getWorldEvent().getRaidManager().isInRaid(player.getLocation()))
		{
			_clansManager.message(player, "You are not allowed to place this in a raid.");
			
			return false;
		}
		
		if (!_clansManager.isInClan(player))
		{
			UtilPlayer.message(player, F.main("Clans", "You must be in a Clan to place a Cannon."));
			return false;
		}

		if (_clansManager.hasTimer(player))
		{
			UtilPlayer.message(player, F.main("Clans", "You cannot place a Cannon whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			return false;
		}
		
		ClanTerritory claim = _clansManager.getClanUtility().getClaim(location);
		
		if (claim != null && !claim.Owner.equals(_clansManager.getClan(player).getName()))
		{
			UtilPlayer.message(player, F.main("Clans", "You must place a Cannon in the Wilderness or your own Territory."));
			return false;
		}
		
		spawnCannon(player, location);
		
		return true;
	}
	
	public Cannon spawnCannon(Player player, Location location)
	{
		return spawnCannon(player, location, true);
	}

	public Cannon spawnCannon(Player player, Location location, boolean syncWithDb)
	{
		Cannon cannon = new Cannon(location, _clansManager.getClan(player), this, syncWithDb);

		UnsyncedSiegeWeapons.put(cannon.getUniqueId(), cannon);
		
		return cannon;
	}
	
	public void dead(SiegeWeapon weapon)
	{
		LiveSiegeWeapons.remove(weapon.getUniqueId());
		SiegeWeapon unsynced = UnsyncedSiegeWeapons.remove(weapon.getUniqueId());
		
		if (unsynced == null)
			_repository.deleteWeapon(weapon.getUniqueId());
	}

	public OutpostManager getOutpostManager()
	{
		return _outpostManager;
	}

	public Gson getGson()
	{
		return _gson;
	}

	public ClansManager getClansManager()
	{
		return _clansManager;
	}
	
	public int randomId()
	{
		/**
		 * prevents id from ever being 0 (which is used internally as NULL)
		 */
		return 1 + UtilMath.random.nextInt(Integer.MAX_VALUE - 1);
	}

	public SiegeWeaponRepository getRepository()
	{
		return _repository;
	}
}
