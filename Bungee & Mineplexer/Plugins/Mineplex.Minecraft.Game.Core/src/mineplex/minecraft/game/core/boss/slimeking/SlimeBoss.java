package mineplex.minecraft.game.core.boss.slimeking;

import java.io.File;
import java.io.IOException;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.EventMap;
import mineplex.minecraft.game.core.boss.EventState;
import mineplex.minecraft.game.core.boss.WorldEvent;
import mineplex.minecraft.game.core.boss.slimeking.creature.SlimeCreature;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SlimeBoss extends WorldEvent
{
	private static final int MAX_SIZE = 16;
	private static final int MIN_SIZE = 2;

	public SlimeBoss(DisguiseManager disguiseManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager, ProjectileManager projectileManager, Location cornerLocation)
	{
		super(disguiseManager, projectileManager, damageManager, blockRestore, conditionManager, "Slime King", cornerLocation, "schematic/ClansSlime.schematic");
	}

	@Override
	protected void customStart()
	{
		Bukkit.broadcastMessage("Custom Start");
		spawnSlime(getCenterLocation(), MAX_SIZE);
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

		if (creature instanceof SlimeCreature)
		{
			splitSlime(((SlimeCreature) creature));
			checkDeath();
		}
	}

	private int getSplitSize(int slimeSize)
	{
		return slimeSize / 2;
	}

	private int getSplitCount(int slimeSize)
	{
		return slimeSize <= 8 ? 4 : 2;
	}

	private double getMaxSlimeHealth(int slimeSize)
	{
		return slimeSize * 20;
	}

	private int getEnrageTicks(int slimeSize)
	{
		return 40 * slimeSize;
	}

	public void splitSlime(SlimeCreature slime)
	{
		int splitCount = getSplitCount(slime.getSize());
		int splitSize = getSplitSize(slime.getSize());

		if (splitSize >= MIN_SIZE)
		{
			for (int i = 0; i < splitCount; i++)
			{
				Location location = slime.getEntity().getLocation();
				SlimeCreature creature = spawnSlime(location, splitSize);
				creature.getEntity().setVelocity(new Vector(Math.random(), 0.1, Math.random()).multiply(0.5));
			}
		}
	}

	private SlimeCreature spawnSlime(Location location, int size)
	{
		SlimeCreature slimeCreature = new SlimeCreature(this, location, size, getMaxSlimeHealth(size), getEnrageTicks(size));
		registerCreature(slimeCreature);
		return slimeCreature;
	}

}
