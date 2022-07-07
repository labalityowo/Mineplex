package mineplex.core.titles.tracks;

import net.md_5.bungee.api.ChatColor;

import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.stats.StatsManager;
import mineplex.database.tables.Stats;

import static mineplex.core.Managers.require;

public class Track implements Listener
{
	private final String _id;
	private final String _shortName;
	private final String _longName;
	private final String _desc;
	private final ChatColor _color;
	private final boolean _hideIfUnowned;

	private final TrackRequirements _trackRequirements;

	private final GadgetManager _gadgetManager = require(GadgetManager.class);
	private final StatsManager _statsManager = require(StatsManager.class);

	private boolean _special = false;

	protected Track(String trackId, String shortName, String description)
	{
		this(trackId, ChatColor.DARK_AQUA, shortName, shortName, description);
	}

	protected Track(String trackId, String shortName, String description, boolean hideIfUnowned)
	{
		this(trackId, ChatColor.DARK_AQUA, shortName, shortName, description, hideIfUnowned);
	}

	protected Track(String trackId, ChatColor color, String shortName, String longName, String description)
	{
		this(trackId, color, shortName, longName, description, false);
	}

	protected Track(String trackId, ChatColor color, String shortName, String longName, String description, boolean hideIfUnowned)
	{
		// Book limits
		Validate.isTrue(shortName.length() <= 16, "Short name cannot be longer than 16 characters");
		Validate.isTrue(trackId.length() <= 32, "ID cannot be longer than 32 characters");

		this._id = trackId;
		this._shortName = shortName;
		this._longName = longName;
		this._desc = description;
		this._color = color;
		this._trackRequirements = new TrackRequirements(this);
		this._hideIfUnowned = hideIfUnowned;

		UtilServer.RegisterEvents(this);
	}
	
	protected void special()
	{
		this._special = true;
	}
	
	public boolean isSpecial()
	{
		return this._special;
	}

	public final TrackRequirements getRequirements()
	{
		return this._trackRequirements;
	}

	public String getStatName()
	{
		return "track." + _id;
	}

	public final void incrementFor(Player player, int amount)
	{
		_statsManager.incrementStat(player, getStatName(), amount);
	}

	public final long getStat(Player player)
	{
		return _statsManager.Get(player).getStat(getStatName());
	}

	public final boolean isSetActive(Player player, Class<? extends GadgetSet> setClass)
	{
		return _gadgetManager.getGadgetSet(setClass).isActive(player);
	}

	public String getId()
	{
		return _id;
	}

	public String getShortName()
	{
		return _shortName;
	}

	public String getLongName()
	{
		return _longName;
	}

	public String getDescription()
	{
		return _desc;
	}

	public ChatColor getColor()
	{
		return this._color;
	}

	public boolean hideIfUnowned()
	{
		return this._hideIfUnowned;
	}
}
