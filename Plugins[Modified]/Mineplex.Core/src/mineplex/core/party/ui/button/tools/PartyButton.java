package mineplex.core.party.ui.button.tools;

import mineplex.core.menu.Button;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import org.bukkit.inventory.ItemStack;

/**
 * A wrapper for all buttons which need to interact with a specific party
 */
public abstract class PartyButton extends Button<PartyManager>
{

	private Party _party;

	public PartyButton(ItemStack itemStack, Party party, PartyManager plugin)
	{
		super(itemStack, plugin);
		_party = party;
	}

	public Party getParty()
	{
		return _party;
	}
}