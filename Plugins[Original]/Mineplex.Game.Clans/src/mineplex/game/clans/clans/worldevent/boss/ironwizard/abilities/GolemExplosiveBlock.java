package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityIronGolem;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.Vec3D;

public class GolemExplosiveBlock extends BossAbility<GolemCreature, IronGolem>
{
	private Map<Integer, Vector> _blocksLocation = new HashMap<>();
	private Location _center;
	private int _explosionsLeft;
	private FallingBlock _fallingBlock;
	private Map<Integer, Integer> _fallingBlocks = new HashMap<>();
	private List<Item> _items = new ArrayList<>();
	private int _strength;
	private Player _target;
	private int _tick;

	public GolemExplosiveBlock(GolemCreature creature, int strength)
	{
		super(creature);

		_strength = strength;
		_center = getLocation().add(0, 3, 0);
		_target = getTarget();

		if (_target != null)
		{
			UtilEnt.CreatureLook(getEntity(), _target);
		}
	}

	@Override
	public boolean canMove()
	{
		return _fallingBlock != null;
	}

	private int clamp(int value)
	{
		if (value < -127)
		{
			return -127;
		}

		if (value > 127)
		{
			return 127;
		}

		return value;
	}

	@Override
	public Player getTarget()
	{
		HashMap<Player, Double> locs = new HashMap<Player, Double>();

		for (Player player : UtilServer.getPlayers())
		{
			double dist = player.getLocation().distance(_center);

			if (dist < 30)
			{
				double score = (dist > 10 ? 30 - dist : 10);

				for (Player p : UtilServer.getPlayers())
				{
					if (player.getLocation().distance(p.getLocation()) < 4)
					{
						score += 7;
					}
				}

				if (player.hasLineOfSight(getEntity()))
				{
					score += 10;
				}

				locs.put(player, score);
			}
		}

		Player lowest = null;

		for (Entry<Player, Double> entry : locs.entrySet())
		{
			if (lowest == null || locs.get(lowest) > locs.get(entry.getKey()))
			{
				lowest = entry.getKey();
			}
		}

		return lowest;
	}

