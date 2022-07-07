package mineplex.minecraft.game.classcombat.item.weapon;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class BoosterAxe extends Item
{
	public BoosterAxe(ItemFactory factory, int gemCost, int tokenCost)
	{
		super(factory, "Booster Axe", new String [] { "Increases Axe Skill level by 2." }, Material.GOLD_AXE, 1, true, gemCost, tokenCost);
	}
}