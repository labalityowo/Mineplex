package mineplex.minecraft.game.core.boss.skeletonking;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.F;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.EventState;
import mineplex.minecraft.game.core.boss.WorldEvent;
import mineplex.minecraft.game.core.boss.skeletonking.minion.MinionType;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SkeletonBoss extends WorldEvent
{
	protected boolean canMove = false;
	
	public SkeletonBoss(DisguiseManager disguiseManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager, ProjectileManager projectileManager, Location cornerLocation)
	{
		super(disguiseManager, projectileManager, damageManager, blockRestore, conditionManager, "Skeleton King", cornerLocation);
	}
	
	@Override
	protected void customStart()
	{
		Bukkit.broadcastMessage(F.main(getName(), "The evils of the world have manifested in the form of the " + getName() + "! Become the champion of Light and destroy him!"));
		spawnNecromancer(getCenterLocation());
		setState(EventState.LIVE);
		announceStart();
	}

	/**
	 * Check if this skeleton boss has been defeated
	 */
	private void checkDeath()
	{
		if (getBossesAlive() == 0)
		{
			setState(EventState.COMPLETE);
			Bukkit.broadcastMessage(F.main(getName(), "The demonic " + getName() + " has been slain!"));
		}
	}

	@Override
	public void removeCreature(EventCreature creature)
	{
		super.removeCreature(creature);

		if (creature instanceof SkeletonCreature)
		{
			checkDeath();
		}
	}
	
	@Override
	protected void setState(EventState state)
	{
		super.setState(state);
		if (state == EventState.LIVE)
		{
			Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
			{
				canMove = true;
			}, 20 * 3);
		}
	}

	public EventCreature spawnMinion(MinionType type, Location location)
	{
		EventCreature minionCreature = type.getNewInstance(this, location);
		if (minionCreature != null)
		{
			registerCreature(minionCreature);
		}
		return minionCreature;
	}

	private SkeletonCreature spawnNecromancer(Location location)
	{
		SkeletonCreature necromancerCreature = new SkeletonCreature(this, location, 2500);
		registerCreature(necromancerCreature);
		return necromancerCreature;
	}
	
	private int getBossesAlive()
	{
		int alive = 0;
		
		for (EventCreature creature : getCreatures())
		{
			if (creature instanceof SkeletonCreature)
			{
				alive++;
			}
		}
		
		return alive;
	}
}