package mineplex.minecraft.game.core.explosion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.explosion.ExplosionEvent;
import mineplex.minecraft.game.core.damage.DamageManager;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EnchantmentProtection;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Explosion;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.Material;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutExplosion;
import net.minecraft.server.v1_8_R3.Vec3D;
import net.minecraft.server.v1_8_R3.World;

public class CustomExplosion extends Explosion
{
	private org.bukkit.entity.LivingEntity _owner;
	private boolean _damageOwner;
	private World _world;
	private DamageManager _manager;
	private String _damageReason;
	private boolean _dropItems = true;
	private boolean _damageBlocksEqually;
	private boolean _createFire;
	private boolean _ignoreRate = true;
	private float _blockExplosionSize;
	private boolean _fallingBlockExplosion;
	private mineplex.core.explosion.Explosion _explosion;
	private float _damage;
	private boolean _useCustomDamage;
	private int _maxFallingBlocks = 0;
	private float _maxDamage = 1000;
	private float _size;
	private boolean _damageBlocks = true;
	private double posX, posY, posZ;
	private boolean _ignoreNonLiving;

	public CustomExplosion(DamageManager manager, mineplex.core.explosion.Explosion explosion, Location loc, float explosionSize,
			String deathCause)
	{
		super(((CraftWorld) loc.getWorld()).getHandle(), null, loc.getX(), loc.getY(), loc.getZ(), explosionSize, false, false);

		posX = loc.getX();
		posY = loc.getY();
		posZ = loc.getZ();
		
		_world = ((CraftWorld) loc.getWorld()).getHandle();
		_manager = manager;
		_damageReason = deathCause;
		_blockExplosionSize = explosionSize;
		_explosion = explosion;
		_size = explosionSize;
	}

	/**
	 * Center of explosion does this much damage
	 */
	public CustomExplosion setExplosionDamage(float damage)
	{
		_damage = damage;
		_useCustomDamage = true;

		return this;
	}

	public CustomExplosion setIgnoreNonLiving(boolean ignoreNonLiving)
	{
		_ignoreNonLiving = ignoreNonLiving;

		return this;
	}
	
	public CustomExplosion setMaxDamage(float maxDamage)
	{
		_maxDamage = maxDamage;

		return this;
	}

	public CustomExplosion setBlockExplosionSize(float explosionSize)
	{
		_blockExplosionSize = explosionSize;

		return this;
	}

	public CustomExplosion setIgnoreRate(boolean ignoreRate)
	{
		_ignoreRate = ignoreRate;

		return this;
	}

	public CustomExplosion setFallingBlockExplosion(boolean fallingBlockExplosion)
	{
		_fallingBlockExplosion = fallingBlockExplosion;

		return this;
	}

	public CustomExplosion setFallingBlockExplosionAmount(int maxFallingBlocks)
	{
		_maxFallingBlocks = maxFallingBlocks;

		return this;
	}

	public CustomExplosion setDamageBlocks(boolean damageBlocks)
	{
		_damageBlocks = damageBlocks;

		return this;
	}

	public CustomExplosion setBlocksDamagedEqually(boolean damageEqually)
	{
		_damageBlocksEqually = damageEqually;

		return this;
	}

	public CustomExplosion explode()
	{
		// Explode the explosion
		a();
		a(true);

		return this;
	}

	public CustomExplosion setDropItems(boolean dropItems)
	{
		_dropItems = dropItems;

		return this;
	}

	public CustomExplosion setPlayer(org.bukkit.entity.LivingEntity player, boolean damageExplosionOwner)
	{
		_owner = player;
		_damageOwner = damageExplosionOwner;

		return this;
	}

