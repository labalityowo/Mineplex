package nautilus.game.arcade.game.games.dragons.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.perks.skeleton.PerkBarrage;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitMarksman extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(4, 4, true),
					new PerkBarrage(6, 200, true, false),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.BOW),
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
			};

	public KitMarksman(ArcadeManager manager)
	{
		super(manager, GameKit.DRAGONS_MARKSMAN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
