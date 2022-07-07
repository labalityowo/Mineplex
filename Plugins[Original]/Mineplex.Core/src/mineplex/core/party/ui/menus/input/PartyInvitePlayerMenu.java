package mineplex.core.party.ui.menus.input;

import mineplex.core.anvilMenu.player.PlayerNameMenu;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.menus.PartyOwnerMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The anvil menu for inviting a player to a party
 */
public class PartyInvitePlayerMenu extends PlayerNameMenu
{

	private PartyManager _partyManager;
	private Party _party;

	public PartyInvitePlayerMenu(PartyManager partyManager, Player player, Party party)
	{
		super(partyManager, partyManager.getClientManager(), player);
		_partyManager = partyManager;
		this._party = party;
	}

	@Override
	public void onSuccess(String name)
	{
		_partyManager.invite(_player, name);
		_player.playSound(_player.getLocation(), Sound.NOTE_PLING, 1, 1.6f);
		_player.closeInventory();
		if (_party == null)
		{
			return;
		}
		new PartyOwnerMenu(_party, _partyManager).open(_player);
	}

	@Override
	public void onFail(String name)
	{

	}
}
