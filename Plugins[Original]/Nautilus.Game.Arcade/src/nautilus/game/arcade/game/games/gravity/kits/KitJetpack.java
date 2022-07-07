package nautilus.game.arcade.game.games.gravity.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitJetpack extends Kit
{

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1, "Space Suit")
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.GOLD_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.GLASS),
			};

	public KitJetpack(ArcadeManager manager)
	{
		super(manager, GameKit.GRAVITY_PLAYER);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
