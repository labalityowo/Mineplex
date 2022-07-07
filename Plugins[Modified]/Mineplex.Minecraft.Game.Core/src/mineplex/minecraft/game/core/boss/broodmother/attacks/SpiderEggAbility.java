package mineplex.minecraft.game.core.boss.broodmother.attacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.broodmother.SpiderBoss;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.Vec3D;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public abstract class SpiderEggAbility extends BossAbility<SpiderCreature, Spider>
{
	private HashMap<Block, Long> _eggs = new HashMap<Block, Long>();
	private ArrayList<FallingBlock> _fallingBlocks = new ArrayList<FallingBlock>();

	public SpiderEggAbility(SpiderCreature creature)
	{
		super(creature);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;

		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		Block block = event.getTo().getBlock().getRelative(BlockFace.DOWN);

		if (!_eggs.containsKey(block))
		{
			return;
		}

		_eggs.remove(block);

		block.setType(Material.AIR);
		block.getWorld().playSound(block.getLocation(), Sound.DIG_SNOW, 2.5F, 0F);
		block.getWorld().playSound(block.getLocation(), Sound.BURP, 2.5F, 1.5F);

		UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.SNOW_BLOCK, 0),
				block.getLocation().add(0.5, 0.4, 0.5), 0.4F, 0.4F, 0.4F, 0, 40, ViewDist.NORMAL, UtilServer.getPlayers());

		UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.DRAGON_EGG, 0),
				block.getLocation().add(0.5, 0.6, 0.5), 0.6F, 0.5F, 0.6F, 0, 80, ViewDist.NORMAL, UtilServer.getPlayers());

	}

	public boolean hasEggsFinished()
	{
		return getEggs() == 0;
	}

	public void setEggsFinished()
	{
		for (FallingBlock block : _fallingBlocks)
		{
			block.remove();
		}

		for (Block b : _eggs.keySet())
		{
			b.setType(Material.AIR);
		}
	}

	protected void shootEgg(Vector vector)
	{
		FallingBlock block = getLocation().getWorld().spawnFallingBlock(getLocation(), Material.DRAGON_EGG, (byte) 0);
		block.setDropItem(false);
		block.setVelocity(vector);

		_fallingBlocks.add(block);

		getLocation().getWorld().playSound(getLocation(), Sound.SHEEP_SHEAR, 2.5F, 0F);
	}

	@EventHandler
	public void onEggTeleport(BlockFromToEvent event)
	{
		if (!_eggs.containsKey(event.getBlock()))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onEggyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<Entry<Block, Long>> itel = _eggs.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<Block, Long> entry = itel.next();

			if (entry.getValue() < System.currentTimeMillis())
			{
				itel.remove();
				entry.getKey().setType(Material.AIR);

				entry.getKey().getWorld()
						.playEffect(entry.getKey().getLocation(), Effect.STEP_SOUND, Material.DRAGON_EGG.getId());

				((SpiderBoss) getBoss().getEvent()).spawnMinion(entry.getKey().getLocation().add(0.5, 0.5, 0.5));
			}
		}

		Iterator<FallingBlock> fallingIterator = _fallingBlocks.iterator();

		while (fallingIterator.hasNext())
		{
			FallingBlock cur = fallingIterator.next();

			if (cur.isDead() || !cur.isValid()  || cur.getVelocity().length() < 0.05 || cur.getTicksLived() > 400
					|| !cur.getWorld().isChunkLoaded(cur.getLocation().getBlockX() >> 4, cur.getLocation().getBlockZ() >> 4))
			{
				fallingIterator.remove();

				Block block = cur.getLocation().getBlock();

				if ((UtilBlock.airFoliage(block) || block.getType() == Material.DRAGON_EGG)
						&& block.getRelative(BlockFace.DOWN).getType() != Material.DRAGON_EGG)
				{
					block.setType(Material.DRAGON_EGG);
					_eggs.put(block, System.currentTimeMillis() + 10000 + UtilMath.r(10000));
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

			net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) cur).getHandle();
			Vec3D vec3d = new Vec3D(nmsEntity.locX, nmsEntity.locY, nmsEntity.locZ);
			Vec3D vec3d1 = new Vec3D(nmsEntity.locX + nmsEntity.motX, nmsEntity.locY + nmsEntity.motY, nmsEntity.locZ
					+ nmsEntity.motZ);

			MovingObjectPosition finalObjectPosition = nmsEntity.world.rayTrace(vec3d, vec3d1, false, true, false);
			vec3d = new Vec3D(nmsEntity.locX, nmsEntity.locY, nmsEntity.locZ);
			vec3d1 = new Vec3D(nmsEntity.locX + nmsEntity.motX, nmsEntity.locY + nmsEntity.motY, nmsEntity.locZ + nmsEntity.motZ);

			if (finalObjectPosition != null)
			{
				vec3d1 = new Vec3D(finalObjectPosition.pos.a, finalObjectPosition.pos.b, finalObjectPosition.pos.c);
			}

			if (finalObjectPosition != null)
			{
				Block block = cur.getWorld().getBlockAt(((int) finalObjectPosition.pos.a), ((int) finalObjectPosition.pos.b), ((int) finalObjectPosition.pos.c));

				if (!UtilBlock.airFoliage(block) && !block.isLiquid())
				{
					nmsEntity.motX = ((float) (finalObjectPosition.pos.a - nmsEntity.locX));
					nmsEntity.motY = ((float) (finalObjectPosition.pos.b - nmsEntity.locY));
					nmsEntity.motZ = ((float) (finalObjectPosition.pos.c - nmsEntity.locZ));
					float f2 = MathHelper.sqrt(nmsEntity.motX * nmsEntity.motX + nmsEntity.motY * nmsEntity.motY + nmsEntity.motZ
							* nmsEntity.motZ);
					nmsEntity.locX -= nmsEntity.motX / f2 * 0.0500000007450581D;
					nmsEntity.locY -= nmsEntity.motY / f2 * 0.0500000007450581D;
					nmsEntity.locZ -= nmsEntity.motZ / f2 * 0.0500000007450581D;

					if ((UtilBlock.airFoliage(block) || block.getType() == Material.DRAGON_EGG)
							&& block.getRelative(BlockFace.DOWN).getType() != Material.DRAGON_EGG)
					{
						block.setType(Material.DRAGON_EGG);
						_eggs.put(block, System.currentTimeMillis() + 5000 + UtilMath.r(5000));
					}

					fallingIterator.remove();
					cur.remove();
				}
			}
			else
			{
				UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.STONE, 0), cur.getLocation()
						.add(0, 0.5, 0), 0.3F, 0.3F, 0.3F, 0, 2, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}

	public int getEggs()
	{
		return _fallingBlocks.size() + _eggs.size();
	}
}
