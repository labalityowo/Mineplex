package nautilus.game.arcade.game.games.quiver.module;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;

public class ModulePayload extends QuiverTeamModule implements Listener
{

	private static final double PAYLOAD_CAPTURE_RANGE = 4;
	private static final float PAYLOAD_VELOCITY = 0.2F;
	private static final int PAYLOAD_MARKER_MAX_DISTANCE = 2;
	private static final int PAYLOAD_RENGERATION_DURATION = 3 * 20;
	private static final int PAYLOAD_RENGERATION_AMPLIFIER = 1;

	private static final float ULTIMATE_PERCENTAGE_PAYLOAD = 0.2F;

	private static final String DATA_POINT_MARKER_START = "PINK";
	public static final String DATA_POINT_RED = "RED";
	public static final String DATA_POINT_BLUE = "BLUE";
	public static final String DATA_POINT_PAYLOAD = "BLACK";

	private Minecart _minecart;

	private GameTeam _teamDirection;

	private int _targetIndex;

	private boolean _hasMoved;
	private boolean _recentlyChanged;

	private double _totalDistance;

	private List<Location> _pathMarkers = new ArrayList<>();

	private PayloadState _payloadState;
	private Team _payloadTeam;

	public ModulePayload(QuiverTeamBase base)
	{
		super(base);

		_payloadState = PayloadState.NONE;
	}

	@Override
	public void setup()
	{
		getBase().Manager.registerEvents(this);
		
		_payloadTeam = getBase().GetScoreboard().getHandle().registerNewTeam("payloadTeam");
		_minecart = getBase().WorldData.World.spawn(getBase().WorldData.GetDataLocs(DATA_POINT_PAYLOAD).get(0), Minecart.class);

		_minecart.setDisplayBlock(new MaterialData(Material.TNT));
		_payloadTeam.addEntry(_minecart.getUniqueId().toString());

		/*
		 * The payload which is represented as a minecart follows a linear path
		 * connecting the two gameTeam's bases. The logic used to calculate the
		 * path that the minecart takes is done below.
		 * 
		 * Initially we need to get a constant point we will use to start our
		 * calculations. This is a DATA_POINT_MARKER_START coloured data point.
		 * This is placed at the red gameTeam's base, at the end of the track.
		 */

		Location start = getBase().WorldData.GetDataLocs(DATA_POINT_MARKER_START).get(0);
		Location last = start;
		ArrayList<Location> dataPoints = new ArrayList<>();

		/*
		 * The dataPoints ArrayList is an unordered list of all the data points.
		 * We add the start location and all red and blue data points.
		 * 
		 * We use red and blue data points so that it is easier for the builders
		 * to see which direction the minecart would move when a certain
		 * gameTeam moved the minecart.
		 * 
		 * Data points are placed above the track, they should be placed at
		 * intervals along the track where it is ensured that the next data
		 * point along the track is the closest relative to all other data
		 * points in the map as well on any point where the track curves.
		 * 
		 */

		dataPoints.add(start);
		dataPoints.addAll(getBase().WorldData.GetDataLocs(DATA_POINT_RED));
		dataPoints.addAll(getBase().WorldData.GetDataLocs(DATA_POINT_BLUE));

		/*
		 * While there are locations still left in the list, we search for the
		 * nearest different data point and add it to a new list, this list
		 * contains all the data points sorted in the correct order of the
		 * track's path.
		 */

		while (!dataPoints.isEmpty())
		{
			Location dataPoint = UtilAlg.findClosest(last, dataPoints);

			_pathMarkers.add(dataPoint);
			dataPoints.remove(dataPoint);
			last = dataPoint;
		}

		/*
		 * We need to calculate the total linear distance between all the stored
		 * data points. This is used later when displaying the Dragon/Wither
		 * progression bar.
		 */
		for (int i = 1; i < _pathMarkers.size(); i++)
		{
			_totalDistance += UtilMath.offset(_pathMarkers.get(i - 1), _pathMarkers.get(i));
		}
	}

	@Override
	public void update(UpdateType updateType)
	{
		if (updateType != UpdateType.FAST || _minecart == null)
		{
			return;
		}
		
		if (_payloadState.equals(PayloadState.RESTARTING))
		{
			UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, _minecart.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 1, 10, ViewDist.LONG);
			return;
		}
		
		/*
		 * In order to determine which direction the payload will move we
		 * calculate how many players are within PAYLOAD_CAPURE_RANGE blocks of
		 * the payload.
		 */

