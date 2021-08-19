package mineplex.bungee.motd;

import java.util.List;

import mineplex.serverdata.data.Data;

/**
 * A GlobalMotd represents a set of MOTD packaged lines.
 * @author MrTwiggy
 *
 */
public class GlobalMotd implements Data
{
	// The unique name representing this MOTD set
	private String _name;
	
	private String _headline;
	public String getHeadline() { return _headline; }
	
	// List of lines describing the MOTD
	private List<String> _motd;
	public List<String> getMotd() { return _motd; }
	
	/**
	 * Constructor
	 * @param name
	 * @param motd
	 */
	public GlobalMotd(String name, String headline, List<String> motd)
	{
		_name = name;
		_headline = headline;
		_motd = motd;
	}
	
	/**
	 * Unique identifying ID associated with this {@link GlobalMotd}.
	 */
	public String getDataId()
	{
		return _name;
	}
}
