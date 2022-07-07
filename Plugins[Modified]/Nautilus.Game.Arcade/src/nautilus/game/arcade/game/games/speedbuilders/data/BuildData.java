package nautilus.game.arcade.game.games.speedbuilders.data;

import java.util.ArrayList;

import mineplex.core.common.util.MapUtil;
import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.BlockStairs;
import net.minecraft.server.v1_8_R3.BlockStairs.EnumStairShape;
import net.minecraft.server.v1_8_R3.IBlockData;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Stairs;

public class BuildData
{
	public SpeedBuilders Game;

	public Location BuildMin;

	public BlockState[][][] Build;
	public BlockState[][] Ground;

	//Store stair shapes for stair fix
	public EnumStairShape[][][] StairShapes;

	public String BuildText;

	public ArrayList<MobData> Mobs = new ArrayList<MobData>();

	private int _timeAdd = 0;
	private int _timeSubtract = 0;
	private int _timeEqual = -1;
	private double _timeMultiplier = 1.0;

	public BuildData(Location loc, String buildText, SpeedBuilders game)
	{
		Build = new BlockState[game.BuildSize][game.BuildSize][game.BuildSize];
		Ground = new BlockState[game.BuildSize][game.BuildSize];
		
		StairShapes = new EnumStairShape[game.BuildSize][game.BuildSize][game.BuildSize];
		
		Game = game;
		
		Location groundMin = loc.clone().subtract(game.BuildSizeDiv2, 11, game.BuildSizeDiv2);
		
		for (int x = 0; x < game.BuildSize; x++)
		{
			for (int z = 0; z < game.BuildSize; z++)
			{
				Ground[x][z] = groundMin.clone().add(x, 0, z).getBlock().getState();
			}
		}

		parseText(buildText);

		Location buildMin = loc.clone().subtract(game.BuildSizeDiv2, 10, game.BuildSizeDiv2);
		
		BuildMin = buildMin;
		
		for (int x = 0; x < game.BuildSize; x++)
		{
			for (int y = 0; y < game.BuildSize; y++)
			{
				for (int z = 0; z < game.BuildSize; z++)
				{
					Block block = buildMin.clone().add(x, y, z).getBlock();
					
					if (block.getType() == Material.SIGN_POST)
					{
						Sign sign = (Sign) block.getState();
						
						EntityType type = null;
						
						try
						{
							type = EntityType.valueOf(sign.getLine(0).toUpperCase());
						}
						catch (IllegalArgumentException e)
						{
							// Not a entity sign or someone messed up...
						}
						
						if (type != null)
						{
							Mobs.add(new MobData(type, x, y, z));
							
							MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
						}
					}

					Build[x][y][z] = block.getState();
					
					if (block.getState().getData() instanceof Stairs)
					{
						net.minecraft.server.v1_8_R3.Block nmsBlock = CraftMagicNumbers.getBlock(block);
						
						IBlockData blockData = nmsBlock.getBlockData();
						blockData = nmsBlock.updateState(blockData, ((CraftWorld) block.getWorld()).getHandle(), new BlockPosition(block.getX(), block.getY(), block.getZ()));
						
						StairShapes[x][y][z] = blockData.get(BlockStairs.SHAPE);
					}
				}
			}
		}
	}

	private void parseText(String buildText)
	{
		StringBuilder sb = new StringBuilder();
		for (String part : buildText.split(" "))
		{
			if (part.matches("^time[\\Q+-=\\E][0-9]+$"))
			{
				// + - = add subtract or set seconds
				try
				{
					int num = Integer.parseInt(part.substring(5));
					switch (part.charAt(4))
					{
						case '-':
							_timeSubtract = num;
							break;
						case '=':
							_timeEqual = num;
							break;
						default:
							_timeAdd = num;
					}
				}
				catch (NumberFormatException e)
				{
					System.out.println("Failed parsing data for customloc: " + buildText);
					e.printStackTrace();
				}
			}
			else if (part.matches("^time\\*[0-9]*\\.?[0-9]+$"))
			{
				// * multiply by a number
				try
				{
					double num = Double.parseDouble(part.substring(5));
					_timeMultiplier = num;
				}
				catch (NumberFormatException e)
				{
					System.out.println("Failed parsing data for customloc: " + buildText);
					e.printStackTrace();
				}
			}
			else
			{
				sb.append(part + " ");
			}
		}

		BuildText = sb.toString().trim();
	}

	public int getBuildTime(int unmodified)
	{
		int newTime = unmodified;
		newTime += _timeAdd;
		newTime -= _timeSubtract;
		newTime = (int) (_timeMultiplier * newTime);
		if (_timeEqual != -1) newTime = _timeEqual;

		// limit to range of 5-60 seconds
		return Math.min(Math.max(newTime, 5), 60);
	}

	public int getPerfectScore()
	{
		int nonAirBlocks = 0;
		
		for (int x = 0; x < Game.BuildSize; x++)
		{
			for (int y = 0; y < Game.BuildSize; y++)
			{
				for (int z = 0; z < Game.BuildSize; z++)
				{
					if (Build[x][y][z].getType() != Material.AIR)
						nonAirBlocks++;
				}
			}
		}
		
		return nonAirBlocks + Mobs.size();
	}

}
