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
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.Vec3D;

public class GolemExplodingAura extends BossAbility<GolemCreature, IronGolem>
{
	private Map<Integer, Integer> _blocks = new HashMap<>();
	private Map<Integer, Location> _blocksLoc = new HashMap<>();
	private List<FallingBlock> _fallingBlocks = new ArrayList<>();
	private Map<Integer, Material> _blockMaterial = new HashMap<>();
	private int _tick;

	public GolemExplodingAura(GolemCreature creature)
	{
		super(creature);
	}

	@Override
	public boolean canMove()
	{
		return false;
	}

	@Override
	public boolean hasFinished()
	{
		return _tick > 20 * 30 && _blocks.isEmpty() && _fallingBlocks.isEmpty();
	}

	@Override
	public void setFinished()
	{
		for (FallingBlock block : _fallingBlocks)
		{
			block.remove();
		}

		int[] ids = new int[_blocks.size() * 2];

		int i = 0;

		for (Entry<Integer, Integer> id : _blocks.entrySet())
		{
			ids[i] = id.getKey();
			ids[i + 1] = id.getValue();

			i += 2;
		}

		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(ids);

		for (Player player : UtilServer.getPlayers())
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick()
	{
		if (_tick < 25 * 25 && getBoss().getHealth() > 30)
		{
			double angle = (2 * Math.PI) / UtilMath.random.nextDouble();
			double x = 1.7 * Math.cos(angle);
			double z = 1.7 * Math.sin(angle);

			Location loc = getLocation().add(x, 1 + (UtilMath.random.nextDouble() * 1.6), z);

			loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.DIRT.getId());

			for (Player player : UtilPlayer.getNearby(getLocation(), 3, true))
			{
				getBoss().getEvent().getDamageManager().NewDamageEvent(player, getEntity(), null, DamageCause.CONTACT,
						6 * getBoss().getDifficulty(), true, true, false, "Iron Wizard Protection", "Iron Wizard Protection");
				UtilAction.velocity(player, UtilAlg.getTrajectory(getEntity(), player), 1, true, 0.3, 0, 0.3, false);
			}
		}

		if (_tick < 20 * 30)
		{
			int key = UtilEnt.getNewEntityId();
			int value = UtilEnt.getNewEntityId();

			Location loc = null;

			for (int i = 0; i < 30; i++)
			{
				double angle = (2 * Math.PI) / UtilMath.random.nextDouble();
				double x = 1.7 * Math.cos(angle);
				double z = 1.7 * Math.sin(angle);

				loc = getLocation().add(x, 1 + (UtilMath.random.nextDouble() * 1.6), z);
				boolean found = false;

				for (Location l : _blocksLoc.values())
				{
					if (l.distance(loc) < 0.3)
					{
						found = true;
						break;
					}
				}

				if (found)
				{
					loc = null;
				}
				else
				{
					break;
				}
			}

			if (loc != null)
			{
				_blocks.put(key, value);
				_blocksLoc.put(key, loc);
				_blockMaterial.put(key, UtilMath.random.nextBoolean() ? Material.DIRT : Material.STONE);

				Packet<?>[] packets = new Packet[3];

				PacketPlayOutSpawnEntityLiving packet1 = new PacketPlayOutSpawnEntityLiving();

				DataWatcher watcher = new DataWatcher(null);
				watcher.a(0, (byte) 32, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0);
				watcher.a(1, 0, net.minecraft.server.v1_8_R3.Entity.META_AIR, 0);

				packet1.a = key;
				packet1.b = EntityType.SILVERFISH.getTypeId();
				packet1.c = (int) Math.floor(loc.getX() * 32);
				packet1.d = (int) Math.floor((loc.getY() - 0.125) * 32);
				packet1.e = (int) Math.floor(loc.getZ() * 32);
				packet1.l = watcher;

				packets[0] = packet1;

				PacketPlayOutSpawnEntity packet2 = new PacketPlayOutSpawnEntity(((CraftEntity) getEntity()).getHandle(), 70,
						_blockMaterial.get(key).getId());

				packet2.a = value;

				packet2.b = (int) Math.floor(loc.getX() * 32);
				packet2.c = (int) Math.floor(loc.getY() * 32);
				packet2.d = (int) Math.floor(loc.getZ() * 32);

				packets[1] = packet2;

				PacketPlayOutAttachEntity packet3 = new PacketPlayOutAttachEntity();

				packet3.b = value;
				packet3.c = key;

				packets[2] = packet3;

				for (Player player : UtilPlayer.getNearby(getLocation(), 70))
				{
					UtilPlayer.sendPacket(player, packets);
				}
			}
		}

		if (_tick % 25 == 0)
		{
			for (int i = 0; i < 3; i++)
				getLocation().getWorld().playSound(getLocation(), Sound.DIG_GRASS, 3, 2);

			for (int key : new ArrayList<Integer>(_blocksLoc.keySet()))
			{

				PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(new int[]
					{
							key, _blocks.remove(key)
					});

				for (Player player : UtilServer.getPlayers())
				{
					UtilPlayer.sendPacket(player, destroyPacket);
				}

				Location loc = _blocksLoc.remove(key);

				FallingBlock falling = loc.getWorld().spawnFallingBlock(loc, _blockMaterial.remove(key), (byte) 0);

				_fallingBlocks.add(falling);

				Vector vec = UtilAlg.getTrajectory(getLocation().add(0, 1, 0), loc);

				vec.setY(Math.max(0.05, vec.getY()));

				falling.setVelocity(vec);

				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.DIRT.getId());
			}
		}

		_tick++;
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
					{
						continue;
					}

					// Creative or Spec
					if (ent instanceof Player)
					{
						if (((Player) ent).getGameMode() == GameMode.CREATIVE || UtilPlayer.isSpectator(ent))
						{
							continue;
						}
					}

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

				{
					getBoss().getEvent().getDamageManager().NewDamageEvent((LivingEntity) victim, getEntity(), null,
							DamageCause.CONTACT, 6 * getBoss().getDifficulty(), true, true, false, "Blocky Iron Wizard Aura",
							"Blocky Iron Wizard Aura");
				}

				fallingIterator.remove();
				cur.remove();

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

					fallingIterator.remove();
					cur.remove();
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
	public boolean inProgress()
	{
		return false;
	}
}