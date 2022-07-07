package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityIronGolem;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.Vec3D;

public class GolemBlockHail extends BossAbility<GolemCreature, IronGolem>
{
	private int _currentBlock;
	private int _currentLevel;
	private List<FallingBlock> _fallingBlocks = new ArrayList<>();
	private Map<Integer, List<BlockHailBlock>> _floatingBlocks = new HashMap<>();
	private Map<String, Integer> _blocks = new HashMap<>();
	private int _levelToReach;
	private boolean _spawned;
	private List<Location> _spawnLocs = new ArrayList<>();
	private Player _target;
	private Location _center;
	private int _ticks;

	public GolemBlockHail(GolemCreature creature)
	{
		super(creature);

		_center = getLocation();

		if (creature.getHealthPercent() > 0.75)
		{
			_levelToReach = 1;
		}
		else if (creature.getHealthPercent() > 0.5)
		{
			_levelToReach = 2;
		}
		else
		{
			_levelToReach = 3;
		}

		_target = getTarget();
	}

	@Override
	public Player getTarget()
	{
		Player target = null;
		double dist = 0;

		if (inProgress())
		{
			for (Player player : UtilPlayer.getNearby(_center, 40, true))
			{
				if (!player.hasLineOfSight(getEntity()))
				{
					continue;
				}

				if (target != null && _blocks.containsKey(player.getName()) && _blocks.get(player.getName()) > 8)
				{
					continue;
				}

				double d = player.getLocation().distance(_center);

				if (target == null || dist > d)
				{
					target = player;
					dist = d;
				}
			}
		}

		return target;
	}

	@Override
	public boolean canMove()
	{
		return _spawned && _floatingBlocks.isEmpty();
	}

