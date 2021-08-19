package mineplex.minecraft.game.core.boss.slimeking.creature;

import mineplex.core.projectile.ProjectileManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.slimeking.SlimeBoss;
import mineplex.minecraft.game.core.boss.slimeking.ability.AbsorbAbility;
import mineplex.minecraft.game.core.boss.slimeking.ability.LeapAbility;
import mineplex.minecraft.game.core.boss.slimeking.ability.RocketAbility;
import mineplex.minecraft.game.core.boss.slimeking.ability.SlamAbility;
import mineplex.minecraft.game.core.boss.slimeking.ability.SlimeAbility;

import org.bukkit.Location;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.SlimeSplitEvent;

public class SlimeCreature extends EventCreature<Slime>
{
	private SlimeBoss _boss;
	private SlimeAbility _currentAbility;
	private boolean _enraged;
	// Storing size here incase one of the slime states decide to change the slime size
	private int _size;
	private int _ticksLived;
	private int _enrageTicks;

	public SlimeCreature(SlimeBoss boss, Location location, int size, double maxHealth, int enrageTicks)
	{
		super(boss, location, "Slime King", true, maxHealth, Slime.class);
		_boss = boss;
		_enraged = false;
		_size = size;
		_ticksLived = 0;
		_enrageTicks = enrageTicks;

		spawnEntity();
	}

	@Override
	protected void spawnCustom()
	{
		getEntity().setSize(_size);
	}

	public ProjectileManager getProjectileManager()
	{
		return _boss.getProjectileManager();
	}

	@EventHandler
	public void abilityTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		_ticksLived++;

		if (_currentAbility == null || (_currentAbility.isIdle() && _currentAbility.getIdleTicks() > 80))
		{
			double rand = Math.random();

			if (rand <= 0.25)
			{
				_currentAbility = new SlamAbility(this);
			}
			else if (rand <= 0.50 && getSize() >= 8)
			{
				_currentAbility = new AbsorbAbility(this);
			}
			else if (rand <= 0.75 && getSize() >= 4)
			{
				_currentAbility = new RocketAbility(this);
			}
			else
			{
				_currentAbility = new LeapAbility(this);
			}
		}

		// Disable Enrage
		// if (!_enraged && _ticksLived >= _enrageTicks)
		// {
		// setEnraged(true);
		// }

		_currentAbility.tick();
	}

	@EventHandler
	public void onSplit(SlimeSplitEvent event)
	{
		if (event.getEntity().equals(getEntity()))
			event.setCancelled(true);
	}

	// @EventHandler
	// public void target(EntityTargetEvent event)
	// {
	// if (event.getEntity().equals(getEntity()))
	// {
	// Bukkit.broadcastMessage("Target Event");
	// Player player = UtilPlayer.getRandomTarget(getEntity().getLocation(), 30);
	// Bukkit.broadcastMessage("Targetting: " + player);
	// event.setTarget(player);
	// }
	// }

	public void setEnraged(boolean enraged)
	{
		if (enraged != _enraged)
		{
			_enraged = enraged;
			setEntityClass(_enraged ? MagmaCube.class : Slime.class);
			spawnEntity();
		}
	}

	public boolean isEnraged()
	{
		return _enraged;
	}

	public int getSize()
	{
		return _size;
	}

	@Override
	public void dieCustom()
	{
	}
}
