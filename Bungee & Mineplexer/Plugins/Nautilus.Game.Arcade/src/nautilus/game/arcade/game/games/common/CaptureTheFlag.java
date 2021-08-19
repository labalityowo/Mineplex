package nautilus.game.arcade.game.games.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.RadarData;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilRadar;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerKitApplyEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.champions.ChampionsCTF;
import nautilus.game.arcade.game.games.common.ctf_data.CarrierCombatDeathEvent;
import nautilus.game.arcade.game.games.common.ctf_data.Flag;
import nautilus.game.arcade.game.games.common.dominate_data.PlayerData;
import nautilus.game.arcade.game.games.common.dominate_data.Resupply;
import nautilus.game.arcade.game.modules.SpawnRegenerationModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class CaptureTheFlag extends TeamGame
{
	public enum Perm implements Permission
	{
		SUDDEN_DEATH_COMMAND,
	}

	//Map Data
	private Location _redFlag;
	private Location _blueFlag;
	private final List<Flag> _flags = new ArrayList<>();
	private final List<Resupply> _resupply = new ArrayList<>();
	
	//Stats
	private final Map<String, PlayerData> _stats = new HashMap<>();
	
	//Captures
	private static final int _victoryCaps = 5;
	private int _redScore = 0, _blueScore = 0;
	
	//Times
	private long _gameTime = 420000;
	
	//protected String[] _blockedItems = new String[] {"SWORD", "AXE", "BOW"};
	private boolean _suddenDeath = false;
	
	private final Map<Player, List<ItemStack>> _hotbars = new HashMap<>();
	private final Map<Player, ItemStack> _helmets = new HashMap<>();
	
	private boolean _flickerStage = false;
	
	public CaptureTheFlag(ArcadeManager manager, GameType type, Kit[] kits)
	{ 
		super(manager, type, kits,   
   
						new String[]
								{ 
				"Capture The Opponent's Flag", 
				"First team to " + _victoryCaps + " Captures",
				"Or with the most Captures after 7 minutes wins"

							 	});

		this.DeathOut = false;
		this.PrepareFreeze = true;  
		this.HungerSet = 20; 
		this.WorldTimeSet = 2000;

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
		
		new SpawnRegenerationModule()
				.register(this);
  
		this.DeathSpectateSecs = 10;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.SUDDEN_DEATH_COMMAND, true, true);
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QA.setPermission(Perm.SUDDEN_DEATH_COMMAND, true, true);
		}
	}

	@Override
	public void ParseData()
	{
		_redFlag = WorldData.GetDataLocs("RED").get(0);
		_blueFlag = WorldData.GetDataLocs("BLUE").get(0);

		for (GameTeam team : GetTeamList())
		{
			if (team.GetColor() == ChatColor.RED || team.GetColor() == ChatColor.BLUE || team.GetColor() == ChatColor.AQUA)
			{
				_flags.add(new Flag(this, team.GetColor() == ChatColor.RED ? _redFlag : _blueFlag, team));
			}
		}
		
		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			_resupply.add(new Resupply(this, loc));
		}

		//Kit spawning

		if (this instanceof ChampionsCTF)
		{
			CreatureAllowOverride = true;
			for (int i = 0; i < GetKits().length && i < WorldData.GetDataLocs("LIGHT_BLUE").size() && i < WorldData.GetDataLocs("PINK").size(); i++)
			{
				GetKits()[i].getGameKit().createNPC(WorldData.GetDataLocs("PINK").get(i));
				GetKits()[i].getGameKit().createNPC(WorldData.GetDataLocs("LIGHT_BLUE").get(i));
			}
			CreatureAllowOverride = false;
		}

		//End kit spawning
	}
	
	@EventHandler
	public void customTeamGeneration(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			if (team.GetColor() == ChatColor.AQUA)
			{
				team.SetColor(ChatColor.BLUE);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockLiquidFlow(BlockPhysicsEvent event)
	{
		Material matOfBlock = event.getBlock().getType();

		if (matOfBlock == Material.STATIONARY_WATER || matOfBlock == Material.SAND || matOfBlock == Material.GRAVEL || matOfBlock == Material.STATIONARY_LAVA)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTNTExplode(EntityExplodeEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getEntityType() == EntityType.PRIMED_TNT)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void updates(UpdateEvent event)
	{  
		if (!IsLive())
		{
			return;
		}
		
		if (event.getType() == UpdateType.FASTEST)
		{
			for (Flag flag : _flags)
			{
				flag.update();
			}
			
			Iterator<Player> hotbarIter = _hotbars.keySet().iterator();
			while (hotbarIter.hasNext())
			{
				Player player = hotbarIter.next();
				
				if (!player.isOnline() || UtilPlayer.isSpectator(player) || !IsAlive(player))
				{
					hotbarIter.remove();
				}
			}
			
			Iterator<Player> helmetIter = _helmets.keySet().iterator();
			while (helmetIter.hasNext())
			{
				Player player = helmetIter.next();
				
				if (!player.isOnline() || UtilPlayer.isSpectator(player) || !IsAlive(player))
				{
					helmetIter.remove();
				}
			}
		}
		else if (event.getType() == UpdateType.TICK)
		{
			for (Flag flag : _flags)
			{
				if (flag.getCarrier() == null)
				{
					for (Player player : GetPlayers(true))
					{
						if (UtilMath.offset2d(player.getLocation(), flag.getPlacedLocation()) < .5 && player.getLocation().getY() > flag.getPlacedLocation().getY() - 2 && player.getLocation().getY() < flag.getPlacedLocation().getY() + 3)
						{
							flag.pickup(player);
						}
					}
				}
			}
		}
		else if (event.getType() == UpdateType.FAST)
		{
			for (Resupply resupply : _resupply)
			{
				resupply.Update();
			}
			
			getFlag(true).handleBottomInfo(_flickerStage, ChatColor.RED);
			getFlag(false).handleBottomInfo(_flickerStage, ChatColor.AQUA);
		}
		else if (event.getType() == UpdateType.SEC)
		{
			for (Flag flag : _flags)
			{
				flag.handleRespawn();
			}
		}
		else
		{
			progressTime();
		}
	}
	
	public void progressTime()
	{
		if (_suddenDeath) return;
		
		long remain = _gameTime - (System.currentTimeMillis() - GetStateTime());
		
		if (remain <= 0)
		{
			if (_redScore > _blueScore)
			{
				endCheckScore(_redScore);
			}
			else if(_redScore < _blueScore)
			{
				endCheckScore(_blueScore);
			}
			else
			{
				_suddenDeath = true;
				this.DeathOut = true;
				UtilTextMiddle.display(C.cYellow + "Sudden Death", "Next Capture Wins! No Respawns!");
				return;
			}	
		}
	}
	
	public void addCapture(GameTeam team)
	{
		if (_suddenDeath)
		{
			if (team.GetColor() == ChatColor.RED)
			{
				_redScore = _redScore + 1;
				endCheckScore(_redScore);
			}
			else
			{
				_blueScore = _blueScore + 1;
				endCheckScore(_blueScore);
			}
		}
		else
		{
			if (team.GetColor() == ChatColor.RED)
			{
				_redScore = Math.min(_victoryCaps, _redScore + 1);
			}
			else
			{
				_blueScore = Math.min(_victoryCaps, _blueScore + 1);
			}
			
			endCheckScore(_victoryCaps);
		}
	}
	
	public void saveInventory(Player player)
	{
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < 9; i++)
		{
			if (player.getInventory().getItem(i) != null)
			{
				items.add(i, player.getInventory().getItem(i));
			}
			else
			{
				items.add(i, new ItemStack(Material.AIR));
			}
		}
		_hotbars.put(player, items);
		
		_helmets.put(player, player.getInventory().getHelmet());
	}
	
	public void resetInventory(Player player)
	{
		if (_hotbars.containsKey(player))
		{
			List<ItemStack> items = _hotbars.get(player);
			for (int i = 0; i < items.size(); i++)
			{
				if (items.get(i).getType() != Material.AIR)
				{
					player.getInventory().setItem(i, items.get(i));
				}
				else
				{
					player.getInventory().setItem(i, new ItemStack(Material.AIR));
				}
			}
			getArcadeManager().getClassManager().hideNextEquipMessage(player.getName());
			player.getInventory().setHelmet(_helmets.get(player));
			//getArcadeManager().getClassManager().toggleMessage(player.getName(), false);
			player.updateInventory();
			player.removePotionEffect(PotionEffectType.SLOW);
			_hotbars.remove(player);
			_helmets.remove(player);
		}
	}
	
	public boolean isAtHome(ChatColor team)
	{
		for (Flag flag : _flags)
		{
			if (flag.getTeam().GetColor() == team)
			{
				return flag.isAtHome();
			}
		}
		return false;
	}
	
	public Location getRedFlag()
	{
		return _redFlag;
	}
	
	public Location getBlueFlag()
	{
		return _blueFlag;
	}
	
	public boolean isInZone(Location location, boolean red)
	{
		if (red)
		{
			if (UtilMath.offset(location, _redFlag) < 4)
			{
				return true;
			}
		}
		else
		{
			if (UtilMath.offset(location, _blueFlag) < 4)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isSuddenDeath()
	{
		return _suddenDeath;
	}
	
	private boolean isEnemyCarrier(Player check, Player stat)
	{
		if (GetTeam(check).GetColor() == GetTeam(stat).GetColor()) return false;
		
		for (Flag f : _flags)
		{
			if (f.getCarrier() == null) continue;
			if (f.getCarrier().getName().equalsIgnoreCase(check.getName())) return true;
		}
		
		return false;
	}
	
	private String getVisualResult(ChatColor base)
	{
		String newc = base.toString();
		
		if (base == ChatColor.BLUE)
		{
			newc = C.cDBlue;
		}
		
		if (base == ChatColor.RED)
		{
			newc = C.cDRed;
		}
		
		return newc;
	}
	
	private Flag getFlag(boolean red)
	{
		if (red)
		{
			for (Flag f : _flags)
			{
				if (f.getTeam().GetColor() == ChatColor.RED)
				{
					return f;
				}
			}
		}
		else
		{
			for (Flag f : _flags)
			{
				if (f.getTeam().GetColor() == ChatColor.BLUE)
				{
					return f;
				}
			}
		}
		return null;
	}
	
	public List<Location> getLocations(boolean base)
	{
		List<Location> locs = new ArrayList<>();
		if (base)
		{
			locs.add(_blueFlag);
			locs.add(_redFlag);
		}
		else
		{
			locs.add(getFlag(true).getPlacedLocation());
			locs.add(getFlag(false).getPlacedLocation());
		}
		
		return locs;
	}
	
	public int getScoreDifference()
	{
		return Math.abs(_blueScore - _redScore);
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		scoreboardWrite();
	}
	
	private void scoreboardWrite() 
	{
		if (!InProgress())
		{
			return;
		}

		//Wipe Last
		Scoreboard.reset();

		//Scores		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cRedB + "Red Team");
		Scoreboard.write(_redScore + " Captures");
		
		String redMessage = "Flag Dropped";
		if (getFlag(true).isAtHome())
		{
			redMessage = "Flag Safe";
		}
		
		if (getFlag(true).getCarrier() != null)
		{
			redMessage = "Flag Taken";
		}
		
		if (_flickerStage)
		{
			if (!getFlag(true).isAtHome())
			{
				Scoreboard.write(C.cRed + redMessage);
			}
			else
			{
				Scoreboard.write(redMessage);
			}
		}
		else
		{
			Scoreboard.write(redMessage);
		}
		//Flag in play stuff
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cAquaB + "Blue Team");
		Scoreboard.write(_blueScore + " Captures");
		
		String blueMessage = "Flag Dropped";
		if (getFlag(false).isAtHome())
		{
			blueMessage = "Flag Safe";
		}
		
		if (getFlag(false).getCarrier() != null)
		{
			blueMessage = "Flag Taken";
		}
		
		if (_flickerStage)
		{
			if (!getFlag(false).isAtHome())
			{
				Scoreboard.write(C.cAqua + blueMessage);
			}
			else
			{
				Scoreboard.write(blueMessage);
			}
		}
		else
		{
			Scoreboard.write(blueMessage);
		}
		
		_flickerStage = !_flickerStage;
		
		//Flag in play stuff
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellowB + "Score to Win");
		Scoreboard.write(_victoryCaps + " Captures");

		Scoreboard.writeNewLine();
		if (_suddenDeath)
		{
			Scoreboard.write(C.cYellowB + "Sudden Death");
			Scoreboard.write("Next Cap Wins");
			Scoreboard.write("No Respawns");
		}
		else
		{
			Scoreboard.write(C.cYellowB + "Time Left");
			Scoreboard.write(UtilTime.MakeStr(Math.max(_gameTime - (System.currentTimeMillis() - GetStateTime()), 0)) + C.cWhite);
		}
		
		Scoreboard.draw();
	}
	
	public void endCheckScore(int needed)   
	{
		if (!IsLive())
		{
			return;
		}
		
		GameTeam winner = null;
 
		if (_redScore >= needed)
		{
			winner = GetTeam(ChatColor.RED);
		}
		else if (_blueScore >= needed)
		{
			winner = GetTeam(ChatColor.BLUE);
		}

		if (winner == null)
		{
			return;
		}
 
		scoreboardWrite();

		//Announce
		AnnounceEnd(winner);

		for (GameTeam team : GetTeamList())
		{
			if (WinnerTeam != null && team.equals(WinnerTeam))
			{
				for (Player player : team.GetPlayers(false))
				{
					AddGems(player, 10, "Winning Team", false, false);
				}
			}

			for (Player player : team.GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
				}
			}
		}
		endElo();
 
		//End
		SetState(GameState.End);
	}
	
	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (assist)
		{
			return .5;
		}
		
		if (_suddenDeath)
		{
			return 2;
		}
		
		return 1; 
	}
	
	@Override
	public String GetMode()
	{
		return "Capture the Flag";
	}

	public PlayerData getStats(Player player)
	{
		if (!_stats.containsKey(player.getName()))
		{
			_stats.put(player.getName(), new PlayerData(player.getName()));
		}
		
		return _stats.get(player.getName());
	}
	
	@EventHandler
	public void statsKillAssistDeath(CombatDeathEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;
		
		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}
		
		Player killed = (Player)event.GetEvent().getEntity();
		getStats(killed).Deaths++;

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			if (killer != null && !killer.equals(killed))
			{
				getStats(killer).Kills++;
			}
		}

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
			if (event.GetLog().GetKiller() != null && log.equals(event.GetLog().GetKiller()))
			{
				continue;
			}

			Player assist = UtilPlayer.searchExact(log.GetName());

			//Assist
			if (assist != null)
			{
				getStats(assist).Assists++;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void statsKillAssistDeath(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);
		if (damager != null)
		{
			getStats(damager).DamageDealt += event.GetDamage();
		}
		
		Player damagee = event.GetDamageePlayer();
		if (damagee != null)
		{
			getStats(damagee).DamageTaken += event.GetDamage();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void usableInteract(PlayerInteractEvent event)
	{
		if (UtilBlock.usable(event.getClickedBlock()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void selectKit(PlayerKitApplyEvent event)
	{
		if (this instanceof ChampionsCTF)
		{
			for (Flag flag : _flags)
			{
				if (!flag.isAtHome())
				{
					if (flag.getCarrier() != null)
					{
						if (flag.getCarrier().getName().equals(event.getPlayer().getName()))
						{
							event.setCancelled(true);
						}
					}
				}
			}
			
			if (_suddenDeath)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void dropFlag(PlayerDropItemEvent event)
	{
		for (Flag flag : _flags)
		{
			if ((flag.getRepresentation().getType() == event.getItemDrop().getItemStack().getType()) || (event.getItemDrop().getItemStack().getType() == Material.COMPASS))
			{
				if (flag.getCarrier() != null)
				{
					if (flag.getCarrier().getName().equals(event.getPlayer().getName()))
					{
						event.setCancelled(false);
						event.getItemDrop().remove();
						flag.drop(event.getPlayer());
						resetInventory(event.getPlayer());
					}
				}
			}
		}
	}
	
	@EventHandler 
	public void radarUpdate(UpdateEvent event)    
	{
		if (!InProgress())
		{
			return;
		}
		
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		List<RadarData> data = new ArrayList<>();
		
		for (Flag f : _flags)
		{
			if (f.getCarrier() == null)
			{
				data.add(new RadarData(f.getPlacedLocation(), f.getTeam().GetColor() + "⚑"));
			}
			else
			{
				data.add(new RadarData(f.getCarrier().getLocation(), getVisualResult(f.getTeam().GetColor()) + "⚑"));
			}
		}
		
		data.add(new RadarData(_redFlag, C.cRed + "☗"));
		data.add(new RadarData(_blueFlag, C.cBlue + "☗"));
		
		for (Player player : UtilServer.getPlayers())
		{
			UtilRadar.displayRadar(player, data);
		}
	}
	
	@EventHandler
	public void useCmd(PlayerCommandPreprocessEvent event)    
	{
		if (!InProgress())
		{
			return;
		}
		
		if (event.getMessage().contains("/suddendeath"))
		{
			boolean authorized = Manager.GetClients().Get(event.getPlayer()).hasPermission(Perm.SUDDEN_DEATH_COMMAND);
			
			if (Manager.GetGameHostManager().isPrivateServer())
			{
				if (Manager.GetGameHostManager().isAdmin(event.getPlayer(), false))
				{
					authorized = true;
				}
			}
			
			if (authorized)
			{
				if (_redScore < _blueScore)
				{
					_redScore = _blueScore;
				}
				else
				{
					_blueScore = _redScore;
				}
				
				_gameTime = (System.currentTimeMillis() - GetStateTime()) + 11;
				for (Player pl : UtilServer.getPlayers())
				{
					UtilPlayer.message(pl, C.cYellowB + event.getPlayer().getName() + " has equalized both teams and enabled Sudden Death!");
				}
			}
			else
			{
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You do not have permission to use this game modifier!"));
			}
			
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void powerupPickup(PlayerPickupItemEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		
		for (Flag f : _flags)
		{
			if (f.getCarrier() == event.getPlayer())
			{
				return;
			}
		}
		
		if (_suddenDeath)
		{
			return;
		}
		
		for (Resupply resupply : _resupply)
		{
			resupply.Pickup(event.getPlayer(), event.getItem());
		}
	}
	
	//Dont allow powerups to despawn
	@EventHandler
	public void itemDespawn(ItemDespawnEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDie(CombatDeathEvent event)
	{
		Player player = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (event.GetLog().GetKiller() != null)
		{
			if (event.GetLog().GetKiller().IsPlayer())
			{
				Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
				if (isEnemyCarrier(player, killer))
				{
					CarrierCombatDeathEvent e = new CarrierCombatDeathEvent(player, killer);
					Bukkit.getPluginManager().callEvent(e);
				}
			}
		}
		
		for (Flag flag : _flags)
		{
			flag.drop(player);
		}
		
		getArcadeManager().getClassManager().forceRemoveFromSuppressed(player.getName());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		for (Flag flag : _flags)
		{
			flag.drop(event.getPlayer());
		}
		
		getArcadeManager().getClassManager().forceRemoveFromSuppressed(event.getPlayer().getName());
	}
	
	@EventHandler
	public void equalizeBrute(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() == null)
		{
			return;
		}
		
		boolean carrier = false;
		for (Flag flag : _flags)
		{
			if (flag.getCarrier() != null)
			{
				if (flag.getCarrier().getName().equals(event.GetDamageePlayer().getName()))
				{
					carrier = true;
				}
			}
		}
		
		if (carrier)
		{
			if (event.GetDamageePlayer().getInventory().getChestplate() != null)
			{
				if (event.GetDamageePlayer().getInventory().getChestplate().getType() == Material.DIAMOND_CHESTPLATE)
				{
					event.SetBrute();
				}
			}
		}
	}
}