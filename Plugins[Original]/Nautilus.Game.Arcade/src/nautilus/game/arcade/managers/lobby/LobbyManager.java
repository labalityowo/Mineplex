package nautilus.game.arcade.managers.lobby;

import java.io.File;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.Maps;

import mineplex.core.PlayerSelector;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlockText;
import mineplex.core.common.util.UtilBlockText.TextAlign;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilLambda;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.gadget.gadgets.item.ItemTrampoline;
import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.event.KitNPCInteractEvent;
import mineplex.core.game.kit.event.KitSelectEvent;
import mineplex.core.scoreboard.MineplexScoreboard;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.LobbyEnt;
import nautilus.game.arcade.managers.voting.Vote;

public abstract class LobbyManager implements Listener
{

	protected static final World WORLD = Bukkit.getWorld("world");

	protected final ArcadeManager _manager;

	private final Map<GameTeam, Location> _teamLocations;
	private final Map<Entity, LobbyEnt> _teams;
	private final Map<Block, Material> _teamBlocks;
	private final Map<Block, Material> _kitBlocks;

	private int _advertiseStage = 0;
	private long _fireworkStart;
	private Color _fireworkColor;

	private Location _gameText;
	private Location _advText;
	private Location _kitText;
	private Location _teamText;
	private Location _carl;
	private Location _missions;
	private Location _spawn;
	private Location _ampStand;
	private boolean _generatePodiums;

	public LobbyManager(ArcadeManager manager, Location missions, Location carl, Location spawn, Location ampStand)
	{
		_manager = manager;

		WORLD.setTime(6000);
		WORLD.setStorm(false);
		WORLD.setThundering(false);
		WORLD.setGameRuleValue("doDaylightCycle", "false");

		setGameText(new Location(WORLD, 0, 110, 50));
		setKitText(new Location(WORLD, -40, 80, 0));
		setTeamText(new Location(WORLD, 40, 80, 0));
		setAdvText(new Location(WORLD, 0, 100, -60));

		_missions = missions;
		_carl = carl;
		_spawn = spawn;
		_ampStand = ampStand;
		_teamLocations = Maps.newHashMap();
		_teams = Maps.newHashMap();
		_teamBlocks = Maps.newHashMap();
		_kitBlocks = Maps.newHashMap();

		_generatePodiums = new File("world/GENPODIUMS.dat").exists() || manager.GetHost() != null;

		if (_generatePodiums)
		{
			System.out.println("Generating podiums via code for both teams and kits.");
		}

		manager.getPluginManager().registerEvents(this, manager.getPlugin());
		System.out.println("Using " + getClass().getSimpleName() + " as the lobby manager");
	}

	public abstract void createTeams(Game game);

	public abstract void createKits(Game game);

	public boolean isMPS()
	{
		return _manager.GetHost() != null;
	}

