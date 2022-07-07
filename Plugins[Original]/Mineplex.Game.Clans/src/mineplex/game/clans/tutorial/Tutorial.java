package mineplex.game.clans.tutorial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.message.ClansMessageManager;
import mineplex.game.clans.tutorial.objective.Objective;
import mineplex.game.clans.tutorial.objective.ObjectiveListener;

public abstract class Tutorial implements Listener, ObjectiveListener
{
	private HashMap<Player, TutorialSession> _playerSessionMap;

	private JavaPlugin _plugin;
	private HologramManager _hologram;
	private ClansMessageManager _message;
	private String _name;
	private String _taskIdentifier;
	private TutorialWorldManager _worldManager;

	// GUI Data
	private Material _guiMaterial;
	private byte _guiData;

	private List<Objective> _objectives;

	public Tutorial(JavaPlugin plugin, ClansMessageManager message, HologramManager hologram, String name, String taskIdentifier, Material guiMaterial, byte data)
	{
		_plugin = plugin;
		_message = message;
		_hologram = hologram;
		_name = name;
		_taskIdentifier = taskIdentifier;

		_guiMaterial = guiMaterial;
		_guiData = data;

		_playerSessionMap = new HashMap<>();
		_objectives = new ArrayList<>();
	}

	protected void addObjective(Objective objective)
	{
		_objectives.add(objective);
		objective.addListener(this);
	}

	public void start(Player player)
	{
		if (!canStart(player))
		{
			return;
		}

		System.out.println(String.format("Tutorial> [%s] started tutorial [%s]", player.getName(), getName()));

		TutorialSession session = new TutorialSession();
		TutorialRegion region = _worldManager == null ? null : _worldManager.getNextRegion();
		session.setRegion(region);

		_playerSessionMap.put(player, session);

		onStart(player);

		// Start at first objective!
		setObjective(player, 0);


		_objectives.forEach(objective -> objective.setup(player, region));
	}

	private void setObjective(Player player, int objective)
	{
		if (_objectives.size() <= objective)
			throw new IndexOutOfBoundsException("Invalid objective index: " + objective + ", size: " + _objectives.size());

		_playerSessionMap.get(player).setObjectiveIndex(objective);
		_objectives.get(objective).start(player);
	}

	public boolean isInTutorial(Player player)
	{
		return _playerSessionMap.containsKey(player);
	}

	public JavaPlugin getPlugin()
	{
		return _plugin;
	}

	public String getName()
	{
		return _name;
	}

	public Material getGuiMaterial()
	{
		return _guiMaterial;
	}

	public byte getGuiData()
	{
		return _guiData;
	}

	public ClansMessageManager getMessage()
	{
		return _message;
	}

	public final String getTaskIdentifier()
	{
		return "clans.tutorial." + _taskIdentifier;
	}

	@Override
	public void onObjectiveFinish(Player player, Objective objective)
	{
		int index = _objectives.indexOf(objective);

		assert index != -1;

		if (index + 1 >= _objectives.size())
		{
			finish(player);
		}
		else
		{
			setObjective(player, index + 1);
		}
	}

	@Override
	public void onObjectiveStart(Player player, Objective objective)
	{
	}

	@Override
	public void onObjectivePlayerUpdate(Player player, Objective objective)
	{
	}

	protected final void finish(Player player)
	{
		_objectives.forEach(objective -> objective.clean(player, getRegion(player)));

		removePlayer(player);

		System.out.println(String.format("Tutorial> [%s] finished tutorial [%s]", player.getName(), getName()));

		playFinishEffects(player.getLocation());
		onFinish(player);
	}

	private void quit(Player player)
	{
		_objectives.forEach(objective -> objective.clean(player, getRegion(player)));

		removePlayer(player);

		System.out.println(String.format("Tutorial> [%s] quit tutorial [%s]", player.getName(), getName()));

		onQuit(player);
	}

	private void removePlayer(Player player)
	{
		TutorialSession session = _playerSessionMap.remove(player);

		if (session != null)
		{
			if (session.getRegion() != null)
			{
				_worldManager.returnRegion(session.getRegion());
			}

			if (session.getSpawnHologram() != null)
				session.getSpawnHologram().stop();

			session.getHolograms().forEach(Hologram::stop);
		}
	}

	public Set<Player> getPlayers()
	{
		return _playerSessionMap.keySet();
	}

	/**
	 * Called when the player finishes the tutorial
	 */
	protected abstract void onFinish(Player player);

	/**
	 * Called when the player starts the tutorial
	 */
	protected abstract void onStart(Player player);

	/**
	 * Called when a player quits the tutorial or leaves the game in the tutorial
	 */
	protected abstract void onQuit(Player player);

