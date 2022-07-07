package mineplex.game.clans.fields;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class FieldMonsterInput 
{
	public enum CustomType
	{
		
	}

	public EntityType type = null;
	public CustomType customType = null;
	
	public int mobMax = 12;
	public double mobRate = 1;
	
	public int radius = 12;
	public int height = 4;
	
	public void Display(Player caller) 
	{
		UtilPlayer.message(caller, F.main("Field Monster", "Field Monster Settings;"));
		UtilPlayer.message(caller, F.desc("Type", type + ""));
		UtilPlayer.message(caller, F.desc("Max", mobMax + ""));
		UtilPlayer.message(caller, F.desc("Rate", mobRate + ""));
		UtilPlayer.message(caller, F.desc("Radius", radius + ""));
		UtilPlayer.message(caller, F.desc("Height ", height + ""));
	}
}
