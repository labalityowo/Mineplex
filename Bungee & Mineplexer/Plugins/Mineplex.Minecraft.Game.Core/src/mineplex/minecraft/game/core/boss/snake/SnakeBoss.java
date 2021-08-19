package mineplex.minecraft.game.core.boss.snake;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.EventState;
import mineplex.minecraft.game.core.boss.WorldEvent;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

public class SnakeBoss extends WorldEvent
{

	public SnakeBoss(DisguiseManager disguiseManager, ProjectileManager projectileManager, DamageManager damageManager,
			BlockRestore blockRestore, ConditionManager conditionManager, Location cornerLocation)
	{
		super(disguiseManager, projectileManager, damageManager, blockRestore, conditionManager, "Snaaaake", cornerLocation,
				"schematic/Golem.schematic");
	}

	@Override
	protected void customStart()
	{
		Bukkit.broadcastMessage("Custom Start");
		spawn(getCenterLocation());
		setState(EventState.LIVE);
		announceStart();
	}

	/**
	 * Check if this slime boss has been defeated
	 */
	private void checkDeath()
	{
		if (getCreatures().size() == 0)
		{
			setState(EventState.COMPLETE);
			Bukkit.broadcastMessage("FINISHED!");
		}
	}

	@Override
	public void removeCreature(EventCreature creature)
	{
		super.removeCreature(creature);

		if (creature instanceof SnakeCreature)
		{
			checkDeath();
		}
	}

	private SnakeCreature spawn(Location location)
	{
		SnakeCreature slimeCreature = new SnakeCreature(this, location);
		registerCreature(slimeCreature);
		return slimeCreature;
	}

}
