package mineplex.game.clans.core;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class ClaimLocation
{
	public final String _worldName;
	public final int _chunkX;
	public final int _chunkZ;

	private ClaimLocation(String worldName, int chunkX, int chunkZ)
	{
		_worldName = worldName;
		_chunkX = chunkX;
		_chunkZ = chunkZ;
	}

	public static ClaimLocation of(String worldName, int chunkX, int chunkZ)
	{
		return new ClaimLocation(worldName, chunkX, chunkZ);
	}

	public static ClaimLocation of(Chunk chunk)
	{
		return new ClaimLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public static ClaimLocation fromStoredString(String storedFormat) // TODO: change stored format for next season
	{
		// Current format: world,x,z
		String[] split = storedFormat.split(",");
		return new ClaimLocation(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
	}

	public String toStoredString() // TODO: change stored format for next season
	{
		return _worldName + "," + _chunkX + "," + _chunkZ;
	}

	public Chunk toChunk()
	{
		return Bukkit.getWorld(_worldName).getChunkAt(_chunkX, _chunkZ);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_worldName, _chunkX, _chunkZ);
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof ClaimLocation))
		{
			return false;
		}

		ClaimLocation that = (ClaimLocation) other;
		return Objects.equals(_worldName, that._worldName) && Objects.equals(_chunkX, that._chunkX) && Objects.equals(_chunkZ, that._chunkZ);
	}
}