package mineplex.core.newnpc;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntitySlime;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;

public class SimpleNPC implements NPC
{

	private static final HologramManager HOLOGRAM_MANAGER = Managers.require(HologramManager.class);

	public static SimpleNPC of(Location location, Class<? extends LivingEntity> classOfEntity, String metadata)
	{
		return of(location, classOfEntity, metadata, 0);
	}

	public static SimpleNPC of(Location location, Class<? extends LivingEntity> classOfEntity, String metadata, int variant)
	{
		Entity entity = ((CraftWorld) location.getWorld()).createEntity(location, classOfEntity);

		if (entity instanceof EntitySkeleton)
		{
			((EntitySkeleton) entity).setSkeletonType(variant);
		}
		else if (entity instanceof EntityHorse)
		{
			((EntityHorse) entity).setType(variant);
		}
		else if (entity instanceof EntitySlime)
		{
			((EntitySlime) entity).setSize(variant);
		}

		return new SimpleNPC(metadata, (LivingEntity) entity.getBukkitEntity());
	}

	final String _metadata;

	protected LivingEntity _entity;
	private Hologram _nameTag;

	SimpleNPC(String metadata)
	{
		this(metadata, null);
	}

	private SimpleNPC(String metadata, LivingEntity entity)
	{
		_metadata = metadata;
		_entity = entity;
	}

	@Override
	public LivingEntity spawnEntity()
	{
		_entity.getLocation().getChunk().load(false);
		LivingEntity entity = ((CraftWorld) _entity.getWorld()).addEntity(((CraftEntity) _entity).getHandle(), SpawnReason.CUSTOM);

		entity.setCanPickupItems(false);
		entity.setRemoveWhenFarAway(false);

		UtilEnt.vegetate(entity, true);
		UtilEnt.ghost(entity, true, false);
		UtilEnt.setFakeHead(entity, true);
		UtilEnt.addFlag(entity, UtilEnt.FLAG_ENTITY_COMPONENT);

		return entity;
	}

	@Override
	public LivingEntity getEntity()
	{
		return _entity;
	}

	@Override
	public Hologram getNameTag()
	{
		if (!hasNameTag())
		{
			_nameTag = new Hologram(HOLOGRAM_MANAGER, _entity.getLocation().add(0, UtilEnt.getHeight(_entity), 0), true, _entity.getCustomName())
					.start();

			_entity.setCustomNameVisible(false);
			_entity.setCustomName(null);
		}

		return _nameTag;
	}

	@Override
	public boolean hasNameTag()
	{
		return _nameTag != null;
	}

	@Override
	public String getMetadata()
	{
		return _metadata;
	}
}
