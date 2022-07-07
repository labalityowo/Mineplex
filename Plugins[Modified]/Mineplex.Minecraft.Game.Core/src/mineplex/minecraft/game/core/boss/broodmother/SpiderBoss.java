package mineplex.minecraft.game.core.boss.broodmother;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.EventState;
import mineplex.minecraft.game.core.boss.WorldEvent;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class SpiderBoss extends WorldEvent
{
	public SpiderBoss(DisguiseManager disguiseManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager, ProjectileManager projectileManager, Location cornerLocation)
	{
		super(disguiseManager, projectileManager, damageManager, blockRestore, conditionManager, "Brood Mother", cornerLocation);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();

		if (UtilMath.offset2d(event.getBlock().getLocation(), getCenterLocation()) > 40)
		{
			return;
		}

		if (block.getType() != Material.WEB)
		{
			return;
		}

		event.setCancelled(true);
		event.getBlock().setType(Material.AIR);
	}

	@Override
	protected void customStart()
	{
		Bukkit.broadcastMessage("Custom Start");
		spawnSpider(getCenterLocation());
		setState(EventState.LIVE);
		announceStart();
	}

	/**
	 * Check if this spider boss has been defeated
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

		if (creature instanceof SpiderCreature)
		{
			checkDeath();
		}
	}

	public SpiderMinionCreature spawnMinion(Location location)
	{
		if (getState() != EventState.LIVE)
		{
			return null;
		}
		SpiderMinionCreature minionCreature = new SpiderMinionCreature(this, location, 15);
		registerCreature(minionCreature);
		return minionCreature;
	}

	private SpiderCreature spawnSpider(Location location)
	{
		SpiderCreature spiderCreature = new SpiderCreature(this, location, 2500);
		registerCreature(spiderCreature);
		return spiderCreature;
	}
}
