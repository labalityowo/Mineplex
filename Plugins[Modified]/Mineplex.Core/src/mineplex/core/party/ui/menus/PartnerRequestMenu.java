package mineplex.core.party.ui.menus;

import mineplex.core.game.GameDisplay;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.PartyMenu;
import mineplex.core.party.ui.button.tools.SelectPartnerGameButton;
import org.bukkit.entity.Player;

/**
 *
 */
public class PartnerRequestMenu extends PartyMenu
{

	private final GameDisplay[] GAMES = {
	  GameDisplay.SurvivalGamesTeams,
	  GameDisplay.SkywarsTeams,
	  GameDisplay.UHC,
	  GameDisplay.SmashTeams,
	};

	private final int INV_SIZE = 54;
	private final int START_SLOT = 20;
	private final int END_SLOT = 24;
	private final int START_SLOT_SECOND = 29;

	private final String _partner;

	public PartnerRequestMenu(String partner, PartyManager plugin)
	{
		super("Select a game", plugin);
		_partner = partner;
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[INV_SIZE];

		int slot = START_SLOT;

		for (GameDisplay gameDisplay : GAMES)
		{
			if (slot > END_SLOT)
			{
				slot = START_SLOT_SECOND;
			}

			buttons[slot++] = new SelectPartnerGameButton(gameDisplay, _partner, getPlugin());
		}

		return pane(buttons);
	}
}
