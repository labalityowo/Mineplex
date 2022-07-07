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
import mineplex.core.party.ui.button.tools.owner.AddPlayerButton;
import mineplex.core.party.ui.button.tools.owner.DisbandPartyButton;
import mineplex.core.party.ui.button.tools.owner.SelectServerButton;
import mineplex.core.party.ui.button.tools.owner.TransferOwnerButton;
import mineplex.core.utils.UtilGameProfile;

/**
 * The display menu for managing parties by the owner
 */
public class PartyOwnerMenu extends PartyMenu
{

	private final int INV_SIZE = 54;
	private final int OWNER_HEAD_SLOT = 13;
	private final int STARTING_SLOT = 20;
	private final int CUT_OFF_SLOT = 25;
	private final int CUT_OFF_SLOT_2 = 34;
	private final int SKIP_TO_SLOT = 29;
	private final int SKIP_TO_SLOT_2 = 38;
	private final int ADD_PLAYER_BUTTON_SLOT = 1;
	private final int TRANSFER_OWNER_BUTTON_SLOT = 7;
	private final int SELECT_SERVER_BUTTON_SLOT = 46;
	private final int LEAVE_PARTY_BUTTON_SLOT = 49;
	private final int DISBAND_PARTY_BUTTON_SLOW = 52;

	private Party _party;

	public PartyOwnerMenu(Party party, PartyManager plugin)
	{
		super("Manage Party", plugin);
		_party = party;
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[INV_SIZE];
		//Tools
		buttons[ADD_PLAYER_BUTTON_SLOT] = new AddPlayerButton(getPlugin(), _party);
		//Transfer ownership
		buttons[TRANSFER_OWNER_BUTTON_SLOT] = new TransferOwnerButton(_party, getPlugin());
		//Go to server
		buttons[SELECT_SERVER_BUTTON_SLOT] = new SelectServerButton(_party);
		//Leave party
		buttons[LEAVE_PARTY_BUTTON_SLOT] = new LeavePartyButton(getPlugin());
		//Disband
		buttons[DISBAND_PARTY_BUTTON_SLOW] = new DisbandPartyButton(getPlugin());

		List<Player> members = new ArrayList<>(_party.getMembers());
		_party.getOwnerAsPlayer().ifPresent(members::remove);
		buttons[OWNER_HEAD_SLOT] = new PartyMemberIcon(_party.getOwner(), _party, true, true);

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

			buttons[slot++] = new PartyMemberIcon(UtilGameProfile.getGameProfile(member), _party, false, true);
		}

		return pane(buttons);
	}
}