package nautilus.game.arcade.game.games.minestrike.items.equipment;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItem;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItemType;

public class DefusalKit extends StrikeItem
{
	public DefusalKit()
	{
		super(StrikeItemType.EQUIPMENT, "Defusal Kit",  new String[] 
				{
				"Halves the time it takes to defuse."
				},
				400, 0, Material.SHEARS);
	}

	@Override
	public boolean pickup(GunModule game, Player player)
	{
		return false;
	}
	
	public void giveToPlayer(Player player, int slot)
	{
		fixStackName();
		
		player.getInventory().setItem(slot, getStack());
		
		UtilPlayer.message(player, F.main("Game", "You equipped " + getName() + "."));
		
		player.getWorld().playSound(player.getLocation(), Sound.HORSE_ARMOR, 1.5f, 1f);
	}
	
	@Override
	public String getShopItemType()
	{
		return C.cDGreen + C.Bold + "Equipment" + ChatColor.RESET;
	}
}
