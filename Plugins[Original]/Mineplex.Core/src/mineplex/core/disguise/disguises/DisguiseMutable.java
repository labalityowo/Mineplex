package mineplex.core.disguise.disguises;

import java.util.function.Predicate;

import com.mineplex.ProtocolVersion;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * Represents a disguise that can "mutate" from one entity type to another.
 */
public abstract class DisguiseMutable extends DisguiseCreature
{
	public DisguiseMutable(EntityType disguiseType, Entity entity)
	{
		super(disguiseType, entity);
	}

	protected void mutate()
	{
//		if (!_spawnedIn)
//			return;

		Predicate<Integer> pred = v -> v >= ProtocolVersion.v1_11;
		sendToWatchers(pred, this::getDestroyPacket);
		sendToWatchers(pred, this::getSpawnPacket);
		sendToWatchers(pred, this::getMetadataPacket);
	}

	private Packet getDestroyPacket()
	{
		return new PacketPlayOutEntityDestroy(new int[] { getEntityId() });
	}

	protected abstract int getTypeId(boolean separate);
}
