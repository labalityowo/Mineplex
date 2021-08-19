package mineplex.minecraft.game.classcombat.item.weapon;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class StandardAxe extends Item
{
	public StandardAxe(ItemFactory factory, int gemCost, int tokenCost)
	{
		super(factory, "Standard Axe", new String[] { "Pretty standard." }, Material.IRON_AXE, 1, true, gemCost, tokenCost);
		
		setFree(true);
	}
}
