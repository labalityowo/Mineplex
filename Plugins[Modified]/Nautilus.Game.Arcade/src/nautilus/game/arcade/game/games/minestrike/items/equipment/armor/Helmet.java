package nautilus.game.arcade.game.games.minestrike.items.equipment.armor;

import org.bukkit.Material;


public class Helmet extends Armor
{
	public Helmet()
	{
		super("Helmet",  new String[] 
				{
				"Reduces damage from head shots"
				},
				350, 0, Material.LEATHER_HELMET);
	}
}
