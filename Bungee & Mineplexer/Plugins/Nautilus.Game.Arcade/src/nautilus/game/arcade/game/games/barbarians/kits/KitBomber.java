package nautilus.game.arcade.game.games.barbarians.kits;

import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBomber;
import nautilus.game.arcade.kit.perks.PerkLeap;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitBomber extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkBomber(8, 2, -1),
					new PerkLeap("Leap", 1, 1, 8000)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE),
					ItemStackFactory.Instance.CreateStack(Material.TNT, (byte) 0, 1, F.item("Throwing TNT")),
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

	public KitBomber(ArcadeManager manager)
	{
		super(manager, GameKit.BARBARIANS_BOMBER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(ARMOR);
	}
}
