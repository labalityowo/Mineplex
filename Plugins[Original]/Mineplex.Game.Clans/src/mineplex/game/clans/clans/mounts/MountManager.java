package mineplex.game.clans.clans.mounts;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.Pair;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.donation.DonationManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.HelmetPacketManager;
import mineplex.game.clans.clans.mounts.Mount.MountType;
import mineplex.game.clans.clans.mounts.Mount.SkinType;
import mineplex.game.clans.clans.mounts.gui.MountShop;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.spawn.Spawn;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.serverdata.Utility;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityLiving;

public class MountManager extends MiniDbClientPlugin<MountOwnerData>
{
	public enum Perm implements Permission
	{
		MOUNT_COMMAND,
		GIVE_MOUNT_COMMAND,
		MOUNT_SKIN_UNLOCK,
	}

	protected static Field JumpBooleanField;
	protected static Field JumpFloatField;
	
	private static final double[] JUMP_STARS = {0.8, 1, 1.2};
	private static final double[] SPEED_STARS = {0.2, 0.27, 0.33};
	private static final int[] STRENGTH_STARS = {1, 2, 3};
	
	private static final long SUMMON_WARMUP = 5000;
	private static final long FORCED_COOLDOWN = 120 * 1000;
	private static final long MAX_TIME_DISMOUNTED = 30000;
	
	private static final int MAX_PER_TYPE = 3;
	
	private final MountRepository _repository;
	private final DonationManager _donationManager;
	
	private final Map<CraftHorse, Mount> _spawnedMounts = new HashMap<>();
	private final Map<Player, Pair<Long, MountToken>> _summoning = new WeakHashMap<>();
	
	private final int _serverId;
	
	public MountManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super("Clans Mount Manager", plugin, clientManager);
		
