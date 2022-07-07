package nautilus.game.arcade.game.games.moba.kit.common;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilItem;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.shop.MobaItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SkillSword extends HeroSkill
{

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.WOOD_SWORD)
			.setTitle(C.cGreenB + "Sword")
			.setUnbreakable(true)
			.build();

	public SkillSword(int slot)
	{
		super("Sword", new String[0], SKILL_ITEM, slot, null);
	}

	@Override
	public void giveItem(Player player)
	{
		Moba host = (Moba) Manager.GetGame();
		List<MobaItem> ownedItems = host.getShop().getOwnedItems(player);

		for (MobaItem item : ownedItems)
		{
			if (UtilItem.isSword(item.getItem()))
			{
				player.getInventory().setItem(getSlot(), item.getItem());
				return;
			}
		}

		player.getInventory().setItem(getSlot(), SKILL_ITEM);
	}
}
