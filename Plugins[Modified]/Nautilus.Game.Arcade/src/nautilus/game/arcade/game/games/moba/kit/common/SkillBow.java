package nautilus.game.arcade.game.games.moba.kit.common;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.shop.MobaItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SkillBow extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Please work"
	};

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.BOW)
			.setTitle(C.cGreenB + "Bow")
			.setUnbreakable(true)
			.build();

	public SkillBow(int slot)
	{
		super("Bow", DESCRIPTION, SKILL_ITEM, slot, null);
	}

	@Override
	public void giveItem(Player player)
	{
		Moba host = (Moba) Manager.GetGame();
		List<MobaItem> ownedItems = host.getShop().getOwnedItems(player);

		for (MobaItem item : ownedItems)
		{
			if (item.getItem().getType() == Material.BOW)
			{
				player.getInventory().setItem(getSlot(), item.getItem());
				return;
			}
		}

		player.getInventory().setItem(getSlot(), SKILL_ITEM);
	}
}
