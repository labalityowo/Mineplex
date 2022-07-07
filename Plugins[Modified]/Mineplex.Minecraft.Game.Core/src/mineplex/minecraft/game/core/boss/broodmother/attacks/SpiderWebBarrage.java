package mineplex.minecraft.game.core.boss.broodmother.attacks;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class SpiderWebBarrage extends BossAbility<SpiderCreature, Spider>
{
	private int _canShoot;
	private ArrayList<FallingBlock> _fallingBlocks = new ArrayList<FallingBlock>();
	private int _tick;

	public SpiderWebBarrage(SpiderCreature creature)
	{
		super(creature);

		_canShoot = (int) (40 + ((1 - creature.getHealthPercent()) * 50));

		Player target = getTarget();

		if (target != null)
		{
			UtilEnt.CreatureLook(getEntity(), target);
		}
	}

	@Override
	public boolean canMove()
	{
		return _canShoot <= 0;
	}

	@Override
	public int getCooldown()
	{
		return 120;
	}

	@Override
	public boolean inProgress()
	{
		return _canShoot > 0 || !_fallingBlocks.isEmpty();
	}

	@Override
	public boolean hasFinished()
	{
		return _canShoot <= 0 && _fallingBlocks.isEmpty();
	}

	@Override
	public void setFinished()
	{
		for (FallingBlock block : _fallingBlocks)
		{
			block.remove();
		}
	}

	public Player getTarget()
	{
		return getTarget(10, 30);
	}

	@Override
	public void tick()
	{
		if (_canShoot > 0 && _tick % 2 == 0)
		{
			_canShoot--;

			Vector vec = getLocation().getDirection().setY(0).normalize();

			vec.add(new Vector(UtilMath.rr(0.45, true), 0, UtilMath.rr(0.45, true))).normalize();

			vec.setY(0.8).multiply(0.75 + UtilMath.rr(0.3, true));

			FallingBlock block = getLocation().getWorld().spawnFallingBlock(getLocation(), Material.WEB, (byte) 0);
			block.setDropItem(false);

			_fallingBlocks.add(block);

			block.setVelocity(vec);
			
			getLocation().getWorld().playSound(getLocation(), Sound.FIZZ, 3, 2F);
		}

		_tick++;
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<FallingBlock> fallingIterator = _fallingBlocks.iterator();

		while (fallingIterator.hasNext())
		{
			FallingBlock cur = fallingIterator.next();

			if (cur.isDead() || !cur.isValid() || cur.getVelocity().length() < 0.05 || cur.getTicksLived() > 400
					|| !cur.getWorld().isChunkLoaded(cur.getLocation().getBlockX() >> 4, cur.getLocation().getBlockZ() >> 4))
			{
				fallingIterator.remove();

				Block block = cur.getLocation().getBlock();

				if (UtilBlock.airFoliage(block) || block.getType() == Material.WEB)
				{
					Bukkit.broadcastMessage("Setting Web");
					getBoss().getEvent().setBlock(block, Material.WEB);
				}

				// Expire
				if (cur.getTicksLived() > 400
						|| !cur.getWorld().isChunkLoaded(cur.getLocation().getBlockX() >> 4, cur.getLocation().getBlockZ() >> 4))
				{
					cur.remove();
					continue;
				}

				cur.remove();
				continue;
			}
		}
	}

}
