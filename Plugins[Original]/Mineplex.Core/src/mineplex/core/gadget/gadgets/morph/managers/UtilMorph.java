package mineplex.core.gadget.gadgets.morph.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.gadget.GadgetManager;

public class UtilMorph
{
	/**
	 * UtilMorph helps disguising and undisguising players, avoiding the use of deprecated methods
	 */

	private static Map<UUID, DisguiseBase> _disguises = new HashMap<>();

	/**
	 * Disguises a player with custom name
	 * @param player The player that will be disguised
	 * @param disguiseBase The disguise that will be applied
	 * @param gadgetManager The gadget manager
	 */
	public static void disguise(Player player, DisguiseBase disguiseBase, GadgetManager gadgetManager)
	{
		_disguises.put(player.getUniqueId(), disguiseBase);
		if (disguiseBase instanceof DisguiseInsentient)
		{
			DisguiseInsentient disguiseInsentient = (DisguiseInsentient) disguiseBase;
			disguiseInsentient.setName(player.getName(), gadgetManager.getClientManager().Get(player).getRealOrDisguisedPrimaryGroup());
			disguiseInsentient.setCustomNameVisible(true);
			gadgetManager.getDisguiseManager().disguise(disguiseInsentient);
		}
		else
		{
			gadgetManager.getDisguiseManager().disguise(disguiseBase);
		}
	}

	/**
	 * Disguises a player with custom name (special case for the Wither Morph)
	 * @param player The player that will be disguised
	 * @param disguiseBase The disguise that will be applied
	 * @param gadgetManager The gadget manager
	 * @param wither
	 */
	public static void disguise(Player player, DisguiseBase disguiseBase, GadgetManager gadgetManager, boolean wither)
	{
		_disguises.put(player.getUniqueId(), disguiseBase);
		if (disguiseBase instanceof DisguiseInsentient)
		{
			DisguiseInsentient disguiseInsentient = (DisguiseInsentient) disguiseBase;
			disguiseInsentient.setName(player.getName(), gadgetManager.getClientManager().Get(player).getRealOrDisguisedPrimaryGroup());
			if (!wither)
			{
				disguiseInsentient.setCustomNameVisible(true);
			}
			gadgetManager.getDisguiseManager().disguise(disguiseInsentient);
		}
		else
		{
			gadgetManager.getDisguiseManager().disguise(disguiseBase);
		}
	}

	/**
	 * Disguises a player without a custom name
	 * @param player The player that will be disguised
	 * @param disguiseInsentient The disguise that will be applied
	 * @param disguiseManager The disguise manager
	 */
	public static void disguise(Player player, DisguiseInsentient disguiseInsentient, DisguiseManager disguiseManager)
	{
		_disguises.put(player.getUniqueId(), disguiseInsentient);
		disguiseManager.disguise(disguiseInsentient);
	}

	/**
	 * Removes the disguise of a player
	 * @param player The player that will be undisguised
	 * @param disguiseManager The disguise manager
	 */
	public static void undisguise(Player player, DisguiseManager disguiseManager)
	{
		if (_disguises.containsKey(player.getUniqueId()))
		{
			DisguiseBase disguiseBase = _disguises.get(player.getUniqueId());
			disguiseManager.undisguise(disguiseBase);
			_disguises.remove(player.getUniqueId());
		}
	}
}