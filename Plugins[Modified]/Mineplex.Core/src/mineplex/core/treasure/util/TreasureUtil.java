package mineplex.core.treasure.util;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import mineplex.core.common.util.UtilBlock;

public class TreasureUtil
{

	public static void playChestOpen(Location location, boolean open)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();
		BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
		TileEntity tileEntity = world.getTileEntity(position);
		world.playBlockAction(position, tileEntity.w(), 1, open ? 1 : 0);
	}

	public static byte getChestFacing(float yaw)
	{
		BlockFace face = UtilBlock.getFace(yaw);

		switch (face)
		{
			case NORTH:
				return 3;
			case SOUTH:
				return 2;
			case WEST:
				return 5;
			case EAST:
				return 4;
		}

		return 0;
	}

	public static byte getPumpkinFacing(float yaw)
	{
		BlockFace face = UtilBlock.getFace(yaw);

		switch (face)
		{
			case NORTH:
				return 0;
			case SOUTH:
				return 2;
			case WEST:
				return 3;
			case EAST:
				return 1;
		}

		return 0;
	}
}
