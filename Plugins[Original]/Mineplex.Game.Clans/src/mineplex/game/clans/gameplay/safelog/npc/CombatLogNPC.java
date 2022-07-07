package mineplex.game.clans.gameplay.safelog.npc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.game.clans.clans.ClansManager;

public class CombatLogNPC
{
	public final static EntityType NPC_TYPE = EntityType.VILLAGER;
	
	private PlayerInfo _playerInfo;
	
	private Hologram _hologram;
	
	private DisguiseManager _disguiseManager;
	private long _spawnDate;
	private final long _endingTime;
	private double _spawnHealth;
	
	private boolean _creative;
	
	private LivingEntity _npc;
	
	private CraftLivingEntity _lastDamager;
	
	private String _userDataPath;
	
	public int getEntityId()
	{
		return _npc.getEntityId();
	}
	
	public CombatLogNPC(Player player, DisguiseManager disguiseManager, HologramManager hologramManager, boolean wasCreative, String userDataPath)
	{
		_playerInfo = new PlayerInfo(player);
		_creative = wasCreative;
		_userDataPath = userDataPath;
		
		_disguiseManager = disguiseManager;
		_hologram = new Hologram(hologramManager, player.getEyeLocation().add(0, 1, 0), C.cYellow + UtilTime.MakeStr(NPCManager.COMBAT_LOG_DURATION) + C.cWhite + " Seconds left before despawn");
		_spawnDate = 0;
		_endingTime = System.currentTimeMillis() + NPCManager.COMBAT_LOG_DURATION;
		_spawnHealth = player.getHealth();
		_hologram.start();
	}
	
	/**
	 * Called when the {@code _npc} associated with this CombatLogNPC is killed
	 * and thus drops all the owner's items.
	 */
	public void onDeath(CraftLivingEntity killer)
	{
		Location location = _npc.getLocation();
		World world = location.getWorld();
		
		File file = new File(world.getWorldFolder(), String.format("playerdata/%s.dat", _playerInfo.getPlayerUuid()));
		file.delete(); // Delete the player's .dat file so they will join with
						// empty inventory/respawn on next login
		if (killer != null)
		{
			String killerName = "Unknown";
			
			if (killer instanceof CraftPlayer)
			{
				killerName = ((CraftPlayer) killer).getName();
			}
			else
			{
				killerName = UtilEnt.getName(killer);
			}
			
			try
			{
				DataOutputStream stream = new DataOutputStream(new FileOutputStream(_userDataPath + String.format("DEATH_%s.dat", _playerInfo.getPlayerUuid())));
				
				stream.writeLong(System.currentTimeMillis());
				stream.writeInt(killerName.length());
				stream.writeBytes(killerName);
				
				stream.close();
			}
			catch (IOException e)
			{
				System.out.println(String.format("FATAL ERROR while trying to create player death lock for %s, meaning %s will not be informed that they died next time they log in.", _playerInfo.getPlayerName(), _playerInfo.getPlayerName()));
			}
			
			UtilServer.broadcast(F.main("Death", F.elem(_playerInfo.getPlayerName()) + " was killed by " + F.elem(killerName) + " while combat logged."));
		}
		
		_playerInfo.dropItems(location);
		_disguiseManager.undisguise(_npc);
	}
	
	public void update()
	{
		_hologram.setText("Quitting in " + UtilTime.MakeStr(Math.max(_endingTime - System.currentTimeMillis(), 0)));
	}
	
	/**
	 * @return true, if the {@code _npc} associated with this CombatLogNPC is
	 *         alive, false otherwise.
	 */
	public boolean isAlive()
	{
		return _npc != null && !_npc.isDead();
	}
	
	/**
	 * @return the amount of time (in milliseconds) that this npc has been alive
	 *         an spawned in.
	 */
	public long getAliveDuation()
	{
		return System.currentTimeMillis() - _spawnDate;
	}
	
	public void spawn()
	{
		if (_npc != null) despawn();
		
		_npc = spawnNpc(getPlayer());
		_spawnDate = System.currentTimeMillis();
	}
	
	public void despawn()
	{
		System.out.println("Despawning");
		if (_npc != null)
		{
			_npc.remove();
			_npc = null;
			_hologram.stop();
			_hologram = null;
		}
	}
	
	public void remove()
	{
		_hologram.stop();
		_hologram = null;
	}
	
	public PlayerInfo getPlayerInfo()
	{
		return _playerInfo;
	}
	
	public Player getPlayer()
	{
		return _playerInfo.getPlayer();
	}
	
	public boolean matchesPlayer(Player player)
	{
		return _playerInfo.getPlayerName().equalsIgnoreCase(player.getName());
	}
	
	private LivingEntity spawnNpc(Player player)
	{
		Location spawnLoc = player.getLocation();
		Skeleton skel = player.getWorld().spawn(spawnLoc, Skeleton.class);
		skel.setMetadata("CombatLogNPC", new FixedMetadataValue(ClansManager.getInstance().getPlugin(), player.getUniqueId().toString()));
		skel.teleport(spawnLoc);
		skel.setHealth(_spawnHealth);
		UtilEnt.vegetate(skel);
		UtilEnt.silence(skel, true);
		
		skel.getEquipment().setHelmet(player.getInventory().getHelmet());
		skel.getEquipment().setChestplate(player.getInventory().getChestplate());
		skel.getEquipment().setLeggings(player.getInventory().getLeggings());
		skel.getEquipment().setBoots(player.getInventory().getBoots());
		skel.getEquipment().setItemInHand(player.getItemInHand());

		// Get in range
		List<Player> inRange = UtilPlayer.getNearby(spawnLoc, 75d);
		
		// Disguise
		DisguisePlayer disguise = new DisguisePlayer(skel, ((CraftPlayer) player).getHandle().getProfile());
		_disguiseManager.disguise(disguise, attempted -> inRange.contains(attempted));
		
		_hologram.setFollowEntity(skel);
			
		return skel;
	}
	
	public boolean wasCreative()
	{
		return _creative;
	}
	
	public CraftLivingEntity getLastDamager()
	{
		return _lastDamager;
	}
	
	public void setLastDamager(CraftLivingEntity damager)
	{
		_lastDamager = damager;
	}
}