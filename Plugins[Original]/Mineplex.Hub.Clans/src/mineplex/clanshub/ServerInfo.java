package mineplex.clanshub;

/**
 * Data class for loaded servers
 */
public class ServerInfo
{
	public String Name;
	public String MOTD;
	public int CurrentPlayers = 0;
	public int MaxPlayers = 0;
	public boolean Hardcore = false;
	
	/**
	 * Checks how many slots are left on this server
	 * @return The amount of slots that are left on this server
	 */
	public int getAvailableSlots()
	{
		return MaxPlayers - CurrentPlayers;
	}
}