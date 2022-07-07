package mineplex.minecraft.game.classcombat.item.weapon;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class StandardBow extends Item
{
	public StandardBow(ItemFactory factory, int gemCost, int tokenCost)
	{
		super(factory, "Standard Bow", new String[] { "Pretty standard." }, Material.BOW, 1, true, gemCost, tokenCost);
		
		setFree(true);
	}
}