	@Override
	public void a()
	{
		if (Math.max(_blockExplosionSize, this._size) < 0.1F)
		{
			return;
		}

		HashSet hashset = new HashSet();

		for (int k = 0; k < 16; k++)
		{
			for (int i = 0; i < 16; i++)
			{
				for (int j = 0; j < 16; j++)
				{
					if ((k == 0) || (k == 15) || (i == 0) || (i == 15) || (j == 0) || (j == 15))
					{
						double d0 = k / 15.0F * 2.0F - 1.0F;
						double d1 = i / 15.0F * 2.0F - 1.0F;
						double d2 = j / 15.0F * 2.0F - 1.0F;
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

						d0 /= d3;
						d1 /= d3;
						d2 /= d3;
						float f1 = this._blockExplosionSize * (0.7F + this._world.random.nextFloat() * 0.6F);
						double d4 = this.posX;
						double d5 = this.posY;
						double d6 = this.posZ;

						for (; f1 > 0.0F; f1 -= 0.225F)
						{
							BlockPosition blockposition = new BlockPosition(d4, d5, d6);
							IBlockData iblockdata = this._world.getType(blockposition);

							if (iblockdata.getBlock().getMaterial() != Material.AIR)
							{
								float f2 = this.source != null ? this.source.a(this, this._world, blockposition, iblockdata)
										: (_damageBlocksEqually ? Blocks.DIRT : iblockdata.getBlock()).a((Entity) null);

								f1 -= (f2 + 0.3F) * 0.3F;
							}

							if ((f1 > 0.0F)
									&& ((this.source == null) || (this.source.a(this, this._world, blockposition, iblockdata, f1)))
									&& (blockposition.getY() < 256) && (blockposition.getY() >= 0))
							{
								hashset.add(blockposition);
							}

							d4 += d0 * 0.300000011920929D;
							d5 += d1 * 0.300000011920929D;
							d6 += d2 * 0.300000011920929D;
						}
					}
				}
			}
		}

		this.getBlocks().addAll(hashset);

		float f3 = _size * 2F;

		int i = MathHelper.floor(this.posX - f3 - 1.0D);
		int j = MathHelper.floor(this.posX + f3 + 1.0D);
		int k = MathHelper.floor(this.posY - f3 - 1.0D);
		int k1 = MathHelper.floor(this.posY + f3 + 1.0D);
		int l1 = MathHelper.floor(this.posZ - f3 - 1.0D);
		int i2 = MathHelper.floor(this.posZ + f3 + 1.0D);
		List list = this._world.getEntities(this.source, new AxisAlignedBB(i, k, l1, j, k1, i2));
		Vec3D vec3d = new Vec3D(this.posX, this.posY, this.posZ);

		for (int j2 = 0; j2 < list.size(); j2++)
		{
			Entity entity = (Entity) list.get(j2);

			if (entity.getBukkitEntity() == _owner && !_damageOwner)
				continue;

			if (!(entity.getBukkitEntity() instanceof LivingEntity) && _ignoreNonLiving)
				continue;

			double d7 = entity.f(this.posX, this.posY, this.posZ) / this._size; // XXX
			if (d7 <= 1.0D)
			{
				double d0 = entity.locX - this.posX;
				double d1 = entity.locY + entity.getHeadHeight() - this.posY;
				double d2 = entity.locZ - this.posZ;
				double d8 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

				if (d8 != 0.0D)
				{
					d0 /= d8;
					d1 /= d8;
					d2 /= d8;

					// Performs a raytrace that determines the percentage of solid blocks between the two
					double d9 = this._world.a(vec3d, entity.getBoundingBox()); // XXX
					double d10 = (1.0D - d7) * d9;
					float damage;

					if (_useCustomDamage)
					{
						damage = Math.max(0, (int) ((_damage * d9) * (d8 / _size)));
					}
					else
					{
						damage = (int) ((d10 * d10 + d10) / 2.0D * 8.0D * this._size + 1.0D);
						damage = Math.min(damage, _maxDamage);
					}

					if (entity.getBukkitEntity() instanceof LivingEntity)
					{
						_manager.NewDamageEvent((LivingEntity) entity.getBukkitEntity(), _owner, null, new Location(_world.getWorld(), posX, posY, posZ),
								DamageCause.ENTITY_EXPLOSION, damage, true, _ignoreRate, false, _damageReason, _damageReason, false);
					}
					else
					{
						CraftEventFactory.entityDamage = this.source;
						entity.damageEntity(DamageSource.explosion(this), damage);
						CraftEventFactory.entityDamage = null;
					}

					double d11 = EnchantmentProtection.a(entity, d10); // XXX

					/*entity.motX += d0 * d11;
					entity.motY += d1 * d11;
					entity.motZ += d2 * d11;*/

					if (((entity instanceof EntityHuman)) && (!((EntityHuman) entity).abilities.isInvulnerable))
					{
						this.b().put((EntityHuman) entity, new Vec3D(d0 * d10, d1 * d10, d2 * d10));
					}
				}
			}
		}
	}

