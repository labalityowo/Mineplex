package nautilus.game.arcade.game.games.christmasnew.section.four;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R3.EntityLargeFireball;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLargeFireball;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmas.ChristmasAudio;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;

class MobDefense extends SectionChallenge
{

	private static final int GENERATOR_HEALTH = 200;
	private static final int GENERATOR_RANGE_SQUARED = 36;
	private static final String DEFAULT_TEXT = C.cGreen + "Bridge Generator";

	private final List<Location> _mobSpawns;
	private final Location _generator;
	private final Location _ghastSpawn;
	private final List<Location> _healthIndicators;
	private final List<Hologram> _healthHolograms;
	private final List<Location> _particles;

	private int _generatorHealth;
	private int _wave;
	private Ghast _ghast;

	MobDefense(ChristmasNew host, Section section, List<Location> mobSpawns, Location ghastSpawn, Location generator, List<Location> healthIndicators, List<Location> particles)
	{
		super(host, null, section);

		_mobSpawns = mobSpawns;
		_generator = generator;
		_ghastSpawn = ghastSpawn;
		_healthIndicators = healthIndicators;
		_healthHolograms = new ArrayList<>(healthIndicators.size());
		_particles = particles;
		_generatorHealth = GENERATOR_HEALTH;

		_ghastSpawn.setYaw(180);
	}

	@Override
	public void onPresentCollect()
	{

	}

	@Override
	public void onRegister()
	{
		HologramManager manager = _host.getArcadeManager().getHologramManager();
		_healthIndicators.forEach(location -> _healthHolograms.add(new Hologram(manager, location, DEFAULT_TEXT).start()));
	}

	@Override
	public void onUnregister()
	{
		_healthHolograms.forEach(Hologram::stop);
	}

	@EventHandler
	public void updateMobs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		int zombies = 0, skeletons = 0, creepers = 0, blazes = 0, pigZombies = 0;
		int players = _host.GetPlayers(true).size();

		switch (_wave)
		{
			case 1:
				zombies = 1;
				break;
			case 2:
				zombies = 1;
				skeletons = 1;
				pigZombies = 1;
				break;
			case 3:
				zombies = 2;
				creepers = 1;
				break;
			case 4:
				zombies = 2;
				skeletons = 1;

				if (Math.random() < 0.2)
				{
					blazes = 1;
				}
				break;
		}

		spawnMob(Zombie.class, zombies, players);
		spawnMob(Skeleton.class, skeletons, players);
		spawnMob(Creeper.class, creepers, players);
		spawnMob(Blaze.class, blazes, players);
		spawnMob(PigZombie.class, pigZombies, players);
	}

	@EventHandler
	public void updateTracking(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Entity entity : _entities)
		{
			if (!(entity instanceof Creature) || !entity.isValid())
			{
				continue;
			}

			Creature creature = (Creature) entity;
			Location creatureLocation = creature.getLocation();
			LivingEntity target = creature.getTarget();
			Location targetLocation;
			double offset = UtilMath.offsetSquared(creatureLocation, _generator);

			if (target == null)
			{
				targetLocation = _generator;
			}
			else if (_generatorHealth > 0)
			{
				targetLocation = target.getLocation();
			}
			else
			{
				targetLocation = _host.getSleigh().GetLocation();
			}

			if (_generatorHealth > 0 && offset < GENERATOR_RANGE_SQUARED)
			{
				_generatorHealth--;

				if (Math.random() < 0.1)
				{
					creatureLocation.getWorld().playEffect(creatureLocation.add(0, 1.3, 0), Effect.STEP_SOUND, Material.QUARTZ_BLOCK);
				}

				if (_generatorHealth <= 0)
				{
					_host.sendSantaMessage("Oh no one of the generators has been destroyed! If we lose both we'll never save Christmas in time!", ChristmasNewAudio.SANTA_GEN_DESTROYED);
					_healthHolograms.forEach(Hologram::stop);
					_generator.getWorld().createExplosion(_generator.clone().add(0, 2.5, 0), 6);
				}
			}

			UtilEnt.CreatureMove(creature, targetLocation, 1.2F);
		}
	}

	@EventHandler
	public void updateGhastFire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || _ghast == null || !_ghast.isValid())
		{
			return;
		}

		Location location = _ghast.getLocation().add(0, 0.5, 0);

		if (UtilMath.offset2dSquared(location, _ghastSpawn) > 25)
		{
			_ghast.teleport(_ghastSpawn);
		}

		Fireball fireball = _ghast.launchProjectile(LargeFireball.class);
		fireball.setBounce(false);

		EntityLargeFireball eFireball = ((CraftLargeFireball) fireball).getHandle();
		Vector direction = UtilAlg.getTrajectory(location, _generator).multiply(0.1);
		eFireball.dirX = direction.getX();
		eFireball.dirY = direction.getY() - 0.005;
		eFireball.dirZ = direction.getZ();
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (_ghast == null)
		{
			return;
		}

		Projectile projectile = event.getEntity();

		if (projectile instanceof LargeFireball && UtilMath.offsetSquared(projectile.getLocation(), _generator) < 25)
		{
			_generatorHealth -= 5;
		}
	}

	@EventHandler
	public void updateHolograms(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _wave == 0 || isDead())
		{
			return;
		}

		String text = MobaUtil.getProgressBar(_generatorHealth, GENERATOR_HEALTH, 40);

		_healthHolograms.forEach(hologram -> hologram.setText(text));

		_particles.forEach(location -> UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, null, 0.1F, _wave, ViewDist.NORMAL));
	}

	private void spawnMob(Class<? extends Entity> clazz, int amount, int players)
	{
		amount *= Math.max(players / 6, 1);

		for (int i = 0; i < amount; i++)
		{
			Entity entity = spawn(UtilAlg.Random(_mobSpawns), clazz);
			UtilEnt.setTickWhenFarAway(entity, true);
		}
	}

	public void spawnGhast(int players)
	{
		_ghast = spawn(_ghastSpawn, Ghast.class);

		int health = players * 10;
		_ghast.setMaxHealth(health);
		_ghast.setHealth(_ghast.getMaxHealth());
		UtilEnt.vegetate(_ghast);
	}

	public void setWave(int wave)
	{
		_wave = wave;
	}

	public boolean isDead()
	{
		return _generatorHealth <= 0;
	}
}
