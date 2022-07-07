package nautilus.game.arcade.gametutorial;

import java.util.HashMap;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.visibility.VisibilityManager;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.gametutorial.events.GameTutorialEndEvent;
import nautilus.game.arcade.gametutorial.events.GameTutorialPhaseEvent;
import nautilus.game.arcade.gametutorial.events.GameTutorialStartEvent;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class GameTutorial
{
	
	public ArcadeManager Manager;
	
	private TutorialPhase[] _phases;
	private GameTeam _team;
	private HashMap<Player, Location> _players;
	
	private TutorialPhase _currentPhase;
	
	private boolean _hasEnded;
	private boolean _hasStarted;
	
	private int _tick;
	
	private long _started;

	public boolean SetTutorialPositions = true; 
	public boolean TeleportOnEnd = true;
	public boolean RunTasksSync = true;
	public boolean PlayTutorialSounds = false;
	public boolean ShowPrepareTimer = false;
	public boolean CustomEnding = false;
	public boolean TutorialNotification = false;
	
	public long TimeBetweenPhase = 0;
	public long StartAfterTutorial = 5000;
	public long CustomEndingTime = 5000;
	
	public GameTutorial(ArcadeManager manager, TutorialPhase[] phases)
	{
		Manager = manager;
		_phases = phases;
		_players = new HashMap<>();
	}
	
	/** 
	 *	start the Tutorial (never use this)
	 */
	final public void start()
	{
		_hasStarted = true;
		_tick = 0;
		for (TutorialPhase phase : _phases)
			phase.setTutorial(this);
		
		if (TutorialNotification)
		{
			TutorialPhase phase = getPhase(1);
			for (TutorialText text : phase.getText())
			{
				int index = text.ID();
				text.setID(index + 1); 
			}
			TutorialText[] newText = new TutorialText[phase.getText().length + 1];
			for (int i = 0; i < newText.length; i++)
			{
				if (i == 0)
				{
					newText[i] = new TutorialText("Please notice that this is a Tutorial", 20, 1);
					continue;
				}
				else
				{
					newText[i] = phase.getText()[i - 1];
				}
			}
			phase.setText(newText);
		}
			
		Manager.GetChat().setChatSilence(60000, false);
		_started = System.currentTimeMillis();
		Manager.getPluginManager().callEvent(new GameTutorialStartEvent(this));
		onStart();
		preparePlayers();
		_currentPhase = getNextPhase();
		Manager.runSyncLater(new Runnable()
		{
			@Override
			public void run()
			{	
				nextPhase(true);
			}
		}, 40);
		_currentPhase.teleport();
	}
	
	/** 
	 * Stting next Phase/ending Tutorial
	 */
	protected void nextPhase(boolean phaseOne)
	{
		TutorialPhase from = _currentPhase;
		if (!phaseOne)
			_currentPhase = getNextPhase();
			
		if (_currentPhase == null)
		{
			// has ended
			if (!CustomEnding)
			{
				onEnd();
				_hasEnded = true;
				endTutorial();
				final GameTutorial tutorial = this;
				Manager.runSyncLater(new Runnable()
				{
					@Override
					public void run()
					{
						Manager.getPluginManager().callEvent(new GameTutorialEndEvent(tutorial));
					}
				}, 5);
			}
		}
		else
		{
			// setting another Phase, if Tutorial hasn't stopped yet
			if (!_hasEnded)
			{
				Manager.GetChat().setChatSilence(70000, false);
				onPhaseChange(_currentPhase);
				Manager.getPluginManager().callEvent(new GameTutorialPhaseEvent(this, from, _currentPhase));
				_currentPhase.start(phaseOne);
			}
		}
	}
	
	public void setTeam(GameTeam team)
	{
		_team = team;
	}
	
	private void endTutorial()
	{
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		for (final Player player : _players.keySet())
		{
			Manager.runSyncLater(new Runnable()
			{
				@Override
				public void run()
				{
					// Player visibility/fly mode
					for (Player other : Manager.GetGame().GetPlayers(false))
					{
						if (player == other)
							continue;
						
						vm.showPlayer(other, player, "Game Tutorial");
					}	
					player.setAllowFlight(false);
					player.setFlying(false);
				}
			}, 5);
			if (TeleportOnEnd)
			{
				Manager.runSyncLater(new Runnable()
				{
					@Override
					public void run()
					{
						// Spawn teleporting
						_team.SpawnTeleport();
					}
				}, 5);
			}
		}
		// setting the right prepare Time after the Tutorial ends
		Manager.GetChat().setChatSilence(StartAfterTutorial, false);
		Manager.GetGame().PrepareTime = (System.currentTimeMillis() - Manager.GetGame().GetStateTime()) + StartAfterTutorial;
	}
	
	protected TutorialPhase getNextPhase()
	{
		// getting next TutorialPhase
		for (TutorialPhase phase : _phases)
		{
			if (_currentPhase == null && phase.ID() == 1)
			{
				return phase;
			}
			else if (_currentPhase != null && _currentPhase.ID() + 1 == phase.ID())
			{
				return phase;
			}
		}
		return null;
	}
	
	private void preparePlayers()
	{
		for (Player player : UtilServer.getPlayers())
		{
			// setting Players into fly mode and save their Locations
			int i = 0;
			if (Manager.GetGame().GetTeam(player) == _team)
			{
				_players.put(player, Manager.GetGame().GetTeam(player).GetSpawns().get(i));
				player.setAllowFlight(true);
				player.setFlying(true);
				i++;
			}	
		}
	}
	
	public TutorialPhase getPhase(int index)
	{
		for (TutorialPhase phase : _phases)
		{
			if (phase.ID() == index)
				return phase;
		}
		return null;
	}
	
	public boolean hasEnded()
	{
		return _hasEnded;
	}
	
	public boolean hasStarted()
	{
		return _hasStarted;
	}
	
	public HashMap<Player, Location> getPlayers()
	{
		return _players;
	}
	
	public GameTeam getTeam()
	{
		return _team;
	}
	
	/**
	 * only available if CustomEnding is enabled
	 * You can end the tutorial with this method
	 * if you set CutomEnding to true, you have to run this method!
	 */
	public void end()
	{
		if (CustomEnding)
		{
			// Ending 
			onEnd();
			_hasEnded = true;
			Thread thread = _currentPhase.getThread(); 
			if (thread.isAlive())
				thread.destroy();
				
			endTutorial();
			final GameTutorial tutorial = this;
			Manager.runSyncLater(new Runnable()
			{
				@Override
				public void run()
				{
					Manager.getPluginManager().callEvent(new GameTutorialEndEvent(tutorial));
				}
			}, 5);
		}
		else
		{
			System.out.println("Only allowed while Custom Ending is enabled");
		}
	}

	public int tick()
	{
		if (!_hasEnded && hasStarted())
		{
			VisibilityManager vm = Managers.require(VisibilityManager.class);
			for (Player player : UtilServer.getPlayers())
			{
				for (Player other : _players.keySet())
				{
					vm.hidePlayer(player, other, "Game Tutorial");
				}
			}
		}
		_tick++;
		return _tick;
	}
	
	public TutorialPhase[] getPhases()
	{
		return _phases;
	}
	
	public TutorialPhase getCurrentPhase()
	{
		return _currentPhase;
	}
	
	public long getTutorialStart()
	{
		return _started;
	}
	
	public long getRunning()
	{
		return System.currentTimeMillis() - _started;
	}
	
	public long getPhaseTime()
	{
		return _currentPhase.getPhaseTime();
	}
	
	/*
	 * some overrideable methods that can be used to synchronize the tutorial events
	 */
	
	public void onTick(int tick){}
	
	public void onStart(){}
	
	public void onPhaseChange(TutorialPhase phase){}
	
	public void onEnd(){}	
}