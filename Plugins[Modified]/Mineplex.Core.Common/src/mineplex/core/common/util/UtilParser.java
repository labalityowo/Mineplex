package mineplex.core.common.util;

import org.bukkit.event.entity.EntityDamageEvent;

public class UtilParser
{
	public static String parseDamageCause(EntityDamageEvent.DamageCause cause)
	{
		if (cause == null)
			return "Unknown";
		switch (cause)
		{
			case CONTACT:
				return "Cactus";
			case ENTITY_ATTACK:
				return "Attack";
			case PROJECTILE:
				return "Ranged Weapon";
			case SUFFOCATION:
				return "Suffocation";
			case FALL:
				return "Fall";
			case FIRE:
				return "Fire";
			case FIRE_TICK:
				return "Burning";
			case MELTING:
				return "Melting";
			case LAVA:
				return "Lava";
			case DROWNING:
				return "Drowning";
			case BLOCK_EXPLOSION:
				return "Explosion";
			case ENTITY_EXPLOSION:
				return "Explosion";
			case VOID:
				return "Void";
			case LIGHTNING:
				return "Lightning";
			case SUICIDE:
				return "Suicide";
			case STARVATION:
				return "Hunger";
			case POISON:
				return "Poison";
			case MAGIC:
				return "Thrown Potion";
			case WITHER:
				return "Wither Effect";
			case FALLING_BLOCK:
				return "Falling Block";
			case THORNS:
				return "Thorns Enchantment";
			case CUSTOM:
				return "Custom";
			default:
				return "The Mighty defek7";
		}
	}
}
