package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.Vec3D;

public class GolemWallExplode extends BossAbility<GolemCreature, IronGolem>
{
	private Map<BlockFace, List<Block>> _blockWalls = new HashMap<>();
	private List<String> _dontTarget = new ArrayList<>();
	private List<FallingBlock> _fallingBlocks = new ArrayList<>();
	private int _maxTimes = UtilMath.r(2) + 1;
	private int _tick;
	private int _timesDone;
	private Map<BlockFace, Long> _wallTimers = new HashMap<>();

	public GolemWallExplode(GolemCreature creature)
	{
		super(creature);
	}

	@Override
	public boolean canMove()
	{
		return true;
	}

	@Override
	public int getCooldown()
	{
		return 20;
	}

	private float getMod(int div)
	{
		return UtilMath.random.nextFloat() / div;
	}

	@Override
	public Player getTarget()
	{
		for (Player player : UtilPlayer.getNearby(getLocation(), 15, true))
		{
			if (_dontTarget.contains(player.getName()))
			{
				continue;
			}

			if (player.getLocation().distance(getLocation()) <= 4)
			{
				continue;
			}

			return player;
		}

		return null;
	}

	@Override
	public boolean hasFinished()
	{
		return _wallTimers.isEmpty() && _fallingBlocks.isEmpty() && _timesDone >= _maxTimes;
	}

	@SuppressWarnings("deprecation")
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
							DamageCause.CONTACT, 10 * getBoss().getDifficulty(), true, true, false, "Iron Wizard Wall Explosion",
							"Iron Wizard Wall Explosion");
				}

				cur.remove();
				fallingIterator.remove();

				Vector vec = cur.getVelocity();

				UtilAction.velocity(victim, vec, 1.5, true, 0, 0.2, 1, true);
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

		for (List<Block> list : _blockWalls.values())
		{
			for (Block b : list)
			{
				b.setType(Material.AIR);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick()
	{
		if (_tick++ % 30 == 0 && _timesDone < _maxTimes)
		{
			_dontTarget.clear();

			_timesDone++;

			for (int i = 0; i < 10; i++)
			{
				Player target = getTarget();

				if (target == null)
				{
					if (_dontTarget.isEmpty())
					{
						_timesDone = _maxTimes;
					}
				}
				else
				{
					_dontTarget.add(target.getName());

					UtilEnt.CreatureLook(getEntity(), target);

					BlockFace face = UtilShapes.getFacing(getLocation().getYaw());

					if (_wallTimers.containsKey(face))
					{
						continue;
					}

					ArrayList<Block> blocks = new ArrayList<Block>();

					Location loc = getLocation().getBlock().getLocation().add(0.5, 0, 0.5);

					int mul = (face.getModX() != 0 && face.getModZ() != 0) ? 2 : 3;

					loc.add(face.getModX() * mul, 0, face.getModZ() * mul);

					Block b = loc.getBlock();

					BlockFace sideFace = UtilShapes.getSideBlockFaces(face, true)[0];
					boolean invalid = false;

					for (int mult = -3; mult <= 3; mult++)
					{
						Block block = b;

						if (Math.abs(mult) < 3)
						{
							block = block.getRelative(face);
						}

						block = block.getRelative(sideFace, mult);

						if (Math.abs(mult) == 3 && face.getModX() != 0 && face.getModZ() != 0)
						{
							block = block.getRelative(UtilShapes.getSideBlockFaces(face, false)[mult < 0 ? 0 : 1]);
						}

						if (!UtilAlg.HasSight(getLocation(), block.getLocation()))
						{
							invalid = true;
							break;
						}

						Block under = block.getRelative(0, -1, 0);

						if (!UtilBlock.solid(under))
						{
							continue;
						}

						for (int y = 0; y <= 1; y++)
						{
							block = block.getRelative(0, y, 0);

							if (block.getType() != Material.AIR)
							{
								invalid = true;
								break;
							}

							blocks.add(block);
						}

						if (invalid)
						{
							break;
						}
					}

					if (invalid)
					{
						continue;
					}

					for (Block block : blocks)
					{
						block.setType(block.getWorld().getBlockAt(block.getX(), b.getY() - 1, block.getZ()).getType());
						block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
					}

					_blockWalls.put(face, blocks);
					_wallTimers.put(face, System.currentTimeMillis());
					break;
				}
			}
		}

		Iterator<Entry<BlockFace, List<Block>>> itel = _blockWalls.entrySet().iterator();
		boolean doExplode = false;

		while (itel.hasNext())
		{
			Entry<BlockFace, List<Block>> entry = itel.next();
			BlockFace face = entry.getKey();

			if (UtilTime.elapsed(_wallTimers.get(face), 1000))
			{
				doExplode = true;
				itel.remove();
				_wallTimers.remove(face);

				for (Block b : entry.getValue())
				{
					FallingBlock block = getEntity().getWorld().spawnFallingBlock(b.getLocation().add(0.5, 0, 0.5), b.getType(),
							(byte) 0);

					block.setDropItem(false);

					int index = entry.getValue().indexOf(b);

					BlockFace f = index == 8 || index == 9 ? BlockFace.SELF
							: UtilShapes.getSideBlockFaces(face, true)[index > 6 ? 0 : 1];

					block.setVelocity(new Vector((face.getModX() * 0.6) + (f.getModX() * (0.05 + getMod(10))), 0.2 + getMod(15),
							(face.getModZ() * 0.6) + (f.getModZ() * (0.05 + getMod(10)))));

					_fallingBlocks.add(block);

					b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
					b.setType(Material.AIR);
				}
			}
		}

		if (doExplode)
		{
			onUpdate(new UpdateEvent(UpdateType.TICK));
		}
	}

	@Override
	public boolean inProgress()
	{
		return false;
	}
}