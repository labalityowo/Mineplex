package nautilus.game.arcade.game.games.minecraftleague.variation.wither;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.DataLoc;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.game.games.minecraftleague.data.TeamTowerBase;
import nautilus.game.arcade.game.games.minecraftleague.variation.GameVariation;
import nautilus.game.arcade.game.games.minecraftleague.variation.wither.data.TeamAltar;
import nautilus.game.arcade.game.games.minecraftleague.variation.wither.data.WitherMinionManager;
import nautilus.game.arcade.game.games.minecraftleague.variation.wither.data.WitherPathfinder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WitherVariation extends GameVariation
{
	private ConcurrentHashMap<GameTeam, TeamAltar> _altars = new ConcurrentHashMap<GameTeam, TeamAltar>();
	
	private Wither _wither;
	private WitherPathfinder _pathfinder;
	
	private WitherMinionManager _skellyMan;
	
	public boolean WitherSpawned;
	
	private GameTeam _wowner = null;
	
	public WitherVariation(MinecraftLeague host)
	{
		super(host);
		WitherSpawned = false;
		_skellyMan = new WitherMinionManager(this, WorldData.GetDataLocs(DataLoc.WITHER_SKELETON.getKey()));
	}
	
	@Override
	public void customDeregister()
	{
		HandlerList.unregisterAll(_skellyMan);
	}
	
	@Override
	public void ParseData()
	{
		_altars.put(Host.GetTeam(ChatColor.AQUA), new TeamAltar(this, Host.GetTeam(ChatColor.AQUA), WorldData.GetDataLocs(DataLoc.BLUE_ALTAR.getKey()).get(0)));
		_altars.put(Host.GetTeam(ChatColor.RED), new TeamAltar(this, Host.GetTeam(ChatColor.RED), WorldData.GetDataLocs(DataLoc.RED_ALTAR.getKey()).get(0)));
	}
	
	@Override
	public String[] getTeamScoreboardAdditions(GameTeam team)
	{
		String skulls = "";
		for (int i = 1; i < 4; i++)
		{
			if (!skulls.equalsIgnoreCase(""))
				skulls = skulls + " ";
			
			if (_altars.get(team).getPlacedSkulls() >= i)
				skulls = skulls + ChatColor.GREEN + "☠";
			else
				skulls = skulls + ChatColor.GRAY + "☠";
		}
		
		return new String[] {"Skulls: " + skulls};
	}
	
	private Location getNearest(Location to, ConcurrentHashMap<Location, Double> options)
	{
		Location ret = null;
		double dist = Double.MAX_VALUE;
		
		for (Location check : options.keySet())
		{
			if (options.get(check) < dist)
			{
				ret = check;
				dist = options.get(check);
			}
		}
		
		return ret;
	}
	
	private LinkedList<Location> getWaypoints(Location altar)
	{
		LinkedList<Location> waypoints = new LinkedList<Location>();
		ConcurrentHashMap<Location, Double> distances = new ConcurrentHashMap<Location, Double>();
		
		for (Location loc : WorldData.GetDataLocs(DataLoc.WITHER_WAYPOINT.getKey()))
		{
			distances.put(loc, altar.distance(loc));
		}
		
		while (distances.size() >= 1)
		{
			Location act = getNearest(altar, distances);
			waypoints.add(act);
			distances.remove(act);
		}
		
		return waypoints;
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (event.getType() != UpdateType.TICK)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (player.hasPotionEffect(PotionEffectType.WITHER))
				{
					player.removePotionEffect(PotionEffectType.WITHER);
					player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 6, 0));
				}
				if (!Host.IsAlive(player))
					continue;
				/*GameTeam enemy = null;
				if (Host.GetTeam(player).GetColor() == ChatColor.RED)
				{
					enemy = Host.GetTeam(ChatColor.AQUA);
				}
				else
				{
					enemy = Host.GetTeam(ChatColor.RED);
				}
				if (UtilMath.offset(player, Host.getActiveTower(enemy).getEntity()) <= 7)
				{
					for (Location loc : UtilShapes.getCircle(player.getEyeLocation(), true, 3))
					{
						if (new Random().nextInt(5) <= 3)
						{
							UtilParticle.PlayParticle(ParticleType.DRIP_LAVA, loc, null, 0, 2, ViewDist.SHORT, player);
						}
					}
				}*/
			}
			if (_pathfinder != null)
				if (_pathfinder.update())
				{
					GameTeam team = _pathfinder.getTeam();
					_pathfinder = null;
					WitherSpawned = false;
					_wowner = null;
					_skellyMan.onWitherDeath();
					UtilTextMiddle.display("", team.GetColor() + team.getDisplayName() + "'s Wither has been Defeated!");
					//Host.Objective.resetTeamToMainObjective(Host.GetTeam(ChatColor.RED));
					//Host.Objective.resetTeamToMainObjective(Host.GetTeam(ChatColor.AQUA));
				}
		}
	}
	
	@EventHandler
	public void onWitherSpawn(EntitySpawnEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (event.getEntity() instanceof Wither)
		{
			if (WitherSpawned)
			{
				event.setCancelled(true);
				return;
			}
			
			for (GameTeam team : _altars.keySet())
			{
				GameTeam enemy = null;
				if (team.GetColor() == ChatColor.AQUA)
				{
					enemy = Host.GetTeam(ChatColor.RED);
				}
				else if (team.GetColor() == ChatColor.RED)
				{
					enemy = Host.GetTeam(ChatColor.AQUA);
				}
				if (_altars.get(team).ownsWither(event))
				{
					if (Host.getTowerManager().getAmountAlive(enemy) < 1)
					{
						event.setCancelled(true);
						return;
					}
					WitherSpawned = true;
					_wowner = team;
					_wither = (Wither)event.getEntity();
					_wither.setCustomName(team.GetColor() + team.getDisplayName() + "'s Wither");
					_wither.setCustomNameVisible(true);
					UtilTextMiddle.display("", C.cWhite + team.getDisplayName() + " Team has spawned a Wither Boss!", UtilServer.getPlayers());
					_pathfinder = new WitherPathfinder(this, _wither, getWaypoints(_altars.get(team).getLocation()), team, enemy, Host.getTowerManager());
					_skellyMan.onWitherSpawn();
					//Host.Objective.setMainObjective(new GearObjective());
					//Host.Objective.setTeamObjective(team, new WitherObjective("Attack Enemy Towers"));
					//Host.Objective.setTeamObjective(enemy, new WitherObjective("Kill Wither"));
					/*for (Player player : Host.GetPlayers(true))
					{
						Host.Objective.resetPlayerToMainObjective(player);
					}*/
					Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
					{
						public void run()
						{
							Host.CreatureAllowOverride = false;
						}
					}, 20 * 3);
					Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
					{
						public void run()
						{
							_altars.get(team).spawnSoulsand();
						}
					}, 20 * 35);
				}
			}
			
			if (_wither == null)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void handleWitherDrops(EntityDeathEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (event.getEntity() instanceof Wither)
		{
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}
	
	//@SuppressWarnings("deprecation")
	@EventHandler
	public void onExplode(EntityExplodeEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (event.getEntity() instanceof Wither)
		{
			event.blockList().clear();
			return;
		}
		if (event.getEntity() instanceof WitherSkull)
		{
			event.setCancelled(true);
			
			if (!((WitherSkull)event.getEntity()).isCharged())
				return;
			
			LinkedList<TeamTowerBase> red = Host.getTowerManager().getTeamTowers(Host.GetTeam(ChatColor.RED));
			LinkedList<TeamTowerBase> blue = Host.getTowerManager().getTeamTowers(Host.GetTeam(ChatColor.AQUA));
			HashMap<Block, Double> inside = UtilBlock.getInRadius(event.getLocation().getBlock(), 4, false);
			
			double dmg = 10 * (Host.GetTeam(ChatColor.RED).GetPlayers(true).size() + Host.GetTeam(ChatColor.AQUA).GetPlayers(true).size());
			for (TeamTowerBase tb : red)
			{
				if (inside.containsKey(tb.getLocation().getBlock()))
				{
					tb.damage(dmg, null);
					Host.Alert.alert(tb.getTeam(), tb);
				}
			}
			for (TeamTowerBase tb : blue)
			{
				if (inside.containsKey(tb.getLocation().getBlock()))
				{
					tb.damage(dmg, null);
					Host.Alert.alert(tb.getTeam(), tb);
				}
			}
			for (Block b : inside.keySet())
			{
				if (inside.get(b) > 2)
					continue;
				
				for (TeamAltar altar : _altars.values())
				{
					if (altar.isInsideAltar(b.getLocation()))
						continue;
				}
				for (GameTeam owner : Host.GetTeamList())
				{
					for (TeamTowerBase tb : Host.getTowerManager().getTeamTowers(owner))
					{
						if (!tb.Vulnerable)
							continue;
						
						if (tb.getLocation().distance(b.getLocation()) < 7)
						{						
							if (b.getType() == Material.BEDROCK)
								continue;
							if (b.getType() == Material.OBSIDIAN)
							{
								if (new Random().nextDouble() > (.75 / 2))
									continue;
							}
							if (b.getType() == Material.AIR)
								continue;
							if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER)
								continue;
							if (b.getType() == Material.STATIONARY_LAVA || b.getType() == Material.LAVA)
								continue;
							//WorldData.World.dropItem(b.getLocation(), new ItemBuilder(b.getType()).setData(b.getData()).build());
							b.setType(Material.AIR);
						}
					}
				}
			}
		}
		else if (event.getEntity() instanceof TNTPrimed)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event)
	{
		if (!Host.IsLive())
			return;
		
		for (TeamAltar altar : _altars.values())
		{
			if (!altar.canPlace(event.getPlayer(), event.getBlock().getType(), event.getBlock().getLocation(), true))
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event)
	{
		if (!Host.IsLive())
			return;
		
		for (TeamAltar altar : _altars.values())
		{
			if (!altar.canBreak(event.getPlayer(), event.getBlock(), true))
				event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handlePlace(PlayerBucketEmptyEvent event)
	{
		if (!Host.IsLive())
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		
		for (TeamAltar altar : _altars.values())
		{
			if (!altar.canPlace(event.getPlayer(), Material.WATER, block.getLocation(), true))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handleBreak(PlayerBucketEmptyEvent event)
	{
		if (!Host.IsLive())
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		
		for (TeamAltar altar : _altars.values())
		{
			if (!altar.canBreak(event.getPlayer(), block, true))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onShoot(ProjectileLaunchEvent event)
	{
		if (!Host.IsLive())
			return;
		
		if (event.getEntity()instanceof WitherSkull)
		{
			final WitherSkull entity = (WitherSkull)event.getEntity();
			if (entity.isCharged())
			{
				event.setCancelled(true);
			}
			else
			{
				if (!((Wither)entity.getShooter()).hasMetadata("Shooting"))
					event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void preserveAltars(EntityChangeBlockEvent event)
	{
		if (!Host.IsLive())
			return;
		
		for (TeamAltar altar : _altars.values())
		{
			if (altar.isInsideAltar(event.getBlock().getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void protectWither(EntityDamageByEntityEvent event)
	{
		if (!Host.IsLive())
			return;
		if (!WitherSpawned)
			return;
		
		if (event.getEntity() instanceof Wither)
		{
			if (event.getDamager() instanceof Player)
			{
				Player dmg = (Player)event.getDamager();
				if (_wowner.HasPlayer(dmg))
				{
					event.setCancelled(true);
					return;
				}
			}
			if (event.getDamager() instanceof Projectile)
			{
				if (((Projectile)event.getDamager()).getShooter() instanceof Player)
				{
					Player dmg = (Player) ((Projectile)event.getDamager()).getShooter();
					if (_wowner.HasPlayer(dmg))
					{
						event.setCancelled(true);
						return;
					}
				}
			}
			event.setDamage(event.getDamage() * .8);
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerGameRespawnEvent event)
	{
		if (!Host.IsLive())
			return;
		
		Manager.GetCondition().Clean(event.GetPlayer());
		/*if (Host.Objective.getPlayerObjective(event.GetPlayer()).equals("RETURN_SKULL"))
		{
			Host.Objective.resetPlayerToMainObjective(event.GetPlayer());
		}*/
	}
}
