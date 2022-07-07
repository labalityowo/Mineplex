package mineplex.game.clans.gameplay;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.Recipe;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class CustomRecipes implements Listener
{
	private static final Material[] DISABLED_RECIPES = { Material.EXPLOSIVE_MINECART, Material.MINECART, Material.JUKEBOX, Material.FISHING_ROD, Material.BED, Material.BOAT, Material.HOPPER, Material.HOPPER_MINECART };
	
	@EventHandler
	public void onPlayerCraftItem(CraftItemEvent event)
	{
		if (isDisabledRecipe(event.getRecipe()))
		{
			event.setCancelled(true);
			notify(event.getWhoClicked(), "Crafting this item is disabled!");
		}
	}
	
	private boolean isDisabledRecipe(Recipe recipe)
	{
		Material itemType = recipe.getResult().getType();
		
		for (Material disabledRecipe : DISABLED_RECIPES)
		{
			if (disabledRecipe == itemType)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static void notify(HumanEntity player, String message)
	{
		UtilPlayer.message(player, F.main("Recipes", message));
	}
}