	protected abstract boolean canStart(Player player);

	public void unregisterAll()
	{
		HandlerList.unregisterAll(this);
		_objectives.forEach(Objective::unregisterAll);
	}

	public List<String> getScoreboardLines(Player player)
	{
		ArrayList<String> lines = new ArrayList<>();
		TutorialSession session = _playerSessionMap.get(player);
		if (session != null)
		{
			lines.add(" ");

			int objectiveIndex = session.getObjectiveIndex();
			Objective currentObjective = _objectives.get(objectiveIndex);
			lines.add(C.cGreenB + currentObjective.getName(player));
			lines.add(" ");
			currentObjective.addScoreboardLines(player, lines);
		}
		return lines;
	}

	public void setWorldManager(TutorialWorldManager worldManager)
	{
		_worldManager = worldManager;
	}

	public TutorialWorldManager getWorldManager()
	{
		return _worldManager;
	}

	public TutorialRegion getRegion(Player player)
	{
		if(player == null || !player.isOnline() || _playerSessionMap == null || _playerSessionMap.get(player) == null) return null;
		return _playerSessionMap.get(player).getRegion();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		if (isInTutorial(event.getPlayer()))
		{
			quit(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent event)
	{
		if (isInTutorial(event.getPlayer()))
		{
			quit(event.getPlayer());
		}
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent event)
	{
		if (event.getEntity() instanceof Player && isInTutorial(((Player) event.getEntity())))
		{
			event.setFoodLevel(20);
		}
	}

	@EventHandler
	public void displayDescription(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Map.Entry<Player, TutorialSession> entry : _playerSessionMap.entrySet())
		{
			String prefix = entry.getValue().incrementAndGetColorTick() % 2 == 0 ? C.cWhite : C.cGreen;
			Objective objective = _objectives.get(entry.getValue().getObjectiveIndex());
			UtilTextBottom.display(prefix + objective.getDescription(entry.getKey()), entry.getKey());
		}
	}

	@EventHandler
	public void displayText(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Map.Entry<Player, TutorialSession> entry : _playerSessionMap.entrySet())
		{
			Player player = entry.getKey();
			Objective objective = _objectives.get(entry.getValue().getObjectiveIndex());

			if (entry.getValue().incrementAndGetTextSeconds() < 20)
			{
				// 20 second delay between displaying.
				return;
			}

			objective.displayChatMessages(player);
		}
	}

	public void addHologram(Player player, Location location, String... text)
	{
		if (_playerSessionMap.containsKey(player))
		{
			Hologram hologram = new Hologram(_hologram, location, text);
			_playerSessionMap.get(player).getHolograms().add(hologram);
			hologram.start();
		}
	}

	public void setSpawnHologram(Player player, Location location, String... text)
	{
		if (_playerSessionMap.containsKey(player))
		{
			TutorialSession session = _playerSessionMap.get(player);
			if (session.getSpawnHologram() == null && !session.isRemovedHologram())
			{
				Hologram hologram = new Hologram(_hologram, location, text);
				session.setSpawnHologram(hologram);
				hologram.start();
			}
			else
			{
				session.getSpawnHologram().setText(text);
			}
		}
	}

	public void removeSpawnHologram(Player player)
	{
		if (_playerSessionMap.containsKey(player))
		{
			TutorialSession session = _playerSessionMap.get(player);
			if (session.getSpawnHologram() != null)
			{
				session.getSpawnHologram().stop();
				session.setSpawnHologram(null);
			}

			session.setRemovedHologram(true);
		}
	}

	public TutorialSession getTutorialSession(Player player)
	{
		return _playerSessionMap.get(player);
	}

	private void playFinishEffects(Location location)
	{
		// Firework
		UtilFirework.launchFirework(
				location,
				FireworkEffect.Type.STAR,
				Color.GREEN,
				true,
				true,
				null,
				1
		);

		// Particles
		UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER,
				location, 0.5F, 0.5F, 0.5F, 1, 3, UtilParticle.ViewDist.LONG);
		UtilParticle.PlayParticle(UtilParticle.ParticleType.ENCHANTMENT_TABLE,
				location, 0.5F, 0.5F, 0.5F, 1, 3, UtilParticle.ViewDist.LONG);
		UtilParticle.PlayParticle(UtilParticle.ParticleType.HEART,
				location, 0.5F, 0.5F, 0.5F, 1, 3, UtilParticle.ViewDist.LONG);
		UtilParticle.PlayParticle(UtilParticle.ParticleType.NOTE,
				location, 0.5F, 0.5F, 0.5F, 1, 3, UtilParticle.ViewDist.LONG);
	}
}
