package mineplex.core.common.util;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UtilRadar 
{
	public static void displayRadar(Player player, List<RadarData> dataList)
	{
		displayRadar(player, dataList, true);
	}
	
	public static void displayRadar(Player player, List<RadarData> dataList, boolean bossBar)
	{
		int radarChars = 59;
		int radarSpaces = radarChars;
		
		//get bearings for each element
		for (RadarData data : dataList)
		{
			double pYaw = UtilAlg.GetYaw(player.getLocation().getDirection());
			double relYaw = UtilAlg.GetYaw(UtilAlg.getTrajectory(player.getLocation(), data.Loc));
			
			data.setBearing(relYaw - pYaw);
			
			radarSpaces -= ChatColor.stripColor(data.Text).length();
		}
		
		//sort
		sortScores(dataList);
					
		//draw
		String text = C.cPurple + C.Bold + "Radar [" + ChatColor.RESET;
		int radarSpacesDrawn = 0;
		int radarCharsDrawn = 0;
		
		for (RadarData data : dataList)
		{
			//behind to left
			if (data.getBearing() < -90)
			{
				text += ChatColor.RESET + data.Text;
				radarCharsDrawn += ChatColor.stripColor(data.Text).length();
			}
			//behind to right
			else if (data.getBearing() > 90)
			{
				//finish spaces
				while (radarSpacesDrawn < radarSpaces)
				{
					text += " ";
					radarSpacesDrawn++;
					radarCharsDrawn++;
				}	
				
				text += ChatColor.RESET + data.Text;
				radarCharsDrawn += ChatColor.stripColor(data.Text).length();
			}
			//in front
			else
			{
				double percent = (data.getBearing() + 90D) / 180D;
				
				while (percent >= (double)radarCharsDrawn/(double)radarChars && radarSpacesDrawn<radarSpaces)
				{
					text += " ";
					radarSpacesDrawn++;
					radarCharsDrawn++;
				}
				
				text += ChatColor.RESET + data.Text;
				radarCharsDrawn += ChatColor.stripColor(data.Text).length();
			}
		}
		
		//finish spaces (only needed if nothing was on right)
		while (radarSpacesDrawn < radarSpaces)
		{
			text += " ";
			radarSpacesDrawn++;
		}	
		
		text += C.cPurple + C.Bold + "] Radar";
			
		UtilTextTop.display(text, player);

		if (bossBar)
		{
			UtilTextTop.display(text, player);
		}
		else
		{
			UtilTextBottom.display(text, player);
		}
	}
	
	private static void sortScores(List<RadarData> dataList) 
	{
		for (int i=0 ; i<dataList.size() ; i++)
		{
			for (int j=dataList.size()-1 ; j>0 ; j--)
			{
				if (dataList.get(j).getBearing() < dataList.get(j-1).getBearing())
				{
					RadarData temp = dataList.get(j);
					dataList.set(j, dataList.get(j-1));
					dataList.set(j-1, temp);
				}
			}
		}
	}
}