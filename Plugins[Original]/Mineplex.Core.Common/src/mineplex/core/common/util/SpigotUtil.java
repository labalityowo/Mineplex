package mineplex.core.common.util;

import net.minecraft.server.v1_8_R3.EntityTameableAnimal;

import org.bukkit.craftbukkit.libs.com.google.common.base.Optional;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTameableAnimal;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Tameable;

public class SpigotUtil
{
    // Explanation:
    // - Tameable animals (wolves, ocelots) keep track of their most
    //   recent owner.
    // - When an animal is assigned a new owner, its data watcher is
    //   updated with the new owner's UUID
    // - During this process, the old owner's UUID is checked against
    //   the new one
    // - If the animal didn't have a previous owner, the old owner's
    //   UUID is the empty string.
    // - UUID.fromString() is called on the empty string, and throws
    //   an exception.
    //
    // We can mitigate this issue by manually setting a previous owner
    // UUID before we call Tameable#setOwner(AnimalTamer)
    //
    // (note: this does not apply to horses)
    public static void setOldOwner_RemoveMeWhenSpigotFixesThis(Tameable tameable, AnimalTamer tamer)
    {
        ((CraftTameableAnimal)tameable).getHandle().getDataWatcher().watch(17, tamer.getUniqueId().toString(), EntityTameableAnimal.META_OWNER, Optional.absent());
    }
}
