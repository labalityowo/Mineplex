package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.gadget.GadgetManager;

/**
 * Handles custom particle effects for the arcade hub kit selectors
 */
public abstract class KitSelectorGadget extends Gadget
{

	public KitSelectorGadget(GadgetManager gadgetManager, String name, String[] lore, int cost, Material mat, byte data,
							 String... alternativeSalesPackageNames)
	{
		super(gadgetManager, GadgetType.KIT_SELECTOR, name, lore, cost, mat, data, 1, alternativeSalesPackageNames);
	}

	/**
	 * Plays the next particle for the selected entity
	 * @param entity The entity of the selected kit
	 * @param playTo The player that will receive the particles
	 */
	public abstract void playParticle(Entity entity, Player playTo);

}
