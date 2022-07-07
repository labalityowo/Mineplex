package mineplex.core.bonuses.gui.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.common.util.C;
import mineplex.core.gui.GuiItem;
import mineplex.core.itemstack.ItemBuilder;

public class LevelRewardsButton implements GuiItem
{

	private final LevelingManager _manager;
	private final Player _player;
	private final ItemStack _itemStack;

	public LevelRewardsButton(LevelingManager manager, Player player)
	{
		_manager = manager;
		_player = player;
		long levelsToClaim = manager.getUnclaimedLevels(player);
		boolean anyToClaim = levelsToClaim > 0;
		_itemStack = new ItemBuilder(anyToClaim ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
				.setTitle((anyToClaim ? C.cGreenB : C.cRedB) + "Level Rewards")
				.addLore("", "Earn rewards every time you", "level up your Mineplex Level!", "Unclaimed Rewards: " + C.cYellow + levelsToClaim, "", C.cGreen + "Click here to view rewards")
				.setGlow(anyToClaim)
				.build();
	}

	@Override
	public void setup()
	{

	}

	@Override
	public void close()
	{

	}

	@Override
	public void click(ClickType clickType)
	{
		_manager.getShop().attemptShopOpen(_player);
	}

	@Override
	public ItemStack getObject()
	{
		return _itemStack;
	}
}
