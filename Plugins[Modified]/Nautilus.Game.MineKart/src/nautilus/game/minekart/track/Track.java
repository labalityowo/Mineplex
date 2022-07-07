package nautilus.game.minekart.track;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import mineplex.core.common.util.F;
import mineplex.core.common.util.FileUtil;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.WorldUtil;
import mineplex.core.recharge.Recharge;
import mineplex.core.teleport.Teleport;
import nautilus.game.minekart.gp.GP;
import nautilus.game.minekart.gp.GPBattle;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.track.ents.*;
import nautilus.minecraft.core.utils.ZipUtil;
import net.minecraft.server.v1_7_R1.ChunkPreLoadEvent;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.util.Vector;

public class Track 
{
	public enum TrackState
	{
		Loading,
		Countdown,
		Live,
		Ending,
		Ended
	}

	private World World = null;

	private int MinX = 0;
	private int MinZ = 0;
	private int MaxX = 0;
	private int MaxZ = 0;
	private int CurX = 0;
	private int CurZ = 0;
	
	private GP GP;
	private Teleport _teleport;
	private Recharge _recharge;
	private int _trackId;
	
	private String _name;
	private String _file;

	//Race Data
	private TrackState _state = TrackState.Loading;
	private long _stateTime = System.currentTimeMillis();
	private int _countdown = 6;
	private boolean _nextTrack = false;
	
	private HashMap<Kart, Double> _scores = new HashMap<Kart, Double>();
	private ArrayList<Kart> _positions = new ArrayList<Kart>();

	//Map Data
	private float _yaw = 0f;
	private ArrayList<Location> _kartStart;			
	private ArrayList<Location> _returnPoints;	
	private ArrayList<Location> _trackProgress;	
	private ArrayList<TrackItem> _itemBlocks;
	
	private HashMap<Location, Double> _jumps;
	
	private ArrayList<TrackEntity> _creatures;
	
	private ArrayList<Integer> _trackBlocks;
	private ArrayList<Integer> _returnBlocks;

	public Track(GP gp, Teleport teleport, Recharge recharge, String file, int id)
	{
		GP = gp;
		_teleport = teleport;
		_recharge = recharge;
		
		_trackId = id;

		_file = file;

		_kartStart = new ArrayList<Location>();			
		_returnPoints = new ArrayList<Location>();	
		_trackProgress = new ArrayList<Location>();	
		_itemBlocks = new ArrayList<TrackItem>();
		
		_jumps = new HashMap<Location, Double>();
		_creatures = new ArrayList<TrackEntity>();

		_trackBlocks = new ArrayList<Integer>();
		_returnBlocks = new ArrayList<Integer>();
		
		//Register Self
		GP.Manager.TrackManager.RegisterTrack(this);
	}
	
	public GP GetGP()
	{
		return GP;
	}

	public String GetName()
	{
		return _name;
	}

	public String GetFile()
	{
		return _file;
	}

	public World GetWorld() 
	{
		return World;
	}

	public float GetYaw()
	{
		return _yaw;
	}

	public ArrayList<Location> GetSpawns()
	{
		return _kartStart;
	}

	public HashMap<Kart, Double> GetScores() 
	{
		return _scores;
	}

	public ArrayList<Kart> GetPositions() 
	{
		return _positions;
	}

	public ArrayList<Location> GetProgress()
	{
		return _trackProgress;
	}

	public ArrayList<Location> GetReturn()
	{
		return _returnPoints;
	}

	public ArrayList<TrackItem> GetItems()
	{
		return _itemBlocks;
	}
	
	public HashMap<Location, Double> GetJumps()
	{
		return _jumps;
	}
	
	public ArrayList<TrackEntity> GetCreatures()
	{
		return _creatures;
	}
	
	public ArrayList<Integer> GetTrackBlocks()
	{
		return _trackBlocks;
	}
	
	public ArrayList<Integer> GetReturnBlocks()
	{
		return _returnBlocks;
	}

	public void SetState(TrackState state)
	{
		if (_state != state)
		{
			if (state == TrackState.Ended || _state == TrackState.Ended)
				GP.SwitchScoreboards();
			else if (state == TrackState.Countdown || _state == TrackState.Countdown) 
				GP.SwitchScoreboards();
		}
		
		_state = state;
		_stateTime = System.currentTimeMillis();
		_countdown = 31;
		
		if (state == TrackState.Countdown)
			_countdown = 8;
	}

