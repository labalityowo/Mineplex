package nautilus.game.arcade.game.games.zombiesurvival.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkIronSkin;
import nautilus.game.arcade.kit.perks.PerkMammoth;

public class KitSurvivorKnight extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkIronSkin(1),
					new PerkMammoth()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD),
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.IRON_HELMET),
			};

	public KitSurvivorKnight(ArcadeManager manager)
	{
		super(manager, GameKit.ZOMBIE_SURVIVAL_KNIGHT, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
