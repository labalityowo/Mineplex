package mineplex.core.party.ui.button;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Menu;
import mineplex.core.party.Lang;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.constants.PartyRemoveReason;
import mineplex.core.party.ui.button.tools.PartyButton;
import mineplex.core.utils.UtilGameProfile;

/**
 * The button representing a Party member.
 */
public class PartyMemberIcon extends PartyButton
{
	private final String _name;

	private ItemStack _itemStack;

	public PartyMemberIcon(GameProfile playerProfile, Party party, boolean owner, boolean isOwnerView)
	{
		super(null, party, null);
		ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
				.setTitle(C.cYellow + playerProfile.getName());
		if (owner)
		{
			builder.addLore(" ", C.cGreenB + "Leader");
		}

		if (!owner && isOwnerView)
		{
			builder.addLore(" ", C.cRed + "Shift-Right-Click to kick");
		}
		_itemStack = builder.build();

		UtilGameProfile.setGameProfile(playerProfile, _itemStack);

		_name = playerProfile.getName();
	}

	@Override
	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (!getParty().getOwnerName().equalsIgnoreCase(player.getName()))
		{
			return;
		}

		if (clickType == ClickType.SHIFT_RIGHT)
		{
			Player target = Bukkit.getPlayerExact(_name);
			if (target != null && !getParty().isOwner(target) && target != player && getParty().isMember(target))
			{
				Lang.REMOVED.send(target);
				Managers.require(PartyManager.class).removeFromParty(target, PartyRemoveReason.KICKED);
				Menu.get(player.getUniqueId()).resetAndUpdate();
			}
		}
	}
}
