package mineplex.core.disguise.disguises;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mineplex.MetadataRewriter;
import com.mineplex.ProtocolVersion;

import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.DataWatcher.WatchableObject;

import org.bukkit.entity.EntityType;

public abstract class DisguiseCreature extends DisguiseInsentient
{
	public DisguiseCreature(EntityType disguiseType, org.bukkit.entity.Entity entity)
	{
		super(disguiseType, entity);
	}

	@SuppressWarnings("deprecation")
	public Packet getSpawnPacket()
	{
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
		packet.a = getEntity().getId();
		packet.b = (byte) getDisguiseType().getTypeId();
		packet.c = MathHelper.floor(getEntity().locX * 32.0D);
		packet.d = MathHelper.floor(getEntity().locY * 32.0D);
		packet.e = MathHelper.floor(getEntity().locZ * 32.0D);
		packet.i = (byte) ((int) (getEntity().yaw * 256.0F / 360.0F));
		packet.j = (byte) ((int) (getEntity().pitch * 256.0F / 360.0F));
		packet.k = (byte) ((int) (getEntity().yaw * 256.0F / 360.0F));
		packet.uuid = getEntity().getUniqueID();

        double var2 = 3.9D;
        double var4 = 0;
        double var6 = 0;
        double var8 = 0;

        if (var4 < -var2)
        {
            var4 = -var2;
        }

        if (var6 < -var2)
        {
            var6 = -var2;
        }

        if (var8 < -var2)
        {
            var8 = -var2;
        }

        if (var4 > var2)
        {
            var4 = var2;
        }

        if (var6 > var2)
        {
            var6 = var2;
        }

        if (var8 > var2)
        {
            var8 = var2;
        }

        packet.f = (int)(var4 * 8000.0D);
        packet.g = (int)(var6 * 8000.0D);
        packet.h = (int)(var8 * 8000.0D);
		
        packet.l = DataWatcher;
		packet.m = DataWatcher.b();
		
		return packet;
	}

	// ---- Metadata processing

	// This WON'T be post-processed by the Spigot metadata processor

	@Override
	public Packet modifySpawnPacket(int protocol, Packet packet)
	{
		if (protocol >= ProtocolVersion.v1_10_PRE)
		{
			PacketPlayOutSpawnEntityLiving newSpawn = (PacketPlayOutSpawnEntityLiving) getSpawnPacket();

			// Allow the entity type to be changed (needed on 1.11+)
			newSpawn.b = getTypeId(protocol >= ProtocolVersion.v1_11);

			boolean hasArms = false;
			List<WatchableObject> meta = DataWatcher.b();

			if (meta != null)
			{
				// Run the meta through our Spigot rewriter
				meta = MetadataRewriter.rewrite(getTypeId(false), protocol, meta).objects;

				// Remove indexes >= 12 on 1.11+
				if (protocol >= ProtocolVersion.v1_11)
				{
					Iterator<WatchableObject> iter = meta.iterator();
					while (iter.hasNext())
					{
						WatchableObject next = iter.next();
						if (next.getIndex().a() == 6)
						{
							hasArms = true;
						} else if (next.getIndex().a() >= 12)
						{
							iter.remove();
						}
					}
				}
			} else
			{
				meta = new ArrayList<>();
			}

			if (!hasArms)
			{
				WatchableObject<Byte> arms = new WatchableObject<>(0, 0, null,
						new DataIndex<>(6, DataType.BYTE), (byte) 0);
				meta.add(arms);
			}

			newSpawn.m = meta;
			return newSpawn;
		}

		return packet;
	}

	protected int getTypeId(boolean separate)
	{
		return getDisguiseType().getTypeId();
	}

	// This WILL be post-processed by Spigot's metadata processor

	@Override
	public Packet modifyMetaPacket(int protocol, Packet packet)
	{
		if (protocol >= ProtocolVersion.v1_10_PRE)
		{
			PacketPlayOutEntityMetadata newMeta = new PacketPlayOutEntityMetadata();
			newMeta.a = getEntityId();

			List<WatchableObject> meta = MetadataRewriter.rewrite(getTypeId(false), protocol, DataWatcher.c()).objects;

			for (int i = 0; i < meta.size(); i++)
			{
				WatchableObject object = meta.get(i);
				int index = object.getIndex().a();
				if (index >= 6)
				{
					index--;
					meta.set(i, new WatchableObject(0, 0, null,
							new DataIndex(index, object.getIndex().b()), object.getValue()));
				}
			}

			newMeta.b = meta;
			return newMeta;
		}

		return packet;
	}
}
