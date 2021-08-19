package nautilus.game.arcade.game.games.moba.buff;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BuffManager implements Listener
{

	private final Map<LivingEntity, List<Buff<? extends LivingEntity>>> _buffs;

	public BuffManager()
	{
		_buffs = new HashMap<>();
	}

	public void apply(Buff<? extends LivingEntity> buff)
	{
		if (UtilPlayer.isSpectator(buff.getEntity()))
		{
			return;
		}

		_buffs.putIfAbsent(buff.getEntity(), new ArrayList<>(3));
		_buffs.get(buff.getEntity()).add(buff);
		buff.apply();
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Iterator<LivingEntity> iterator = _buffs.keySet().iterator();

		while (iterator.hasNext())
		{
			LivingEntity entity = iterator.next();
			List<Buff<? extends LivingEntity>> buffs = _buffs.get(entity);

			if (entity.isDead() || !entity.isValid() || UtilPlayer.isSpectator(entity) || entity instanceof Player && !((Player) entity).isOnline())
			{
				buffs.forEach(Buff::expire);
				iterator.remove();
				continue;
			}

			Iterator<Buff<? extends LivingEntity>> buffIterator = buffs.iterator();

			while (buffIterator.hasNext())
			{
				Buff buff = buffIterator.next();

				if (UtilTime.elapsed(buff.getStart(), buff.getDuration()))
				{
					buff.expire();
					buffIterator.remove();
				}
			}
		}
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		_buffs.forEach((livingEntity, buffs) -> buffs.forEach(Buff::expire));
	}
}
