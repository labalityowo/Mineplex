package mineplex.core.party.ui.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.menu.Button;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.PartyMenu;
import mineplex.core.party.ui.button.PartyMemberIcon;
import mineplex.core.party.ui.button.tools.LeavePartyButton;
import mineplex.core.party.ui.button.tools.view.SuggestPlayerButton;
import mineplex.core.utils.UtilGameProfile;

/**
 * The menu a player see's when he is a member, and not an owner, of a party.
 */
public class PartyViewMenu extends PartyMenu
{

	private final int INV_SIZE = 54;
	private final int OWNER_HEAD_SLOT = 13;
	private final int STARTING_SLOT = 20;
	private final int CUT_OFF_SLOT = 25;
	private final int CUT_OFF_SLOT_2 = 34;
	private final int SKIP_TO_SLOT = 29;
	private final int SKIP_TO_SLOT_2 = 38;
	private final int LEAVE_PARTY_BUTTON_SLOT = 3;
	private final int SUGGEST_PLAYER_BUTTON_SLOT = 5;

	private Party _party;

	public PartyViewMenu(Party party, PartyManager plugin)
	{
		super(party.getOwnerName() + "'s Party", plugin);
		_party = party;
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[INV_SIZE];
		//Tools
		buttons[LEAVE_PARTY_BUTTON_SLOT] = new LeavePartyButton(getPlugin());
		//Suggest Player
		buttons[SUGGEST_PLAYER_BUTTON_SLOT] = new SuggestPlayerButton(_party, getPlugin());

		List<Player> members = new ArrayList<>(_party.getMembers());
		_party.getOwnerAsPlayer().ifPresent(members::remove);
		buttons[OWNER_HEAD_SLOT] = new PartyMemberIcon(_party.getOwner(), _party, true, false);

		int slot = STARTING_SLOT;
		//Players
		for (Player member : members)
		{
			if (slot == CUT_OFF_SLOT)
			{
				slot = SKIP_TO_SLOT;
			}
			else if (slot == CUT_OFF_SLOT_2)
			{
				slot = SKIP_TO_SLOT_2;
			}

			buttons[slot++] = new PartyMemberIcon(UtilGameProfile.getGameProfile(member), _party, false, false);
		}

		return pane(buttons);
	}
}