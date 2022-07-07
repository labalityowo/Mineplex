package mineplex.game.clans.clans.event;

import java.sql.Timestamp;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.game.clans.core.repository.tokens.ClanToken;

public class ClanCreationCompleteEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private Player _founder;
	private int _id;
	private String _name;
	private String _description;
	private String _home;
	private boolean _admin;
	private int _energy;
	private int _kills;
	private int _murders;
	private int _deaths;
	private int _warWins;
	private int _warLosses;
	private Timestamp _dateCreated;
	private Timestamp _lastOnline;

	public ClanCreationCompleteEvent(ClanToken token, Player founder)
	{
		_founder = founder;

		_id = token.Id;
		_name = token.Name;
		_description = token.Description;
		_home = token.Home;
		_admin = token.Admin;
		_energy = token.Energy;
		_kills = token.Kills;
		_murders = token.Murder;
		_deaths = token.Deaths;
		_warWins = token.WarWins;
		_warLosses = token.WarLosses;
		_dateCreated = token.DateCreated;
		_lastOnline = token.LastOnline;
	}

	public Player getFounder()
	{
		return _founder;
	}

	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getDescription()
	{
		return _description;
	}

	public void setDescription(String description)
	{
		_description = description;
	}

	public String getHome()
	{
		return _home;
	}

	public void setHome(String home)
	{
		_home = home;
	}

	public boolean isAdmin()
	{
		return _admin;
	}

	public void setAdmin(boolean admin)
	{
		_admin = admin;
	}

	public int getEnergy()
	{
		return _energy;
	}

	public void setEnergy(int energy)
	{
		_energy = energy;
	}

	public int getKills()
	{
		return _kills;
	}

	public void setKills(int kills)
	{
		_kills = kills;
	}

	public int getMurders()
	{
		return _murders;
	}

	public void setMurders(int murders)
	{
		_murders = murders;
	}

	public int getDeaths()
	{
		return _deaths;
	}

	public void setDeaths(int deaths)
	{
		_deaths = deaths;
	}

	public int getWarWins()
	{
		return _warWins;
	}

	public void setWarWins(int warWins)
	{
		_warWins = warWins;
	}

	public int getWarLosses()
	{
		return _warLosses;
	}

	public void setWarLosses(int warLosses)
	{
		_warLosses = warLosses;
	}

	public Timestamp getDateCreated()
	{
		return _dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated)
	{
		_dateCreated = dateCreated;
	}

	public Timestamp getLastOnline()
	{
		return _lastOnline;
	}

	public void setLastOnline(Timestamp lastOnline)
	{
		_lastOnline = lastOnline;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}