		int gameTeamACount = 0, gameTeamBCount = 0;

		for (Player player : UtilPlayer.getNearby(_minecart.getLocation(), PAYLOAD_CAPTURE_RANGE))
		{
			GameTeam gameTeam = getBase().GetTeam(player);

			if (getBase().GetTeamList().get(0).equals(gameTeam))
			{
				gameTeamACount++;
			}
			else
			{
				gameTeamBCount++;
			}

			player.removePotionEffect(PotionEffectType.REGENERATION);
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PAYLOAD_RENGERATION_DURATION, PAYLOAD_RENGERATION_AMPLIFIER));
			getBase().getQuiverTeamModule(ModuleUltimate.class).incrementUltimate(player, ULTIMATE_PERCENTAGE_PAYLOAD);
		}

		UtilTextTop.display(getTopBar(), UtilServer.getPlayers());

		/*
		 * _recentlyChanged is used to show that on the targetIndex needs to be
		 * updated as the payload's direction has changed.
		 */

		if (gameTeamACount > gameTeamBCount && gameTeamBCount == 0)
		{
			if (_teamDirection != null)
			{
				if (!_teamDirection.equals(getBase().GetTeamList().get(0)))
				{
					setMinecartTeam(getBase().GetTeamList().get(0));
					_recentlyChanged = true;
				}
			}

			_teamDirection = getBase().GetTeamList().get(0);
		}
		else if (gameTeamACount < gameTeamBCount && gameTeamACount == 0)
		{
			if (_teamDirection != null)
			{
				if (!_teamDirection.equals(getBase().GetTeamList().get(1)))
				{
					setMinecartTeam(getBase().GetTeamList().get(1));
					_recentlyChanged = true;
				}
			}

			_teamDirection = getBase().GetTeamList().get(1);
		}
		else
		{
			if (gameTeamACount > 0 && gameTeamBCount > 0)
			{
				_payloadState = PayloadState.CONTESTED;
			}
			else
			{
				_payloadState = PayloadState.NONE;
			}

			setMinecartTeam(_payloadState);
			UtilAction.zeroVelocity(_minecart);
			return;
		}

		setMinecartTeam(_teamDirection);
		_payloadState = PayloadState.MOVING;

		if (_teamDirection.equals(getBase().GetTeamList().get(0)))
		{
			// If the minecart has never moved
			if (!_hasMoved)
			{
				_targetIndex = _pathMarkers.size() / 2;
			}
			else if (isMinecartNearMarker(_pathMarkers.get(_targetIndex)) || _recentlyChanged)
			{
				_targetIndex++;
				_recentlyChanged = false;
			}
		}
		else
		{
			// If the minecart has never moved
			if (!_hasMoved)
			{
				_targetIndex = _pathMarkers.size() / 2 - 2;
			}
			else if (isMinecartNearMarker(_pathMarkers.get(_targetIndex)) || _recentlyChanged)
			{
				_targetIndex--;
				_recentlyChanged = false;
			}
		}

		/*
		 * The minecart's velocity is set to the vector between the the
		 * minecart's current location and the next data point it will reach.
		 */

		_minecart.setVelocity(UtilAlg.getTrajectory(_minecart.getLocation(), _pathMarkers.get(_targetIndex)).normalize().multiply(PAYLOAD_VELOCITY));
		_hasMoved = true;
	}
	
	@Override
	public void finish()
	{
		UtilServer.Unregister(this);
	}

	public boolean isMinecartNearMarker(Location marker)
	{
		return isMinecartNearMarker(marker, PAYLOAD_MARKER_MAX_DISTANCE);
	}

	public boolean isMinecartNearMarker(Location marker, double distance)
	{
		return UtilMath.offset(_minecart.getLocation(), marker) < distance;
	}

	public double getTrackDistanceToMarker(Location marker)
	{
		return getTrackDistanceToMarker(marker, _teamDirection);
	}

	public double getTrackDistanceToMarker(Location marker, GameTeam gameTeam)
	{
		double distance = 0;

		if (_minecart == null)
		{
			return distance;
		}

		if (gameTeam == null)
		{
			return _totalDistance / 2;
		}

		if (_targetIndex > _pathMarkers.size() - 1 || _targetIndex < 0)
		{
			return distance;
		}

		if (gameTeam.equals(_teamDirection))
		{
			distance += UtilMath.offset(_minecart.getLocation(), _pathMarkers.get(_targetIndex));
		}

		if (getBase().GetTeamList().get(0).equals(gameTeam))
		{
			if (!gameTeam.equals(_teamDirection))
			{
				if (_targetIndex < _pathMarkers.size() - 1)
				{
					distance += UtilMath.offset(_minecart.getLocation(), _pathMarkers.get(_targetIndex + 1));
				}
				else
				{
					distance += UtilMath.offset(_minecart.getLocation(), _pathMarkers.get(_pathMarkers.size() - 1));
				}
			}

			for (int i = _targetIndex + 1; i < _pathMarkers.size(); i++)
			{
				distance += UtilMath.offset(_pathMarkers.get(i - 1), _pathMarkers.get(i));
			}
		}
		else
		{
			if (!gameTeam.equals(_teamDirection))
			{
				if (_targetIndex > 0)
				{
					distance += UtilMath.offset(_minecart.getLocation(), _pathMarkers.get(_targetIndex - 1));
				}
				else
				{
					// distance += UtilMath.offset(_minecart.getLocation(),
					// _pathMarkers.get(0));
				}
			}

			for (int i = _targetIndex - 1; i >= 0; i--)
			{
				distance += UtilMath.offset(_pathMarkers.get(i + 1), _pathMarkers.get(i));
			}
		}

		return distance;
	}

	public Location getDestination(GameTeam gameTeam)
	{
		if (getBase().GetTeamList().get(0).equals(gameTeam))
		{
			return _pathMarkers.get(_pathMarkers.size() - 1);
		}

		return _pathMarkers.get(0);
	}

	public void resetMinecart()
	{
		_payloadState = PayloadState.RESTARTING;
		setMinecartTeam(_payloadState);
		_hasMoved = false;
		_recentlyChanged = false;
		UtilAction.zeroVelocity(_minecart);
	}

	@EventHandler
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event)
	{
		if (event.getVehicle() instanceof Minecart)
		{
			event.setCollisionCancelled(true);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event)
	{
		if (event.getVehicle() instanceof Minecart)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event)
	{
		if (event.getVehicle() instanceof Minecart)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event)
	{
		Vehicle vehicle = event.getVehicle();

		if (vehicle instanceof Minecart && vehicle.getVelocity().lengthSquared() > (PAYLOAD_VELOCITY * PAYLOAD_VELOCITY))
		{
			vehicle.setVelocity(vehicle.getVelocity().normalize().multiply(PAYLOAD_VELOCITY));
		}
	}

	public void setMinecartTeam(GameTeam gameTeam)
	{
		_payloadTeam.setPrefix(gameTeam.GetColor().toString());
	}

	public void setMinecartTeam(PayloadState payloadState)
	{
		switch (payloadState)
		{
		case RESTARTING:
			_payloadTeam.setPrefix(C.cGreen);
			break;
		case CONTESTED:
			_payloadTeam.setPrefix(C.cPurple);
			break;
		case NONE:
			_payloadTeam.setPrefix(C.cWhite);
			break;
		default:
			break;
		}
	}

	public String getTopBar()
	{
		String progress = C.cAqua;
		GameTeam gameTeamA = getBase().GetTeamList().get(0);

		int percentage = (int) (((int) getTrackDistanceToMarker(getDestination(gameTeamA), gameTeamA) / _totalDistance) * 100);
		boolean switched = false;

		if (_teamDirection == null)
		{
			percentage = 50;
		}

		for (int i = 0; i < 25; i++)
		{
			if (percentage / 4 == i)
			{
				switched = true;
				progress += C.cWhite + "•" + C.cRed;
			}
			else if (switched)
			{
				progress += "<";
			}
			else
			{
				progress += ">";
			}
		}

		return C.cAqua + "♚ " + C.cWhite + "[ " + progress + C.cWhite + " ]" + C.cRed + " ♚";
	}
	
	public void setState(PayloadState payloadState)
	{
		_payloadState = payloadState;
	}
	
	public Minecart getMinecart()
	{
		return _minecart;
	}
	
	public PayloadState getState()
	{
		return _payloadState;
	}
	
	public GameTeam getTeamDirection()
	{
		return _teamDirection;
	}
	
	public List<Location> getPathMarkers()
	{
		return _pathMarkers;
	}

	public static enum PayloadState
	{
		NONE, MOVING, CONTESTED, RESTARTING;
	}

}
