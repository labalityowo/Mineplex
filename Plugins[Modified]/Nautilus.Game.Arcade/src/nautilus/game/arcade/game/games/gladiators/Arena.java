package nautilus.game.arcade.game.games.gladiators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.gladiators.events.PlayerChangeArenaEvent;

/**
 * Created by William (WilliamTiger).
 * 07/12/15
 */
public class Arena
{

	private Gladiators _host;
	private ArenaType _colour;
	private Location _mid;
	private ArrayList<Location> _spawns;

	private Arena _parent;
	private Arena[] _childs = new Arena[2];
	private boolean _isUsed;

	private ArrayList<Location> _doorBlocks;
	private boolean _isOpenDoor;
	private boolean _doBye;

	private ArrayList<Player> _pastPlayers;

	private ArenaState _state;
	private long _stateTime;

	private ArrayList<Player> _alreadyAlertedPleaseWait;
	private boolean _alertedAlready2;
	
	private boolean _poison;

	private HashMap<Player, ArrayList<ParticleData>> _particles;

	public Arena(Gladiators host, Location mid, ArenaType colour)
	{
		_host = host;
		_mid = mid;
		_colour = colour;
		_spawns = new ArrayList<>();
		_parent = null;
		_isUsed = false;
		_doorBlocks = new ArrayList<>();
		_isOpenDoor = false;
		_pastPlayers = new ArrayList<>();
		_state = ArenaState.EMPTY;
		_stateTime = System.currentTimeMillis();
		_particles = new HashMap<>();
		_doBye = false;
		_alreadyAlertedPleaseWait = new ArrayList<>();
		_alertedAlready2 = false;
		_poison = false;
		
		setupSpawns();
	}

	public boolean isDoBye()
	{
		return _doBye;
	}

	public void setDoBye(boolean doBye)
	{
		_doBye = doBye;
	}

	public Arena getParent()
	{
		return _parent;
	}

	public long getStateTime()
	{
		return _stateTime;
	}

	public void setStateTime(long stateTime)
	{
		_stateTime = stateTime;
	}

	public void setParent(Arena parent)
	{
		_parent = parent;
	}

	public Arena getChildAt(int index)
	{
		return _childs[index];
	}

	public Arena[] getChilds()
	{
		return _childs;
	}

	public int getCapacity()
	{
		int cap = _childs.length;

		for(Arena child : _childs)
		{
			if(child != null)
				if(child.isUsed()) cap--;
		}

		return cap;
	}

	public ArenaState getState()
	{
		return _state;
	}

	public void setState(ArenaState state)
	{
		_state = state;
	}

	public ArrayList<Location> getDoorBlocks()
	{
		return _doorBlocks;
	}

	public void setChild(int index, Arena child)
	{
		_childs[index] = child;
		child.setParent(this);
	}

	public void getUsageMap(HashMap<Arena, Integer> used)
	{
		if(isUsed()) used.put(this, getCapacity());

		for(Arena child : _childs)
		{
			if(child != null) child.getUsageMap(used);
		}
	}

	public boolean areChildrenUsed()
	{
		for(Arena child : _childs)
		{
			if(child != null)
				if(!child.isUsed()) return false;
		}

		return true;
	}

	public Arena getUnusedChild()
	{
		for(Arena child : _childs)
		{
			if(child != null)
				if(!child.isUsed()) return child;
		}

		return null;
	}

	private void setupSpawns()
	{
		ArrayList<Location> possible = (ArrayList<Location>) _host.WorldData.GetDataLocs("BLACK").clone();
		_mid.setY(UtilAlg.findClosest(_mid, possible).getY());

		_spawns.add(correctFace(UtilAlg.findClosest(_mid, possible)));
		possible.remove(_spawns.get(0));
		_spawns.add(correctFace(UtilAlg.findClosest(_mid, possible)));
	}

