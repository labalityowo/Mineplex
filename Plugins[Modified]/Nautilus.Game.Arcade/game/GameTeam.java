package nautilus.game.arcade.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.game.kit.KitAvailability;
import mineplex.core.visibility.VisibilityManager;

import nautilus.game.arcade.gametutorial.GameTutorial;
import nautilus.game.arcade.kit.Kit;

public class GameTeam
{
	private static final AtomicLong TEAM_ID = new AtomicLong();

	private final Game Host;

	private double _respawnTime = 0;

	public enum PlayerState
	{
		IN("In"),
		OUT("Out");

		private final String _name;

		PlayerState(String name)
		{
			_name = name;
		}

		public String GetName()
		{
			return _name;
		}
	}

	private final long _teamId = TEAM_ID.getAndIncrement();

	private String _name;
	private String _displayName;
	private ChatColor _color;
	private boolean _displayTag;

	private GameTutorial _tutorial;

	private final Map<Player, PlayerState> _players = new HashMap<>();

	private List<Location> _spawns;

	private Creature _teamEntity = null;

	private final Set<Kit> _kitRestrict = new HashSet<>();

	private boolean _visible = true;

	//Records order players go out in
	private List<Player> _places = new ArrayList<>();
	private long _teamCreatedTime = System.currentTimeMillis(); // Used just for SpectatorPage so that teams remain ordered

	public GameTeam(Game host, String name, ChatColor color, List<Location> spawns, boolean tags)
	{
		Host = host;

		_displayName = null;
		_name = name;
		_color = color;
		_spawns = spawns;
		_displayTag = tags;
	}

	public GameTeam(Game host, String name, ChatColor color, List<Location> spawns)
	{
		this(host, name, color, spawns, false);
	}

	public long getCreatedTime()
	{
		return _teamCreatedTime;
	}

	public String GetName()
	{
		return _name;
	}

	public ChatColor GetColor()
	{
		return _color;
	}

	public List<Location> GetSpawns()
	{
		return _spawns;
	}

	private Location fixFacing(Location loc)
	{
		if (Host.FixSpawnFacing)
		{
			float yaw = UtilAlg.GetYaw(UtilAlg.getTrajectory2d(loc, Host.GetSpectatorLocation()));

			yaw = (int) (yaw / 90) * 90;

			loc = loc.clone();
			loc.setYaw(yaw);
		}

		return loc;
	}

	public Location GetSpawn()
	{
		//Keep allies together
		if (!Host.IsLive() && Host.SpawnNearAllies)
		{
			//Find Location Nearest Ally
			Location loc = UtilAlg.getLocationNearPlayers(_spawns, GetPlayers(true), Host.GetPlayers(true));
			if (loc != null)
				return fixFacing(loc);

			//No allies existed spawned yet

			//Spawn near enemies (used for SG)
			if (Host.SpawnNearEnemies)
			{
				loc = UtilAlg.getLocationNearPlayers(_spawns, Host.GetPlayers(true), Host.GetPlayers(true));
				if (loc != null)
					return fixFacing(loc);
			}
			//Spawn away from enemies
			else
			{
				loc = UtilAlg.getLocationAwayFromPlayers(_spawns, Host.GetPlayers(true));
				if (loc != null)
					return fixFacing(loc);
			}
		}
		else
		{
			//Spawn near players 
			if (Host.SpawnNearEnemies)
			{
				Location loc = UtilAlg.getLocationNearPlayers(_spawns, Host.GetPlayers(true), Host.GetPlayers(true));
				if (loc != null)
					return fixFacing(loc);
			}
			//Spawn away from players
			else
			{
				Location loc = UtilAlg.getLocationAwayFromPlayers(_spawns, Host.GetPlayers(true));
				if (loc != null)
					return fixFacing(loc);
			}
		}

		return fixFacing(_spawns.get(UtilMath.r(_spawns.size())));
	}

