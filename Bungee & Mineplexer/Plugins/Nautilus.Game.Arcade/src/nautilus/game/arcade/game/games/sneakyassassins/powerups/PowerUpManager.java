package nautilus.game.arcade.game.games.sneakyassassins.powerups;

import mineplex.core.common.util.*;
import mineplex.core.updater.*;
import mineplex.core.updater.event.*;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.*;
import nautilus.game.arcade.game.*;
import nautilus.game.arcade.game.Game.*;
import nautilus.game.arcade.game.games.sneakyassassins.event.PlayerMasterAssassinEvent;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;

import java.util.*;

public class PowerUpManager implements Listener
{
	private final Game _game;
	private final Random _random;
	private final List<Location> _spawnLocations;
	private final List<PowerUpItem> _powerUps = new ArrayList<>();
	private Location _lastLocation = null;
	private int _nextSpawnCountdown = -1;
	private final Map<UUID, Integer> _powerUpCount = new HashMap<>();

	public PowerUpManager(Game game, Random random, List<Location> spawnLocations)
	{
		_game = game;
		_random = random;
		_spawnLocations = spawnLocations;

		getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
	}

	public Random getRandom()
	{
		return _random;
	}

	public List<Location> getSpawnLocations()
	{
		return _spawnLocations;
	}

	public Location nextLocation()
	{
		if (getSpawnLocations().size() == 1)
			_lastLocation = getSpawnLocations().get(0);
		else if (getSpawnLocations().size() > 1)
		{
			int index = getRandom().nextInt(getSpawnLocations().size());

			if (_lastLocation != null)
			{
				while (getSpawnLocations().get(index).equals(_lastLocation))
					index = getRandom().nextInt(getSpawnLocations().size());
			}

			_lastLocation = getSpawnLocations().get(index);
		}

		return _lastLocation;
	}

	public Plugin getPlugin()
	{
		return getGame().Manager.getPlugin();
	}

	public void spawnNextPowerUp()
	{
		PowerUpItem powerUp = new PowerUpItem(this, nextLocation());
		getPowerUps().add(powerUp);

		powerUp.activate();

		getGame().Announce(F.main("Game", C.cYellow + C.Bold + "Powerup Spawning..."));
	}

	public List<PowerUpItem> getPowerUps()
	{
		return _powerUps;
	}

	boolean removePowerUp(PowerUpItem powerUp)
	{
		return _powerUps.remove(powerUp);
	}

	public void schedulePowerUpSpawn(int seconds)
	{
		_nextSpawnCountdown = seconds;
	}

	public Game getGame()
	{
		return _game;
	}

	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetGame() == getGame())
		{
			switch (event.GetState())
			{
				case Live:
					schedulePowerUpSpawn(20);
					break;
			}
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC && getGame().GetState() == Game.GameState.Live && _nextSpawnCountdown >= 0)
		{
			if (_nextSpawnCountdown == 0)
				spawnNextPowerUp();

			_nextSpawnCountdown--;
		}

		for (PowerUpItem powerUp : getPowerUps())
			powerUp.onUpdate(event);
	}

	@EventHandler
	public void onPowerUpProximity(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Player closestPlayer = null;
		double closestOffset = 0;
		PowerUpItem powerUp = null;
		
		//Pickup?
		for (Player player : UtilServer.getPlayers())
		{
			if (!getGame().IsAlive(player))
				continue;
			
			for (PowerUpItem cur : _powerUps)
			{
				if (cur.getNPC() == null)
					continue;
				
				double offset = UtilMath.offset(player, cur.getNPC());
					
				if (offset < 1 && (closestPlayer == null || offset < closestOffset))
				{
					closestPlayer = player;
					closestOffset = offset;
					powerUp = cur;
				}
			}
		}
		
		//Collect
		if (closestPlayer != null)
		{
			int count = getPowerUpCount(closestPlayer);

			if (count <= 4)
			{
				powerUp.remove();

				//Give Smoke Bomb
				PowerUpType.SMOKE_BOMB.powerUpPlayer(closestPlayer, getRandom());

				//Master
				if (count == 4)
				{
					PlayerMasterAssassinEvent pmae = new PlayerMasterAssassinEvent(closestPlayer);

					Bukkit.getPluginManager().callEvent(pmae);

					if (!pmae.isCancelled())
					{
						PowerUpType.COMPASS.powerUpPlayer(closestPlayer, getRandom());

						UtilPlayer.message(closestPlayer, F.main("Game", "You now have a " + F.item("Compass") + " for tracking players"));

						getGame().Announce(F.main("Game", F.name(C.Bold + closestPlayer.getName()) + " has become a " + F.skill(C.Bold + "Master Assassin")));

						for (Player player : UtilServer.getPlayers())
							player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);

						incrementPowerUpCount(closestPlayer);
					}
				}
				//Armor + Weapon
				else
				{
					PowerUpType.WEAPON.powerUpPlayer(closestPlayer, getRandom());
					PowerUpType.ARMOR.powerUpPlayer(closestPlayer, getRandom());

					UtilPlayer.message(closestPlayer, F.main("Game", "Your " + F.item("Sword") + " and " + F.item("Armor") + " have been upgraded!"));

					getGame().Announce(F.main("Game", F.name(closestPlayer.getName()) + " collected a " + F.skill("Powerup") + "."));

					incrementPowerUpCount(closestPlayer);
				}
			}
			
			schedulePowerUpSpawn(10);
		}
	}

	@EventHandler
	public void onUpdateCompass(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (getGame().GetState() != GameState.Live)
			return;

		Collection<? extends Player> players = Bukkit.getOnlinePlayers();

		for (Player player : players)
		{
			if (!getGame().IsPlaying(player))
				continue;

			if (player.getInventory().contains(Material.COMPASS))
				continue;

			Player closest = null;
			double minDistanceSquared = Double.NaN;

			for (Player other : players)
			{
				if (other == player)
					continue;

				if (!getGame().IsPlaying(other))
					continue;

				double distanceSquared = player.getLocation().distanceSquared(other.getLocation());

				if (closest == null || distanceSquared < minDistanceSquared)
				{
					closest = other;
					minDistanceSquared = distanceSquared;
				}
			}

			if (closest != null)
				player.setCompassTarget(closest.getLocation());
		}
	}

	public int getPowerUpCount(Player player)
	{
		Integer count = _powerUpCount.get(player.getUniqueId());

		return count == null ? 0 : count;
	}

	public int incrementPowerUpCount(Player player)
	{
		int count = getPowerUpCount(player) + 1;

		_powerUpCount.put(player.getUniqueId(), count);

		return count;
	}
	
	@EventHandler
	public void cancelNpcDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Skeleton)
			event.SetCancelled("NPC Cancel");
	}
	
	@EventHandler
	public void playNpcParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
	
		for (PowerUpItem cur : _powerUps)
			cur.powerupParticles();
	}
		
}
