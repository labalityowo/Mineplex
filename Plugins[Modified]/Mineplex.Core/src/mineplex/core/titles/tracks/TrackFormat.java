package mineplex.core.titles.tracks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;

public class TrackFormat
{
	private final ChatColor _chatColor;
	private final ChatColor _magicPrefixSuffix;

	private List<String> _lines;
	private int _ticks;

	public TrackFormat(ChatColor color)
	{
		this(color, null);
	}

	public TrackFormat(ChatColor color, ChatColor prefixSuffixColor)
	{
		this._chatColor = color;
		this._magicPrefixSuffix = prefixSuffixColor;
	}

	public TrackFormat animated(int ticks, String... lines)
	{
		_ticks = ticks;
		_lines = new ArrayList<>();
		for (String line : lines)
		{
			ComponentBuilder builder = new ComponentBuilder("");
			preFormat(builder);
			builder.append(line);
			format(builder);
			builder.append("", ComponentBuilder.FormatRetention.NONE);
			postFormat(builder);
			_lines.add(BaseComponent.toLegacyText(builder.create()));
		}
		return this;
	}

	public void preFormat(ComponentBuilder component)
	{
		if (_magicPrefixSuffix != null)
		{
			component
					.append("A", ComponentBuilder.FormatRetention.NONE)
					.obfuscated(true)
					.color(_magicPrefixSuffix)
					.append(" ", ComponentBuilder.FormatRetention.NONE);
		}
		else
		{
			component
					.append("", ComponentBuilder.FormatRetention.NONE);
		}
	}

	public void postFormat(ComponentBuilder component)
	{
		if (_magicPrefixSuffix != null)
		{
			component
					.append(" ", ComponentBuilder.FormatRetention.NONE)
					.append("A", ComponentBuilder.FormatRetention.NONE)
					.obfuscated(true)
					.color(_magicPrefixSuffix)
					.append("", ComponentBuilder.FormatRetention.NONE);
		}
		else
		{
			component
					.append("", ComponentBuilder.FormatRetention.NONE);
		}
	}

	public void format(ComponentBuilder component)
	{
		component
				.color(_chatColor);
	}

	public ChatColor getColor()
	{
		return this._chatColor;
	}

	public List<String> getAnimatedLines()
	{
		return _lines;
	}

	public boolean isAnimated()
	{
		return _lines != null && !_lines.isEmpty();
	}

	public int getDelay()
	{
		return _ticks;
	}
}