		try
		{
			JumpBooleanField = EntityLiving.class.getDeclaredField("aY");
			JumpBooleanField.setAccessible(true);
			JumpFloatField = EntityHorse.class.getDeclaredField("br");
			JumpFloatField.setAccessible(true);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		_serverId = ClansManager.getInstance().getServerId();
		_repository = new MountRepository(plugin, this, _serverId);
		_donationManager = donationManager;
		
		final MountShop shop = new MountShop(this);
		
		addCommand(new CommandBase<MountManager>(this, Perm.MOUNT_COMMAND, "mounts", "mount")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				shop.attemptShopOpen(caller);
			}
		});
		
		addCommand(new CommandBase<MountManager>(this, Perm.GIVE_MOUNT_COMMAND, "givemount")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 4)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /" + _aliasUsed + " <SPEED> <JUMP> <STRENGTH> <TYPE>"));
					return;
				}
				
				Integer speed = 0;
				Integer jump = 0;
				Integer strength = 0;
				MountType type = null;
				
				try
				{
					speed = Integer.parseInt(args[0]);
				}
				catch (Exception e)
				{
					UtilPlayer.message(caller, F.main(getName(), "Invalid speed!"));
					return;
				}
				
				try
				{
					jump = Integer.parseInt(args[1]);
				}
				catch (Exception e)
				{
					UtilPlayer.message(caller, F.main(getName(), "Invalid jump!"));
					return;
				}
				
				try
				{
					strength = Integer.parseInt(args[2]);
				}
				catch (Exception e)
				{
					UtilPlayer.message(caller, F.main(getName(), "Invalid strength!"));
					return;
				}
				
				try
				{
					type = MountType.valueOf(args[3]);
				}
				catch (Exception e)
				{
					UtilPlayer.message(caller, F.main(getName(), "Invalid type!"));
					return;
				}
				
				caller.getInventory().addItem(new MountClaimToken(jump, speed, strength, type).toItem());
			}
		});
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.MOUNT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.GIVE_MOUNT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.MOUNT_SKIN_UNLOCK, true, true);
	}
	
	public DonationManager getDonationManager()
	{
		return _donationManager;
	}
	
	public MountRepository getRepository()
	{
		return _repository;
	}
	
	@Override
	public void disable()
	{
		_summoning.clear();
		_spawnedMounts.keySet().forEach(CraftHorse::remove);
		_spawnedMounts.clear();
	}
	
	public boolean summonMount(Player player, MountToken token)
	{
		if (_summoning.containsKey(player))
		{
			UtilPlayer.message(player, F.main(getName(), "You are already summoning a mount!"));
			return false;
		}
		if (Spawn.getInstance().isSafe(player.getLocation()))
		{
			UtilPlayer.message(player, F.main(getName(), "You cannot summon a mount in safezones!"));
			return false;
		}
		if (ClansManager.getInstance().getNetherManager().isInNether(player))
		{
			UtilPlayer.message(player, F.main(getName(), "You cannot summon a mount in the Nether!"));
			return false;
		}
		if (ClansManager.getInstance().getWorldEvent().getRaidManager().isInRaid(player.getLocation()))
		{
			UtilPlayer.message(player, F.main(getName(), "You cannot summon a mount inside a raid!"));
			return false;
		}
		for (double x = -2; x <= 2; x++)
		{
			for (double z = -2; z <= 2; z++)
			{
				Location loc = player.getLocation().add(x, 0, z);
				ClanTerritory territory = ClansManager.getInstance().getClanUtility().getClaim(loc);
				if (territory != null)
				{
					ClanInfo cInfo = ClansManager.getInstance().getClan(player);
					if (cInfo != null && !cInfo.getName().equals(territory.Owner) && !ClansManager.getInstance().getClan(territory.Owner).isAdmin())
					{
						UtilPlayer.message(player, F.main(getName(), "You cannot summon a mount that close to another Clan's territory!"));
						return false;
					}
				}
			}
		}
		if (!Recharge.Instance.usable(player, "Mount Spawn Delay"))
		{
			UtilPlayer.message(player, F.main(getName(), "You cannot summon a mount so soon after your last one was forcibly despawned!"));
			return false;
		}
		_spawnedMounts.values().stream().filter(mount -> mount.getOwner().getEntityId() == player.getEntityId()).findFirst().ifPresent(mount -> mount.despawn(false));
		_summoning.put(player, Pair.create(System.currentTimeMillis(), token));
		UtilPlayer.message(player, F.main(getName(), "You are now summoning your mount! Please remain still for 5 seconds!"));
		return true;
	}
	
	public void giveMount(Player player, MountType type)
	{
		Pair<MountToken, MountStatToken> tokens = Get(player).grantMount(type);
		_repository.saveMount(ClientManager.getAccountId(player), tokens.getLeft(), tokens.getRight());
	}
	
	public void giveMount(Player player, MountClaimToken token)
	{
		Pair<MountToken, MountStatToken> tokens = Get(player).grantMount(token.Type, token.SpeedStars, token.JumpStars, token.StrengthStars);
		_repository.saveMount(ClientManager.getAccountId(player), tokens.getLeft(), tokens.getRight());
	}
	
	public void removeMountToken(Player player, MountToken token, Runnable after)
	{
		getRepository().deleteMount(token, id ->
		{
			Get(player).removeMount(id);
			Pair<Long, MountToken> summonPair = _summoning.get(player);
			if (summonPair != null)
			{
				if (summonPair.getRight().Id == id)
				{
					UtilEnt.addFlag(player, "REMOVED_MOUNT_TOKEN");
				}
			}
			after.run();
		});
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			Iterator<Entry<CraftHorse, Mount>> mountIterator = _spawnedMounts.entrySet().iterator();
			while (mountIterator.hasNext())
			{
				Entry<CraftHorse, Mount> entry = mountIterator.next();
				if (entry.getKey().isDead() || !entry.getKey().isValid())
				{
					mountIterator.remove();
					entry.getValue().despawn(false);
					continue;
				}
				if (entry.getValue().getOwner().isDead() || !entry.getValue().getOwner().isValid() || !entry.getValue().getOwner().isOnline())
				{
					mountIterator.remove();
					entry.getValue().despawn(false);
					continue;
				}
				if (Spawn.getInstance().isSafe(entry.getKey().getLocation()))
				{
					mountIterator.remove();
					entry.getValue().despawn(false);
					continue;
				}
				if (ClansManager.getInstance().getNetherManager().isInNether(entry.getKey().getLocation()))
				{
					mountIterator.remove();
					entry.getValue().despawn(false);
					continue;
				}
				if (ClansManager.getInstance().getWorldEvent().getRaidManager().isInRaid(entry.getKey().getLocation()))
				{
					mountIterator.remove();
					entry.getValue().despawn(false);
					continue;
				}
				if (entry.getKey().getPassenger() == null)
				{
					if (UtilEnt.GetMetadata(entry.getKey(), "DISMOUNT_TIME") != null)
					{
						Long dismount = UtilEnt.GetMetadata(entry.getKey(), "DISMOUNT_TIME");
						if (UtilTime.elapsed(dismount, MAX_TIME_DISMOUNTED))
						{
							mountIterator.remove();
							entry.getValue().despawn(false);
							continue;
						}
					}
					else
					{
						UtilEnt.SetMetadata(entry.getKey(), "DISMOUNT_TIME", System.currentTimeMillis());
					}
				}
				else
				{
					UtilEnt.removeMetadata(entry.getKey(), "DISMOUNT_TIME");
				}
				entry.getValue().update();
			}
			
			Iterator<Entry<Player, Pair<Long, MountToken>>> summoningIterator = _summoning.entrySet().iterator();
			while (summoningIterator.hasNext())
			{
				Entry<Player, Pair<Long, MountToken>> entry = summoningIterator.next();
				if (UtilEnt.hasFlag(entry.getKey(), "REMOVED_MOUNT_TOKEN"))
				{
					summoningIterator.remove();
					UtilEnt.removeFlag(entry.getKey(), "REMOVED_MOUNT_TOKEN");
					continue;
				}
				if (UtilTime.elapsed(entry.getValue().getLeft(), SUMMON_WARMUP))
				{
					summoningIterator.remove();
					if (entry.getKey().isDead() || !entry.getKey().isValid() || !entry.getKey().isOnline())
					{
						continue;
					}
					if (Spawn.getInstance().isSafe(entry.getKey().getLocation()))
					{
						continue;
					}
					if (ClansManager.getInstance().getNetherManager().isInNether(entry.getKey().getLocation()))
					{
						continue;
					}
					if (ClansManager.getInstance().getWorldEvent().getRaidManager().isInRaid(entry.getKey().getLocation()))
					{
						continue;
					}
					entry.getValue().getRight().Type.spawn(entry.getKey(), entry.getValue().getRight().Skin, Get(entry.getKey()).getStatToken(entry.getValue().getRight()));
					continue;
				}
				if (UtilEnt.hasFlag(entry.getKey(), "MOVED_WHILE_SUMMONING_MOUNT"))
				{
					summoningIterator.remove();
					UtilEnt.removeFlag(entry.getKey(), "MOVED_WHILE_SUMMONING_MOUNT");
					UtilPlayer.message(entry.getKey(), F.main(getName(), "You have stopped summoning your mount as you have moved!"));
					continue;
				}
			}
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (_summoning.containsKey(event.getPlayer()) && !UtilEnt.hasFlag(event.getPlayer(), "MOVED_WHILE_SUMMONING_MOUNT"))
		{
			Block from = event.getFrom().getBlock();
			Block to = event.getTo().getBlock();
			
			if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())
			{
				return;
			}
			UtilEnt.addFlag(event.getPlayer(), "MOVED_WHILE_SUMMONING_MOUNT");
		}
	}
	
	@EventHandler
	public void onSummon(MountSpawnEvent event)
	{
		_spawnedMounts.put(event.getMount().getEntity(), event.getMount());
		UtilPlayer.message(event.getMount().getOwner(), F.main(getName(), "Your mount has spawned!"));
	}
	
	@EventHandler
	public void onDespawn(MountDespawnEvent event)
	{
		_spawnedMounts.remove(event.getMount().getEntity());
		event.getMount().getEntity().eject();
		event.getMount().getEntity().remove();
		UtilPlayer.message(event.getMount().getOwner(), F.main(getName(), "Your mount has despawned!"));
		if (event.isForced())
		{
			Recharge.Instance.use(event.getMount().getOwner(), "Mount Spawn Delay", FORCED_COOLDOWN, false, false);
		}
	}
	
	@EventHandler
	public void onUseToken(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}
		if (event.getItem() == null)
		{
			return;
		}
		MountClaimToken token = MountClaimToken.fromItem(event.getItem());
		if (token == null)
		{
			return;
		}
		
		event.setCancelled(true);
		
		if (Get(event.getPlayer()).getAmountOwned(token.Type) >= MAX_PER_TYPE)
		{
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You have reached the maximum amount of that type of mount!"));
			return;
		}
		
		giveMount(event.getPlayer(), token);
		UtilPlayer.message(event.getPlayer(), F.main(getName(), "You have redeemed your mount!"));
		event.getPlayer().getInventory().setItemInHand(null);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void redirectHorseDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() == null || !(event.GetDamageeEntity() instanceof Horse))
		{
			return;
		}
		Mount mount = _spawnedMounts.get(event.GetDamageeEntity());
		if (mount == null)
		{
			return;
		}
		if (event.GetDamagerPlayer(true) != null && event.GetDamagerPlayer(true).getEntityId() == mount.getOwner().getEntityId())
		{
			event.SetCancelled("Damaging own mount");
			mount.despawn(false);
			return;
		}
		if (mount.getEntity().getPassenger() == null)
		{
			event.SetCancelled("Killing riderless mount");
			mount.despawn(true);
			return;
		}
		event.setDamagee(mount.getOwner());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void handleRiderHits(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() == null || event.GetDamageePlayer().getVehicle() == null || !(event.GetDamageePlayer().getVehicle() instanceof Horse))
		{
			return;
		}
		if (event.GetDamagerPlayer(true) != null && event.GetDamagerPlayer(true).getEntityId() == event.GetDamageePlayer().getEntityId())
		{
			return;
		}
		Mount mount = _spawnedMounts.get(event.GetDamageePlayer().getVehicle());
		if (mount == null)
		{
			return;
		}
		if (event.GetCause() != DamageCause.FALL)
		{
			mount.handleHit();
		}
	}
	
	@EventHandler
	public void onEditHorseInventory(InventoryClickEvent event)
	{
		if (!(event.getClickedInventory() instanceof HorseInventory))
		{
			return;
		}
		if (!_spawnedMounts.containsKey(event.getClickedInventory().getHolder()))
		{
			return;
		}
		if (event.getSlot() == 0 || event.getSlot() == 1)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void mountInteract(PlayerInteractEntityEvent event)
	{
		if (!(event.getRightClicked() instanceof Horse))
		{
			return;
		}
		Mount mount = _spawnedMounts.get(event.getRightClicked());
		if (mount == null)
		{
			return;
		}
		if (mount.getOwner().getEntityId() == event.getPlayer().getEntityId())
		{
			if (UtilEnt.hasFlag(event.getRightClicked(), "HelmetPacket.RiderMelon"))
			{
				HelmetPacketManager.getInstance().refreshToAll(event.getPlayer(), new ItemStack(Material.MELON_BLOCK));
			}
			return;
		}

		UtilPlayer.message(event.getPlayer(), F.main(getName(), "This is not your Mount!"));
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		_spawnedMounts.entrySet().forEach(entry ->
		{
			if (UtilWorld.isInChunk(entry.getKey().getLocation(), event.getChunk()))
			{
				entry.getValue().despawn(false);
			}
		});
	}
	
	@EventHandler
	public void onDismount(EntityDismountEvent event)
	{
		if (_spawnedMounts.containsKey(event.getEntity()) && event.getDismounted() instanceof Player)
		{
			Player player = (Player) event.getDismounted();
			runSyncLater(() ->
			{
				player.teleport(event.getEntity());
				if (UtilEnt.hasFlag(event.getEntity(), "HelmetPacket.RiderMelon"))
				{
					HelmetPacketManager.getInstance().refreshToAll(player, null);
				}
			}, 1L);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onShootBow(EntityShootBowEvent event)
	{
		LivingEntity entity = event.getEntity();
		Entity vehicle = entity.getVehicle();
		if (entity instanceof Player && vehicle != null && _spawnedMounts.containsKey(vehicle))
		{
			UtilPlayer.message(entity, F.main(getName(), "You cannot shoot while mounted!"));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onUseSkill(SkillTriggerEvent event)
	{
		Player rider = event.GetPlayer();
		Entity vehicle = rider.getVehicle();
		if (vehicle != null && _spawnedMounts.containsKey(vehicle))
		{
			UtilPlayer.message(rider, F.main(getName(), "You cannot use skills while mounted!"));
			event.SetCancelled(true);
		}
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT am.id, am.mountTypeId, am.mountSkinId, ms.statToken FROM accountClansMounts AS am INNER JOIN clansMountStats AS ms ON ms.mountId = am.id WHERE am.accountId=" + accountId + " AND am.serverId=" + _serverId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		MountOwnerData data = new MountOwnerData();
		while (resultSet.next())
		{
			MountToken token = new MountToken();
			token.Id = resultSet.getInt("id");
			token.Type = MountType.getFromId(resultSet.getInt("mountTypeId"));
			token.Skin = SkinType.getFromId(resultSet.getInt("mountSkinId"));
			MountStatToken statToken = Utility.deserialize(resultSet.getString("statToken"), MountStatToken.class);
			data.acceptLoad(token, statToken);
		}
		Set(uuid, data);
	}

	@Override
	protected MountOwnerData addPlayer(UUID uuid)
	{
		return new MountOwnerData();
	}
	
	public static double getSpeed(int stars)
	{
		stars = Math.min(Math.max(1, stars), 3);
		return SPEED_STARS[stars - 1];
	}
	
	public static double getJump(int stars)
	{
		stars = Math.min(Math.max(1, stars), 3);
		return JUMP_STARS[stars - 1];
	}
	
	public static int getStrength(int stars)
	{
		stars = Math.min(Math.max(1, stars), 3);
		return STRENGTH_STARS[stars - 1];
	}
}