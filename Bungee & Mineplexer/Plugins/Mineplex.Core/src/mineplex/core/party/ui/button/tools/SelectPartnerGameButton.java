package mineplex.core.party.ui.button.tools;

import mineplex.core.common.util.C;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.Menu;
import mineplex.core.party.PartyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 *
 */
public class SelectPartnerGameButton extends Button<PartyManager>
{

	private static final String COMMAND = "/teamprefs ";

	private final String _partner;
	private final String _gameName;

	public SelectPartnerGameButton(GameDisplay gameDisplay, String partner, PartyManager plugin)
	{
		super(new ItemBuilder(gameDisplay.getMaterial())
		  .setTitle(C.cYellow + gameDisplay.getName())
		  .setData(gameDisplay.getMaterialData())
		  .build(), plugin);
		_partner = partner;
		_gameName = gameDisplay.getName();
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		player.chat(COMMAND + _partner + " " + _gameName);
		Menu.get(player.getUniqueId()).setUseClose(true);
		player.closeInventory();
	}
}
