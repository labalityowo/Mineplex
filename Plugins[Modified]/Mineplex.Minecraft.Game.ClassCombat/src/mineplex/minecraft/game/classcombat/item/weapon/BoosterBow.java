package mineplex.minecraft.game.classcombat.item.weapon;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class BoosterBow extends Item
{
	public BoosterBow(ItemFactory factory, int gemCost, int tokenCost)
	{
		super(factory, "Booster Bow", new String [] { "Increases Bow Skill level by 1." }, Material.BOW, 1, true, gemCost, tokenCost);
	}
}