package nautilus.game.minekart.item;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import mineplex.core.common.util.C;
import nautilus.game.minekart.item.use_custom.*;
import nautilus.game.minekart.item.use_default.*;

public enum KartItemType 
{
	//Default
	Banana(				"Banana", 			Material.GOLD_INGOT, 		1,	new UseBanana()),
	BananaBunch(		"Banana Bunch", 	Material.GOLD_SPADE, 		6,	new UseBanana()),
	FakeItem(			"Fake Item", 		Material.FLINT, 			1,	new UseFakeItem()),

	SingleGreenShell(	"Green Shell", 		Material.SLIME_BALL, 		1, 	new UseGreenShell()),
	DoubleGreenShell(	"2x Green Shell", 	Material.MELON_SEEDS, 		1, 	new UseGreenShell()),
	TripleGreenShell(	"3x Green Shell", 	Material.PUMPKIN_SEEDS, 	1, 	new UseGreenShell()),

	SingleRedShell(		"Red Shell", 		Material.MAGMA_CREAM, 		1, 	new UseRedShell()),
	DoubleRedShell(		"2x Red Shell", 	Material.RAW_FISH, 			1, 	new UseRedShell()),
	TripleRedShell(		"3x Red Shell", 	Material.COOKED_FISH, 		1, 	new UseRedShell()),

	Ghost(				"Ghost",			Material.GHAST_TEAR, 		1, 	new UseGhost()),
	Star(				"Star", 			Material.NETHER_STAR, 		1, 	new UseStar()),
	Lightning(			"Lightning", 		Material.GLOWSTONE_DUST, 	1, 	new UseLightning()),

	SingleMushroom(		"1x Mushroom", 		Material.BREAD, 			1, 	new UseMushroom()),
	DoubleMushroom(		"2x Mushroom", 		Material.BOWL, 				1, 	new UseMushroom()),
	TripleMushroom(		"3x Mushroom", 		Material.MUSHROOM_SOUP,		1, 	new UseMushroom()),
	SuperMushroom(		"Super Mushroom", 	Material.GOLDEN_APPLE, 		1, 	new UseMushroom()),

	//Custom	
	Chicken("Egg Blaster", Material.EGG, 16, 0.16, new UseChicken(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "16-Round Egg Blaster.",
			}),
	
	Pig("Pig Stink", Material.PORK, 1, 0.12, new UsePig(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Confuses all players.",
				ChatColor.RESET + C.cWhite + "Lasts 20 seconds"
			}),
	
	Wolf("Heart Barrier", Material.APPLE, 1, 0.16, new UseWolf(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Blocks 1 Shell/Banana/Fake Item",
				ChatColor.RESET + C.cWhite + "Lasts 60 Seconds"
			}),
	
	Spider("Spiderlings", Material.SEEDS,  1, 0.16,	new UseSpider(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Release 1 Spiderling at each player.",
				ChatColor.RESET + C.cWhite + "Spiderlings hunt players, causing a crash.",
				ChatColor.RESET + C.cWhite + "Lasts 15 seconds."
			}),
	
	Blaze("Infernal Kart", Material.BLAZE_POWDER, 1, 0.16, new UseBlaze(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Boost forwards with amazing handling.", 
				ChatColor.RESET + C.cWhite + "Leaves a trail of flames, slowing players."
			}),
			
	Sheep("Super Sheep", Material.IRON_SPADE, 1, 0.08, new UseSheep(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Super Sheep flies around the track.",
				ChatColor.RESET + C.cWhite + "Hunts down other nearby players.",
				ChatColor.RESET + C.cWhite + "Lasts 15 seconds"
			}),
			
	Enderman("Blink", Material.ENDER_PEARL, 1, 0.16, new UseEnderman(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Instantly teleport forward 20 blocks.",
				ChatColor.RESET + C.cWhite + "Converts velocity into new direction.",
				ChatColor.RESET + C.cWhite + "3 Uses."
			}),
			
	Cow("Stampede", Material.DIAMOND_SPADE, 1, 0.16, new UseCow(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Angry cows charge foward at players."
			}),
			
	Golem("Earthquake", Material.COAL, 1, 0.08, new UseGolem(), new String[] 
			{
				"",
				ChatColor.RESET + C.cWhite + "Halves all players velocity.",
				ChatColor.RESET + C.cWhite + "Enemies are propelled upwards.",
				ChatColor.RESET + C.cWhite + "More powerful at close range."
			});

	private String _name;
	private Material _mat;
	private int _amount;
	
	private ItemUse _action;

	private double _chance = 0;
	private String[] _customDesc = new String[] {"Default"};
	
	KartItemType(String name, Material mat, int amount, ItemUse action)
	{
		_name = name;
		_mat = mat;
		_amount = amount;
		_action = action;
	}
	
	KartItemType(String name, Material mat, int amount, double customChance, ItemUse action, String[] customString)
	{
		_name = name;
		_mat = mat;
		_amount = amount;
		
		_action = action;
		
		_chance = customChance;
		_customDesc = customString;
	}

	public String GetName()
	{
		return _name;
	}

	public ItemUse GetAction()
	{
		return _action;
	}

	public Material GetMaterial()
	{
		return _mat;
	}

	public int GetAmount() 
	{
		return _amount;	
	}
	
	public double GetChance()
	{
		return _chance;
	}

	public String[] GetDesc()
	{
		return _customDesc;
	}
	
	public static ArrayList<KartItemType> GetItem(int pos) 
	{
		ArrayList<KartItemType> itemBag = new ArrayList<KartItemType>();

		if (pos == -1)
		{
			for (int i=1 ; i>0 ; i--)
				itemBag.add(KartItemType.Star);
			
			for (int i=1 ; i>0 ; i--)
				itemBag.add(KartItemType.Ghost);
			
			for (int i=1 ; i>0 ; i--)
				itemBag.add(KartItemType.TripleRedShell);
			
			for (int i=2 ; i>0 ; i--)
				itemBag.add(KartItemType.TripleGreenShell);
			
			for (int i=2 ; i>0 ; i--)
				itemBag.add(KartItemType.BananaBunch);
			
			for (int i=2 ; i>0 ; i--)
				itemBag.add(KartItemType.FakeItem);
			
			for (int i=3 ; i>0 ; i--)
				itemBag.add(KartItemType.SingleRedShell);
			
			for (int i=3 ; i>0 ; i--)
				itemBag.add(KartItemType.Banana);
			
			for (int i=4 ; i>0 ; i--)
				itemBag.add(KartItemType.SingleGreenShell);	
		}
		
		else 
		{
			for (int i=20 - (3 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.Lightning);
			
			for (int i=20 - (2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.Star);
			
			for (int i=20 - (2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.SuperMushroom);
			
			for (int i=20 - (2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.Ghost);
			
			for (int i=20 - (2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.TripleRedShell);
			
			for (int i=20 - (1 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.TripleGreenShell);
			
			for (int i=20 - (1 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.TripleMushroom);
			
			for (int i=20 - (1 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.BananaBunch);
			
			for (int i=5 + 	(2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.SingleRedShell);
			
			for (int i=10 + (2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.SingleMushroom);
			
			for (int i=10 + (2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.SingleGreenShell);	
			
			for (int i=0 + 	(2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.FakeItem);
			
			for (int i=0 + 	(2 * 9-pos) ; i>0 ; i--)
				itemBag.add(KartItemType.Banana);
		}

		return itemBag;
	}
}
