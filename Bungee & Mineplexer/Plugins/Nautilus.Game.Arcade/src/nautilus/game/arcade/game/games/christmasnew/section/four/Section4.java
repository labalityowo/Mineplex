package nautilus.game.arcade.game.games.christmasnew.section.four;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilTime;
import mineplex.core.lifetimes.Component;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;

public class Section4 extends Section
{

	private static final long WAVE_TIME = TimeUnit.SECONDS.toMillis(30);
	private static final int WAVES = 4;

	private long _start;
	private long _lastWave;
	private int _wave;

	public Section4(ChristmasNew host, Location sleighTarget)
	{
		super(host, sleighTarget);

		registerChallenges(
				new MobDefense(
						host,
						this,
						_worldData.GetDataLocs("SILVER"),
						_worldData.GetCustomLocs("GHAST LEFT").get(0),
						_worldData.GetCustomLocs("GEN LEFT").get(0),
						_worldData.GetCustomLocs("HEALTH LEFT"),
						_worldData.GetCustomLocs("PARTICLES LEFT")
				),
				new MobDefense(
						host,
						this,
						_worldData.GetDataLocs("GRAY"),
						_worldData.GetCustomLocs("GHAST RIGHT").get(0),
						_worldData.GetCustomLocs("GEN RIGHT").get(0),
						_worldData.GetCustomLocs("HEALTH RIGHT"),
						_worldData.GetCustomLocs("PARTICLES RIGHT")
				)
		);
	}

	@Override
	public void onRegister()
	{

	}

	@Override
	public void onUnregister()
	{
		_host.sendSantaMessage("You did it! My magical bridge has been turned on.", ChristmasNewAudio.SANTA_BRIDGE_ON);
	}

	@Override
	public boolean isComplete()
	{
		return _wave > WAVES;
	}

	@Override
	public void onSantaTarget()
	{
		_start = System.currentTimeMillis();
		_host.sendSantaMessage("Oh no! My magical bridge has been turned off. Defend the generators while they charge up!", ChristmasNewAudio.SANTA_BRIDGE_OFF);
	}

	@EventHandler
	public void updateWaves(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _start == 0 || !_host.IsLive())
		{
			return;
		}

		if (UtilTime.elapsed(_lastWave, WAVE_TIME))
		{
			_lastWave = System.currentTimeMillis();
			_wave++;

			setObjective("Defend the Generators", ((double) _wave / WAVES));

			switch (_wave)
			{
				case 2:
					_host.sendSantaMessage("Ahh! Look it's some ghasts and ghouls!", ChristmasNewAudio.SANTA_GHASTS);

					int players = _host.GetPlayers(true).size();

					for (Component component : _components)
					{
						if (component instanceof MobDefense)
						{
							((MobDefense) component).spawnGhast(players);
						}
					}
					break;
				case 3:
					_host.sendSantaMessage("Great job! Just a little bit longer.", ChristmasNewAudio.SANTA_LONGER);
					break;
				case 4:
					_host.sendSantaMessage("Almost there!", ChristmasNewAudio.SANTA_ALMOST_THERE);
					break;
			}

			for (Component component : _components)
			{
				if (component instanceof MobDefense)
				{
					((MobDefense) component).setWave(_wave);
				}
			}
		}

		for (Component component : _components)
		{
			if (component instanceof MobDefense && !((MobDefense) component).isDead())
			{
				return;
			}
		}

		_host.endGame(false, "Both generators were destroyed!");
	}
}
