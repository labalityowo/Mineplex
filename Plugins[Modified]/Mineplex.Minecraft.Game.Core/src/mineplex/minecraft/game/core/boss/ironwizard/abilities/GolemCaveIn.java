package mineplex.minecraft.game.core.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.Vec3D;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class GolemCaveIn extends BossAbility<GolemCreature, IronGolem>
{
	private ArrayList<Block> _blocks = new ArrayList<Block>();
	private ArrayList<FallingBlock> _fallingBlocks = new ArrayList<FallingBlock>();
	private int _tick;

	public GolemCaveIn(GolemCreature creature)
	{
		super(creature);
	}

	@Override
	public boolean canMove()
	{
		return false;
	}

	@Override
	public Player getTarget()
	{
		if (getTarget(4) == null)
		{
			return getTarget(40);
		}

		return null;
	}

	@Override
	public boolean hasFinished()
	{
		return _tick > 60 && _fallingBlocks.isEmpty();
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

			if (cur.isDead() || !cur.isValid() || cur.getTicksLived() > 400
					|| !cur.getWorld().isChunkLoaded(cur.getLocation().getBlockX() >> 4, cur.getLocation().getBlockZ() >> 4))
			{
				fallingIterator.remove();

				Block block = cur.getLocation().getBlock();
				block.setTypeIdAndData(0, (byte) 0, true);
				cur.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, cur.getBlockId());

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

			double distanceToEntity = 0.0D;
			LivingEntity victim = null;

			net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) cur).getHandle();
			Vec3D vec3d = new Vec3D(nmsEntity.locX, nmsEntity.locY, nmsEntity.locZ);
			Vec3D vec3d1 = new Vec3D(nmsEntity.locX + nmsEntity.motX, nmsEntity.locY + nmsEntity.motY,
					nmsEntity.locZ + nmsEntity.motZ);

			MovingObjectPosition finalObjectPosition = nmsEntity.world.rayTrace(vec3d, vec3d1, false, true, false);
			vec3d = new Vec3D(nmsEntity.locX, nmsEntity.locY, nmsEntity.locZ);
			vec3d1 = new Vec3D(nmsEntity.locX + nmsEntity.motX, nmsEntity.locY + nmsEntity.motY, nmsEntity.locZ + nmsEntity.motZ);

			if (finalObjectPosition != null)
			{
				vec3d1 = new Vec3D(finalObjectPosition.pos.a, finalObjectPosition.pos.b, finalObjectPosition.pos.c);
			}

			for (Object entity : ((CraftWorld) cur.getWorld()).getHandle().getEntities(((CraftEntity) cur).getHandle(),
					((CraftEntity) cur).getHandle().getBoundingBox().a(((CraftEntity) cur).getHandle().motX,
							((CraftEntity) cur).getHandle().motY, ((CraftEntity) cur).getHandle().motZ).grow(2, 2, 2)))
			{
				Entity bukkitEntity = ((net.minecraft.server.v1_8_R3.Entity) entity).getBukkitEntity();

				if (bukkitEntity instanceof LivingEntity)
				{
					LivingEntity ent = (LivingEntity) bukkitEntity;

					// Avoid Self
					if (ent.equals(getEntity()))
						continue;

					// Creative or Spec
					if (ent instanceof Player)
						if (((Player) ent).getGameMode() == GameMode.CREATIVE || UtilPlayer.isSpectator(ent))
							continue;

					// float f1 = (float)(nmsEntity.boundingBox.a() * 0.6f);
					AxisAlignedBB axisalignedbb1 = ((CraftEntity) ent).getHandle().getBoundingBox().grow(1F, 1F, 1F);
					MovingObjectPosition entityCollisionPosition = axisalignedbb1.a(vec3d, vec3d1);

					if (entityCollisionPosition != null)
					{
						double d1 = vec3d.distanceSquared(entityCollisionPosition.pos);
						if ((d1 < distanceToEntity) || (distanceToEntity == 0.0D))
						{
							victim = ent;
							distanceToEntity = d1;
						}
					}
				}
			}

			if (victim != null)
			{
				cur.getWorld().playEffect(victim.getEyeLocation().subtract(0, 0.5, 0), Effect.STEP_SOUND, cur.getBlockId());

				if (canDamage(victim))
				{
					getBoss().getEvent().getDamageManager().NewDamageEvent((LivingEntity) victim, getEntity(), null,
							DamageCause.CONTACT, 10 * getBoss().getDifficulty(), true, true, false, "Iron Wizard Cave In",
							"Iron Wizard Cave In");
				}

				cur.remove();
				fallingIterator.remove();
			}
			else if (finalObjectPosition != null)
			{
				Block block = cur.getWorld().getBlockAt(((int) finalObjectPosition.pos.a), ((int) finalObjectPosition.pos.b), ((int) finalObjectPosition.pos.c));

				if (!UtilBlock.airFoliage(block) && !block.isLiquid())
				{
					nmsEntity.motX = ((float) (finalObjectPosition.pos.a - nmsEntity.locX));
					nmsEntity.motY = ((float) (finalObjectPosition.pos.b - nmsEntity.locY));
					nmsEntity.motZ = ((float) (finalObjectPosition.pos.c - nmsEntity.locZ));
					float f2 = MathHelper.sqrt(
							nmsEntity.motX * nmsEntity.motX + nmsEntity.motY * nmsEntity.motY + nmsEntity.motZ * nmsEntity.motZ);
					nmsEntity.locX -= nmsEntity.motX / f2 * 0.0500000007450581D;
					nmsEntity.locY -= nmsEntity.motY / f2 * 0.0500000007450581D;
					nmsEntity.locZ -= nmsEntity.motZ / f2 * 0.0500000007450581D;

					cur.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, cur.getBlockId());
					cur.remove();
					fallingIterator.remove();
				}
			}
			else
			{
				UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.STONE, 0),
						cur.getLocation().add(0, 0.5, 0), 0.3F, 0.3F, 0.3F, 0, 2, UtilParticle.ViewDist.NORMAL,
						UtilServer.getPlayers());
			}
		}
	}

	@Override
	public void setFinished()
	{
		for (FallingBlock block : _fallingBlocks)
		{
			block.remove();
		}

		for (Block block : _blocks)
		{
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
			block.setType(Material.AIR);
		}
	}

	@Override
	public void tick()
	{
		if (_tick++ == 0)
		{
			Location l = getLocation();

			ArrayList<Location> blocks = UtilShapes.getSphereBlocks(l, 3, 3, true);

			for (Location loc : blocks)
			{
				if (loc.getBlockY() >= l.getBlockY())
				{
					Block b = loc.getBlock();

					if (b.getType() == Material.AIR)
					{
						_blocks.add(b);

						loc.setY(l.getY() - 1);

						b.setType(loc.getBlock().getType());

						b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, loc.getBlock().getTypeId());
					}
				}
			}
		}

		if (_tick % 5 == 0)

		{
			for (Player player : UtilPlayer.getNearby(getLocation(), 2.5, true))
			{
				player.teleport(player.getLocation().add(0, 4, 0));
				UtilAction.velocity(player, new Vector(UtilMath.r(10) - 5, 3, UtilMath.r(10) - 5).normalize());
			}
		}

		if (_tick < 200)

		{
			Location loc = getLocation();
			loc.setY(loc.getY() + 4);

			for (int i = 0; i < 30; i++)
			{
				loc.setY(loc.getY() + 1);
				Block b = loc.getBlock();

				if (UtilBlock.solid(b))
				{
					break;
				}
			}

			if (!UtilBlock.solid(loc.getBlock()))
			{
				return;
			}

			List<Player> players = UtilPlayer.getNearby(getLocation(), 50, true);

			for (int i = 0; i < players.size() * 2; i++)
			{
				int dist = UtilMath.r(10);

				if (dist < 3)
				{
					dist = 2;
				}
				else if (dist < 5)
				{
					dist = 5;
				}
				else
				{
					dist = 10;
				}

				Location l = players.get(UtilMath.r(players.size())).getLocation().add(UtilMath.r(dist * 2) - dist, 0,
						UtilMath.r(dist * 2) - dist);
				l.setY(loc.getY());

				Block b = l.getBlock();
				l.subtract(0, 1, 0);

				if (UtilBlock.solid(b))
				{
					if (l.getBlock().getType() == Material.AIR)
					{
						if (UtilAlg.HasSight(l, getLocation().add(0, 4, 0)))
						{
							FallingBlock block = b.getWorld().spawnFallingBlock(b.getLocation().add(0.5, -1, 0.5), b.getTypeId(),
									b.getData());

							block.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, block.getBlockId());
							block.setDropItem(false);

							_fallingBlocks.add(block);
						}
					}
				}
			}
		}

	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

}
