package nautilus.game.arcade.game.games.halloween.waves;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.NamedAudio;

public abstract class WaveBase 
{
	protected Halloween Host;
	
	protected String _name;
	protected boolean _displayWaveNumber = true;
	protected boolean _announceWaveChat = true;
	protected boolean _announceWaveTitle = true;
	protected NamedAudio _audio;
	
	protected long _start;
	protected long _duration;
	
	private int _tick = 0;
	
	protected List<Location> _spawns;
	
	private boolean _spawnBeacons = true;
	private boolean _announceStart = true;
	private boolean _displayProgress = true;
	
	protected String _titleColor = C.cYellow;
	
	protected String[] _desc = null;
	
	public WaveBase(Halloween host, String name, long duration, List<Location> spawns, NamedAudio audio)
	{
		Host = host;
		
		_name = name;
		_audio = audio;
		
		_start = System.currentTimeMillis();
		_duration = duration;
		
		_spawns = spawns;
	}
	
	public Location GetSpawn()
	{
		return _spawns.get(UtilMath.r(_spawns.size()));
	}
	
	public boolean Update(int wave)
	{
		//End
		if (_tick > 0 && UtilTime.elapsed(_start, _duration) && CanEnd())
		{
			System.out.println("Wave " + wave + " has ended.");
			return true;
		}
		
		//Start
		if (_tick == 0)
			_start = System.currentTimeMillis();
			
		//Announce
		if (_tick == 0)
		{
			System.out.println("Wave " + wave + " has started.");
			if(_announceStart)
			{	
				if(_announceWaveChat) 
				{
					String number = C.cRed + C.Bold + "Wave " + wave;
					String name = C.cYellow + _name;
					if(name != null)
					{
						number += ": ";
					}
					if(_name == null) name = "";
					if(!_displayWaveNumber) number = "";
					
					String waveName = number + name;
					
					Host.Announce(waveName);
				}
				if(_desc != null)
				{
					for(String l : _desc)
					{
						Host.Announce(C.cGray + "  ● " + C.cYellow + l);
					}
				}
				
				if(_announceWaveTitle) 
				{
					String waveTitle = _displayWaveNumber ? _titleColor + "Wave " + wave : _name;
					String waveSub = _displayWaveNumber ? _name : "";
					UtilTextMiddle.display(waveTitle, waveSub, 10, 100, 20);
				}
				
				
				if (_audio != null)
				{
					Host.playSound(_audio);
				}
			}
		}
			
		//Display
		if(_displayProgress)
		{
			for (Player player : UtilServer.getPlayers())
			{
				player.setExp(Math.min(0.999f, (float)(_duration - (System.currentTimeMillis() - _start)) / (float)_duration));
			}
		}
		
		//Spawn Beacons
		if (_tick == 0 && _spawnBeacons)
			SpawnBeacons(_spawns);

		//Spawn
		Host.CreatureAllowOverride = true;	
		Spawn(_tick++);
		Host.CreatureAllowOverride = false;
		
		return false;
	}
	
	public void SpawnBeacons(List<Location> locs)
	{
		//Average Location
		Vector total = new Vector(0,0,0);
		for (Location loc : locs)
			total.add(loc.toVector());
		total.multiply((double)1/(double)locs.size());
		
		//Beacon
		Block block = total.toLocation(locs.get(0).getWorld()).getBlock().getRelative(BlockFace.DOWN);
		Host.Manager.GetBlockRestore().add(block, 138, (byte) 0, _duration);
		
		for (int x=-1 ; x<=1 ; x++)
			for (int z=-1 ; z<=1 ; z++)
				Host.Manager.GetBlockRestore().add(block.getRelative(x, -1, z), 42, (byte) 0, _duration);
		
		//Clear Laser
		while (block.getY() < 250)
		{
			block = block.getRelative(BlockFace.UP);
			if (block.getType() != Material.AIR)
				Host.Manager.GetBlockRestore().add(block, 0, (byte) 0, _duration);
		}
	}
	
	public boolean CanEnd() 
	{
		return true;
	}
	
	public void setAnnounceStart(boolean announceStart)
	{
		_announceStart = announceStart;
	}
	
	public void setDisplayProgress(boolean displayProgress)
	{
		_displayProgress = displayProgress;
	}
	
	public void setSpawnBeacons(boolean spawnBeacons)
	{
		_spawnBeacons = spawnBeacons;
	}
	
	public boolean getAnnounceStart()
	{
		return _announceStart;
	}
	
	public boolean getDisplayProgress()
	{
		return _displayProgress;
	}
	
	public boolean getSpawnBeacons()
	{
		return _spawnBeacons;
	}

	public abstract void Spawn(int tick);
}
