package mineplex.core.titles.tracks.custom;

import net.md_5.bungee.api.ChatColor;

import org.apache.commons.lang3.Validate;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class TrackBuilder
{
	private String _id;
	private String _shortName;
	private String _longName;
	private ChatColor _color;
	private String _desc;
	private boolean _hideIfUnowned;

	private String _tierName;
	private String _tierDesc;
	private ChatColor _tierColor;
	private ChatColor _magicColor;

	private int _ticks;
	private String[] _frames;

	private TrackBuilder(String id)
	{
		this._id = id;
	}

	public TrackBuilder withShortName(String shortName)
	{
		Validate.notNull(shortName, "Short name cannot be null");
		this._shortName = shortName.length() > 16 ? shortName.substring(0, 16) : shortName;
		return this;
	}

	public TrackBuilder withLongName(String longName)
	{
		Validate.notNull(longName, "Long name cannot be null");
		this._longName = longName;
		return this;
	}

	public TrackBuilder withColor(ChatColor color)
	{
		Validate.notNull(color, "Color cannot be null");
		this._color = color;
		return this;
	}

	public TrackBuilder withDescription(String desc)
	{
		Validate.notNull(desc, "Description cannot be null");
		this._desc = desc;
		return this;
	}

	public TrackBuilder setHideIfUnowned(boolean hide)
	{
		this._hideIfUnowned = hide;
		return this;
	}

	public TrackBuilder setTierName(String tierName)
	{
		Validate.notNull(tierName, "Tier name cannot be null");
		this._tierName = tierName;
		return this;
	}

	public TrackBuilder setTierDesc(String tierDesc)
	{
		Validate.notNull(tierDesc, "Tier desc cannot be null");
		this._tierDesc = tierDesc;
		return this;
	}

	public TrackBuilder setTierColor(ChatColor tierColor)
	{
		Validate.notNull(tierColor, "Tier color cannot be null");
		this._tierColor = tierColor;
		return this;
	}

	public TrackBuilder setTierMagicColor(ChatColor magicColor)
	{
		Validate.notNull(magicColor, "Magic color cannot be null");
		this._magicColor = magicColor;
		return this;
	}

	public TrackBuilder setFrames(String... frames)
	{
		Validate.notNull(frames, "Frames cannot be null");
		if (frames.length == 0)
			return this;
		this._frames = frames;
		return this;
	}

	public TrackBuilder setTicks(int ticks)
	{
		this._ticks = ticks;
		return this;
	}

	public <T extends Track> T build()
	{
		Validate.notNull(_id, "ID cannot be null");
		Validate.notNull(_color, "Color cannot be null");
		Validate.notNull(_shortName, "Short name cannot be null");

		if (_desc == null)
			_desc = _shortName;
		if (_longName == null)
			_longName = _shortName;
		if (_tierColor == null)
			_tierColor = _color;
		if (_tierName == null)
			_tierName = _shortName;
		if (_tierDesc == null)
			_tierDesc = _desc;

		if (_frames == null)
		{
			return (T) new ItemizedTrack(_id, _color, _shortName, _longName, _desc, _hideIfUnowned)
			{
				ItemizedTrack init()
				{
					getRequirements()
							.addTier(new TrackTier(
									_tierName,
									_tierDesc,
									this::owns,
									new TrackFormat(_tierColor, _magicColor)
							));
					return this;
				}
			}.init();
		}
		else
		{
			Validate.isTrue(_ticks >= 1, "Ticks must be >= 1");

			return (T) new ItemizedTrack(_id, _color, _shortName, _longName, _desc, _hideIfUnowned)
			{
				ItemizedTrack init()
				{
					getRequirements()
							.addTier(new TrackTier(
									_tierName,
									_tierDesc,
									this::owns,
									new TrackFormat(_tierColor, _magicColor)
											.animated(_ticks, _frames)
							));
					return this;
				}
			}.init();
		}
	}

	public static TrackBuilder builder(String id)
	{
		Validate.notNull(id, "ID cannot be null");
		Validate.isTrue(id.length() <= 32, "ID must not be longer than 32 characters");
		return new TrackBuilder(id);
	}
}