	public void AddPlayer(Player player, boolean in)
	{
		_players.put(player, in ? PlayerState.IN : PlayerState.OUT);

		UtilPlayer.message(player, F.main("Team", _color + C.Bold + "You joined " + getDisplayName() + " Team."));
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl -> vm.refreshVisibility(pl, player));
	}

	public void DisbandTeam()
	{
		for (Player player : _players.keySet())
		{
			Host.getArcadeManager().GetLobby().RemovePlayerFromTeam(player, this);
			UtilPlayer.message(player, F.main("Team", _color + C.Bold + getDisplayName() + " Team was disbanded."));
		}

		_players.clear();
	}

	public void RemovePlayer(Player player)
	{
		_players.remove(player);
	}

	public boolean HasPlayer(Player player)
	{
		return _players.containsKey(player);
	}

	public boolean HasPlayer(String name, boolean alive)
	{
		for (Player player : _players.keySet())
			if (player.getName().equals(name))
				if (!alive || _players.get(player) == PlayerState.IN)
					return true;

		return false;
	}

	public int GetSize()
	{
		return _players.size();
	}

	public void SetPlayerState(Player player, PlayerState state)
	{
		if (player == null)
			return;

		_players.put(player, state);
	}

	public boolean IsTeamAlive()
	{
		for (PlayerState state : _players.values())
			if (state == PlayerState.IN)
				return true;

		return false;
	}

	public ArrayList<Player> GetPlayers(boolean playerIn)
	{
		ArrayList<Player> alive = new ArrayList<>();

		for (Player player : _players.keySet())
			if (!playerIn || (_players.get(player) == PlayerState.IN && player.isOnline()))
				alive.add(player);

		return alive;
	}

	public String GetFormattedName()
	{
		return GetColor() + "§l" + GetName();
	}

	public Location SpawnTeleport(Player player)
	{
		Location l = GetSpawn();
		player.teleport(l);
		return l;
	}

	public void SpawnTeleport(Player player, Location location)
	{
		player.leaveVehicle();
		player.eject();
		player.teleport(location);
	}

	public void SpawnTeleport()
	{
		SpawnTeleport(true);
	}

	public void SpawnTeleport(boolean aliveOnly)
	{
		for (Player player : GetPlayers(aliveOnly))
		{
			SpawnTeleport(player);
		}
	}

	public Set<Kit> GetRestrictedKits()
	{
		return _kitRestrict;
	}

	public boolean KitAllowed(Kit kit)
	{
		return kit.GetAvailability() != KitAvailability.Null && !_kitRestrict.contains(kit);
	}

	public boolean IsAlive(Player player)
	{
		return player.isOnline() && _players.getOrDefault(player, PlayerState.OUT) == PlayerState.IN;

	}

	public void SetColor(ChatColor color)
	{
		_color = color;
	}

	public void SetName(String name)
	{
		_name = name;
	}

	public void setDisplayName(String name)
	{
		_displayName = name;
	}

	public String getDisplayName()
	{
		if (_displayName == null)
			return _name;

		return _displayName;
	}

	public byte GetColorData()
	{
		if (GetColor() == ChatColor.WHITE) return (byte) 0;
		if (GetColor() == ChatColor.GOLD) return (byte) 1;
		if (GetColor() == ChatColor.LIGHT_PURPLE) return (byte) 2;
		if (GetColor() == ChatColor.AQUA) return (byte) 3;
		if (GetColor() == ChatColor.YELLOW) return (byte) 4;
		if (GetColor() == ChatColor.GREEN) return (byte) 5;
		if (GetColor() == ChatColor.LIGHT_PURPLE) return (byte) 6;
		if (GetColor() == ChatColor.DARK_GRAY) return (byte) 7;
		if (GetColor() == ChatColor.GRAY) return (byte) 8;
		if (GetColor() == ChatColor.DARK_AQUA) return (byte) 9;
		if (GetColor() == ChatColor.DARK_PURPLE) return (byte) 10;
		if (GetColor() == ChatColor.BLUE) return (byte) 11;
		if (GetColor() == ChatColor.DARK_BLUE) return (byte) 11;
		//if (GetColor() == ChatColor.BROWN)		return (byte)12;
		if (GetColor() == ChatColor.DARK_GREEN) return (byte) 13;
		if (GetColor() == ChatColor.RED) return (byte) 14;
		if (GetColor() == ChatColor.DARK_RED) return (byte) 14;
		else return (byte) 15;
	}

	public Color GetColorBase()
	{
		if (GetColor() == ChatColor.WHITE) return Color.WHITE;
		if (GetColor() == ChatColor.GOLD) return Color.ORANGE;
		if (GetColor() == ChatColor.LIGHT_PURPLE) return Color.FUCHSIA;
		if (GetColor() == ChatColor.AQUA) return Color.AQUA;
		if (GetColor() == ChatColor.YELLOW) return Color.YELLOW;
		if (GetColor() == ChatColor.GREEN) return Color.GREEN;
		if (GetColor() == ChatColor.DARK_GRAY) return Color.GRAY;
		if (GetColor() == ChatColor.GRAY) return Color.GRAY;
		if (GetColor() == ChatColor.DARK_AQUA) return Color.AQUA;
		if (GetColor() == ChatColor.DARK_PURPLE) return Color.PURPLE;
		if (GetColor() == ChatColor.BLUE) return Color.BLUE;
		if (GetColor() == ChatColor.DARK_BLUE) return Color.BLUE;
		if (GetColor() == ChatColor.DARK_GREEN) return Color.GREEN;
		if (GetColor() == ChatColor.RED) return Color.RED;
		else return Color.WHITE;
	}

	public DyeColor getDyeColor()
	{
		if (GetColor() == ChatColor.WHITE) return DyeColor.WHITE;
		if (GetColor() == ChatColor.GOLD) return DyeColor.ORANGE;
		if (GetColor() == ChatColor.LIGHT_PURPLE) return DyeColor.PINK;
		if (GetColor() == ChatColor.AQUA) return DyeColor.LIGHT_BLUE;
		if (GetColor() == ChatColor.YELLOW) return DyeColor.YELLOW;
		if (GetColor() == ChatColor.GREEN) return DyeColor.LIME;
		if (GetColor() == ChatColor.DARK_GRAY) return DyeColor.GRAY;
		if (GetColor() == ChatColor.GRAY) return DyeColor.SILVER;
		if (GetColor() == ChatColor.DARK_AQUA) return DyeColor.CYAN;
		if (GetColor() == ChatColor.DARK_PURPLE) return DyeColor.PURPLE;
		if (GetColor() == ChatColor.BLUE) return DyeColor.BLUE;
		if (GetColor() == ChatColor.DARK_BLUE) return DyeColor.BLUE;
		if (GetColor() == ChatColor.DARK_GREEN) return DyeColor.GREEN;
		if (GetColor() == ChatColor.RED) return DyeColor.RED;
		else return DyeColor.WHITE;
	}

	public void SetTeamEntity(Creature ent)
	{
		_teamEntity = ent;
	}

	public LivingEntity GetTeamEntity()
	{
		return _teamEntity;
	}

	public void SetSpawns(List<Location> spawns)
	{
		_spawns = spawns;
	}

	public void SetVisible(boolean b)
	{
		_visible = b;
	}

	public boolean GetVisible()
	{
		return _visible;
	}

	/*
	 * Whether this GameTeam should show a prefix
	 */
	public boolean GetDisplaytag()
	{
		return _displayTag;
	}

	public void SetRespawnTime(double i)
	{
		_respawnTime = i;
	}

	public double GetRespawnTime()
	{
		return _respawnTime;
	}

	public void SetPlacement(Player player, PlayerState state)
	{
		if (state == PlayerState.OUT)
		{
			if (!_places.contains(player))
				_places.add(0, player);
		}
		else
			_places.remove(player);
	}

	public List<Player> GetPlacements(boolean includeAlivePlayers)
	{
		if (includeAlivePlayers)
		{
			ArrayList<Player> placesClone = new ArrayList<>(_places);

			for (Player player : GetPlayers(true))
			{
				placesClone.add(0, player);
			}

			return placesClone;
		}

		return _places;
	}

	public GameTutorial getTutorial()
	{
		return _tutorial;
	}

	public void setTutorial(GameTutorial tutorial)
	{
		_tutorial = tutorial;
	}

	public long getTeamId()
	{
		return this._teamId;
	}
}