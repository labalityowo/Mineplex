package nautilus.game.arcade.game.games.moba.boss.wither.attack;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import nautilus.game.arcade.game.games.moba.boss.MobaBossAttack;
import nautilus.game.arcade.game.games.moba.boss.wither.WitherBoss;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class BossAttackEarthquake implements MobaBossAttack
{

	private static final String ATTACK_NAME = "Earthquake";
	private static final int RADIUS = 8;
	private static final int DAMAGE = 4;
	private static final double FALLING_BLOCK_CHANCE = 0.1;

	private final WitherBoss _boss;
	private final Set<FallingBlock> _entities;

	public BossAttackEarthquake(WitherBoss boss)
	{
		_boss = boss;
		_entities = new HashSet<>();

		UtilServer.RegisterEvents(this);
	}

	@Override
	public void run()
	{
		LivingEntity boss = _boss.getEntity();

		for (Block block : UtilBlock.getBlocksInRadius(boss.getLocation(), RADIUS))
		{
			// Only want blocks that are on the floor
			if (block.getType() == Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR || Math.random() > FALLING_BLOCK_CHANCE)
			{
				continue;
			}

			FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 1, 0.5), block.getType(), block.getData());
			fallingBlock.setHurtEntities(false);
			fallingBlock.setDropItem(false);
			fallingBlock.setVelocity(UtilAlg.getTrajectory(boss, fallingBlock).multiply(0.25).setY(0.7 + Math.random()));
		}

		for (Entry<LivingEntity, Double> entry : UtilEnt.getInRadius(boss.getLocation(), RADIUS).entrySet())
		{
			LivingEntity entity = entry.getKey();
			double dist = entry.getValue();

			if (MobaUtil.isTeamEntity(entity, _boss.getTeam()))
			{
				continue;
			}

			_boss.getHost().getArcadeManager().GetDamage().NewDamageEvent(entity, boss, null, DamageCause.CUSTOM, DAMAGE * (dist + 0.5), false, true, false, _boss.getName(), ATTACK_NAME);
			UtilAction.velocity(entity, UtilAlg.getTrajectory(boss, entity).setY(1));
		}
	}

	@Override
	public void cleanup()
	{
		UtilServer.Unregister(this);
	}

	@EventHandler
	public void entityChangeBlock(EntityChangeBlockEvent event)
	{
		if (_entities.contains(event.getEntity()))
		{
			event.setCancelled(true);
			event.getEntity().remove();
			_entities.remove(event.getEntity());
		}
	}
}
