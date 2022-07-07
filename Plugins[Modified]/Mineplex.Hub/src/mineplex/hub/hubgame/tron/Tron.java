package mineplex.hub.hubgame.tron;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.HubGameType;
import mineplex.hub.hubgame.common.general.GameDescriptionComponent;
import mineplex.hub.hubgame.common.general.GameTimeoutComponent;
import mineplex.hub.hubgame.common.general.MissionsComponent;
import mineplex.hub.hubgame.common.general.PlacesComponent;
import mineplex.hub.hubgame.common.map.PreventNonAlivePlayersComponent;
import mineplex.hub.hubgame.common.map.TeleportIntoMapComponent;

public class Tron extends CycledGame
{

	private static final byte[] COLOURS =
			{
				1, 5, 3, 10
			};
	private static final int DEFAULT_SIZE = 10;

	private final Map<Player, TronBike> _bikes;
	private int _trailSize;

	public Tron(HubGameManager manager)
	{
		super(manager, HubGameType.TRON);
		_bikes = new HashMap<>();

		registerComponent(new TeleportIntoMapComponent(this, _worldData.getIronLocations("RED")));
		registerComponent(new GameDescriptionComponent(this));
		registerComponent(new GameTimeoutComponent(this, TimeUnit.MINUTES.toMillis(3)));
		registerComponent(new PlacesComponent(this));
		registerComponent(new MissionsComponent(this));

		List<Location> corners = _worldData.getIronLocations("WHITE");
		Location a = corners.get(0);
		Location b = corners.get(1);

		registerComponent(new PreventNonAlivePlayersComponent(this, a, b));
	}

	@Override
	public void onPrepare()
	{
		_trailSize = DEFAULT_SIZE;

		UtilServer.runSyncLater(() ->
		{
			int colourId = 0;

			for (Player player : getAlivePlayers())
			{
				TronBike bike = new TronBike(this, player, COLOURS[colourId++]);
				_bikes.put(player, bike);
			}

		}, 1);
	}

	@EventHandler
	public void updateBikes(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_bikes.forEach((player, bike) ->
		{
			if (!isAlive(player))
			{
				return;
			}

			bike.updateDirection();

			if (isLive() && bike.updateLocation())
			{
				Location location = player.getLocation();

				player.getWorld().playSound(location, Sound.EXPLODE, 1, 1);
				UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location,0, 0, 0, 0.1F, 1, ViewDist.NORMAL);
				announce(F.name(player.getName()) + " got vapourised.");
				onPlayerDeath(player);
				bike.clean();
			}
			else if (player.getVehicle() == null || !player.getVehicle().equals(bike.getEntity()))
			{
				bike.getEntity().setPassenger(player);
			}
		});
	}

	@EventHandler
	public void updateTrailSize(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !isLive())
		{
			return;
		}

		_trailSize++;
	}

	@Override
	public boolean endCheck()
	{
		return getAlivePlayers().size() <= 1;
	}

	@Override
	public void onCleanup()
	{
		super.onCleanup();
		_bikes.values().forEach(TronBike::clean);
		_bikes.clear();
	}

	@Override
	public void onCleanupPlayer(Player player)
	{
		super.onCleanupPlayer(player);

		TronBike bike = _bikes.remove(player);

		if (bike != null)
		{
			bike.clean();
		}
	}

	public int getTrailSize()
	{
		return _trailSize;
	}
}
