package mineplex.core.party.ui;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.IconButton;
import mineplex.core.menu.Menu;
import mineplex.core.party.PartyManager;
import org.bukkit.Material;

/**
 * A class to manage dynamic creation of GUI's
 */
public abstract class PartyMenu extends Menu<PartyManager>
{
	public PartyMenu(String name, PartyManager plugin)
	{
		super(name, plugin);
	}
}