	public TrackState GetState()
	{
		return _state;
	}
	
	public long GetStateTime()
	{
		return _stateTime;
	}

	public void Update()
	{
		String type = "Race";
		if (GP instanceof GPBattle)
			type = "Battle";
		
		if (_state == TrackState.Loading)
		{
			
		}
		else if (_state == TrackState.Countdown)
		{
			if (UtilTime.elapsed(_stateTime, 1000))
			{
				_stateTime = System.currentTimeMillis();
				_countdown--;

				//Inform + Sound
				for (Player cur : GP.GetPlayers())
				{
					if (_countdown > 5)
					{
						
					}
					else if (_countdown > 0)		
					{
						UtilPlayer.message(cur, F.main("MK", type + " begins in " + F.time(_countdown + " Seconds") + "..."));
						cur.playSound(cur.getLocation(), Sound.NOTE_PIANO, 1f, 1f);
					}
					else				
					{
						UtilPlayer.message(cur, F.main("MK", type + " has started!"));
						cur.playSound(cur.getLocation(), Sound.NOTE_PIANO, 2f, 2f);
					}
				}

				if (_countdown <= 0)
					SetState(TrackState.Live);
			}
		}
		else if (_state == TrackState.Live)
		{

		}
		else if (_state == TrackState.Ending)
		{
			boolean allFinished = true;
			
			for (Kart kart : GetGP().GetKarts())
				if (!kart.HasFinishedTrack())
					allFinished = false;
			
			if (allFinished)
			{
				GP.Announce(F.main("MK", type + " has ended."));
				SetState(TrackState.Ended);
			}
			
			else if (UtilTime.elapsed(_stateTime, 1000))
			{
				_stateTime = System.currentTimeMillis();
				_countdown--;

				//Inform + Sound
				if (_countdown%5 == 0)
				{
					GP.Announce(F.main("MK", type + " ends in " + F.time(_countdown + " Seconds") + "..."));
					
					for (Player cur : GP.GetPlayers())
					{
						if (_countdown > 0)	cur.playSound(cur.getLocation(), Sound.NOTE_PIANO, 1f, 0.5f);			
						else				cur.playSound(cur.getLocation(), Sound.NOTE_PIANO, 2f, 0f);
					}
				}

				if (_countdown <= 0)
				{
					GP.Announce(F.main("MK", type + " has ended."));
					SetState(TrackState.Ended);
				}
			}
		}
		else
		{
			if (_nextTrack)
				return;
			
			if (UtilTime.elapsed(_stateTime, 10000))
			{
				GetGP().NextTrack();
				_nextTrack = true;
			}
		}
	}

	protected String GetFolder()
	{
		return GetGP().GetId() + "-" + GetGP().GetSet().GetName() + "-" + GetFile();
	}

	public void Initialize()
	{
		final Track track = this;
		
		System.out.println("Initializing....");

		UtilServer.getServer().getScheduler().runTaskAsynchronously(GP.Manager.GetPlugin(), new Runnable()
		{
			public void run()
			{
				//Unzip
				track.UnzipWorld();
				
				//Load Track Data Sync
				UtilServer.getServer().getScheduler().runTask(GP.Manager.GetPlugin(), new Runnable()
				{
					public void run()
					{
						//Start World
						World = WorldUtil.LoadWorld(new WorldCreator(GetFolder()));
						
						//Load Track Data
						track.LoadTrackData();
					}
				});
			}
		});
	}
	
	protected void UnzipWorld() 
	{
		String folder = GetFolder();
		new File(folder).mkdir();
		new File(folder + File.separatorChar + "region").mkdir();
		new File(folder + File.separatorChar + "data").mkdir();
		ZipUtil.UnzipToDirectory(GetFile() + ".zip", folder);
	}
	
