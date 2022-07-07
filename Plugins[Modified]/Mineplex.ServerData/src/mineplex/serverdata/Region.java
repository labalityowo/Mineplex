package mineplex.serverdata;

import java.io.File;

/**
 * Region enumerates the various geographical regions where Mineplex servers are
 * hosted.
 * @author Ty
 *
 */
public enum Region
{
	US,
	EU,
	ALL;
	
	
	/**
	 * @return the geographical {@link Region} of the current running process.
	 */
	public static Region currentRegion()
	{
		return !new File("eu.dat").exists() ? Region.US : Region.EU;
	}
}
