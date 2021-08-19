package mineplex.minecraft.game.core.boss.ironwizard;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.F;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.EventState;
import mineplex.minecraft.game.core.boss.WorldEvent;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GolemBoss extends WorldEvent
{
	public GolemBoss(DisguiseManager disguiseManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager, ProjectileManager projectileManager, Location cornerLocation)
	{
		super(disguiseManager, projectileManager, damageManager, blockRestore, conditionManager, "Iron Wizard", cornerLocation);
	}

	@Override
	protected void customStart()
	{
		Bukkit.broadcastMessage(F.main(getName(), "The mighty " + getName() + " challenges you to face him!"));
		spawnGolem(getCenterLocation());
		setState(EventState.LIVE);
		announceStart();
	}

	/**
	 * Check if this golem boss has been defeated
	 */
	private void checkDeath()
	{
		if (getCreatures().size() == 0)
		{
			setState(EventState.COMPLETE);
			Bukkit.broadcastMessage(F.main(getName(), "The mighty " + getName() + " has fallen!"));
		}
	}

	@Override
	public void removeCreature(EventCreature creature)
	{
		super.removeCreature(creature);

		if (creature instanceof GolemCreature)
		{
			checkDeath();
		}
	}

	private GolemCreature spawnGolem(Location location)
	{
		GolemCreature golemCreature = new GolemCreature(this, location, 3000);
		registerCreature(golemCreature);
		return golemCreature;
	}
}