package mineplex.core.shop.confirmation;

import org.bukkit.inventory.Inventory;

/**
 * @author Shaun Bennett
 */
public interface ConfirmationProcessor
{
	public void init(Inventory inventory);
	public void process(ConfirmationCallback callback);
}
