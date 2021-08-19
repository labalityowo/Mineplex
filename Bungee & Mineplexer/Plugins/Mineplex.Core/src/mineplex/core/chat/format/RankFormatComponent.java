package mineplex.core.chat.format;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;

public class RankFormatComponent implements ChatFormatComponent
{

	private final CoreClientManager _clientManager;

	public RankFormatComponent(CoreClientManager clientManager)
	{
		_clientManager = clientManager;
	}

	@Override
	public BaseComponent getText(Player player)
	{
		PermissionGroup group = _clientManager.Get(player).getRealOrDisguisedPrimaryGroup();

		TextComponent component = new TextComponent(group.getDisplay(false, true, false, false));
		component.setColor(group.getColor().asBungee());
		component.setBold(true);
		component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(group.getDisplay(true, true, true, true) + ChatColor.WHITE + "\n" + group.getDescription())
				.create()));

		return component;
	}
}
