package mineplex.minecraft.game.classcombat.item.weapon;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class BoosterSword extends Item
{
	public BoosterSword(ItemFactory factory, int gemCost, int tokenCost)
	{
		super(factory, "Booster Sword", new String[] { "Increases Sword Skill level by 2." }, Material.GOLD_SWORD, 1, true, gemCost, tokenCost);
	}
}