package nautilus.game.arcade.game.games.minecraftleague.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.DataLoc;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.world.WorldData;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TowerManager implements Listener
{
	public MinecraftLeague Host;
	private ConcurrentHashMap<TeamTowerBase, GameTeam> _towers = new ConcurrentHashMap<TeamTowerBase, GameTeam>();
	private ConcurrentHashMap<GameTeam, Integer> _vulnerableTower = new ConcurrentHashMap<GameTeam, Integer>();
	private ConcurrentHashMap<TeamTowerBase, DefenderAI> _def = new ConcurrentHashMap<TeamTowerBase, DefenderAI>();
	//private ConcurrentHashMap<TeamTowerBase, MapZone> _mapZone = new ConcurrentHashMap<TeamTowerBase, MapZone>();
	private ConcurrentHashMap<GameTeam, TeamBeacon> _beacons = new ConcurrentHashMap<GameTeam, TeamBeacon>();
	private HashSet<UUID> _waitingPlayers = new HashSet<UUID>();
	private OreGenerator _ore;
	public boolean Attack = false;
	
	public TowerManager(MinecraftLeague host)
	{
		Host = host;
		_ore = new OreGenerator();
	}
	
	private void makeVulnerable(TeamTowerBase base)
	{
		if (base instanceof TeamTower)
			_vulnerableTower.put(base.getTeam(), ((TeamTower)base).Number);
		else
			_vulnerableTower.put(base.getTeam(), 3);
		
		base.Vulnerable = true;
		_beacons.get(base.getTeam()).setBlock(base.getBeacon().getBlock());
	}
	
	public void ironOreGen(GameTeam team, boolean start)
	{
		int amount = 20;
		if (start)
			amount = 50;
		
		for (Location loc : Host.WorldData.GetCustomLocs(DataLoc.DIAMOND_ORE.getKey()))
		{
			loc.getBlock().setType(Material.DIAMOND_ORE);
		}
		
		if (start)
		{
			if (team.GetColor() == ChatColor.RED)
				_ore.generateOre(Material.IRON_ORE, Host.WorldData.GetCustomLocs(DataLoc.RED_ORE.getKey()), amount);
			else
				_ore.generateOre(Material.IRON_ORE, Host.WorldData.GetCustomLocs(DataLoc.BLUE_ORE.getKey()), amount);
			
			for (Location loc : Host.WorldData.GetCustomLocs(DataLoc.MOSH_IRON.getKey()))
			{
				loc.getBlock().setType(Material.STONE);
			}
		}
		else
		{
			_ore.generateOre(Material.IRON_ORE, Host.WorldData.GetCustomLocs(DataLoc.MOSH_IRON.getKey()), amount);
			UtilTextMiddle.display("", "Valuable Ores have respawned in the middle!");
		}
	}

	private List<TeamTowerBase> getAllTeamTowers(GameTeam team)
	{
		List<TeamTowerBase> ret = new ArrayList<TeamTowerBase>();

		for (TeamTowerBase tower : _towers.keySet())
		{
			if (_towers.get(tower).GetColor() == team.GetColor())
			{
				ret.add(tower);
			}
		}

		return ret;
	}
	
	public LinkedList<TeamTowerBase> getTeamTowers(GameTeam team)
	{
		LinkedList<TeamTowerBase> ret = new LinkedList<TeamTowerBase>();
		TeamTower one = null;
		TeamTower two = null;
		TeamCrystal three = null;
		
		for (TeamTowerBase tower : getAllTeamTowers(team))
		{
			if (tower instanceof TeamCrystal)
			{
				three = (TeamCrystal) tower;
				continue;
			}
			if (one == null)
			{
				one = (TeamTower) tower;
				continue;
			}
			if (one.Number > ((TeamTower)tower).Number)
			{
				two = one;
				one = (TeamTower) tower;
				continue;
			}
			two = (TeamTower) tower;
		}
		
		ret.add(one);
		ret.add(two);
		ret.add(three);
		
		return ret;
	}
	
	public Integer getAmountAlive(GameTeam team)
	{
		int i = 0;
		
		for (TeamTowerBase tower : getAllTeamTowers(team))
		{
			if (tower.Alive)
				i++;
		}
		
		return i;
	}
	
	public TeamTowerBase getVulnerable(GameTeam team)
	{
		return getTeamTowers(team).get(_vulnerableTower.get(team) - 1);
	}
	
	public void parseTowers(WorldData data)
	{
		GameTeam red = Host.GetTeam(ChatColor.RED);
		GameTeam blue = Host.GetTeam(ChatColor.AQUA);
		//int[] redRGB = new int[] {255, 0, 0};
		//int[] blueRGB = new int[] {0, 0, 255};
		
		_towers.put(new TeamTower(Host, this, red, data.GetCustomLocs(DataLoc.RED_TOWER.getKey() + " 1").get(0), 1), red);
		_towers.put(new TeamTower(Host, this, red, data.GetCustomLocs(DataLoc.RED_TOWER.getKey() + " 2").get(0), 2), red);
		_towers.put(new TeamCrystal(Host, this, red, data.GetCustomLocs(DataLoc.RED_CRYSTAL.getKey()).get(0)), red);
		
		_towers.put(new TeamTower(Host, this, blue, data.GetCustomLocs(DataLoc.BLUE_TOWER.getKey() + " 1").get(0), 1), blue);
		_towers.put(new TeamTower(Host, this, blue, data.GetCustomLocs(DataLoc.BLUE_TOWER.getKey() + " 2").get(0), 2), blue);
		_towers.put(new TeamCrystal(Host, this, blue, data.GetCustomLocs(DataLoc.BLUE_CRYSTAL.getKey()).get(0)), blue);
		
		for (TeamTowerBase tower : _towers.keySet())
		{
			_def.put(tower, new DefenderAI(this, tower));
			/*int[] rgb;
			if (tower.getGameTeam().GetColor() == red.GetColor())
				rgb = redRGB;
			else
				rgb = blueRGB;
			
			MapZone zone = new MapZone(tower.getLocation(), rgb);
			Host.MapZones.add(zone);
			_mapZone.put(tower, zone);*/
		}
		
		TeamBeacon redb = new TeamBeacon(red, getTeamTowers(red).getFirst().getBeacon().getBlock());
		_beacons.put(red, redb);
		Host.Beacons.put(red, redb);
		TeamBeacon blueb = new TeamBeacon(blue, getTeamTowers(blue).getFirst().getBeacon().getBlock());
		_beacons.put(blue, blueb);
		Host.Beacons.put(blue, blueb);
		makeVulnerable(getTeamTowers(red).getFirst());
		makeVulnerable(getTeamTowers(blue).getFirst());
		ironOreGen(red, true);
		ironOreGen(blue, true);
	}
	
	public void prepareHealth(int players, double multiplier)
	{
		for (TeamTowerBase tower : _towers.keySet())
		{
			if (tower instanceof TeamCrystal)
				tower.setMaxHealth(players * multiplier);
			else
			{
				int divisor = 3 - ((TeamTower)tower).Number;
				double rawHealth = (.67 * multiplier) * players;
				Double health = new BigDecimal(rawHealth / divisor).intValue() * 1D;
				tower.setMaxHealth(health);
			}
		}
	}
	
	public void handleTowerDeath(TeamTowerBase towerBase)
	{
		towerBase.setVulnerable(false);
		//ironOreGen(towerBase.getGameTeam());
		/*for (Player player : towerBase.getGameTeam().GetPlayers(true))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 60, 1));
		}*/
		/*Bukkit.getScheduler().runTaskLater(Host.Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				UtilTextMiddle.display("", towerBase.getGameTeam().GetColor() + towerBase.getGameTeam().getName() + " Team ores have been replenished!", UtilServer.getPlayers());
			}
		}, 20 * 5);*/
		//_mapZone.get(towerBase).setValid(false);
		if (towerBase instanceof TeamCrystal)
		{
			GameTeam enemy = null;
			if (towerBase.getTeam() == Host.GetTeam(ChatColor.RED))
				enemy = Host.GetTeam(ChatColor.AQUA);
			else
				enemy = Host.GetTeam(ChatColor.RED);
			
			Host.ScoreboardAutoWrite = false;
			Host.writeEndSb(enemy.GetColor() + enemy.getDisplayName());
			Host.AnnounceEnd(enemy);
			
			for (GameTeam team : Host.GetTeamList())
			{
				if (enemy != null && team.equals(enemy))
				{
					for (Player player : team.GetPlayers(false))
						Host.AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
						Host.AddGems(player, 10, "Participation", false, false);
			}
			Host.SetState(GameState.End);
			
			//Host.Objective.setTeamObjective(enemy, new KillObjective());
			
			//Host.TeamPoison.put(towerBase.getGameTeam(), System.currentTimeMillis() + (UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.MILLISECONDS) - UtilTime.convert(1, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)));
			
			return;
		}
		for (Player player : towerBase.getTeam().GetPlayers(true))
		{
			giveArmor(player);
		}
		makeVulnerable(getTeamTowers(towerBase.getTeam()).get(_vulnerableTower.get(towerBase.getTeam())));
	}
	
	public void giveArmor(Player player)
	{
		if (!UtilPlayer.isSpectator(player))
		{
			if (UtilItem.isLeatherProduct(player.getInventory().getHelmet()))
			{
				player.getInventory().setHelmet(new ItemBuilder(Material.GOLD_HELMET).setUnbreakable(true).build());
			}
			else if (UtilItem.isLeatherProduct(player.getInventory().getLeggings()))
			{
				player.getInventory().setLeggings(new ItemBuilder(Material.GOLD_LEGGINGS).setUnbreakable(true).build());
			}
			else if (UtilItem.isLeatherProduct(player.getInventory().getChestplate()))
			{
				player.getInventory().setChestplate(new ItemBuilder(Material.GOLD_CHESTPLATE).setUnbreakable(true).build());
			}
			else if (UtilItem.isLeatherProduct(player.getInventory().getBoots()))
			{
				player.getInventory().setBoots(new ItemBuilder(Material.GOLD_BOOTS).setUnbreakable(true).build());
			}
			_waitingPlayers.remove(player.getUniqueId());
		}
		else 
		{
			_waitingPlayers.add(player.getUniqueId());
		}
	}
	
	public void toggleAttack()
	{
		//Attack = !Attack;
	}
	
	public void update()
	{
		for (TeamTowerBase tower : _towers.keySet())
		{
			tower.update();
			_def.get(tower).update();
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.SEC)
			return;
		for(UUID uuid : _waitingPlayers)
		{
			Player player = Bukkit.getPlayer(uuid);
			if(UtilPlayer.isSpectator(player))
			{
				continue;
			}
			giveArmor(player);
		}
	}
}