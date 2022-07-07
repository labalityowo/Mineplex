package nautilus.game.minekart.gp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.fusesource.jansi.Ansi.Color;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.fakeEntity.FakeEntityManager;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartType;
import nautilus.game.minekart.track.Track;
import nautilus.game.minekart.track.Track.TrackState;

public class GP 
{
	public enum GPState
	{
		Recruit,
		Live,
		Ended
	}

	public GPManager Manager;

	private GPState _state = GPState.Recruit;

	private int _gpId = 0;

	private HashMap<Player, KartType> _players = new HashMap<Player, KartType>();
	
	private Scoreboard _scoreScoreboard;
	private Objective _scoreObjective;
	
	private Scoreboard _posScoreboard;
	private Objective _posObjective;

	private boolean _switchScoreboards;
	
	private GPSet _trackSet;
	private int _trackIndex = -1;
	private Track[] _trackArray = null;

	private int _maxKarts = 10;

	public GP(GPManager manager, GPSet trackSet)
	{
		Manager = manager;
		_trackSet = trackSet;

		_gpId = GetNewId();

		//Prepare Tracks
		_trackArray = new Track[_trackSet.GetMapNames().length];
		for (int i=0 ; i<_trackArray.length ; i++)		
		{
			_trackArray[i] = new Track(this, Manager.GetTeleport(), Manager.GetRecharge(), _trackSet.GetMapNames()[i], i);	
		}
	}

	public GPState GetState()
	{
		return _state;
	}

	public int GetId()
	{
		return _gpId;
	}

	public GPSet GetSet()
	{
		return _trackSet;
	}

	public Track GetTrack()
	{
		if (_trackIndex == -1)
			return null;

		if (_trackIndex >= _trackArray.length)
			return null;

		return _trackArray[_trackIndex];
	}
	
