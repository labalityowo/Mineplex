package mineplex.core.titles.tracks;

import java.util.function.Function;
import java.util.function.Predicate;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.entity.Player;

public class TrackTier
{
	private final String _title;
	private final String _description;
	private final Function<Player, Long> _current;
	private final int _goal;
	private final TrackFormat _format;

	public TrackTier(String title, String description, Predicate<Player> condition, TrackFormat format)
	{
		this._title = title;
		this._current = player -> condition.test(player) ? 1L : 0L;
		this._goal = 1;
		this._description = description;
		this._format = format;
	}

	public TrackTier(String title, String description, Function<Player, Long> current, int goal, TrackFormat format)
	{
		this._title = title;
		this._current = current;
		this._goal = goal;
		this._description = description;
		this._format = format;
	}

	public boolean test(Player player)
	{
		return getProgress(player) >= 1.0;
	}

	public double getProgress(Player player)
	{
		return ((double) get(player)) / _goal;
	}

	public long get(Player player)
	{
		return _current.apply(player);
	}

	public String getTitle()
	{
		return this._title;
	}

	public String getDescription()
	{
		return this._description;
	}

	public int getGoal()
	{
		return _goal;
	}

	public TrackFormat getFormat()
	{
		return this._format;
	}

	public String getDisplayName()
	{
		ComponentBuilder builder = new ComponentBuilder("");
		getFormat().preFormat(builder);
		builder.append(getTitle(), ComponentBuilder.FormatRetention.NONE);
		getFormat().format(builder);
		getFormat().postFormat(builder);

		return BaseComponent.toLegacyText(builder.create());
	}
}