	@Override
	public boolean hasFinished()
	{
		return _target == null || (_fallingBlock != null && !_fallingBlock.isValid() && _explosionsLeft == 0);
	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(getEntity()))
		{
			if (_tick >= 40 + (40 * _strength) && _tick <= 50 + (40 * _strength))
			{
				event.SetCancelled("Iron Wizard charging bomb");
			}

			event.SetKnockback(false);
		}
	}

	public void onExplode(final Location loc)
	{
		for (int i = 0; i < _strength * 2; i++)
		{
			if (i == 0)
			{
				onSubExplode(loc);
			}
			else
			{
				_explosionsLeft++;

				Bukkit.getScheduler().scheduleSyncDelayedTask(UtilServer.getPlugin(), () ->
				{
					onSubExplode(loc);
					_explosionsLeft--;
				}, 2 * i);
			}
		}
	}

	public void onSubExplode(Location loc)
	{
		for (int i = 0; i < 2; i++)
		{
			Location l = loc.clone().add(UtilMath.r(_strength * 4) - (_strength * 2), UtilMath.r(_strength * 2),
					UtilMath.r(_strength * 4) - (_strength * 2));

			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, l, _strength * 3, 1, _strength * 3, 0, _strength * 4,
					ViewDist.LONG, UtilServer.getPlayers());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (_fallingBlock == null)
		{
			return;
		}

		if (_fallingBlock.isDead()
				|| !_fallingBlock.isValid()
				|| _fallingBlock.getTicksLived() > 400
				|| !_fallingBlock.getWorld().isChunkLoaded(_fallingBlock.getLocation().getBlockX() >> 4,
						_fallingBlock.getLocation().getBlockZ() >> 4))
		{
			Block block = _fallingBlock.getLocation().getBlock();
			block.setTypeIdAndData(0, (byte) 0, true);
			_fallingBlock.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, _fallingBlock.getBlockId());

			// Expire
			if (_fallingBlock.getTicksLived() > 400
					|| !_fallingBlock.getWorld().isChunkLoaded(_fallingBlock.getLocation().getBlockX() >> 4,
							_fallingBlock.getLocation().getBlockZ() >> 4))
			{
				_fallingBlock.remove();
				return;
			}

			_fallingBlock.remove();
			return;
		}

		double distanceToEntity = 0.0D;
		LivingEntity victim = null;

		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) _fallingBlock).getHandle();
		Vec3D vec3d = new Vec3D(nmsEntity.locX, nmsEntity.locY, nmsEntity.locZ);
		Vec3D vec3d1 = new Vec3D(nmsEntity.locX + nmsEntity.motX, nmsEntity.locY + nmsEntity.motY, nmsEntity.locZ + nmsEntity.motZ);

		MovingObjectPosition finalObjectPosition = nmsEntity.world.rayTrace(vec3d, vec3d1, false, true, false);
		vec3d = new Vec3D(nmsEntity.locX, nmsEntity.locY, nmsEntity.locZ);
		vec3d1 = new Vec3D(nmsEntity.locX + nmsEntity.motX, nmsEntity.locY + nmsEntity.motY, nmsEntity.locZ + nmsEntity.motZ);

		if (finalObjectPosition != null)
		{
			vec3d1 = new Vec3D(finalObjectPosition.pos.a, finalObjectPosition.pos.b, finalObjectPosition.pos.c);
		}

		for (Object entity : ((CraftWorld) _fallingBlock.getWorld()).getHandle().getEntities(
				((CraftEntity) _fallingBlock).getHandle(),
				((CraftEntity) _fallingBlock).getHandle().getBoundingBox().a(((CraftEntity) _fallingBlock).getHandle().motX,
						((CraftEntity) _fallingBlock).getHandle().motY, ((CraftEntity) _fallingBlock).getHandle().motZ).grow(2,
						2, 2)))
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
			onExplode(victim.getEyeLocation());

			_fallingBlock.remove();
		}
		else if (finalObjectPosition != null)
		{
			Block block = _fallingBlock.getWorld()
					.getBlockAt(((int) finalObjectPosition.pos.a), ((int) finalObjectPosition.pos.b), ((int) finalObjectPosition.pos.c));

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

				onExplode(block.getLocation().add(0.5, 0.5, 0.5));

				_fallingBlock.remove();
			}
		}
		else
		{
			UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.STONE, 0),
					_fallingBlock.getLocation().add(0, 0.5, 0), 0.3F, 0.3F, 0.3F, 0, 2, UtilParticle.ViewDist.NORMAL,
					UtilServer.getPlayers());
		}
	}

	@Override
	public void setFinished()
	{
		int[] ids = new int[_fallingBlocks.size() * 2];

		int index = 0;

		for (Entry<Integer, Integer> entry : _fallingBlocks.entrySet())
		{
			ids[index] = entry.getKey();
			ids[index + 1] = entry.getValue();
			index += 2;
		}

		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(ids);

		for (Player player : Bukkit.getOnlinePlayers())
		{
			UtilPlayer.sendPacket(player, packet);
		}

		for (Item item : _items)
		{
			item.remove();
		}

		if (_fallingBlock != null)
		{
			_fallingBlock.remove();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick()
	{
		IronGolem entity = getEntity();

		Iterator<Item> itel = _items.iterator();

		while (itel.hasNext())
		{
			Item item = itel.next();

			Vector vec = item.getVelocity();
			Location loc = item.getLocation();

			if (item.getTicksLived() > 100 || vec.getY() <= 0 || loc.distance(_center) > loc.add(vec).distance(_center)
					|| UtilEnt.isGrounded(item))
			{
				itel.remove();
			}
		}

		// This spawns a floating block
		if (_tick >= 20 && _tick % 60 == 0 && _fallingBlocks.size() < _strength)
		{
			int id = UtilEnt.getNewEntityId();
			int id2 = UtilEnt.getNewEntityId();

			_fallingBlocks.put(id, id2);
			_blocksLocation.put(id, _center.toVector());

			Packet<?>[] packets = new Packet[3];

			PacketPlayOutSpawnEntityLiving packet1 = new PacketPlayOutSpawnEntityLiving();

			DataWatcher watcher = new DataWatcher(null);
			watcher.a(0, (byte) 32, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0);
			watcher.a(1, 0, net.minecraft.server.v1_8_R3.Entity.META_AIR, 0);

			packet1.a = id;
			packet1.b = EntityType.SILVERFISH.getTypeId();
			packet1.c = (int) Math.floor(_center.getX() * 32);
			packet1.d = (int) Math.floor((_center.getY() - 0.125) * 32);
			packet1.e = (int) Math.floor(_center.getZ() * 32);
			packet1.l = watcher;

			packets[0] = packet1;

			PacketPlayOutSpawnEntity packet2 = new PacketPlayOutSpawnEntity(((CraftEntity) entity).getHandle(), 70,
					Material.DIRT.getId());

			packet2.a = id2;

			packet2.b = (int) Math.floor(_center.getX() * 32);
			packet2.c = (int) Math.floor(_center.getY() * 32);
			packet2.d = (int) Math.floor(_center.getZ() * 32);

			packets[1] = packet2;

			PacketPlayOutAttachEntity packet3 = new PacketPlayOutAttachEntity();

			packet3.b = id2;
			packet3.c = id;

			packets[2] = packet3;

			for (Player player : UtilServer.getPlayers())
			{
				if (player.getLocation().distance(_center) < 80)
				{
					UtilPlayer.sendPacket(player, packets);
				}
			}
		}

		// This spawns a item that flies above the golem's head and disappears
		if (UtilMath.r(6) == 0 && _tick < 40 + (_strength * 40))
		{
			double angle = ((2 * Math.PI) / 30) * UtilMath.r(30);
			double x = 5 * Math.cos(angle);
			double z = 5 * Math.sin(angle);
			Location loc = _center.clone().add(x, -3, z);

			Material mat = null;

			switch (UtilMath.r(3))
			{
			case 0:
				mat = Material.DIRT;
				break;
			case 1:
				mat = Material.STONE;
				break;
			case 2:
				mat = Material.COBBLESTONE;
				break;
			default:
				break;
			}

			Item item = loc.getWorld().dropItem(loc, new ItemBuilder(mat).setTitle(System.currentTimeMillis() + "").build());

			item.setPickupDelay(999999);

			Vector vec = UtilAlg.getTrajectory(_center, item.getLocation());
			
			vec.normalize().multiply(5);

			item.setVelocity(vec);

			// TODO Fix velocity

			_items.add(item);
		}

		// 10 being when items no longer fly in, 0 being when its shot.
		int ticksTillFired = (60 + (40 * _strength)) - _tick;

		if (ticksTillFired > 20)
		{
			int strength = (int) Math.floor(_tick / 20D);

			int nine = 8 - strength;

			if (_tick % nine == 0)
			{
				_center.getWorld().playSound(_center, Sound.DIG_GRASS, strength + 1, 1F);
			}
		}
		else if (ticksTillFired < 0)
		{
			if (_tick % 3 == 0)
			{
				_center.getWorld().playSound(_fallingBlock.getLocation(), Sound.WOOD_CLICK, _strength + 1, 0.4F);
			}
		}

		// The location the falling blocks need to stick by
		Vector blockCenter = _center.toVector();

		if (ticksTillFired >= 0 && ticksTillFired <= 20)
		{
			Vector vec = entity.getLocation().add(entity.getLocation().getDirection().setY(0).normalize().multiply(1.2))
					.add(0, 1, 0).toVector();

			blockCenter = UtilAlg.getTrajectory(_center.toVector(), blockCenter);
			vec.multiply(ticksTillFired / 10D);

			_center.getWorld().playSound(_center, Sound.DIG_SNOW, _strength + 1, 0);
		}
		else if (_fallingBlock != null)
		{
			blockCenter = _fallingBlock.getLocation().add(0, 0.5, 0).toVector();
		}

		// Move the fake floating blocks
		for (Entry<Integer, Integer> entry : _fallingBlocks.entrySet())
		{
			int id = entry.getKey();
			Vector vec = _blocksLocation.get(id);

			int x = clamp((int) ((blockCenter.getX() - vec.getX()) * 32) + (UtilMath.r(8) - 4));
			int y = clamp((int) ((blockCenter.getY() - vec.getY()) * 32) + (UtilMath.r(8) - 4));
			int z = clamp((int) ((blockCenter.getZ() - vec.getZ()) * 32) + (UtilMath.r(8) - 4));

			vec.add(new Vector(x, y, z));

			PacketPlayOutEntity.PacketPlayOutRelEntityMove packet = new PacketPlayOutEntity.PacketPlayOutRelEntityMove();

			packet.a = id;

			packet.b = (byte) x;
			packet.c = (byte) y;
			packet.d = (byte) z;

			for (Player player : UtilServer.getPlayers())
			{
				if (player.getLocation().distance(_center) < 70)
				{
					UtilPlayer.sendPacket(player, packet);

					UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.STONE, 0),
							vec.toLocation(_center.getWorld()), 0.7F, 0.7F, 0.7F, 0, 11, ViewDist.NORMAL, player);
				}
			}
		}

		if (ticksTillFired == 0)
		{
			int id = _fallingBlocks.keySet().iterator().next();

			PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[]
				{
						id, _fallingBlocks.get(id)
				});

			for (Player player : Bukkit.getOnlinePlayers())
			{
				UtilPlayer.sendPacket(player, packet);
			}

			_fallingBlocks.remove(id);

			_fallingBlock = _center.getWorld().spawnFallingBlock(_blocksLocation.get(id).toLocation(_center.getWorld()),
					Material.STONE, (byte) 0);

			Vector vec1 = _fallingBlock.getLocation().toVector();
			Vector vec2 = _target.getLocation().toVector();

			Vector vec = UtilAlg.calculateVelocity(vec1, vec2, (int) (vec1.distanceSquared(vec2) / 4));

			_fallingBlock.setVelocity(vec);

			EntityIronGolem golem = ((CraftIronGolem) entity).getHandle();

			golem.world.broadcastEntityEffect(golem, (byte) 4);
		}

		_tick++;
	}

	@Override
	public boolean inProgress()
	{
		return false;
	}
}