package nautilus.game.arcade.gametutorial;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTextMiddle;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class TutorialPhase
{

	public abstract int ID(); 
	
	private GameTutorial _tutorial;
	private TutorialText[] _text;
	private Location _location;
	private Location _target;
	private boolean _hasEnded;
	
	private Thread _thread;
	
	private long _started;
	
	private TutorialText _currentText;
	
	public TutorialPhase(TutorialText[] text)
	{
		_text = text;
	}
	
	/** 
	 *	start the Phase (never use this as well)
	 */
	final public void start(boolean phaseOne)
	{
		_hasEnded = false;
		_started = System.currentTimeMillis();
		onStart();
		if(!phaseOne)
		{
			teleport();
		}
		displayText();
	}
	
	/**
	 * Teleporting Players and keeping them if SetTutorialPositions == true
	 */
	final public void teleport()
	{
		if(!getTutorial().SetTutorialPositions)
			return;
			
		if(_location != null && _target != null)
		{
			prepareLocations();
			updatePlayers();
		}
	}
	
	/**
	 * preparing Pitch/Yaw of the location
	 */
	public void prepareLocations()
	{
		Vector vector = new Vector(_target.getBlockX() - _location.getBlockX(), _target.getBlockY() - _location.getBlockY(), _target.getBlockZ() - _location.getBlockZ());
		float pitch = UtilAlg.GetPitch(vector);
		float yaw = UtilAlg.GetYaw(vector);
		_location.setPitch(pitch);
		_location.setYaw(yaw);
	}
	
	/**
	 * teleporting players until Phase ends
	 */
	private void updatePlayers()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(!_hasEnded && !getTutorial().hasEnded())
				{
					_tutorial.Manager.runSync(new Runnable()
					{
						@Override
						public void run()
						{
							// teleport Players Sync
							if(!_hasEnded && !getTutorial().hasEnded())
							{
								for(Player player : _tutorial.getPlayers().keySet())
								{
									player.setAllowFlight(true);
									player.setFlying(true);
									player.teleport(_location);
								}
							}
						}
					});
					try
					{
						// sleep for 1 tick
						Thread.sleep(50);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	/**
	 * displays all messages set in the constructor
	 * will end Phase if no more messages are available or Tutorial has already ended
	 */
	public void displayText()
	{
		_thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(true)
				{
					TutorialText text = getNextMessage();
					if(text == null)
					{
						// ending Phase
						_tutorial.Manager.runSyncLater(new Runnable()
						{
							@Override
							public void run()
							{
								_hasEnded = true;
								onEnd();		
								_tutorial.nextPhase(false);				
							}
						}, getTutorial().TimeBetweenPhase);
						break;
					}
					else
					{
						// displaying next message
						Player[] players = new Player[_tutorial.getPlayers().keySet().size()];
						int i = 0;
						for(Player player : _tutorial.getPlayers().keySet())
						{
							if(_tutorial.PlayTutorialSounds)
							{
								if(text.getSound() != null)
									player.playSound(player.getLocation(), text.getSound(), 2f, 2f);
							}
							players[i] = player;
							i++;
						}
						displayMessage(text);    
 						UtilTextMiddle.display(text.getTitle(), text.getText(), 0, text.getStayTime(), 0, players);
 						try
						{
							Thread.sleep(text.getStayTime() * 50);
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
					
			}
		});
		_thread.start();
	}
	
	/**
	 * firing abstract method Sync/Async depending on the RunTasksSync Flag
	 */
	private void displayMessage(final TutorialText text)
	{
		if(_tutorial.RunTasksSync)
		{
			_tutorial.Manager.runSync(new Runnable()	
			{
				@Override
				public void run()
				{
					onMessageDisplay(text);
				}
			});
		}
		else
		{
			onMessageDisplay(text);
		}
	}
	
	/**
	 * getting next message
	 */
	protected TutorialText getNextMessage()
	{	
		for(TutorialText text : _text)
		{
			if(_currentText == null && text.ID() == 1)
			{
				_currentText = text;
				return text;
			}
			else if(_currentText != null && _currentText.ID() + 1 == text.ID())
			{
				_currentText = text;
				return text;
			}
		}
		return null;
	}
	
	public TutorialText[] getText()
	{
		return _text;
	}
	
	public TutorialText getCurrentText()
	{
		return _currentText;
	}
	
	public void setText(TutorialText[] text)
	{
		_text = text;
	}
	
	public Location getLocation()
	{
		return _location;
	}
	
	public void setTutorial(GameTutorial tutorial)
	{
		_tutorial = tutorial;
	}
	
	public void setLocation(Location location)
	{
		_location = location;
		
		if (_location != null && _target != null)
			prepareLocations();
	}
	
	public void setTarget(Location target)
	{
		_target = target;
		
		if (_location != null && _target != null)
			prepareLocations();
	}
	
	public GameTutorial getTutorial()
	{
		return _tutorial;
	}
	
	public Location getTarget()
	{
		return _target;
	}
	
	public long getPhaseTime()
	{
		return _started;
	}
	
	public Thread getThread()
	{
		return _thread;
	}
	
	/*
	 * some overrideable methods that can be used to synchronize the tutorial events
	 */
	
	public void onStart(){}
	
	public void onMessageDisplay(TutorialText text){}
	
	public void onEnd(){}
	
}
