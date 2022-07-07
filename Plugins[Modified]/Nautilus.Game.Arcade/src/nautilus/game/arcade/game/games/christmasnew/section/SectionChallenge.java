package nautilus.game.arcade.game.games.christmasnew.section;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.lifetimes.ListenerComponent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.present.Present;
import nautilus.game.arcade.world.WorldData;

public abstract class SectionChallenge extends ListenerComponent implements SectionRegister
{

	protected final ChristmasNew _host;
	protected final WorldData _worldData;

	private final Present _present;
	protected final Section _section;

	protected final List<Entity> _entities;

	public SectionChallenge(ChristmasNew host, Location present, Section section)
	{
		_host = host;
		_worldData = host.WorldData;

		if (present != null)
		{
			host.CreatureAllowOverride = true;
			_present = new Present(present);
			host.CreatureAllowOverride = false;
			section.register(_present);
		}
		else
		{
			_present = null;
		}

		_section = section;
		_entities = new ArrayList<>();
	}

	public abstract void onPresentCollect();

	@Override
	public void deactivate()
	{
		super.deactivate();

		_entities.forEach(Entity::remove);
		_entities.clear();
	}

	public <T extends Entity> T spawn(Location location, Class<T> classOfT)
	{
		_host.CreatureAllowOverride = true;

		T entity = location.getWorld().spawn(location, classOfT);
		_entities.add(entity);

		_host.CreatureAllowOverride = false;
		return entity;
	}

	@EventHandler
	public void updateDeadMobs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		_entities.removeIf(entity -> entity.isDead() || !entity.isValid());
	}

	@EventHandler
	public void updatePresents(UpdateEvent event)
	{
		if (_present == null)
		{
			return;
		}

		if (event.getType() == UpdateType.TICK)
		{
			_present.updateRotation();
		}
		else if (event.getType() == UpdateType.FAST && !_present.isCollected())
		{
			List<Player> alive = _host.GetPlayers(true);
			boolean containsCheck = alive.size() > 1;

			for (Player player : alive)
			{
				if (_present.isColliding(player, containsCheck))
				{
					_section.onPresentCollect(player, _present);
					return;
				}
			}
		}
	}

	public ChristmasNew getHost()
	{
		return _host;
	}

	public Present getPresent()
	{
		return _present;
	}
}
