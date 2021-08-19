package mineplex.core.party.ui.menus.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.anvilMenu.player.PlayerNameMenu;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.party.Lang;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;

/**
 *
 */
public class PartyTransferOwnerMenu extends PlayerNameMenu
{

	private PartyManager _partyManager;
	private Party _party;

	public PartyTransferOwnerMenu(PartyManager partyManager, Player player, Party party)
	{
		super(partyManager, partyManager.getClientManager(), player);
		_partyManager = partyManager;
		this._party = party;
	}


	@Override
	public void onSuccess(String name)
	{
		Player player = Bukkit.getPlayer(name);
		if (player == null)
		{
			UtilPlayer.message(_player, F.main("Party", "Could not find " + F.elem(name) + "!"));
			return;
		}
		if (!_party.isMember(player))
		{
			Lang.NOT_MEMBER.send(_player, name);
			return;
		}
		if (player == _player)
		{
			UtilPlayer.message(_player, F.main("Party", "You can't promote yourself!"));
			return;
		}

		_party.setOwner(player);
		Lang.TRANSFER_OWNER.send(_party, _player.getName(), name);
		_player.closeInventory();
		_player.chat("/party");
	}

	@Override
	public void onFail(String name)
	{

	}
}
