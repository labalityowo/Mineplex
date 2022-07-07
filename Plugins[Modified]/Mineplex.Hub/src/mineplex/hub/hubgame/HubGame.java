package mineplex.hub.hubgame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.lifetimes.PhasedLifetime;
import mineplex.core.newnpc.NPC;
import mineplex.core.world.MineplexWorld;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.common.HubGameComponent;

public abstract class HubGame extends PhasedLifetime<GameState> implements Listener
{

	private final HubGameManager _manager;
	private final HubGameType _type;
	private final Map<Class<? extends HubGameComponent<?>>, HubGameComponent<?>> _components;

	protected final MineplexWorld _worldData;

	private final Location _spawn;

	public HubGame(HubGameManager manager, HubGameType type)
	{
		_manager = manager;
		_type = type;
		_components = new HashMap<>();

		_worldData = manager.getHubManager().getWorldData();
		_spawn = _worldData.getSpongeLocation(type.name() + " SPAWN");

		start(GameState.Waiting);
		UtilServer.RegisterEvents(this);
	}

	public abstract void onPlayerDeath(Player player);

	public abstract List<Player> getAlivePlayers();

	public abstract void onCleanupPlayer(Player player);

	public HubGameManager getManager()
	{
		return _manager;
	}

	public HubGameType getGameType()
	{
		return _type;
	}

	@SuppressWarnings("unchecked")
	protected <T extends HubGameComponent<?>> T registerComponent(T instance)
	{
		_components.put((Class<? extends HubGameComponent<?>>) instance.getClass(), instance);
		register(instance);
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T extends HubGameComponent<?>> T getComponent(Class<T> clazz)
	{
		return (T) _components.get(clazz);
	}

	public void setNpc(NPC npc)
	{
		if (npc.getEntity() != null)
		{
			_spawn.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(_spawn, npc.getEntity().getLocation())));
		}
	}

	public Location getSpawn()
	{
		return _spawn;
	}

	public boolean isAlive(Player player)
	{
		return getAlivePlayers().contains(player);
	}

	public void announce(String message)
	{
		getAlivePlayers().forEach(player -> player.sendMessage(F.main(getManager().getName(), message)));
	}
}
