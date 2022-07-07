package nautilus.game.arcade.game.games.minestrike.items.equipment.armor;

import org.bukkit.Material;


public class Kevlar extends Armor
{
	public Kevlar()
	{
		super("Kevlar",  new String[] 
				{
				"Reduces damage from body shots"
				},
				650, 0, Material.LEATHER_CHESTPLATE); 
	}
}
