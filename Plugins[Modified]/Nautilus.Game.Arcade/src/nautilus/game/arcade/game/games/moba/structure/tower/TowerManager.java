package nautilus.game.arcade.game.games.moba.structure.tower;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TowerManager implements Listener
{

	private static final int FIRST_TOWER_HEALTH = 500;
	private static final int SECOND_TOWER_HEALTH = 1000;
	private static final int PROJECTILE_RANGE_SQUARED = 4;

	private final Moba _host;

	private final List<Tower> _towers;
	private final Map<Entity, Pair<Player, Double>> _projectilesToCheck;

	public TowerManager(Moba host)
	{
		_host = host;

		_towers = new ArrayList<>(12);
		_projectilesToCheck = new HashMap<>();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		Map<String, Location> towers = _host.getLocationStartsWith("TOWER");

		for (Entry<String, Location> entry : towers.entrySet())
		{
			String key = entry.getKey();
			Location location = entry.getValue();
			String[] components = key.split(" ");

			if (components.length < 3)
			{
				continue;
			}

			String team = components[1];

			boolean firstTower;

			try
			{
				firstTower = components[2].equalsIgnoreCase("1");
			}
			catch (NumberFormatException e)
			{
				continue;
			}

			int health = firstTower ? FIRST_TOWER_HEALTH : SECOND_TOWER_HEALTH;
			GameTeam gameTeam = _host.getTeam(team);

			if (gameTeam == null)
			{
				continue;
			}

			_towers.add(new Tower(_host, location, gameTeam, health, firstTower));
		}

		_towers.sort((o1, o2) ->
		{
			if (o1.isFirstTower())
			{
				return Integer.MIN_VALUE;
			}

			if (!o2.isFirstTower())
			{
				return Integer.MAX_VALUE;
			}

			return 0;
		});

		_host.CreatureAllowOverride = true;
		for (Tower tower : _towers)
		{
			tower.setup();
		}
		_host.CreatureAllowOverride = false;
	}

	@EventHandler
	public void updateTower(UpdateEvent event)
	{
		if (!_host.IsLive())
		{
			return;
		}

		if (event.getType() == UpdateType.FASTEST)
		{
			for (Tower tower : _towers)
			{
				tower.updateTarget();
			}
		}
		else if (event.getType() == UpdateType.SEC)
		{
			for (Tower tower : _towers)
			{
				tower.updateDamage();
			}
		}
		else if (event.getType() == UpdateType.SLOW)
		{
			for (Tower tower : _towers)
			{
				tower.updateHealing();
			}
		}
	}

	@EventHandler
	public void guardianDamage(CustomDamageEvent event)
	{
		for (Tower tower : _towers)
		{
			if (tower.getStand().equals(event.GetDamageeEntity()))
			{
				tower.getStand().setFireTicks(0);
				event.SetCancelled("Tower Guardian");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void crystalDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof EnderCrystal)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void crystalDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof EnderCrystal) || !_host.IsLive())
		{
			return;
		}

		Entity damager = event.getDamager();
		Player player = null;
		Tower tower = null;

		for (Tower other : _towers)
		{
			if (other.getCrystal().equals(event.getEntity()))
			{
				tower = other;
				break;
			}
		}

		if (tower == null)
		{
			return;
		}

		if (damager instanceof Player)
		{
			player = (Player) damager;
		}
		else if (damager instanceof Projectile)
		{
			Projectile projectile = (Projectile) damager;
			ProjectileSource source = projectile.getShooter();

			if (source instanceof Player)
			{
				player = (Player) source;
			}
		}

		if (player == null)
		{
			return;
		}

		GameTeam team = _host.GetTeam(player);

		if (UtilPlayer.isSpectator(player) || team == null || !canDamage(tower, team) || shouldCancelDamage(tower, player) || !Recharge.Instance.use(player, "Damage Tower", 200, false, false))
		{
			return;
		}

		tower.damage(event.getDamage());

		if (Recharge.Instance.use(player, "Tower Sound", 500, false, false))
		{
			playSound(player, tower);
		}
	}

	public void addProjectile(Player shooter, Entity entity, double damage)
	{
		_projectilesToCheck.put(entity, Pair.create(shooter, damage));
	}

	@EventHandler
	public void updateProjectiles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<Entity> iterator = _projectilesToCheck.keySet().iterator();

		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			Pair<Player, Double> pair = _projectilesToCheck.get(entity);
			Player player = pair.getLeft();
			GameTeam team = _host.GetTeam(player);

			for (Tower tower : _towers)
			{
				if (tower.isDead() || tower.getOwner().equals(team) || UtilMath.offsetSquared(tower.getCrystal(), entity) > PROJECTILE_RANGE_SQUARED)
				{
					continue;
				}

				if (!shouldCancelDamage(tower, player))
				{
					playSound(player, tower);
					tower.damage(pair.getRight());
				}

				entity.remove();
				iterator.remove();
			}
		}
	}

	public Tower damageTowerAt(Location location, Player shooter, double damage)
	{
		GameTeam team = _host.GetTeam(shooter);

		if (team == null)
		{
			return null;
		}

		for (Tower tower : _towers)
		{
			Location crystalLocation = tower.getCrystal().getLocation();

			if (tower.isDead() || tower.getOwner().equals(team) || UtilMath.offsetSquared(crystalLocation, location) > PROJECTILE_RANGE_SQUARED || !canDamage(tower, team) || shouldCancelDamage(tower, shooter))
			{
				continue;
			}

			playSound(shooter, tower);
			tower.damage(damage);
			return tower;
		}

		return null;
	}

	private boolean shouldCancelDamage(Tower tower, Player shooter)
	{
		Location entityLocation = tower.getCrystal().getLocation();
		Location playerLocation = shooter.getLocation();
		playerLocation.setY(entityLocation.getY());

		if (UtilMath.offsetSquared(playerLocation, entityLocation) > Tower.TARGET_RANGE_SQUARED)
		{
			shooter.playSound(shooter.getLocation(), Sound.NOTE_PLING, 1, 0.9F);
			if (Recharge.Instance.use(shooter, "Tower Cancel Inform", 2000, false, false))
			{
				shooter.sendMessage(F.main("Game", "You cannot damage the enemy tower while outside of the circle! Step inside the circle to deal damage to it."));
			}

			return true;
		}

		return false;
	}

	private void playSound(Player player, Tower tower)
	{
		player.playSound(tower.getCrystal().getLocation(), Sound.BLAZE_HIT, 1, 0.8F);
	}

	public boolean canDamage(Tower tower, GameTeam team)
	{
		// Dead tower, nothing
		// Same team
		if (tower.isDead() || team.equals(tower.getOwner()))
		{
			return false;
		}

		// First tower, all it
		if (tower.isFirstTower())
		{
			return true;
		}

		// Second tower
		// Need to check if previous was destroyed
		for (Tower other : _towers)
		{
			// Is other team
			// Is first tower
			// Is dead
			if (!team.equals(other.getOwner()) && other.isFirstTower() && other.isDead())
			{
				return true;
			}
		}

		return false;
	}

	public String getDisplayString(GameTeam team)
	{
		StringBuilder out = new StringBuilder();

		for (Tower tower : _towers)
		{
			if (!tower.getOwner().equals(team))
			{
				continue;
			}

			double health = tower.getHealth() / tower.getMaxHealth();
			String colour = tower.isDead() ? C.cGray : MobaUtil.getColour(health);

			out.append(colour).append("♛ ");
		}

		return out.toString();
	}

	public List<Tower> getTowers()
	{
		return _towers;
	}
}
