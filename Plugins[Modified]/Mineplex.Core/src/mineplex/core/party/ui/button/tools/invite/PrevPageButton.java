package mineplex.core.party.ui.button.tools.invite;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.IconButton;
import mineplex.core.party.InviteData;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.PartyInvitesMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Shows the previous page of invites for a player
 */
public class PrevPageButton extends Button<PartyManager>
{

	private static final ItemStack ITEM = new ItemBuilder(Material.SIGN).setTitle(C.cYellow + "Previous Page").build();

	private final PartyInvitesMenu _menu;

	public PrevPageButton(PartyInvitesMenu menu, PartyManager plugin)
	{
		super(ITEM, plugin);
		_menu = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_menu.setCurrentPage(_menu.getCurrentPage() - 1);
		if(_menu.getCurrentPage() == 0)
		{
			_menu.setButton(45, new IconButton(new ItemStack(Material.AIR)));
		}
		List<InviteData> data = _menu.getDataForPage(_menu.getCurrentPage());
		for(int i = 0; i < data.size(); i++)
		{
			_menu.setButton(i + _menu.getStartingSlot(), new InviteButton(data.get(i).getInviterName(), getPlugin()));
		}
		_menu.update();
	}
}