	@Override
	public boolean hasFinished()
	{
		return _target == null || !_target.isValid() || ((_fallingBlocks.isEmpty() && _spawned) && _ticks >= 8 * 9)
				|| _center.distance(_target.getLocation()) > 100;
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
				{
					getBoss().getEvent().getDamageManager().NewDamageEvent((LivingEntity) victim, getEntity(), null,
							DamageCause.CONTACT, 10 * getBoss().getDifficulty(), true, true, false, "Iron Wizard Block Hail",
							"Iron Wizard Block Hail");
				}

				if (victim instanceof Player)
				{
					getBoss().getEvent().getCondition().Factory().Slow("Iron Wizard Block Hail", (LivingEntity) victim,
							getEntity(), 3, 2, false, false, false, false);
				}

				fallingIterator.remove();
				cur.remove();
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
	public void setFinished()
	{
		for (List<BlockHailBlock> floatingBlocks : _floatingBlocks.values())
		{
			for (BlockHailBlock falling : floatingBlocks)
			{
				PacketPlayOutEntityDestroy packet = falling.getDestroyPacket();

				for (Player player : UtilServer.getPlayers())
				{
					UtilPlayer.sendPacket(player, packet);
				}
			}
		}

		for (FallingBlock block : _fallingBlocks)
		{
			block.remove();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick()
	{
		if (inProgress())
		{
			for (Player player : UtilPlayer.getNearby(_center, 5, true))
			{
				Location loc = player.getLocation();

				if (Math.abs(loc.getY() - (_center.getY() + 1)) <= 1)
				{
					loc.setY(_center.getY());

					if (loc.distance(_center) < 2.8 + (_currentLevel * 0.75))
					{
						if (canDamage(player))
						{
							getBoss().getEvent().getDamageManager().NewDamageEvent(player, getEntity(), null,
									DamageCause.CONTACT, 10 * getBoss().getDifficulty(), true, true, false,
									"Iron Wizard Protection", "Iron Wizard Protection");

							loc.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, Material.OBSIDIAN.getId());
							loc.getWorld().playEffect(player.getEyeLocation(), Effect.STEP_SOUND, Material.OBSIDIAN.getId());
						}
					}
				}
			}
		}

		if (!_spawned)
		{
			if (_currentBlock >= _spawnLocs.size())
			{

				if (_currentLevel + 1 <= _levelToReach)
				{
					_currentLevel++;
					_currentBlock = 0;

					_spawnLocs = UtilShapes.getDistancedCircle(_center.clone().add(0, 2.8 + (_currentLevel * 0.75), 0), 1.3,
							1 + (_currentLevel * 1.3));

					for (int i = UtilMath.r(_spawnLocs.size()); i > 0; i--)
					{
						_spawnLocs.add(_spawnLocs.remove(0));
					}
				}
			}

			if (_currentBlock < _spawnLocs.size())
			{
				if (_ticks % 2 == 0)
				{
					IronGolem entity = getEntity();

					Location loc = _spawnLocs.get(_currentBlock++);

					List<BlockHailBlock> floatingBlocks = new ArrayList<>();

					if (_floatingBlocks.containsKey(_currentLevel))
					{
						floatingBlocks = _floatingBlocks.get(_currentLevel);
					}
					else
					{
						_floatingBlocks.put(_currentLevel, floatingBlocks);
					}

					if (loc.getBlock().getType() == Material.AIR && UtilAlg.HasSight(entity.getLocation(), loc))
					{

						BlockHailBlock floating = new BlockHailBlock(loc, Material.STONE);
						UtilEnt.CreatureLook(entity, _target);

						floatingBlocks.add(floating);

						Packet<?>[] packets = floating.getSpawnPackets(entity);

						for (Player player : UtilPlayer.getNearby(loc, 100))
						{
							UtilPlayer.sendPacket(player, packets);
						}

						entity.getWorld().playSound(entity.getLocation(), Sound.DIG_GRASS, 3, 0.9F);
					}

					if (_floatingBlocks.size() % 2 == 0)
					{
						Collections.reverse(floatingBlocks);
					}
				}
			}
			else
			{
				_spawned = true;
				_ticks = -20;
			}
		}
		else if (_ticks > 0 && _ticks % 2 == 0 && !_floatingBlocks.isEmpty())
		{
			IronGolem entity = getEntity();

			if (_ticks % 16 == 0)
			{
				_target = getTarget();

				if (_target == null)
					return;
			}

			EntityIronGolem golem = ((CraftIronGolem) entity).getHandle();

			golem.world.broadcastEntityEffect(golem, (byte) 4);
			UtilEnt.CreatureLook(entity, _target);

			BlockHailBlock floatingBlock = null;

			for (int i = 1; i <= _currentLevel; i++)
			{
				if (_floatingBlocks.containsKey(i) && !_floatingBlocks.get(i).isEmpty())
				{
					floatingBlock = _floatingBlocks.get(i).remove(0);

					if (_floatingBlocks.get(i).isEmpty())
					{
						_floatingBlocks.remove(i);
					}

					break;
				}
			}

			PacketPlayOutEntityDestroy packet = floatingBlock.getDestroyPacket();

			for (Player player : UtilServer.getPlayers())
			{
				UtilPlayer.sendPacket(player, packet);
			}

			Location loc = floatingBlock.getLocation();

			FallingBlock b = loc.getWorld().spawnFallingBlock(loc, floatingBlock.getMaterial(), (byte) 0);
			b.setDropItem(false);

			Vector vec = UtilAlg.calculateVelocity(loc.toVector(),
					_target.getLocation().toVector().add(new Vector(UtilMath.r(6 + (_currentLevel * 2)) - (2 + _currentLevel), 0,
							UtilMath.r(6 + (_currentLevel * 2)) - (2 + _currentLevel))),
					6);

			b.setVelocity(vec);

			_fallingBlocks.add(b);

			entity.getWorld().playSound(entity.getLocation(), Sound.IRONGOLEM_THROW, 3, 0.9F);

			_blocks.put(_target.getName(), _blocks.containsKey(_target.getName()) ? _blocks.get(_target.getName()) + 1 : 1);
		}

		List<Location> points = new ArrayList<>();

		for (int i = _currentLevel; i <= _currentLevel; i++)
		{
			points.addAll(UtilShapes.getDistancedCircle(_center.clone().add(0, 3.3 + (i * 0.75), 0), 0.3, 1 + (i * 1.3)));
		}

		for (int i = 0; i < points.size(); i++)
		{
			if (_spawned || i < _ticks)
			{
				Location loc = points.get(i);

				UtilParticle.PlayParticle(
						ParticleType.BLOCK_DUST.getParticle(_spawned && i < _ticks ? Material.STONE : Material.DIRT, 0), loc, 0,
						0, 0, 0, 0, UtilParticle.ViewDist.LONG, UtilServer.getPlayers());
			}
		}

		_ticks++;
	}

	@Override
	public boolean inProgress()
	{
		return !_spawned || !_floatingBlocks.isEmpty();
	}
}