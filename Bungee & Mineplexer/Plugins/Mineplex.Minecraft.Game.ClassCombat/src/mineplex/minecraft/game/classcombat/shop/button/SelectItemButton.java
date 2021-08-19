package mineplex.minecraft.game.classcombat.shop.button;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;
import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.shop.page.SkillPage;

public class SelectItemButton implements IButton
{
	private SkillPage _page;
	private Item _item;
	private boolean _canAfford;
	
	public SelectItemButton(SkillPage page, Item item, boolean canAfford)
	{
		_page = page;
		_item = item;
		_canAfford = canAfford;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType.isLeftClick())
		{
			if (!_canAfford)
			{
				player.playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1f, 0.5f);
				return;
			}

			_page.SelectItem(player, _item);
		}
		else if (clickType.isRightClick())
		{
			_page.DeselectItem(player, _item);
		}
	}

}
