package mineplex.game.nano.game.games.chickenshoot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.components.player.GiveItemComponent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ChickenShoot extends ScoredSoloGame
{

	private static final int MAX_MOBS = 30;

	private final Map<LivingEntity, Integer> _mobs;

	private List<Location> _mobSpawns;

	public ChickenShoot(NanoManager manager)
	{
		super(manager, GameType.CHICKEN_SHOOT, new String[]
				{
						"Look up! " + C.cYellow + "Chickens" + C.Reset + " are falling from the sky!",
						C.cRed + "Shoot Them" + C.Reset + " with your bow for points!",
						C.cYellow + "Most points" + C.Reset + " wins!"
				});

		_mobs = new HashMap<>();

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_endComponent.setTimeout(TimeUnit.SECONDS.toMillis(75));

		new GiveItemComponent(this)
				.setItems(new ItemStack[]
						{
								new ItemBuilder(Material.BOW)
										.addEnchantment(Enchantment.ARROW_INFINITE, 1)
										.setUnbreakable(true)
										.build(),
								new ItemStack(Material.ARROW)
						})
				.setArmour(new ItemStack[]
						{
								null,
								null,
								null,
								new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
										.setPlayerHead("MHF_Chicken")
										.setTitle(C.cYellow + "Chicken Head")
										.build()
						});
	}

	@Override
	protected void parseData()
	{
		_mobSpawns = _mineplexWorld.getIronLocations("LIME");
	}

	@EventHandler
	public void updateMobs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isLive())
		{
			return;
		}

		_mobs.keySet().removeIf(entity ->
		{
			if (!entity.isValid() || UtilEnt.isGrounded(entity))
			{
				kill(entity);
				return true;
			}

			return false;
		});

		_worldComponent.setCreatureAllowOverride(true);

		while (_mobs.size() < MAX_MOBS)
		{
			Location location = UtilAlg.Random(_mobSpawns);

			if (location == null)
			{
				break;
			}

			location = location.clone().add(0, 13, 0);
			location.setYaw(UtilMath.r(360));

			_worldComponent.setCreatureAllowOverride(true);

			Chicken chicken = location.getWorld().spawn(location, Chicken.class);
			int points;
			String colour = "";

			if (Math.random() < 0.1)
			{
				points = 5;
				colour = C.cPurple;

				Zombie zombie = location.getWorld().spawn(location, Zombie.class);

				zombie.setBaby(true);
				zombie.setCustomName(colour + C.Bold + points);
				zombie.setCustomNameVisible(true);
				zombie.setHealth(1);
				zombie.setHealth(1);

				chicken.setPassenger(zombie);

				_mobs.put(zombie, points);
			}
			else
			{
				points = UtilMath.rRange(1, 3);

				switch (points)
				{
					case 1:
						colour = C.cAqua;
						break;
					case 2:
						colour = C.cGreen;
						break;
					case 3:
						colour = C.cYellow;
						break;
				}

				chicken.setCustomName(colour + C.Bold + points);
				chicken.setCustomNameVisible(true);
			}

			chicken.setHealth(1);
			chicken.setMaxHealth(1);

			_mobs.put(chicken, points);
		}

		_worldComponent.setCreatureAllowOverride(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
		{
			event.SetCancelled("No Projectile");
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();
		Integer points = _mobs.remove(entity);

		if (points != null)
		{
			event.getDrops().clear();
			event.setDroppedExp(0);

			kill(entity);

			Player killer = entity.getKiller();

			if (killer == null)
			{
				return;
			}

			incrementScore(killer, points);
			UtilTextMiddle.display(null, C.cGreen + "+" + points, 5, 20, 5, killer);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void projectileHit(ProjectileHitEvent event)
	{
		event.getEntity().remove();
	}

	private void kill(LivingEntity entity)
	{
		if (entity.getPassenger() != null)
		{
			entity.getPassenger().remove();
		}

		if (entity.getVehicle() != null)
		{
			entity.getVehicle().remove();
		}

		if (!entity.isDead())
		{
			entity.remove();
		}
	}
}
