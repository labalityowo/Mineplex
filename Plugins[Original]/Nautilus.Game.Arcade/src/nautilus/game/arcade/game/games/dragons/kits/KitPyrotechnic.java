package nautilus.game.arcade.game.games.dragons.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkSparkler;

public class KitPyrotechnic extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkSparkler(25, 2),
			};


	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.GOLD_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_HELMET),
			};

	public KitPyrotechnic(ArcadeManager manager)
	{
		super(manager, GameKit.DRAGONS_PYROTECHNIC, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