	public void LoadTrackData() 
	{
		//Load Track Data
		try
		{
			FileInputStream fstream = new FileInputStream(GetFolder() + File.separatorChar + "TrackInfo.dat");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = br.readLine()) != null)  
			{
				String[] tokens = line.split(":");
				
				if (tokens.length < 2)
					continue;
				
				if (tokens[0].length() == 0)
					continue;
				
				//TRACK NAME
				if (tokens[0].equalsIgnoreCase("TRACK_NAME"))
				{
					_name = tokens[1];
				}
				
				else if (tokens[0].equalsIgnoreCase("ROAD_TYPES"))
				{
					try
					{
						_trackBlocks.add(Integer.parseInt(tokens[1]));
					}
					catch (Exception e)
					{
						System.out.println("Track Data Read Error: Invalid Road Type [" + tokens[1] + "]");
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("RETURN_TYPES"))
				{
					try
					{
						_returnBlocks.add(Integer.parseInt(tokens[1]));
					}
					catch (Exception e)
					{
						System.out.println("Track Data Read Error: Invalid Return Type [" + tokens[1] + "]");
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("SPAWN_DIRECTION"))
				{
					try
					{
						_yaw = Float.valueOf(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("Track Data Read Error: Invalid Yaw [" + tokens[1] + "]");
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("SPAWNS"))
				{
					for (int i=1 ; i<tokens.length ; i++)
					{
						Location loc = StrToLoc(tokens[i]);
						if (loc == null)	continue;
						
						_kartStart.add(loc);
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("PROGRESS"))
				{
					for (int i=1 ; i<tokens.length ; i++)
					{
						Location loc = StrToLoc(tokens[i]);
						if (loc == null)	continue;
						
						_trackProgress.add(loc);
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("RETURNS"))
				{
					for (int i=1 ; i<tokens.length ; i++)
					{
						Location loc = StrToLoc(tokens[i]);
						if (loc == null)	continue;
						
						_returnPoints.add(loc);
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("ITEMS"))
				{
					for (int i=1 ; i<tokens.length ; i++)
					{
						Location loc = StrToLoc(tokens[i]);
						if (loc == null)	continue;
						
						_itemBlocks.add(new TrackItem(loc));
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("CREATURES"))
				{
					for (int i=1 ; i<tokens.length ; i++)
					{
						String[] ents = tokens[i].split("@");
						
						Location loc = StrToLoc(ents[1]);
						if (loc == null)	continue;
						
						loc = loc.getBlock().getLocation();
						loc.subtract(0, 1, 0);
						 
						TrackEntity ent = null;
						
						if (ents[0].equalsIgnoreCase("BOMB"))	ent = new Bomb(this, loc);
						if (ents[0].equalsIgnoreCase("MOLE"))	ent = new Mole(this, loc);
						if (ents[0].equalsIgnoreCase("FISH"))	ent = new Silverfish(this, loc);
						if (ents[0].equalsIgnoreCase("TRAIN"))	ent = new Train(this, loc);

						if (ent != null)
						{
							_creatures.add(ent);
						}
						else
						{
							System.out.println("Track Data Read Error: Invalid Track Entity [" + ents[0] + "]");
						}
					}
				}
				
				else if (tokens[0].equalsIgnoreCase("JUMPS"))
				{
					for (int i=1 ; i<tokens.length ; i++)
					{
						String[] jumps = tokens[i].split("@");
						
						Location loc = StrToLoc(jumps[1]);
						if (loc == null)	continue;
						
						loc = loc.getBlock().getLocation();
						
						_jumps.put(loc, Double.parseDouble(jumps[0]));
					}
				}
				else if (tokens[0].equalsIgnoreCase("MIN_X"))
				{
					try
					{
						MinX = Integer.parseInt(tokens[1]);
						CurX = MinX;
					}
					catch (Exception e)
					{
						System.out.println("Track Data Read Error: Invalid MinX [" + tokens[1] + "]");
					}
					
				}
				else if (tokens[0].equalsIgnoreCase("MAX_X"))
				{
					try
					{
						MaxX = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("Track Data Read Error: Invalid MaxX [" + tokens[1] + "]");
					}
				}
				else if (tokens[0].equalsIgnoreCase("MIN_Z"))
				{
					try
					{
						MinZ = Integer.parseInt(tokens[1]);
						CurZ = MinZ;
					}
					catch (Exception e)
					{
						System.out.println("Track Data Read Error: Invalid MinZ [" + tokens[1] + "]");
					}
				}
				else if (tokens[0].equalsIgnoreCase("MAX_Z"))
				{
					try
					{
						MaxZ = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("Track Data Read Error: Invalid MaxZ [" + tokens[1] + "]");
					}
				}
			}

			in.close();
			
			GP.Manager.TrackManager.LoadTrack(this);
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public boolean LoadChunks(long maxMilliseconds)
	{
		long startTime = System.currentTimeMillis();
		
		for (; CurX <= MaxX; CurX += 16)
        {	
            for (; CurZ <= MaxZ; CurZ += 16) 
            {
    			if (System.currentTimeMillis() - startTime >= maxMilliseconds)
    				return false;
                
    			World.getChunkAt(new Location(World, CurX, 0, CurZ));
            }
            
            CurZ = MinZ;
        }
		
    	return true;
	}
	
	public void Uninitialize() 
	{
		//Wipe Storage
		_kartStart.clear();
		_returnPoints.clear();
		_trackProgress.clear();
		_itemBlocks.clear();
		_jumps.clear();
		_creatures.clear();
		_trackBlocks.clear();
		_returnBlocks.clear();
		
		//Wipe World
		MapUtil.UnloadWorld(GetGP().Manager.GetPlugin(), World);
		MapUtil.ClearWorldReferences(World.getName());
		FileUtil.DeleteFolder(new File(World.getName()));
		
		World = null;
	}

	private Location StrToLoc(String loc)
	{
		String[] coords = loc.split(",");
		
		try
		{
			return new Location(World, Integer.valueOf(coords[0])+0.5, Integer.valueOf(coords[1]), Integer.valueOf(coords[2])+0.5);
		}
		catch (Exception e)
		{
			System.out.println("Track Data Read Error: Invalid Location String [" + loc + "]");
		}
	
		return null;
	}

	public void SpawnTeleport()
	{
		Track prevTrack = GP.GetTrack(_trackId - 1);
		
		//Use Previous Positions
		if (prevTrack != null)
		{
			int i = 0;
			for (Kart kart : prevTrack.GetPositions())
			{
				Location loc = GetSpawns().get(i);
				loc.setYaw(GetYaw());
				loc.setPitch(30f);
				
				//Battle
				if (GetGP() instanceof GPBattle)
				{
					Vector dir = UtilAlg.getTrajectory(kart.GetDriver().getLocation(), GetProgress().get(0));
					loc.setYaw(UtilAlg.GetYaw(dir));
					loc.setPitch(UtilAlg.GetPitch(dir));
				}
								
				_teleport.TP(kart.GetDriver(), loc, false);
				i++;
			}
		}
		//Use Join Positions
		else
		{
			int i = 0;
			for (Player player : GetGP().GetPlayers())
			{
				Location loc = GetSpawns().get(i);
				loc.setYaw(GetYaw());
				loc.setPitch(30f);
				
				//Battle
				if (GetGP() instanceof GPBattle)
				{
					Vector dir = UtilAlg.getTrajectory(player.getLocation(), GetProgress().get(0));
					loc.setYaw(UtilAlg.GetYaw(dir));
					loc.setPitch(UtilAlg.GetPitch(dir));
				}
				
				_teleport.TP(player, loc, false);
				i++;
			}
		}
	}

	public void RemoveKart(Kart kart) 
	{
		_scores.remove(kart);
		_positions.remove(kart);
	}

	public void ChunkUnload(ChunkUnloadEvent event) 
	{
		if (World == null)
			return;
		
		if (!event.getWorld().equals(World))
			return;
		
		event.setCancelled(true);
	}

	public void ChunkLoad(ChunkPreLoadEvent event) 
	{
		if (World == null)
			return;
		
		if (!event.GetWorld().equals(World))
			return;
		
		int x = event.GetX();
		int z = event.GetZ();
		
		if (x >= MinX >> 4 && x <= MaxX >> 4 && z >= MinZ >> 4 && z <= MaxZ >> 4)
		{
			return;
		}
		
		event.setCancelled(true);
	}

	public Recharge GetRecharge()
	{
		return _recharge;
	}
}
