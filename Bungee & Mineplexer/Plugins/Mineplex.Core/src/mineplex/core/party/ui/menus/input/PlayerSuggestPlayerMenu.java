package mineplex.core.party.ui.menus.input;

import mineplex.core.anvilMenu.player.PlayerNameMenu;
import mineplex.core.common.jsonchat.ChildJsonMessage;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.party.Lang;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * The Anvil Menu for handling suggestions
 */
public class PlayerSuggestPlayerMenu extends PlayerNameMenu
{
	private Party _party;

	public PlayerSuggestPlayerMenu(PartyManager partyManager, Player player, Party party)
	{
		super(partyManager, partyManager.getClientManager(), player);
		this._party = party;
	}

	@Override
	public void onSuccess(String name)
	{
		if(_party == null || _party.getOwnerName() == null)
		{
			Lang.NO_PARTY.send(_player);
			return;
		}

		Player target = Bukkit.getPlayer(name);

		if (target != null)
		{
			if (_party.isMember(target))
			{
				Lang.ALREADY_MEMBER.send(_player, name);
				return;
			}
		}

		Player player = Bukkit.getPlayerExact(_party.getOwnerName());

		_party.sendMessage(C.mHead + "Party> " +  F.name(_player.getName()) + " has suggested " + F.name(name) + " be invited.");

		ChildJsonMessage message = new ChildJsonMessage("").extra(F.main("Party", "Click "));
		message.add(F.link("Invite " + name))
		  .hover(HoverEvent.SHOW_TEXT, C.cGreen + "Clicking this will invite " + C.cYellow + name +  C.cGreen + " to the party")
		  .click(ClickEvent.RUN_COMMAND, "/party gui invite " + name);
		message.add(C.mBody + " to invite them");
		message.sendToPlayer(player);

		_player.closeInventory();
		_player.chat("/party");
	}

	@Override
	public void onFail(String name)
	{

	}
}
