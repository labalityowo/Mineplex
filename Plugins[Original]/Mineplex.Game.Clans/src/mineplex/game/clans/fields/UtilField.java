package mineplex.game.clans.fields;

import mineplex.core.common.util.UtilServer;

public class UtilField 
{
	public static long scale(long regenTime) 
	{
		int players = 80 - Math.min(80, UtilServer.getPlayers().length);
		return (long)(regenTime * (-16 * Math.log(Math.pow(8.01,2) - Math.pow(0.1*players,2)) + 67.6));
	}
}
