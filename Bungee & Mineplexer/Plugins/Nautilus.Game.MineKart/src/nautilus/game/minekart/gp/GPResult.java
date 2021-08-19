package nautilus.game.minekart.gp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mineplex.core.common.util.FileUtil;
import mineplex.core.common.util.FireworkUtil;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.WorldChunkLoader;
import mineplex.core.common.util.WorldLoadInfo;
import mineplex.core.common.util.WorldUtil;
import mineplex.core.donation.DonationManager;
import mineplex.core.fakeEntity.FakeEntity;
import mineplex.core.fakeEntity.FakePlayer;
import nautilus.game.minekart.kart.Kart;
import nautilus.minecraft.core.utils.ZipUtil;
import net.minecraft.server.v1_7_R1.EntityPlayer;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class GPResult 
{
	private GPManager Manager;
	
	private String _file = "Result";
	
	private World _world;
	private GP _gp;
	
	private boolean _initialized = false;
	
	private long _time;
	
	private Kart _first;
	private Kart _second;
	private Kart _third;
	
	private List<Location> _fireworkLocations;
	
	public GPResult(GP gp, DonationManager manager) 
	{
		Manager = gp.Manager;
		
		_gp = gp;
		
		_time = System.currentTimeMillis();
		
		List<Kart> sortedScores = new ArrayList<Kart>(_gp.GetKarts());
		Collections.sort(sortedScores, new ScoreComparator(_gp));
		
		int buffer = (_gp instanceof GPBattle ? 9 : 14);
		
		if (_gp.GetTrack().GetPositions().size() > 0 && sortedScores.size() > 0)
		{
			_first = sortedScores.get(0);
			manager.RewardGems(null, "Earned Minekart", _first.GetDriver().getName(), 8 * _gp.GetPlayers().size() + buffer);
		}
		
		buffer += 2;
		
		if (_gp.GetTrack().GetPositions().size() > 1 && sortedScores.size() > 1)
		{
			_second = sortedScores.get(1);
			manager.RewardGems(null, "Earned Minekart", _second.GetDriver().getName(), 5 * _gp.GetPlayers().size() + buffer);
		}
		
		buffer += 2;
		
		if (_gp.GetTrack().GetPositions().size() > 2 && sortedScores.size() > 2)
		{
			_third = sortedScores.get(2);
			manager.RewardGems(null, "Earned Minekart", _third.GetDriver().getName(), 2 * _gp.GetPlayers().size() + buffer);
		}
		
		buffer += 2;
		
		for (int i = 3;  i < sortedScores.size(); i++)
		{
			manager.RewardGems(null, "Earned Minekart", sortedScores.get(i).GetDriver().getName(), buffer);
		}
		
		_fireworkLocations = new ArrayList<Location>(5);
		
		Initialise();
	}

	public void TeleportPlayers() 
	{
		Location loc = new Location(_world, 10, 23, -22);
		loc.setYaw(180);
		
		FakePlayer firstPlayer = null;
		FakePlayer secondPlayer = null;
		FakePlayer thirdPlayer = null;
		
		FakeEntity firstKart = null;
		FakeEntity secondKart = null;
		FakeEntity thirdKart = null;
		
		if (_first != null)
		{
			Location location = new Location(_world, 10, 25, -29);
			firstPlayer = new FakePlayer(_first.GetDriver().getName(), location);
			firstKart = new FakeEntity(_first.GetEntity().GetEntityType(), location);
		}
		
		if (_second != null)
		{
			Location location = new Location(_world, 6, 24, -29);
			secondPlayer = new FakePlayer(_second.GetDriver().getName(), location);
			secondKart = new FakeEntity(_second.GetEntity().GetEntityType(), location);
		}
		
		if (_third != null)
		{
			Location location = new Location(_world, 14, 23, -29);
			thirdPlayer = new FakePlayer(_third.GetDriver().getName(), location);
			thirdKart = new FakeEntity(_third.GetEntity().GetEntityType(), location);
		}
		
		for (Player player : _gp.GetPlayers())
		{
			if (!player.isOnline())
				continue;

			Manager.GetTeleport().TP(player, loc);
			
			EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
			
			if (_first != null)
			{
				entityPlayer.playerConnection.sendPacket(firstPlayer.Spawn());
				entityPlayer.playerConnection.sendPacket(firstKart.Spawn());
				entityPlayer.playerConnection.sendPacket(firstKart.SetPassenger(firstPlayer.GetEntityId()));
			}
			
			if (_second != null)
			{
				entityPlayer.playerConnection.sendPacket(secondPlayer.Spawn());
				entityPlayer.playerConnection.sendPacket(secondKart.Spawn());
				entityPlayer.playerConnection.sendPacket(secondKart.SetPassenger(secondPlayer.GetEntityId()));
			}
			
			if (_third != null)
			{
				entityPlayer.playerConnection.sendPacket(thirdPlayer.Spawn());
				entityPlayer.playerConnection.sendPacket(thirdKart.Spawn());
				entityPlayer.playerConnection.sendPacket(thirdKart.SetPassenger(thirdPlayer.GetEntityId()));
			}
		}
	}
	
	public void Initialise()
	{
		UtilServer.getServer().getScheduler().runTaskAsynchronously(Manager.GetPlugin(), new Runnable()
		{
			public void run()
			{
				//Unzip
				UnzipWorld();
				
				SetLocations();
				
				//Load Track Data Sync
				UtilServer.getServer().getScheduler().runTask(Manager.GetPlugin(), new Runnable()
				{
					public void run()
					{
						WorldChunkLoader.AddWorld(new WorldLoadInfo(_world, -5, -6, 5, 2), new Runnable()
						{
							public void run()
							{
								_initialized = true;
								TeleportPlayers();
							}
						});
					}
				});
			}
		});
	}

	protected void SetLocations()
	{
		_fireworkLocations.add(new Location(_world, -9.5, 42, -54.5));
		_fireworkLocations.add(new Location(_world, 29.5, 42, -54.5));
		_fireworkLocations.add(new Location(_world, 32.5, 43, -77.5));
		_fireworkLocations.add(new Location(_world, -12.5, 43, -77.5));
		_fireworkLocations.add(new Location(_world, 10, 61, -61));
	}

	public void UnzipWorld() 
	{
		//Unzip
		String folder = _gp.GetId() + "-" + _gp.GetSet().GetName() + "-" + _file;
		new File(folder).mkdir();
		new File(folder + File.separatorChar + "region").mkdir();
		new File(folder + File.separatorChar + "data").mkdir();
		ZipUtil.UnzipToDirectory(_file + ".zip", folder);

		//Start World
		_world = WorldUtil.LoadWorld(new WorldCreator(folder));
	}
	
	public void Uninitialise() 
	{
		MapUtil.UnloadWorld(Manager.GetPlugin(), _world);
		MapUtil.ClearWorldReferences(_world.getName());
		FileUtil.DeleteFolder(new File(_world.getName()));
		
		_first = null;
		_second = null;
		_third = null;
		
		_fireworkLocations.clear();
		
		_world = null;
	}
	
	public boolean End()
	{
		if (!_initialized)
			return false;
		
		if (!UtilTime.elapsed(_time, 30000))
		{
			FireworkUtil.LaunchRandomFirework(_fireworkLocations.get(RandomUtils.nextInt(5)));
			
			return false;
		}
		
		for (Player player : _gp.GetPlayers())
		{
			Manager.Portal.SendPlayerToServer(player, "Lobby");
		}
	
		//Clean
		_gp.GetPlayers().clear();
		_gp = null;
		Uninitialise();
		
		return true;
	}

	public void RemovePlayer(Player player) 
	{
		_gp.RemovePlayer(player, null);
	}
}