	public void writeGameLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_gameText.getWorld(), _gameText.getX(), _gameText.getY(), _gameText.getZ());

		if (line > 0)
		{
			loc.add(0, line * -6, 0);
		}

		BlockFace face = BlockFace.WEST;

		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	public void writeAdvertiseLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_advText.getWorld(), _advText.getX(), _advText.getY(), _advText.getZ());

		if (line > 0)
		{
			loc.add(0, line * -6, 0);
		}

		BlockFace face = BlockFace.EAST;

		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	public void writeKitLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_kitText.getWorld(), _kitText.getX(), _kitText.getY(), _kitText.getZ());

		if (line > 0)
		{
			loc.add(0, line * -6, 0);
		}

		BlockFace face = BlockFace.NORTH;

		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	public void writeTeamLine(String text, int line, int id, byte data)
	{
		Location loc = new Location(_teamText.getWorld(), _teamText.getX(), _teamText.getY(), _teamText.getZ());

		if (line > 0)
		{
			loc.add(0, line * -6, 0);
		}

		BlockFace face = BlockFace.SOUTH;

		UtilBlockText.MakeText(text, loc, face, id, data, TextAlign.CENTER);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void teamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		writeGameLine(event.GetGame().WorldData.MapName, event.GetGame().GetMode() == null ? 1 : 2, 159, (byte) 4);

		//Remove Old Ents
		getTeams().keySet().forEach(Entity::remove);
		getTeams().clear();

		//Remove Blocks
		getTeamBlocks().forEach(Block::setType);
		getTeamBlocks().clear();

		if (event.GetGame().HideTeamSheep)
		{
			return;
		}

		createTeams(event.GetGame());
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			UpdateEnts();
		}
		else if (event.getType() == UpdateType.FASTEST)
		{
			UpdateFirework();
			// TODO TEMPORARY INCREASE 100 -> 200. ALLOW CUSTOM BORDERS WHEN IF REWRITTEN
			PlayerSelector.selectPlayers(UtilLambda.and(PlayerSelector.inWorld(WORLD), UtilLambda.not(PlayerSelector.within(getSpawn(), 200))))
					.forEach(player -> player.teleport(getSpawn()));
		}
		else if (event.getType() == UpdateType.SLOW)
		{
			UpdateAdvertise();
		}

		ScoreboardDisplay(event);
	}

	@EventHandler
	public void onWeather(WeatherChangeEvent event)
	{
		if (!event.getWorld().equals(_spawn.getWorld()))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	private void RemoveInvalidEnts(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		for (Entity ent : WORLD.getEntities())
		{
			if (ent instanceof Creature || ent instanceof Slime)
			{
				if (_teams.containsKey(ent))
				{
					continue;
				}

				if (ent.getPassenger() != null)
				{
					continue;
				}

				ent.remove();
			}
		}
	}

	private void UpdateAdvertise()
	{
		if (_manager.GetGame() == null || !_manager.GetGame().inLobby())
		{
			return;
		}

		_advertiseStage = (_advertiseStage + 1) % 2;

		if (_manager.GetGame().AdvertiseText(this, _advertiseStage))
		{
			return;
		}

		if (_advertiseStage == 0)
		{
			writeAdvertiseLine("GET MINEPLEX ULTRA", 0, 159, (byte) 4);
			writeAdvertiseLine("FOR AMAZING", 1, 159, (byte) 15);
			writeAdvertiseLine("FUN TIMES", 2, 159, (byte) 15);

			writeAdvertiseLine("www.mineplex.com", 4, 159, (byte) 15);
		}
		else if (_advertiseStage == 1)
		{
			writeAdvertiseLine("KEEP CALM", 0, 159, (byte) 4);
			writeAdvertiseLine("AND", 1, 159, (byte) 15);
			writeAdvertiseLine("PLAY MINEPLEX", 2, 159, (byte) 4);

			writeAdvertiseLine("www.mineplex.com", 4, 159, (byte) 15);
		}
	}

	public void UpdateEnts()
	{
		for (Entity ent : _teams.keySet())
		{
			ent.teleport(_teams.get(ent).GetLocation());
		}
	}

	public GameTeam GetClickedTeam(Entity clicked)
	{
		for (LobbyEnt ent : _teams.values())
			if (clicked.equals(ent.GetEnt()))
			{
				return ent.GetTeam();
			}

		return null;
	}

	public void RegisterFireworks(GameTeam winnerTeam)
	{
		if (winnerTeam != null)
		{
			_fireworkColor = Color.GREEN;
			if (winnerTeam.GetColor() == ChatColor.RED)
			{
				_fireworkColor = Color.RED;
			}
			if (winnerTeam.GetColor() == ChatColor.AQUA)
			{
				_fireworkColor = Color.BLUE;
			}
			if (winnerTeam.GetColor() == ChatColor.YELLOW)
			{
				_fireworkColor = Color.YELLOW;
			}

			_fireworkStart = System.currentTimeMillis();
		}
	}

	private void UpdateFirework()
	{
		if (UtilTime.elapsed(_fireworkStart, 10000))
		{
			return;
		}

		UtilFirework.playFirework(getSpawn().clone().add(Math.random() * 160 - 80, 30 + Math.random() * 10, Math.random() * 160 - 80), Type.BALL_LARGE, _fireworkColor, false, false);
	}

	public void displayLast(Game game)
	{
		//Start Fireworks
		RegisterFireworks(game.WinnerTeam);

		_manager.getMineplexGameManager().clearKitNPCs();

		// Remove Old Kits
		getKitBlocks().forEach(Block::setType);
		getKitBlocks().clear();

		//Remove Old Ents
		getTeams().keySet().forEach(Entity::remove);
		getTeams().clear();

		//Remove Blocks
		getTeamBlocks().forEach(Block::setType);
		getTeamBlocks().clear();
	}

	public void displayNext(Game game)
	{
		TimingManager.start("displayNext");

		displayGame(game);
		displayWaiting(false);
		displayKitTeamText(game);

		UtilServer.getPlayersCollection().forEach(this::equipActiveKit);
		createKits(game);

		TimingManager.stop("displayNext");
	}

	public void displayGame(Game game)
	{
		writeGameLine(game.GetType().GetLobbyName(), 0, 159, (byte) 14);

		if (game.GetMode() == null)
		{
			writeGameLine("", 1, Material.AIR.getId(), (byte) 0);
		}
		else
		{
			writeGameLine(game.GetMode(), 1, 159, (byte) 1);
		}
	}

	public void displayKitTeamText(Game game)
	{
		writeKitLine("Select", 0, 159, (byte) 15);
		writeKitLine("Kit", 1, 159, (byte) 4);

		if (game.HideTeamSheep)
		{
			if (game.ReplaceTeamsWithKits)
			{
				writeTeamLine("Select", 0, 159, (byte) 15);
				writeTeamLine("Kit", 1, 159, (byte) 4);
			}
		}
		else
		{
			writeTeamLine("Select", 0, 159, (byte) 15);
			writeTeamLine("Team", 1, 159, (byte) 4);
		}
	}

	public void displayWaiting(boolean forVote)
	{
		if (forVote)
		{
			for (int i = 1; i < 3; i++)
			{
				writeGameLine("", i, Material.AIR.getId(), (byte) 0);
			}
		}

		writeGameLine("waiting for players", 3, 159, (byte) 13);
	}

	public void displayVoting(Vote vote)
	{
		writeGameLine("Voting for the next", 0, Material.STAINED_CLAY.getId(), (byte) 14);
		writeGameLine(vote.getName(), 1, Material.STAINED_CLAY.getId(), (byte) 4);
		writeGameLine("", 2, Material.AIR.getId(), (byte) 0);

		displayVotingTime(vote);
	}

	public void displayVotingTime(Vote vote)
	{
		writeGameLine("Vote ends in " + vote.getTimer(), 3, Material.STAINED_CLAY.getId(), (byte) 13);
	}

	public void ScoreboardDisplay(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Game game = _manager.GetGame();

		if (game != null && !game.inLobby())
		{
			if (game.UseCustomScoreboard)
			{
				return;
			}

			for (Player player : UtilServer.getPlayersCollection())
			{
				player.setScoreboard(_manager.GetGame().GetScoreboard().getScoreboard()); //XXX
			}
		}
		else
		{
			_manager.getScoreboardManager().getScoreboards().entrySet().stream().filter(ent -> Bukkit.getPlayer(ent.getKey()) != null).forEach(ent ->
			{
				Bukkit.getPlayer(ent.getKey()).setScoreboard(ent.getValue().getHandle());
			});
		}
	}

	public void AddPlayerToScoreboards(Player player, GameTeam gameTeam)
	{
		PermissionGroup group;

		if (player == null)
		{
			group = PermissionGroup.PLAYER;
		}
		else
		{
			group = _manager.GetClients().Get(player).getRealOrDisguisedPrimaryGroup();
		}

		String teamId = getTeamId(gameTeam, player);

		for (MineplexScoreboard scoreboard : _manager.getScoreboardManager().getScoreboards().values())
		{
			Team team = scoreboard.getHandle().getTeam(teamId);
			if (team == null)
			{
				team = scoreboard.getHandle().registerNewTeam(teamId);
				if (gameTeam != null)
				{
					if (gameTeam.GetDisplaytag())
					{
						team.setPrefix(gameTeam.GetColor() + C.Bold + gameTeam.GetName() + gameTeam.GetColor() + " ");
					}
					else
					{
						if (group.getDisplay(false, false, false, false).isEmpty())
						{
							team.setPrefix(gameTeam.GetColor() + "");
						}
						else
						{
							team.setPrefix(group.getDisplay(true, true, true, false) + ChatColor.RESET + " " + gameTeam.GetColor());
						}
					}
				}
			}
			if (player != null)
			{
				team.addEntry(player.getName());
			}
		}
	}

	public void RemovePlayerFromTeam(Player player, GameTeam gameTeam)
	{
		PermissionGroup group = _manager.GetClients().Get(player).getRealOrDisguisedPrimaryGroup();

		String teamId = getTeamId(gameTeam, player);

		for (MineplexScoreboard scoreboard : _manager.getScoreboardManager().getScoreboards().values())
		{
			Team team = scoreboard.getHandle().getTeam(teamId);
			if (team != null)
			{
				team.removeEntry(player.getName());
			}
			scoreboard.getHandle().getTeam(group.name()).addEntry(player.getName());
		}
	}

	public String getTeamId(GameTeam gameTeam, Player player)
	{
		PermissionGroup group;

		if (player == null)
		{
			group = PermissionGroup.PLAYER;
		}
		else
		{
			group = _manager.GetClients().Get(player).getRealOrDisguisedPrimaryGroup();
		}

		String rankName = group.name();
		if (player != null)
		{
			boolean rankIsUltra = group == PermissionGroup.PLAYER &&
					_manager.GetDonation().Get(player).ownsUnknownSalesPackage(_manager.GetServerConfig().ServerType + " ULTRA");

			if (rankIsUltra)
			{
				rankName = PermissionGroup.ULTRA.name();
			}
		}

		String teamId;
		if (gameTeam != null && gameTeam.GetDisplaytag())
		{
			teamId = "GT" + String.valueOf(gameTeam.getTeamId());
		}
		else
		{
			// It needs to be color first in order for the client to sort the tab list correctly
			if (gameTeam != null && gameTeam.GetColor() != null)
			{
				teamId = gameTeam.GetColor().getChar() + "." + rankName;
			}
			else
			{
				teamId = rankName;
			}
		}
		return teamId;
	}

	@EventHandler
	public void disallowInventoryClick(InventoryClickEvent event)
	{
		if (_manager.GetGame() == null || !_manager.GetGame().inLobby())
		{
			return;
		}

		if (event.getInventory().getType() == InventoryType.CRAFTING)
		{
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}
	}

	@EventHandler
	public void inventoryUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_manager.IsHotbarInventory())
		{
			return;
		}

		Game game = _manager.GetGame();

		if (game != null && !game.inLobby() && game.GadgetsDisabled)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (player.getOpenInventory().getType() != InventoryType.CRAFTING)
			{
				continue;
			}

			_manager.getCosmeticManager().giveInterfaceItem(player);
			_manager.getBoosterManager().giveInterfaceItem(player);
			_manager.getTitles().giveBookIfNotExists(player, false);
			_manager.getPartyManager().giveItemIfNotExists(player);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void explodeBlockBreakFix(EntityExplodeEvent event)
	{
		if (_manager.GetGame() == null)
		{
			return;
		}

		if (_manager.GetGame().GetState() == GameState.Live)
		{
			return;
		}

		event.blockList().clear();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void velocityEventCancel(PlayerVelocityEvent event)
	{
		if (_manager.GetGame() == null)
		{
			return;
		}

		if (_manager.GetGame().GetState() == GameState.Live)
		{
			return;
		}

		event.setCancelled(true);
	}

	private void equipActiveKit(Player player)
	{
		Game game = _manager.GetGame();

		if (game == null)
		{
			return;
		}

		_manager.getMineplexGameManager().getActiveKit(player, game.GetType().getDisplay()).ifPresent(kit -> setKitByGameKit(player, kit));
	}

	private void setKitByGameKit(Player player, GameKit gameKit)
	{
		Game game = _manager.GetGame();

		for (Kit kit : game.GetKits())
		{
			if (kit.getGameKit().equals(gameKit))
			{
				game.SetKit(player, kit, false);
				return;
			}
		}
	}

	@EventHandler
	public void kitSelect(KitSelectEvent event)
	{
		setKitByGameKit(event.getPlayer(), event.getKit());
	}

	@EventHandler
	public void kitNPCInteract(KitNPCInteractEvent event)
	{
		if (_manager.isChampionsEnabled())
		{
			event.setCancelled(true);
			setKitByGameKit(event.getPlayer(), event.getKit());
		}
	}

	@EventHandler
	public void eqiupKitOnJoin(PlayerJoinEvent event)
	{
		Game game = _manager.GetGame();

		if (game == null)
		{
			return;
		}

		Player player = event.getPlayer();

		if (game.InProgress())
		{
			GameTeam team = game.GetTeam(player);

			if (team == null || !team.IsAlive(player))
			{
				return;
			}
		}

		equipActiveKit(player);
	}

	@EventHandler
	public void onInteractArmorStand(PlayerArmorStandManipulateEvent event)
	{
		if (_manager.GetGame() == null || !_manager.GetGame().inLobby())
		{
			event.setCancelled(true);
		}
	}

	public Location getGameText()
	{
		return _gameText;
	}

	public Location getAdvText()
	{
		return _advText;
	}

	public Location getKitText()
	{
		return _kitText;
	}

	public Location getTeamText()
	{
		return _teamText;
	}

	public Location getCarl()
	{
		return _carl;
	}

	public Location getSpawn()
	{
		return _spawn;
	}

	public Location getAmpStand()
	{
		return _ampStand;
	}

	public void setGameText(Location gameText)
	{
		_gameText = gameText;
	}

	public void setAdvText(Location advText)
	{
		_advText = advText;
	}

	public void setKitText(Location kitText)
	{
		_kitText = kitText;
	}

	public void setTeamText(Location teamText)
	{
		_teamText = teamText;
	}

	public void setCarl(Location carl)
	{
		_carl = carl;
	}

	public void setSpawn(Location spawn)
	{
		_spawn = spawn;
	}

	public void setAmpStand(Location ampStand)
	{
		_ampStand = ampStand;
	}

	public Map<GameTeam, Location> getTeamLocations()
	{
		return _teamLocations;
	}

	public Map<Entity, LobbyEnt> getTeams()
	{
		return _teams;
	}

	public Map<Block, Material> getTeamBlocks()
	{
		return _teamBlocks;
	}

	public Map<Block, Material> getKitBlocks()
	{
		return _kitBlocks;
	}

	public boolean isGeneratePodiums()
	{
		return _generatePodiums;
	}

	public Location getMissions()
	{
		return _missions;
	}

	public void setMissions(Location loc)
	{
		_missions = loc;
	}

	public boolean isNearSpawn(Location location, int distance)
	{
		// offsetSquared should be compared to squared distance, obviously...
		return UtilMath.offsetSquared(_spawn, location) < distance * distance;
	}

	public boolean isNearSpawn(Location location)
	{
		// Default to a 10 block radius
		return isNearSpawn(location, 10);
	}

	@EventHandler
	public void blockTrampolineNearSpawn(GadgetSelectLocationEvent event)
	{
		if (event.getGadget() instanceof ItemTrampoline && isNearSpawn(event.getLocation(), 20))
		{
			event.setCancelled(true);
		}
	}
}
