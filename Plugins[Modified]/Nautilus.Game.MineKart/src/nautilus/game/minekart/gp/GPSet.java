package nautilus.game.minekart.gp;

import org.bukkit.Material;

public enum GPSet 
{	
	MushroomCup("Mushroom Cup", new String[] {"MushroomA", "MushroomB", "MushroomC", "MushroomD"}, 	Material.RAW_CHICKEN, 		false),
	FlowerCup(	"Flower Cup", 	new String[] {"MushroomA", "MushroomB", "MushroomC", "MushroomD"}, 	Material.COOKED_CHICKEN, 	false),
	StarCup(	"Star Cup", 	new String[] {"MushroomA", "MushroomB", "MushroomC", "MushroomD"}, 	Material.CARROT_ITEM, 		false),
	SpecialCup(	"Special Cup", 	new String[] {"MushroomA", "MushroomB", "MushroomC", "MushroomD"}, 	Material.GOLDEN_CARROT, 	false),
	
	Battle("Battle", 			new String[] {"BattleA", "BattleB", "BattleC"}, 					Material.RAW_BEEF, 			true);
	
	
	private String _name;
	private String[] _mapNames;
	private Material _displayMat;
	private boolean _battle;
		
	GPSet(String name, String[] mapNames, Material mat, boolean battle)
	{
		_name = name;
		_mapNames = mapNames;
		_displayMat = mat;
		_battle = battle;
	}
	
	public String GetName()
	{
		return _name;
	}
	
	public String[] GetMapNames()
	{
		return _mapNames;
	}
	
	public Material GetDisplayMaterial()
	{
		return _displayMat;
	}
	
	public boolean IsBattle()
	{
		return _battle;
	}
}
