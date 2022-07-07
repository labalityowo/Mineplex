package mineplex.minecraft.game.core.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityIronGolem;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.Vec3D;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class GolemBlockShot extends BossAbility<GolemCreature, IronGolem>
{
	private HashMap<Integer, Location> _blockLoc = new HashMap<Integer, Location>();
	private HashMap<Integer, Material> _blockType = new HashMap<Integer, Material>();
	private ArrayList<FallingBlock> _current = new ArrayList<FallingBlock>();
	private HashMap<Integer, Long> _preshoot = new HashMap<Integer, Long>();
	private Player _target;
	private HashMap<Integer, Player> _targetBlock = new HashMap<Integer, Player>();
	private HashMap<UUID, Integer> _shotAt = new HashMap<UUID, Integer>();
	private int _thrown;
	private int _tick;
	private int _toThrow;

	public GolemBlockShot(GolemCreature creature)
	{
		super(creature);

		if (creature.getHealthPercent() > 0.75)
		{
			_toThrow = 3;
		}
		else if (creature.getHealthPercent() > 0.5)
		{
			_toThrow = 6;
		}
		else
		{
			_toThrow = 9;
		}

		_target = getTarget();
	}

	@Override
	public boolean canMove()
	{
		return _current.isEmpty() && _thrown == _toThrow;
	}

	@Override
	public Player getTarget()
	{
		Player target = null;
		double dist = 0;

		Location loc1 = getLocation();
		Location loc2 = loc1.clone().add(loc1.getDirection().setY(0).normalize());

		List<Player> players = UtilPlayer.getNearby(getLocation(), 40, true);

		for (Player player : players)
		{
			if (_shotAt.containsKey(player.getUniqueId()) && _shotAt.get(player.getUniqueId()) >= 3)
			{
				continue;
			}

			double dist1 = player.getLocation().distance(loc1);
			double dist2 = player.getLocation().distance(loc2);

			double dist3 = dist1 - dist2;

			if (dist3 < 0.6 || dist1 > 30 || (target != null && dist3 < dist))
			{
				continue;
			}

			if (!player.hasLineOfSight(getEntity()))
			{
				continue;
			}

			target = player;
			dist = dist3;
		}

		return target;
	}

	@Override
	public boolean hasFinished()
	{
		return _current.isEmpty() && _preshoot.isEmpty() && (_target == null || _thrown >= _toThrow);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<FallingBlock> fallingIterator = _current.iterator();

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

				getBoss().getEvent().getDamageManager().NewDamageEvent((LivingEntity) victim, getEntity(), null,
						DamageCause.CONTACT, 10 * getBoss().getDifficulty(), true, true, false, "Iron Wizard Block Shot",
						"Iron Wizard Block Shot");

				cur.remove();
				fallingIterator.remove();

				Vector vec = UtilAlg.getTrajectory(getEntity(), victim);
				vec.setY(0).normalize();

				double strength = 1;

				if (!(victim instanceof Player) || !((Player) victim).isBlocking())
				{
					strength = 1.3;
				}

				UtilAction.velocity(victim, vec, strength, true, 0, 0.2, 1, true);
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
		for (FallingBlock falling : _current)
		{
			falling.remove();
		}

		int[] ids = new int[_preshoot.size()];

		int a = 0;
		for (int id : _preshoot.keySet())
		{
			ids[a++] = id;
		}

		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(ids);

		for (Player player : Bukkit.getOnlinePlayers())
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	@Override
	public void tick()
	{
		if (_target == null || _target.getLocation().distance(getLocation()) > 30 || !_target.hasLineOfSight(getEntity()))
		{
			_target = getTarget();
		}

		Entity entity = getEntity();

		if (_tick++ % 16 == 0 && _target != null && _thrown < _toThrow)
		{
			_thrown++;

			UtilEnt.CreatureLook(entity, _target);
			EntityIronGolem golem = ((CraftIronGolem) entity).getHandle();

			golem.world.broadcastEntityEffect(golem, (byte) 4);

			entity.getWorld().playSound(entity.getLocation(), Sound.IRONGOLEM_THROW, 2, 1);

			Location loc = entity.getLocation();
			loc.setYaw(loc.getYaw() + (UtilMath.r(150) - 75));
			loc.add(loc.getDirection().setY(0).normalize());

			Block block = loc.getBlock();

			if (block.getType() == Material.AIR)
			{
				block = block.getRelative(BlockFace.DOWN);
			}

			Material mat = block.getType();

			if (!UtilBlock.solid(block))
			{
				mat = Material.STONE;
			}

			int id = UtilEnt.getNewEntityId();

			_preshoot.put(id, System.currentTimeMillis());
			_blockType.put(id, mat);
			_blockLoc.put(id, loc.clone().add(0, 0.6, 0));
			_targetBlock.put(id, _target);

			PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(((CraftEntity) entity).getHandle(), 70, mat.getId());

			packet.a = id;

			packet.b = (int) Math.floor(loc.getX() * 32);
			packet.c = (int) Math.floor(loc.getY() * 32);
			packet.d = (int) Math.floor(loc.getZ() * 32);

			packet.g = (int) ((0.45) * 8000);

			PacketPlayOutEntityVelocity packet2 = new PacketPlayOutEntityVelocity(id, 0, 0.45D, 0);

			for (Player player : UtilPlayer.getNearby(loc, 70))
			{
				UtilPlayer.sendPacket(player, packet, packet2);
			}

			_shotAt.put(_target.getUniqueId(),
					(_shotAt.containsKey(_target.getUniqueId()) ? _shotAt.get(_target.getUniqueId()) : 0) + 1);

			_target = getTarget();
		}
		else
		{
			Iterator<Entry<Integer, Long>> itel = _preshoot.entrySet().iterator();

			while (itel.hasNext())
			{
				Entry<Integer, Long> entry = itel.next();

				if (UtilTime.elapsed(entry.getValue(), 920))
				{
					itel.remove();

					int id = entry.getKey();

					PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[]
						{
								id
						});

					for (Player player : Bukkit.getOnlinePlayers())
					{
						UtilPlayer.sendPacket(player, packet);
					}

					Location loc = _blockLoc.get(id);
					FallingBlock falling = loc.getWorld().spawnFallingBlock(loc, _blockType.get(id), (byte) 0);
					falling.setDropItem(false);

					_current.add(falling);

					Player target = _targetBlock.get(id);

					UtilEnt.CreatureLook(entity, target);
					EntityIronGolem golem = ((CraftIronGolem) entity).getHandle();

					golem.world.broadcastEntityEffect(golem, (byte) 4);

					entity.getWorld().playSound(entity.getLocation(), Sound.IRONGOLEM_THROW, 2, 1.2F);
					entity.getWorld().playEffect(falling.getLocation(), Effect.STEP_SOUND, falling.getBlockId());

					Location l = falling.getLocation();
					l.setY(entity.getLocation().getY());

					Location loc1 = target.getEyeLocation();

					if (loc1.getY() - l.getY() > 1)
					{
						loc1.setY(l.getY() + 1);
					}

					int dist = (int) Math.ceil(loc1.toVector().setY(0).distance(l.toVector().setY(0)));

					Vector vector = UtilAlg.calculateVelocity(l.toVector(), loc1.toVector(), dist / 13);

					falling.setVelocity(vector);// .multiply(0.5 + (l.distance(target.getEyeLocation()) / 10)).multiply(0.7));
				}
			}
		}

		if (_thrown >= 3 && !UtilPlayer.getNearby(

		getLocation(), 10, true).isEmpty())
		{
			_thrown = 99;
		}
	}

	@Override
	public boolean inProgress()
	{
		return _thrown < _toThrow || !_current.isEmpty();
	}
}
