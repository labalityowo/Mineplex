package nautilus.game.arcade.game.modules.gamesummary;

import java.util.List;
import java.util.function.Function;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;

public abstract class GameSummaryComponent<T>
{

	protected static final String DOUBLE_ARROW = "»";

	private final GameSummaryComponentType _type;
	private final Function<Player, T> _getFunction;

	public GameSummaryComponent(GameSummaryComponentType type, Function<Player, T> getFunction)
	{
		_type = type;
		_getFunction = getFunction;
	}

	public abstract String getMainText(T data);

	public abstract List<String> getHoverText(T data);

	public boolean sendMessage(Player player)
	{
		T result = _getFunction.apply(player);
		if (result == null)
		{
			return false;
		}

		String mainText = getMainText(result);
		List<String> hoverText = getHoverText(result);

		if (mainText == null || hoverText == null)
		{
			return false;
		}

		BaseComponent[] message = TextComponent.fromLegacyText(mainText);
		String hoverTextString = String.join("\n", hoverText);

		if (!hoverTextString.isEmpty())
		{
			HoverEvent hoverEvent = new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverTextString));

			for (BaseComponent component : message)
			{
				component.setHoverEvent(hoverEvent);
			}
		}

		player.spigot().sendMessage(message);
		return true;
	}

	protected void sendBlank(Player player)
	{
		player.sendMessage("");
	}

	public GameSummaryComponentType getType()
	{
		return _type;
	}
}
