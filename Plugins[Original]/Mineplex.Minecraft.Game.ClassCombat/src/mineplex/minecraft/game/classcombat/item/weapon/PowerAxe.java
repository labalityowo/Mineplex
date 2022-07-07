package mineplex.minecraft.game.classcombat.item.weapon;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class PowerAxe extends Item
{
	public PowerAxe(ItemFactory factory, int gemCost, int tokenCost)
	{
		super(factory, "Power Axe", new String [] { "Increases Axe damage by 1." }, Material.DIAMOND_AXE, 1, true, gemCost, tokenCost);
	}
}