	public Track GetTrack(int id)
	{
		try
		{
			return _trackArray[id];
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public Track[] GetTracks()
	{
		return _trackArray;
	}

	public int GetMaxKarts()
	{
		return _maxKarts;
	}

	public Collection<Player> GetPlayers() 
	{
		return _players.keySet();
	}

	public Collection<Kart> GetKarts() 
	{
		HashSet<Kart> _karts = new HashSet<Kart>();

		for (Player player : GetPlayers())
		{
			Kart kart = Manager.KartManager.GetKart(player);

			if (kart != null)
				_karts.add(kart);
		}

		return _karts;
	}

	public void SetState(GPState state) 
	{
		_state = state;
	}

	public void NextTrack()
	{
		_trackIndex++;

		for (Kart kart : GetKarts())
		{
			kart.ClearTrackData();

			kart.SetItemCycles(0);
			kart.SetItemStored(null);
		}

		if (_trackIndex < _trackArray.length)
		{
			for (Kart kart : GetKarts())
			{
				kart.SetItemStored(null);
				kart.SetItemActive(null);
				
				kart.GetDriver().eject();
				kart.GetDriver().leaveVehicle();
			}

			GetTrack().Initialize();
		}
		else
		{
			_trackIndex--;
			//Load Castle
			Manager.CreateResult(this);

			_trackIndex++;
			
			for (Kart kart : GetKarts())
			{
				Manager.KartManager.RemoveKart(kart.GetDriver());
				FakeEntityManager.Instance.RemoveForward(kart.GetDriver());
				FakeEntityManager.Instance.RemoveFakeVehicle(kart.GetDriver(), kart.GetEntity().GetEntityId());
			}

			Announce(F.main("MK", "Ended Set: " + F.elem(_trackSet.GetName())));

			SetState(GPState.Ended);
		}
	}

	public void AddPlayer(Player player, KartType type) 
	{
		_players.put(player, type);

		Manager.KartManager.AddKart(player, type, this);
	}

	public void RemovePlayer(Player player, Kart kart)
	{
		_players.remove(player);
		
		if (_scoreScoreboard != null)
		{
			_scoreScoreboard.resetScores(Bukkit.getOfflinePlayer(Color.WHITE + player.getName()));
		}
		
		if (_posScoreboard != null)
		{
			ChatColor col = ChatColor.YELLOW;
			if (kart != null && kart.GetLap() > 3)
				col = ChatColor.GREEN;
			
			_posScoreboard.resetScores(Bukkit.getOfflinePlayer(col + player.getName()));
		}

		if (kart != null)
			for (Track track : _trackArray)
				track.RemoveKart(kart);

		if (this instanceof GPBattle)
			Announce(F.main("MK", player.getName() + " has left the Battle."));
		else
			Announce(F.main("MK", player.getName() + " has left the Grand Prix."));
	}

	public void UpdateScoreBoard()
	{
		if (GetTrack() == null)
			return;

		if (GetTrack().GetState() == TrackState.Ended)
		{
			if (_scoreScoreboard == null)
			{
				ScoreboardManager manager = Bukkit.getScoreboardManager();
				_scoreScoreboard = manager.getNewScoreboard();

				_scoreObjective = _scoreScoreboard.registerNewObjective("showposition", "dummy");
				_scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				_scoreObjective.setDisplayName(ChatColor.AQUA + "Total Score");
				
				_switchScoreboards = true;
			}
			
			if (_switchScoreboards)
			{
				for (Kart kart : GetTrack().GetPositions())
				{	
					if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
						continue;

					kart.GetDriver().setScoreboard(_scoreScoreboard);
				}
				
				_switchScoreboards = false;
			}

			for (Kart kart : GetTrack().GetPositions())
			{		
				if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
					continue;
				
				Score score = _scoreObjective.getScore(Bukkit.getOfflinePlayer(UtilPlayer.safeNameLength(ChatColor.WHITE + kart.GetDriver().getName())));
				score.setScore(GetScore(kart));
			}
		}
		else
		{
			if (_posScoreboard == null)
			{
				ScoreboardManager manager = Bukkit.getScoreboardManager();
				_posScoreboard = manager.getNewScoreboard();

				_posObjective = _posScoreboard.registerNewObjective("showposition", "dummy");
				_posObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				_posObjective.setDisplayName(ChatColor.AQUA + "Kart Positions");
				
				_switchScoreboards = true;
			}

			for (Kart kart : GetTrack().GetPositions())
			{		
				if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
					continue;

				ChatColor col = ChatColor.YELLOW;
				
				if (kart.GetLap() > 3)
				{
					col = ChatColor.GREEN;
					_posScoreboard.resetScores(Bukkit.getOfflinePlayer(UtilPlayer.safeNameLength(ChatColor.YELLOW + kart.GetDriver().getName())));
				}
				else
				{
					_posScoreboard.resetScores(Bukkit.getOfflinePlayer(UtilPlayer.safeNameLength(ChatColor.GREEN + kart.GetDriver().getName())));
				}
					
				Score score = _posObjective.getScore(Bukkit.getOfflinePlayer(UtilPlayer.safeNameLength(col + kart.GetDriver().getName())));
				score.setScore(kart.GetLapPlace() + 1);
			}
			
			if (_switchScoreboards)
			{				
				for (Kart kart : GetTrack().GetPositions())
				{	
					if (kart.GetDriver() == null || !kart.GetDriver().isOnline())
						continue;

					
					kart.GetDriver().setScoreboard(_posScoreboard);
				}
				
				_switchScoreboards = false;
			}
		}
	}

	public int GetScore(Kart kart) 
	{
		int score = 0;

		for (Track track : _trackArray)
			for (int i=0 ; i<track.GetPositions().size() ; i++)
				if (track.GetPositions().get(i).equals(kart))
				{
					score += Math.max(1, 10 - (i*2));
				}

		return score;
	}

	public int GetNewId() 
	{
		File file = new File("TrackId.dat");

		//Write If Blank
		if (!file.exists())
		{
			try
			{
				FileWriter fstream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fstream);

				out.write("0");

				out.close();
			}
			catch (Exception e)
			{
				System.out.println("Error: Track GetId Write Exception");
			}
		}

		int id = 0;

		//Read
		try
		{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();

			id = Integer.parseInt(line);

			in.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: Track GetId Read Exception");
			id = 0;
		}

		try
		{
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("" + (id + 1));

			out.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: Track GetId Re-Write Exception");
		}

		return id;
	}

	public void Announce(String string)
	{
		for (Player player : GetPlayers())
			UtilPlayer.message(player, string);
	}

	public void Unload()
	{
		for (Track track : _trackArray)
		{
			track.GetPositions().clear();
			track.GetScores().clear();
		}
	}

	public void SwitchScoreboards()
	{
		_switchScoreboards = true;
	}
}	
