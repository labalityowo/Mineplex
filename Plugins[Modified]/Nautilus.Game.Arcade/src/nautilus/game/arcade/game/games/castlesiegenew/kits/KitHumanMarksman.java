package nautilus.game.arcade.game.games.castlesiegenew.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.perks.skeleton.PerkBarrage;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitHumanMarksman extends KitCastleSiege
{


	private static final Perk[] PERKS =
			{
					new PerkBarrage(5, 250, true, false),
					new PerkFletcher(2, 4, false),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.STONE_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.BOW),
					ItemStackFactory.Instance.CreateStack(Material.ARROW, 32),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP)
			};

	private static final ItemStack[] ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET)
			};

	public KitHumanMarksman(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_HUMAN_MARKSMAN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(ARMOR);
		player.getInventory().addItem(PLAYER_ITEMS[3], PLAYER_ITEMS[3]);
	}
}
