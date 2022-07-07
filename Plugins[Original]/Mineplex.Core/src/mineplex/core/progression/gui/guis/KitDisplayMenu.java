package mineplex.core.progression.gui.guis;

import mineplex.core.menu.Button;
import mineplex.core.menu.IconButton;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import mineplex.core.progression.gui.buttons.KitIconButton;
import mineplex.core.progression.gui.buttons.KitPermanentDefaultButton;
import mineplex.core.progression.gui.buttons.KitSelectButton;
import mineplex.core.progression.gui.buttons.KitUpgradeDetailsButton;
import mineplex.core.progression.gui.buttons.KitUpgradeMenuButton;
import mineplex.core.progression.gui.buttons.KitXPButton;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * This is the main menu a player sees when we selects an NPC Kit
 * Displays the most important information about this kit
 */
public class KitDisplayMenu extends KitMenu
{

	private static final int[] UPGRADE_SLOTS = {
	  27, 29, 31, 33, 35
	};

	private static final int[] PERM_SLOTS = {
	  0, 1, 9, 10
	};

	private static final int[] SELECT_SLOTS = {
	  7, 8, 16, 17
	};

	public KitDisplayMenu(ProgressiveKit kit, KitProgressionManager plugin)
	{
		super(kit, plugin);
	}

	@Override
	public Button[] setUp(Player player)
	{
		Button[] buttons = new Button[51];

		setUpSelecting(buttons);

		setUpIcon(buttons, player);

		setUpDetails(buttons, player);

		setUpNextMenuButtons(buttons, player);

		return buttons;
	}

	/**
	 * Set up the buttons corresponding to opening {@link KitInformationTrackerMenu}
	 *
	 * @param buttons The array of buttons we're modifying
	 * @param player  The player whose data we'll be using
	 */
	private void setUpNextMenuButtons(Button[] buttons, Player player)
	{
		if (getKit().usesXp())
		{
			buttons[48] = new KitXPButton(getKit(), player, getPlugin());
		}
		buttons[getKit().usesXp() ? 50 : 49] = new KitUpgradeMenuButton(getKit(), player, getPlugin());
	}

	/**
	 * Set up the Kit display's icon
	 *
	 * @param buttons The array of buttons we're modifying
	 */
	private void setUpIcon(Button[] buttons, Player player)
	{
		buttons[13] = new KitIconButton(getKit(), player);
	}

	/**
	 * Set up the details (info) regarding the upgrades to this kit
	 *
	 * @param buttons The array of buttons we're modifying
	 */
	private void setUpDetails(Button[] buttons, Player player)
	{
		if(getKit().showUpgrades())
		{
			Map<Integer, List<String>> details = getKit().getUpgradeDetails();
			int index = 0;
			for(int i : UPGRADE_SLOTS)
			{
				buttons[i] = new KitUpgradeDetailsButton(getKit(), player, index + 1, details.get(index++));
			}
			return;
		}

		for (int i : UPGRADE_SLOTS)
		{
			buttons[i] = new IconButton(COMING_SOON);
		}
	}

	/**
	 * Set up the options for selecting this kit
	 * Either permanent or just for this game
	 *
	 * @param buttons The array of buttons we're modifying
	 */
	private void setUpSelecting(Button[] buttons)
	{
		for (int permSlot : PERM_SLOTS)
		{
			buttons[permSlot] = new KitPermanentDefaultButton(getKit());
		}

		for (int tempSlot : SELECT_SLOTS)
		{
			buttons[tempSlot] = new KitSelectButton(getKit());
		}
	}

	@Override
	public void onClose(Player player)
	{

	}
}
