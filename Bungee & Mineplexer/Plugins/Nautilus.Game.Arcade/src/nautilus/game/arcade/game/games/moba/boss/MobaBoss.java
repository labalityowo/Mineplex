package nautilus.game.arcade.game.games.moba.boss;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.ai.MobaAI;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class MobaBoss implements Listener
{

	protected final Moba _host;
	protected LivingEntity _entity;
	protected Location _location;
	private int _respawnTime;
	private long _lastDeath;

	private List<MobaBossAttack> _attacks;

	public MobaBoss(Moba host, Location location)
	{
		this(host, location, -1);
	}

	public MobaBoss(Moba host, Location location, int respawnTime)
	{
		_host = host;
		_location = location;
		_respawnTime = respawnTime;
		_lastDeath = -1;
		_attacks = new ArrayList<>(3);
	}

	public void setup()
	{
		_entity = spawnEntity();
		UtilServer.RegisterEvents(this);
	}

	public void cleanup()
	{
		UtilServer.Unregister(this);
		_attacks.forEach(MobaBossAttack::cleanup);
	}

	public void addAttack(MobaBossAttack attack)
	{
		_attacks.add(attack);
	}

	@EventHandler
	public void updateAttack(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || getAi().getTarget() == null)
		{
			return;
		}

		MobaBossAttack attack = UtilAlg.Random(_attacks);

		if (attack == null)
		{
			return;
		}

		attack.run();
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !_host.IsLive())
		{
			return;
		}

		getAi().updateTarget();
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		if (_entity != null && _entity.equals(event.getEntity()))
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
			_entity = null;
			_lastDeath = System.currentTimeMillis();
			getAi().setEntity(null);
		}
	}

	@EventHandler
	public void updateRespawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _lastDeath == -1 || !UtilTime.elapsed(_lastDeath, _respawnTime))
		{
			return;
		}

		_lastDeath = -1;
		_entity = spawnEntity();
		getAi().setEntity(_entity);
	}

	public void registerBoss()
	{
		_host.getBossManager().registerBoss(this);
	}

	public abstract LivingEntity spawnEntity();

	public abstract MobaAI getAi();

	public abstract String getName();

	public Moba getHost()
	{
		return _host;
	}

	public LivingEntity getEntity()
	{
		return _entity;
	}

	public boolean isDead()
	{
		return _entity == null || _entity.isDead() || !_entity.isValid();
	}
}
