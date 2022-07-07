package mineplex.game.nano.game.games.slimecycles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.components.player.NightVisionComponent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SlimeCycles extends SoloGame
{

	private final DyeColor[] _colours =
			{
					DyeColor.RED,
					DyeColor.LIME,
					DyeColor.LIGHT_BLUE,
					DyeColor.PURPLE,
					DyeColor.ORANGE,
					DyeColor.YELLOW,
					DyeColor.BLUE,
					DyeColor.PINK,
					DyeColor.CYAN,
					DyeColor.WHITE
			};
	private int _colourIndex;

	private final Map<Player, SlimeBike> _bikes;
	private int _trailSize;

	public SlimeCycles(NanoManager manager)
	{
		super(manager, GameType.SLIME_CYCLES, new String[]
				{
						"Control your " + C.cGreen + "Slime" + C.Reset + " by looking.",
						C.cRed + "Avoid" + C.Reset + " other trails and walls.",
						C.cYellow + "Last player" + C.Reset + " standing wins!",
				});

		_bikes = new HashMap<>();
		_trailSize = 10;

		_damageComponent.setPvp(false);
		_damageComponent.setFall(false);

		_teamComponent.setRespawnRechargeTime(TimeUnit.SECONDS.toMillis(12));

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(3));

		new NightVisionComponent(this);
	}

	@Override
	protected void parseData()
	{
		_mineplexWorld.getWorld().setTime(18000);
	}

	@Override
	public void disable()
	{
		_bikes.clear();
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		getManager().runSyncLater(() ->
		{
			_worldComponent.setCreatureAllowOverride(true);
			_bikes.put(player, new SlimeBike(this, player, _colours[_colourIndex]));
			_worldComponent.setCreatureAllowOverride(false);

			_colourIndex = (_colourIndex + 1) % _colours.length;
		}, 1);
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Slime)
		{
			event.SetCancelled("Bike Damage");
		}
	}

	@EventHandler
	public void updateBikes(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !inProgress())
		{
			return;
		}

		_bikes.entrySet().removeIf(entry ->
		{
			Player player = entry.getKey();
			SlimeBike bike = entry.getValue();

			if (!player.equals(bike.getEntity().getPassenger()))
			{
				bike.getEntity().setPassenger(player);
			}

			if (!isLive())
			{
				return false;
			}

			bike.updateDirection();

			if (bike.updateLocation() && !hasRespawned(player))
			{
				Location location = player.getLocation();

				player.getWorld().playSound(location, Sound.EXPLODE, 1, 1);
				UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, null, 0, 1, ViewDist.LONG);
				bike.clean();
				getManager().getDamageManager().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, getGameType().getName(), "Vaporisation");
				return true;
			}

			return false;
		});
	}

	@EventHandler
	public void updateTrailSize(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC || !isLive())
		{
			return;
		}

		_trailSize++;

		if (_trailSize == 70)
		{
			getAlivePlayers().forEach(player -> addStat(player, "SlimeCyclesTail", 1, true, false));
		}
	}

	int getTrailSize()
	{
		return _trailSize;
	}
}