	private Location correctFace(Location l)
	{
		l.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(l, _mid)));
		l.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(l, _mid)));
		return l;
	}

	public Gladiators getHost()
	{
		return _host;
	}

	public ArenaType getColour()
	{
		return _colour;
	}

	public Location getMid()
	{
		return _mid;
	}

	public ArrayList<Location> getSpawns()
	{
		return _spawns;
	}

	public ArrayList<Location> capacitySpawns()
	{
		ArrayList<Location> ret = new ArrayList<>();

		if (getCapacity() == 0) return ret;
		if (getCapacity() == 1)
		{
			ret.add(_spawns.get(0));
			return ret;
		}
		if (getCapacity() == 2)
		{
			ret.add(_spawns.get(0));
			ret.add(_spawns.get(1));
			return ret;
		}

		return ret;
	}

	public ArrayList<Player> getPastPlayers()
	{
		return _pastPlayers;
	}

	public boolean isUsed()
	{
		return _isUsed;
	}

	public void setIsUsed(boolean isUsed)
	{
		_isUsed = isUsed;
	}

	public void closeDoor()
	{
		_host.Manager.getScheduler().scheduleSyncDelayedTask(_host.Manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				for (Location loc : _doorBlocks)
					loc.getBlock().setType(Material.OBSIDIAN);

				// Lag exploit check
				for (Player p : getPastPlayers())
				{
					Arena closest = _host.getArenaByMid(UtilAlg.findClosest(p.getLocation(), _host.getAllArenaMids()));
					if (closest != _host.getPlayerArenas().get(p))
						p.teleport(UtilAlg.findClosest(p.getLocation(), getSpawns()));
				}
			}
		}, 5L);
	}

	public boolean isOpenDoor()
	{
		return _isOpenDoor;
	}

	public void openDoor()
	{
		_isOpenDoor = true;

		_host.Manager.getScheduler().scheduleSyncDelayedTask(_host.Manager.getPlugin(), () -> {

			for (Location loc : _doorBlocks)
			{
				loc.getBlock().setType(Material.AIR);
			}

		}, 5L);
	}

	public void update()
	{
		if (getPastPlayers().size() <= 0)
		{
			setState(ArenaState.EMPTY);
			return; // Empty check.
		}

		if (_state.equals(ArenaState.WAITING))
		{
			_poison = false;
			
			if (_host.getRoundState() != RoundState.FIGHTING)
				return;

			for (Player p : getPastPlayers())
			{
				if (_alreadyAlertedPleaseWait.contains(p))
					continue;

				UtilTextMiddle.display(C.cAqua + "Please Wait", "The next round will start shortly", 0, 20 * 120, 0, p); // 2 min

				_alreadyAlertedPleaseWait.add(p);
			}
		}
		else if (_state.equals(ArenaState.FIGHTING))
		{
			if (getPastPlayers().size() == 1)
			{
				openDoor();

				setState(ArenaState.RUNNING);
				setStateTime(System.currentTimeMillis());
				return;
			}

			if (!UtilTime.elapsed(_stateTime, 60000))
				return; // No poison yet.

			_poison = true;
			for (Player p : getPastPlayers())
			{
				UtilTextBottom.display(C.cRed + C.Bold + "YOU ARE POISONED! KEEP FIGHTING!", p);
				_host.Manager.GetDamage().NewDamageEvent(p, null, null, EntityDamageEvent.DamageCause.CUSTOM, 1D, false, true, true, "Health Loss", "Health Loss");
			}
		}
		else if (_state.equals(ArenaState.RUNNING))
		{
			for (Player p : getPastPlayers())
			{
				if (_alertedAlready2)
					continue;

				UtilTextMiddle.display(C.cGreen + "Next Battle", "Follow the particles", 0, 20 * 120, 0, p); // 2 min
			}

			_alertedAlready2 = true;

			if (UtilTime.elapsed(_stateTime, 15000))
				handleSlowMovers();
		}
	}

	public void updateTick()
	{
		if (_state.equals(ArenaState.RUNNING))
		{
			for (Player p : getPastPlayers())
				showParticles(p);
		}
	}

	private void showParticles(Player p)
	{
		if (!getPastPlayers().contains(p) || !_state.equals(ArenaState.RUNNING))
		{
			_particles.remove(p);
			return;
		}

		//New Trails
		if (Recharge.Instance.use(p, "Particle Trail", 3000, false, false))
		{
			if (!_particles.containsKey(p))
				_particles.put(p, new ArrayList<ParticleData>());

			Location end = UtilAlg.findClosest(_mid, _host.WorldData.GetDataLocs("PINK"));

			_particles.get(p).add(new ParticleData(p, end));
		}

		//Old Trails
		if (_particles.containsKey(p) && !_particles.get(p).isEmpty())
		{
			Iterator<ParticleData> trailIter = _particles.get(p).iterator();

			while (trailIter.hasNext())
			{
				ParticleData data = trailIter.next();

				//Returns true if its hit the endpoint
				if (data.update())
					trailIter.remove();
			}
		}
	}

	private void handleSlowMovers()
	{
		setState(ArenaState.ENDED);

		Arena next = _host.getArenaByMid(UtilAlg.findClosest(_mid, _host.getAllArenaMidsOfType(_host.getNextColour(_colour))));

		for (Player p : new ArrayList<Player>(_pastPlayers))
		{
			// TP after 15 seconds of waiting.

			p.teleport(UtilAlg.getLocationAwayFromPlayers(next.getSpawns(), _host.GetPlayers(true)).clone());
			//p.sendMessage("HANDLE SLOW MOVERS METHOD!");
			_host.Manager.getPluginManager().callEvent(new PlayerChangeArenaEvent(p, next, this));
			closeDoor();
			_host.setPlayerArena(p, next);
			UtilTextBottom.display("§c§lTELEPORTED! YOU TOOK TOO LONG!", p);
			next.setDoBye(true);
		}

		_pastPlayers.clear(); // Clear out the un-used players.
	}
	
	public boolean isPoisoned()
	{
		return _poison;
	}
}