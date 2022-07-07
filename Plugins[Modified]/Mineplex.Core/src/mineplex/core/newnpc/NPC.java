package mineplex.core.newnpc;

import org.bukkit.entity.LivingEntity;

import mineplex.core.hologram.Hologram;

public interface NPC
{

	LivingEntity spawnEntity();

	LivingEntity getEntity();

	Hologram getNameTag();

	boolean hasNameTag();

	String getMetadata();

}
