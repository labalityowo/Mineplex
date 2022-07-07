package mineplex.minecraft.game.classcombat.item.weapon;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class PowerSword extends Item
{
	public PowerSword(ItemFactory factory, int gemCost, int tokenCost)
	{
		super(factory, "Power Sword", new String[] { "Increases sword damage by 1." }, Material.DIAMOND_SWORD, 1, true, gemCost, tokenCost);
	}
}
