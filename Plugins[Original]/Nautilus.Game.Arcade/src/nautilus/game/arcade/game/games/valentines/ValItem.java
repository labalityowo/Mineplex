package nautilus.game.arcade.game.games.valentines;

import org.bukkit.Material;

public enum ValItem 
{
	FLOWER1("Red Rose", 
			"She will love this bouquet of roses!",
			Material.RED_ROSE, 0),
	
	FLOWER2("Dandelion", 
			"I hope she's not allergic to these!",			
			Material.YELLOW_FLOWER, 0),
	
	FLOWER3("Blue Orchid", 
			"Thank you! These are her favourites!",
			Material.RED_ROSE, 1),
	
	WINE("Blue Wine",
			"This will go great with our steak dinner!",
			Material.POTION, 0),
	
	GRASS("Organic Free Range Grass", 
			"Yum! This is much tastier than caged grass!",
			Material.LONG_GRASS, 1),
	
	DIAMONDS("Diamond Earings",
			"I saved up for months for this gift!",
			Material.DIAMOND, 0),
	
	EMERALDS("Cold Hard Cash", 
			"They say love don't cost a thing. That's a lie!",
			Material.EMERALD, 0),
	
	BOOK("Love Poems", 
			"I will use impress her with poetry!",
			Material.BOOK, 0),
	
	WATCH("Fancy Pocket Watch", 
			"This Moolex Watch sure looks good on me!",
			Material.WATCH, 0);

	
	private String _title;
	private String _endText;
	private Material _item;
	private byte _itemByte;
	
	ValItem(String title, String endText, Material item, int data)
	{
		_title = title;
		_item = item;
		_endText = endText;
		_itemByte = (byte)data;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public Material getMaterial()
	{
		return _item;
	}

	public String getEndText() 
	{
		return _endText;
	}

	public byte getData() 
	{
		return _itemByte;
	}
}
