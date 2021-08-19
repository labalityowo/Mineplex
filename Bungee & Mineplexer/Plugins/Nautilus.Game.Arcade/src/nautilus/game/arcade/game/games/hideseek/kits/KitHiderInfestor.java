package nautilus.game.arcade.game.games.hideseek.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitHiderInfestor extends Kit
{


	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.MAGMA_CREAM, (byte) 0, 1, C.cYellow + C.Bold + "Click Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Infest Block/Animal")
			};

	public KitHiderInfestor(ArcadeManager manager)
	{
		super(manager, GameKit.HIDE_AND_SEEK_INFESTOR);
	}

	@Override
	public void GiveItems(Player player)
	{
		//Swap
		player.getInventory().setItem(3, PLAYER_ITEMS[0]);

		DisguiseSlime slime = new DisguiseSlime(player);
		slime.SetSize(2);
		Manager.GetDisguise().disguise(slime);
	}
}