	@Override
	public void a(boolean flag)
	{
		this._world.makeSound(this.posX, this.posY, this.posZ, "random.explode", 4.0F,
				(1.0F + (this._world.random.nextFloat() - this._world.random.nextFloat()) * 0.2F) * 0.7F);
	    if ((this._blockExplosionSize >= 2.0F) && (this._damageBlocks))
	        this._world.addParticle(EnumParticle.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D, new int[0]);
	      else {
	        this._world.addParticle(EnumParticle.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D, new int[0]);
		}

		if (_damageBlocks)
		{
			org.bukkit.World bworld = this._world.getWorld();

			List blockList = new ArrayList();

			for (int i1 = this.getBlocks().size() - 1; i1 >= 0; i1--)
			{
				BlockPosition cpos = this.getBlocks().get(i1);

				org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());

				if (bblock.getType() != org.bukkit.Material.AIR)
				{
					blockList.add(bblock);
				}
			}

			ExplosionEvent event = _owner == null || !(_owner instanceof Player) ? new ExplosionEvent(blockList) : new ExplosionEvent(blockList, (Player) _owner);
			this._world.getServer().getPluginManager().callEvent(event);

			this.getBlocks().clear();

			for (org.bukkit.block.Block bblock : event.GetBlocks())
			{
				BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
				this.getBlocks().add(coords);
			}

			if (event.GetBlocks().isEmpty())
			{
				this.wasCanceled = true;
				return;
			}

			if (_fallingBlockExplosion)
			{
				List<org.bukkit.block.Block> blocks = new ArrayList<>(event.GetBlocks());

				if (blocks.size() > _maxFallingBlocks && _maxFallingBlocks >= 0)
				{
					Collections.shuffle((ArrayList) blocks);

					int toRemove = blocks.size() - _maxFallingBlocks;

					for (int i = 0; i < toRemove; i++)
					{
						blocks.remove(0);
					}
				}

				_explosion.BlockExplosion(blocks, new Location(_world.getWorld(), posX, posY, posZ), false, false);
			}

			Iterator iterator = this.getBlocks().iterator();

			while (iterator.hasNext())
			{
				BlockPosition blockposition = (BlockPosition) iterator.next();
				Block block = this._world.getType(blockposition).getBlock();

				this._world.spigotConfig.antiXrayInstance.updateNearbyBlocks(this._world, blockposition);
				if (flag)
				{
					double d0 = blockposition.getX() + this._world.random.nextFloat();
					double d1 = blockposition.getY() + this._world.random.nextFloat();
					double d2 = blockposition.getZ() + this._world.random.nextFloat();
					double d3 = d0 - this.posX;
					double d4 = d1 - this.posY;
					double d5 = d2 - this.posZ;
					double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);

					d3 /= d6;
					d4 /= d6;
					d5 /= d6;
					double d7 = 0.5D / (d6 / this._blockExplosionSize + 0.1D);

					d7 *= (this._world.random.nextFloat() * this._world.random.nextFloat() + 0.3F);
					d3 *= d7;
					d4 *= d7;
					d5 *= d7;
					this._world.addParticle(EnumParticle.EXPLOSION_NORMAL, (d0 + this.posX * 1.0D) / 2.0D,
							(d1 + this.posY * 1.0D) / 2.0D, (d2 + this.posZ * 1.0D) / 2.0D, d3, d4, d5, new int[0]);
					this._world.addParticle(EnumParticle.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
				}

				if (block.getMaterial() != Material.AIR)
				{
					if (block.a(this) && _dropItems)
					{
						block.dropNaturally(this._world, blockposition, this._world.getType(blockposition), _blockExplosionSize,
								0);
					}

					this._world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
					block.wasExploded(this._world, blockposition, this);
				}
			}
		}

		if (this._createFire)
		{
			Iterator iterator = this.getBlocks().iterator();

			while (iterator.hasNext())
			{
				BlockPosition blockposition = (BlockPosition) iterator.next();
				if ((this._world.getType(blockposition).getBlock().getMaterial() == Material.AIR)
						&& (this._world.getType(blockposition.down()).getBlock().o()) && (UtilMath.r(3) == 0))
				{
					if (!CraftEventFactory.callBlockIgniteEvent(this._world, blockposition.getX(), blockposition.getY(),
							blockposition.getZ(), this).isCancelled())
						this._world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
				}
			}
		}

		PacketPlayOutExplosion explosion = new PacketPlayOutExplosion(this.posX, this.posY, this.posZ, this._blockExplosionSize,
				new ArrayList(), null);
		for (Player p : Bukkit.getOnlinePlayers())
			UtilPlayer.sendPacket(p, explosion);
	}

	public float getSize()
	{
		return _size;
	}

}
