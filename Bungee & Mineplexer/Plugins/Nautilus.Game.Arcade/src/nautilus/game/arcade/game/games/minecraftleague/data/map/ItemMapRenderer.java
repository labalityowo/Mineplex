package nautilus.game.arcade.game.games.minecraftleague.data.map;

import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.game.games.minecraftleague.data.MapZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ItemMapRenderer extends MapRenderer
{
	private ItemMapManager _manager;
	private MinecraftLeague _host;

	public ItemMapRenderer(ItemMapManager itemMapManager, MinecraftLeague host)
	{
		super(true);

		_manager = itemMapManager;
		_host = host;
	}

	@Override
	public void render(MapView mapView, MapCanvas canvas, Player player)
	{
		int zoom = _manager.getScale();

		Byte[][] map = _manager.getMap(zoom);

		int centerX = 0;
		int centerZ = 0;

		// We have this cooldown to squeeze out every single bit of performance from the server.
		if (Recharge.Instance.use(player, "Draw Map", 4000, false, false))
		{
			for (int mapX = 0; mapX < 128; mapX++)
			{
				for (int mapZ = 0; mapZ < 128; mapZ++)
				{
					int blockX = centerX + (mapX - 64);
					int blockZ = centerZ + (mapZ - 64);

					int pixelX = blockX + (map.length / 2);
					int pixelZ = blockZ + (map.length / 2);

					Byte color;

					if (!(pixelX < 0 || pixelZ < 0 || pixelX >= map.length || pixelZ >= map.length)
							&& map[pixelX][pixelZ] != null)
					{
						color = map[pixelX][pixelZ];

						blockX *= zoom;
						blockZ *= zoom;
					}
					else
					{
						color = (byte) 0;
					}

					for (MapZone od : _host.MapZones)
					{
						if (od.isInRadius(blockX, blockZ)) // TODO Some math to figure out if this pixel is going to be colored in for the circle or not.
						{
							if (od.isValid())
								color = MapPalette.matchColor(od.getRed(), od.getGreen(), od.getBlue());
						}
						
						/*if (od.isBase(blockX - 20, blockZ + 15))
						{
							color = MapPalette.matchColor(Color.PINK);
							Bukkit.broadcastMessage("X:" + blockX + " Z:" + blockZ + "/" + od.getBase());
						}*/
					}

					canvas.setPixel(mapX, mapZ, color);
				}
			}

			player.sendMap(mapView);
		}

		MapCursorCollection cursors = canvas.getCursors();

		while (cursors.size() > 0)

		{
			cursors.removeCursor(cursors.getCursor(0));
		}

		// TODO If you want players to see each other as cursors. Otherwise delete this bit.
		for (

		Player other : Bukkit.getOnlinePlayers())

		{
			if (player.canSee(other) && other.isValid())
			{
				Location l = other.getLocation();

				double mapX = (l.getX() - _manager.getX()) / zoom;
				double mapZ = (l.getZ() - _manager.getZ()) / zoom;

				if (mapX > -64 && mapX < 64 && mapZ > -64 && mapZ < 64)
				{
					MapCursor.Type cursorDisplay;
					MapCursor.Type friend;
					MapCursor.Type foe;
					
					if (_host.GetTeam(player).GetColor() == ChatColor.RED)
					{
						friend = MapCursor.Type.RED_POINTER;
						foe = MapCursor.Type.BLUE_POINTER;
					}
					else
					{
						friend = MapCursor.Type.BLUE_POINTER;
						foe = MapCursor.Type.RED_POINTER;
					}

					if (player == other)
					{
						cursorDisplay = MapCursor.Type.WHITE_POINTER;
					}
					else if (_host.GetTeam(player) == _host.GetTeam(other))
					{
						cursorDisplay = friend;
					}
					else if (_host.GetTeam(player) != _host.GetTeam(other))
					{
						if (_host.OverTime)
							cursorDisplay = foe;
						else
							continue;
					}
					else
					{
						continue;
					}

					byte b0 = (byte) (int) Math.min(127, (double) (mapX * 2.0F) + 0.5D);
					byte b1 = (byte) (int) Math.max(-127, (double) (mapZ * 2.0F) + 0.5D);

					byte rotation = (byte) (int) ((l.getYaw() * 16D) / 360D);

					MapCursor cursor = new MapCursor(b0, b1, (byte) (rotation & 0xF), cursorDisplay.getValue(), true);

					cursors.addCursor(cursor);
				}
			}
		}
	}
}