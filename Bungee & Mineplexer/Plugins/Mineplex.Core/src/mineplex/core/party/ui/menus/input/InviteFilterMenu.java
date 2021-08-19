package mineplex.core.party.ui.menus.input;

import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.PartyInvitesMenu;
import mineplex.core.anvilMenu.PlayerInputActionMenu;
import org.bukkit.entity.Player;

/**
 * The anvil menu for filtering the players invite menu
 */
public class InviteFilterMenu extends PlayerInputActionMenu
{

	public InviteFilterMenu(PartyManager partyManager, Player player, Party party)
	{
		super(partyManager, player);
	}

	@Override
	public void inputReceived(String name)
	{
		_player.closeInventory();
		if(name.equalsIgnoreCase("Clear Filter") || name.equalsIgnoreCase(" "))
		{
			name = null;
		}
		new PartyInvitesMenu((PartyManager) _plugin, name).open(_player);
	}
}
