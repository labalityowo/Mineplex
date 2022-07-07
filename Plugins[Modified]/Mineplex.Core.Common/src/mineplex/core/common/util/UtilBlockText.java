package mineplex.core.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class UtilBlockText 
{
	public enum TextAlign
	{
		LEFT,
		RIGHT,
		CENTER
	}

	public static final Map<Character, int[][]> alphabet = new HashMap<>();
	
	public static Collection<Block> MakeText(String string, Location loc, BlockFace face, int id, byte data, TextAlign align)
	{
		return MakeText(string, loc, face, id, data, align, true);
	}

	public static Collection<Block> MakeText(String string, Location loc, BlockFace face, int id, byte data, TextAlign align, boolean setAir)
	{
		Set<Block> changes = new HashSet<>();
		
		if (alphabet.isEmpty())
			PopulateAlphabet();

		Block block = loc.getBlock();

		//Get Width
		int width = 0;
		for (char c : string.toLowerCase().toCharArray())
		{
			int[][] letter = alphabet.get(c);

			if (letter == null)
				continue;

			width += letter[0].length+1;
		}

		//Shift Blocks
		if (align == TextAlign.CENTER || align == TextAlign.RIGHT)
		{
			int divisor = 1;
			if (align == TextAlign.CENTER)
				divisor = 2;

			block = block.getRelative(face, (-1 * width/divisor) + 1);
		}

		//Clean
		if (setAir)
		{
			World world = loc.getWorld();
			int bX = loc.getBlockX();
			int bY = loc.getBlockY();
			int bZ = loc.getBlockZ();
			for (int y=0 ; y<5 ; y++)
			{
				if (align == TextAlign.CENTER)
					for (int i=-64 ; i<=64 ; i++)
					{
						MapUtil.QuickChangeBlockAt(world, bX + i * face.getModX(), bY + i * face.getModY(), bZ + i * face.getModZ(), Material.AIR);
					}


				if (align == TextAlign.LEFT)
					for (int i=0 ; i<=128 ; i++)
					{
						MapUtil.QuickChangeBlockAt(world, bX + i * face.getModX(), bY + i * face.getModY(), bZ + i * face.getModZ(), Material.AIR);
					}


				if (align == TextAlign.RIGHT)
					for (int i=-128 ; i<=0 ; i++)
					{
						MapUtil.QuickChangeBlockAt(world, bX + i * face.getModX(), bY + i * face.getModY(), bZ + i * face.getModZ(), Material.AIR);
					}


				bY -=1;
			}	
		}
		
		World world = block.getWorld();
		int bX = block.getX();
		int bY = block.getY();
		int bZ = block.getZ();

		//Make Blocks
		for (char c : string.toLowerCase().toCharArray())
		{
			int[][] letter = alphabet.get(c);

			if (letter == null)
				continue;

			for (int[] aLetter : letter)
			{
				for (int anALetter : aLetter)
				{
					if (anALetter == 1)
					{
						Block changed = world.getBlockAt(bX, bY, bZ);

						changes.add(changed);
						MapUtil.QuickChangeBlockAt(changed.getLocation(), id, data);
					}

					//Forward
					bX += face.getModX();
					bY += face.getModY();
					bZ += face.getModZ();
				}

				//Back
				bX += face.getModX() * -1 * aLetter.length;
				bY += face.getModY() * -1 * aLetter.length;
				bZ += face.getModZ() * -1 * aLetter.length;

				//Down
				bY--;
			}

			bY += 5;
			bX += face.getModX() * (letter[0].length + 1);
			bY += face.getModY() * (letter[0].length + 1);
			bZ += face.getModZ() * (letter[0].length + 1);
		}
		
		return changes;
	}

	private static void PopulateAlphabet()
	{
		alphabet.put('0', new int[][] {
				{1,1,1},
				{1,0,1},
				{1,0,1},
				{1,0,1},
				{1,1,1}
		});

		alphabet.put('1', new int[][] {
				{1,1,0},
				{0,1,0},
				{0,1,0},
				{0,1,0},
				{1,1,1}
		});

		alphabet.put('2', new int[][] {
				{1,1,1},
				{0,0,1},
				{1,1,1},
				{1,0,0},
				{1,1,1}
		});

		alphabet.put('3', new int[][] {
				{1,1,1},
				{0,0,1},
				{1,1,1},
				{0,0,1},
				{1,1,1}
		});

		alphabet.put('4', new int[][] {
				{1,0,1},
				{1,0,1},
				{1,1,1},
				{0,0,1},
				{0,0,1}
		});

		alphabet.put('5', new int[][] {
				{1,1,1},
				{1,0,0},
				{1,1,1},
				{0,0,1},
				{1,1,1}
		});

		alphabet.put('6', new int[][] {
				{1,1,1},
				{1,0,0},
				{1,1,1},
				{1,0,1},
				{1,1,1}
		});

		alphabet.put('7', new int[][] {
				{1,1,1},
				{0,0,1},
				{0,0,1},
				{0,0,1},
				{0,0,1}
		});

		alphabet.put('8', new int[][] {
				{1,1,1},
				{1,0,1},
				{1,1,1},
				{1,0,1},
				{1,1,1}
		});

		alphabet.put('9', new int[][] {
				{1,1,1},
				{1,0,1},
				{1,1,1},
				{0,0,1},
				{1,1,1}
		});

		alphabet.put('.', new int[][] {
				{0},
				{0},
				{0},
				{0},
				{1}
		});

		alphabet.put('!', new int[][] {
				{1},
				{1},
				{1},
				{0},
				{1}
		});

		alphabet.put(' ', new int[][] {
				{0,0},
				{0,0},
				{0,0},
				{0,0},
				{0,0}
		});

		alphabet.put('a', new int[][] {
				{1,1,1,1},
				{1,0,0,1},
				{1,1,1,1},
				{1,0,0,1},
				{1,0,0,1}
		});

		alphabet.put('b', new int[][] {
				{1,1,1,0},
				{1,0,0,1},
				{1,1,1,0},
				{1,0,0,1},
				{1,1,1,0}
		});

		alphabet.put('c', new int[][] {
				{1,1,1,1},
				{1,0,0,0},
				{1,0,0,0},
				{1,0,0,0},
				{1,1,1,1}
		});

		alphabet.put('d', new int[][] {
				{1,1,1,0},
				{1,0,0,1},
				{1,0,0,1},
				{1,0,0,1},
				{1,1,1,0}
		});

		alphabet.put('e', new int[][] {
				{1,1,1,1},
				{1,0,0,0},
				{1,1,1,0},
				{1,0,0,0},
				{1,1,1,1}
		});

		alphabet.put('f', new int[][] {
				{1,1,1,1},
				{1,0,0,0},
				{1,1,1,0},
				{1,0,0,0},
				{1,0,0,0}
		});

		alphabet.put('g', new int[][] {
				{1,1,1,1},
				{1,0,0,0},
				{1,0,1,1},
				{1,0,0,1},
				{1,1,1,1}
		});

		alphabet.put('h', new int[][] {
				{1,0,0,1},
				{1,0,0,1},
				{1,1,1,1},
				{1,0,0,1},
				{1,0,0,1}
		});

		alphabet.put('i', new int[][] {
				{1,1,1},
				{0,1,0},
				{0,1,0},
				{0,1,0},
				{1,1,1}
		});

		alphabet.put('j', new int[][] {
				{1,1,1,1},
				{0,0,1,0},
				{0,0,1,0},
				{1,0,1,0},
				{1,1,1,0}
		});

		alphabet.put('k', new int[][] {
				{1,0,0,1},
				{1,0,1,0},
				{1,1,0,0},
				{1,0,1,0},
				{1,0,0,1}
		});

		alphabet.put('l', new int[][] {
				{1,0,0,0},
				{1,0,0,0},
				{1,0,0,0},
				{1,0,0,0},
				{1,1,1,1}
		});

		alphabet.put('m', new int[][] {
				{1,1,1,1,1},
				{1,0,1,0,1},
				{1,0,1,0,1},
				{1,0,0,0,1},
				{1,0,0,0,1}
		});

		alphabet.put('n', new int[][] {
				{1,0,0,1},
				{1,1,0,1},
				{1,0,1,1},
				{1,0,0,1},
				{1,0,0,1}
		});

		alphabet.put('o', new int[][] {
				{1,1,1,1},
				{1,0,0,1},
				{1,0,0,1},
				{1,0,0,1},
				{1,1,1,1}
		});

		alphabet.put('p', new int[][] {
				{1,1,1,1},
				{1,0,0,1},
				{1,1,1,1},
				{1,0,0,0},
				{1,0,0,0}
		});

		alphabet.put('q', new int[][] {
				{1,1,1,1},
				{1,0,0,1},
				{1,0,0,1},
				{1,0,1,0},
				{1,1,0,1}
		});

		alphabet.put('r', new int[][] {
				{1,1,1,1},
				{1,0,0,1},
				{1,1,1,0},
				{1,0,0,1},
				{1,0,0,1}
		});

		alphabet.put('s', new int[][] {
				{1,1,1,1},
				{1,0,0,0},
				{1,1,1,1},
				{0,0,0,1},
				{1,1,1,1}
		});

		alphabet.put('t', new int[][] {
				{1,1,1,1,1},
				{0,0,1,0,0},
				{0,0,1,0,0},
				{0,0,1,0,0},
				{0,0,1,0,0}
		});

		alphabet.put('u', new int[][] {
				{1,0,0,1},
				{1,0,0,1},
				{1,0,0,1},
				{1,0,0,1},
				{1,1,1,1}
		});

		alphabet.put('v', new int[][] {
				{1,0,0,1},
				{1,0,0,1},
				{1,0,0,1},
				{1,0,0,1},
				{0,1,1,0}
		});

		alphabet.put('w', new int[][] {
				{1,0,0,0,1},
				{1,0,0,0,1},
				{1,0,1,0,1},
				{1,0,1,0,1},
				{1,1,1,1,1}
		});

		alphabet.put('x', new int[][] {
				{1,0,0,1},
				{1,0,0,1},
				{0,1,1,0},
				{1,0,0,1},
				{1,0,0,1}
		});

		alphabet.put('y', new int[][] {
				{1,0,0,1},
				{1,0,0,1},
				{1,1,1,1},
				{0,0,0,1},
				{1,1,1,1}
		});

		alphabet.put('z', new int[][] {
				{1,1,1,1},
				{0,0,0,1},
				{0,0,1,0},
				{0,1,0,0},
				{1,1,1,1}
		});

		alphabet.put(':', new int[][]
				{
						{0},
						{1},
						{0},
						{1},
						{0}
				});

		alphabet.put('=', new int[][]
				{
						{0,0,0},
						{1,1,1},
						{0,0,0},
						{1,1,1},
						{0,0,0}
				});

		// THIS IS A SINGLE SPACE
		alphabet.put('|', new int[][]
				{
						{0},
						{0},
						{0},
						{0},
						{0}
				});
	}
}
