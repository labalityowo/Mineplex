package nautilus.game.pvp.modules.clans;

import java.util.HashSet;


public class ClansBlocks 
{
	public ClansBlocks(Clans clans)
	{
		
	}
	
	public HashSet<Integer> denyInteract = new HashSet<Integer>();
	public HashSet<Integer> allowInteract = new HashSet<Integer>();

	public HashSet<Integer> denyUsePlace = new HashSet<Integer>();
	public HashSet<Integer> allowUsePlace = new HashSet<Integer>();
	
	public HashSet<Integer> allowBreak = new HashSet<Integer>();
	
	public boolean denyInteract(int id)
	{
		if (denyInteract.isEmpty())
		{
			denyInteract.add(22);		//Lapis
			denyInteract.add(23);		//Dispenser
			denyInteract.add(26);		//Bed
			denyInteract.add(54);		//Chest
			denyInteract.add(61);		//Furance
			denyInteract.add(62);		//Furnace
			denyInteract.add(64);		//Wood Door
			//denyInteract.add(69);		//Lever
			//denyInteract.add(70);		//Stone Plate
			denyInteract.add(71);		//Iron Door
			//denyInteract.add(72);		//Wood Plate
			//denyInteract.add(77);		//Stone Button
			denyInteract.add(93);		//Repeater
			denyInteract.add(94);		//Repeater
			denyInteract.add(96);		//Trap Door
			denyInteract.add(107);		//Fence Gate
			denyInteract.add(117);		//Brewing Stand
			denyInteract.add(146);		//Trap Chest
		}

		return denyInteract.contains(id);
	}

	public boolean allowInteract(int id)
	{
		if (allowInteract.isEmpty())
		{
			allowInteract.add(64);		//Wood Door
			//allowInteract.add(69);	//Lever
			//allowInteract.add(70);	//Stone Plate
			allowInteract.add(71);		//Iron Door
			//allowInteract.add(72);	//Wood Plate
			//allowInteract.add(77);	//Stone Button
			allowInteract.add(96);		//Trap Door
			allowInteract.add(107);		//Fence Gate
		}

		return allowInteract.contains(id);
	}

	public boolean denyUsePlace(int id)
	{
		if (denyUsePlace.isEmpty())
		{
			//List PLACEABLE ITEMS
			denyUsePlace.add(259);	//Flint & Steel	
			denyUsePlace.add(321);	//Painting
			denyUsePlace.add(323);	//Sign
			denyUsePlace.add(324);	//Wood Door
			denyUsePlace.add(326);	//Water Bucket
			denyUsePlace.add(327);	//Lava Bucket
			denyUsePlace.add(330);	//Iron Door
			denyUsePlace.add(331);	//Redstone
			denyUsePlace.add(333);	//Boat	
			denyUsePlace.add(355);	//Bed	
			denyUsePlace.add(356);	//Redstone Repeater
			denyUsePlace.add(379);	//Brewing Stand
			denyUsePlace.add(380);	//Cauldron
			denyUsePlace.add(389);	//Frame
			denyUsePlace.add(390);	//Pot
			denyUsePlace.add(404);	//Comparator
			denyUsePlace.add(407);	//TNT Cart
		}
		
		if (id == 65)
			return false;

		if (id > 0 && id < 256)
			return true;

		return denyUsePlace.contains(id);
	}

	public boolean allowUsePlace(int id)
	{
		if (allowUsePlace.isEmpty())
		{
			allowUsePlace.add(37);	//Flower
			allowUsePlace.add(38);	//Flower
			allowUsePlace.add(65);	//Ladder
		}

		return allowUsePlace.contains(id);
	}

	public boolean canBreak(int id)
	{
		if (allowBreak.isEmpty())
		{
			allowBreak.add(6);		//Saplings
			allowBreak.add(12);		//Sand
			allowBreak.add(13);		//Gravel
			allowBreak.add(22);		//Blue
			allowBreak.add(30);		//Web
			
			allowBreak.add(39);		//Brown Mushroom
			allowBreak.add(40);		//Red Mushroom
			allowBreak.add(59);		//Wheat Seeds
			allowBreak.add(81);		//Cactus
			allowBreak.add(83);		//Sugar Cane
			allowBreak.add(86);		//Pumpkin
			allowBreak.add(103);	//Melon
			allowBreak.add(104);	//Pumpkin Stem
			allowBreak.add(105);	//Melon Stem
			allowBreak.add(115);	//Nether Wart
			allowBreak.add(127);	//Cocoa Plant
			allowBreak.add(141);	//Carrot
			allowBreak.add(142);	//Potato
			
			allowBreak.add(58);		//Workbench
			allowBreak.add(61);		//Furnace
			allowBreak.add(62);		//Furnace
			//allowBreak.add(116);	//Enchanting
			//allowBreak.add(117);	//Brewing
			allowBreak.add(138);	//Beacon
			//allowBreak.add(145);	//Anvil
		}

		return allowBreak.contains(id);
	}
}
