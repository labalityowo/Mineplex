package nautilus.game.arcade.game.games.castlesiegenew.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkConstructor;
import nautilus.game.arcade.kit.perks.PerkMammoth;

public class KitHumanKnight extends KitCastleSiege
{

	private static final Perk[] PERKS =
			{
					new PerkMammoth(),
					new PerkConstructor("Constructor", 40, 2, Material.FENCE, "Castle Barricade", true)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.BOW),
					ItemStackFactory.Instance.CreateStack(Material.ARROW, 64),
					ItemStackFactory.Instance.CreateStack(Material.FENCE, (byte) 0, 2, F.item("Castle Barricade")),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP)
			};

	private static final ItemStack[] ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.IRON_HELMET)
			};

	public KitHumanKnight(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_HUMAN_KNIGHT, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(ARMOR);
	}
}
