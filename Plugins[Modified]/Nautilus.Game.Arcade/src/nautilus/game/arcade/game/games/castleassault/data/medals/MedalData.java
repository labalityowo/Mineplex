package nautilus.game.arcade.game.games.castleassault.data.medals;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import mineplex.core.common.util.C;

public class MedalData
{
	private Map<MedalType, Double> _medalScore;
	
	public MedalData()
	{
		_medalScore = new HashMap<>();
	}
	
	public Double getScore(MedalType type)
	{
		return _medalScore.computeIfAbsent(type, key -> 0D);
	}
	
	public void addScore(MedalType kingDmg, Double score)
	{
		_medalScore.merge(kingDmg, score, Double::sum);
	}
	
	public static enum MedalLevel
	{
		GOLD(C.cGold + "Gold", ChatColor.GOLD),
		SILVER(C.cGray + "Silver", ChatColor.GRAY),
		BRONZE(C.cWhite + "Bronze", ChatColor.WHITE)
		;
		
		private String _name;
		private ChatColor _color;
		
		private MedalLevel(String name, ChatColor color)
		{
			_name = name;
			_color = color;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public ChatColor getColor()
		{
			return _color;
		}
	}
}