package mineplex.core.party.ui.button.tools.invite;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.party.PartyManager;

/**
 * Represents an invitation, to which a player can accept or deny
 */
public class InviteButton extends Button<PartyManager>
{

	private String _name;

	public InviteButton(String name, PartyManager plugin)
	{
		super(new ItemBuilder(Material.SKULL_ITEM)
				.setTitle(C.cYellow + name)
				.setLore(" ", C.cYellow + "Right-Click " + C.cGray + "to deny the invite", C.cYellow + "Left-Click " + C.cGray + "to accept the invite")
				.setData((short) 3)
				.setPlayerHead(name)
				.build(), plugin);
		_name = name;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType == ClickType.LEFT)
		{
			getPlugin().acceptInviteBySender(player, _name);
		}
		else if (clickType == ClickType.RIGHT)
		{
			getPlugin().denyInviteBySender(player, _name);
		}
		player.closeInventory();
	}
}