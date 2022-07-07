package nautilus.game.arcade.game.games.zombiesurvival.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkLeap;
import nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitSurvivorRogue extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1, 1, 8000),
					new PerkSpeed(0)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE),
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.LEATHER_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.LEATHER_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.LEATHER_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.LEATHER_HELMET),
			};

	public KitSurvivorRogue(ArcadeManager manager)
	{
		super(manager, GameKit.ZOMBIE_SURVIVAL_ROUGE, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
