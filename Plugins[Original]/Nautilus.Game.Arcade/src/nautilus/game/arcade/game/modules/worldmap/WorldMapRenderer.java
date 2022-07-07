package nautilus.game.arcade.game.modules.worldmap;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.game.Game;

public abstract class WorldMapRenderer<T extends Game> extends MapRenderer
{

	private static final long DRAW_RATE = TimeUnit.SECONDS.toMillis(4);

	protected final T _game;
	protected WorldMapModule _manager;

	public WorldMapRenderer(T game)
	{
		_game = game;
	}

	public void setManager(WorldMapModule manager)
	{
		_manager = manager;
	}

	@Override
	public void render(MapView mapView, MapCanvas canvas, Player player)
	{
		preRender(player);

		int zoom = _manager.getScale();
		Byte[][] map = _manager.getMap(zoom);

		int centerX = 0;
		int centerZ = 0;

		if (Recharge.Instance.use(player, "Draw Map", DRAW_RATE, false, false))
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

					if (!(pixelX < 0 || pixelZ < 0 || pixelX >= map.length || pixelZ >= map.length) && map[pixelX][pixelZ] != null)
					{
						color = map[pixelX][pixelZ];

						blockX *= zoom;
						blockZ *= zoom;

						color = renderBlock(player, color, mapX, mapZ, blockX, blockZ);
					}
					else
					{
						color = (byte) 0;
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

		renderCursors(canvas, player);
	}

	protected void addCursor(MapCanvas canvas, Location location, MapCursor.Type type)
	{
		MapCursorCollection cursors = canvas.getCursors();
		int zoom = _manager.getScale();
		double mapX = (location.getX() - _manager.getX()) / zoom;
		double mapZ = (location.getZ() - _manager.getZ()) / zoom;

		if (mapX > -64 && mapX < 64 && mapZ > -64 && mapZ < 64)
		{
			byte b0 = (byte) (int) Math.min(127, mapX * 2F + 0.5D);
			byte b1 = (byte) (int) Math.max(-127, mapZ * 2F + 0.5D);
			byte rotation = (byte) (int) (location.getYaw() * 16D / 360D);

			MapCursor cursor = new MapCursor(b0, b1, (byte) (rotation & 0xF), type.getValue(), true);

			cursors.addCursor(cursor);
		}
	}

	public abstract void renderTick();

	protected abstract void preRender(Player player);

	protected abstract byte renderBlock(Player player, byte color, int mapX, int mapZ, int blockX, int blockZ);

	protected abstract void renderCursors(MapCanvas canvas, Player player);
}
