package mineplex.core.antihack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.ChatModifier;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;

public class CheckThresholds
{
	private final String _friendlyName;
	private final int _low;
	private final int _med;
	private final int _high;

	public CheckThresholds(String friendlyName, int low, int med, int high)
	{
		_friendlyName = friendlyName;
		_low = low;
		_med = med;
		_high = high;
	}

	public String getFriendlyName()
	{
		return _friendlyName;
	}

	public void format(ComponentBuilder builder, int violationLevel)
	{
		builder.append(_friendlyName, ComponentBuilder.FormatRetention.NONE).color(getSeverity(violationLevel)._color);
	}

	public Severity getSeverity(int violationLevel)
	{
		if (violationLevel >= _high)
		{
			return Severity.HIGH;

		} else if (violationLevel >= _med)
		{
			return Severity.MEDIUM;

		} else if (violationLevel >= _low)
		{
			return Severity.LOW;
		}

		return Severity.NONE;
	}

	public enum Severity
	{
		NONE(ChatColor.GREEN),
		LOW(ChatColor.GREEN),
		MEDIUM(ChatColor.GOLD),
		HIGH(ChatColor.RED),
		;
		private final ChatColor _color;

		Severity(ChatColor color)
		{
			_color = color;
		}
	}
}
