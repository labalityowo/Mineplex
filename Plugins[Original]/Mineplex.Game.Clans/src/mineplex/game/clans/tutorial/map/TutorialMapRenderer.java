package mineplex.game.clans.tutorial.map;

import java.awt.*;
import java.util.List;

import mineplex.core.recharge.Recharge;
import mineplex.game.clans.tutorial.TutorialManager;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.TutorialType;
import mineplex.game.clans.tutorial.TutorialWorldManager;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class TutorialMapRenderer extends MapRenderer
{
	private ClansMainTutorial _tutorial;
	private int _minX;
	private int _minZ;

	public TutorialMapRenderer(ClansMainTutorial tutorialManager, int minX, int minZ)
	{
		super(true);

		_tutorial = tutorialManager;
		_minX = minX;
		_minZ = minZ;
	}

	@Override
	public void render(MapView mapView, MapCanvas canvas, Player player)
	{
		TutorialMapManager tutorialMap = _tutorial.getMapManager();
		int zoom = tutorialMap.getScale();

		Byte[][] map = tutorialMap.getMap(zoom);

		int centerX = 0;
		int centerZ = 0;

		// We have this cooldown to squeeze out every single bit of performance from the server.
		if (Recharge.Instance.use(player, "Draw Map", 4000, false, false))
		{
			TutorialRegion region = _tutorial.getWorldManager().getCenterRegion();
			MinMaxArea shops = getArea(region, ClansMainTutorial.Bounds.SHOPS);
			MinMaxArea fields = getArea(region, ClansMainTutorial.Bounds.FIELDS);
			MinMaxArea spawn = getArea(region, ClansMainTutorial.Bounds.SPAWN);
			MinMaxArea claim = getArea(region, ClansMainTutorial.Bounds.LAND_CLAIM);
			MinMaxArea enemy = getArea(region, ClansMainTutorial.Bounds.ENEMY_LAND);

			for (int mapX = 0; mapX < 128; mapX++)
			{
				for (int mapZ = 0; mapZ < 128; mapZ++)
				{
					int blockX = centerX + (mapX - 64);
					int blockZ = centerZ + (mapZ - 64);

					int pixelX = blockX + (map.length / 2);
					int pixelZ = blockZ + (map.length / 2);

					int xRegion = mapX + _minX - 6;
					int zRegion = mapZ + _minZ;

					Byte color = 0;


					if (!(pixelX < 0 || pixelZ < 0 || pixelX >= map.length || pixelZ >= map.length)
							&& map[pixelX][pixelZ] != null)
					{
						color = map[pixelX][pixelZ];

						if (!((color <= -113 || color >= 0) && color <= 127))
							color = (byte) 0;

						blockX *= zoom;
						blockZ *= zoom;

						color = attemptDraw(false, color, Color.WHITE, new Color(50, 150, 255), xRegion, zRegion, shops, mapX, mapZ);
						color = attemptDraw(false, color, Color.WHITE, new Color(255, 120, 0), xRegion, zRegion, fields, mapX, mapZ);
						color = attemptDraw(false, color, Color.WHITE, new Color(0, 255, 100), xRegion, zRegion, spawn, mapX, mapZ);
						color = attemptDraw(true, color, Color.CYAN, null, xRegion, zRegion, claim, mapX, mapZ);
						color = attemptDraw(true, color, Color.RED, null, xRegion, zRegion, enemy, mapX, mapZ);
					}

					/* TODO Figure out if you want to colorize this pixel
					{
						color = MapPalette.matchColor(r, g, b);
					}*/

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

		Location l = player.getLocation().clone();
		l.setX((((int)l.getX()) % TutorialWorldManager.BLOCKS_BETWEEN_TUTORIALS) + (l.getX() - ((int) l.getX())));
		l.setZ((((int)l.getZ()) % TutorialWorldManager.BLOCKS_BETWEEN_TUTORIALS) + (l.getZ() - ((int) l.getZ())));

		double mapX = (l.getX() - tutorialMap.getX()) / zoom;
		double mapZ = (l.getZ() - tutorialMap.getZ()) / zoom;

		if (mapX > -64 && mapX < 64 && mapZ > -64 && mapZ < 64)
		{
			byte b0 = (byte) (int) Math.min(127, (double) (mapX * 2.0F) + 0.5D);
			byte b1 = (byte) (int) Math.max(-127, (double) (mapZ * 2.0F) + 0.5D);

			byte rotation = (byte) (int) ((l.getYaw() * 16D) / 360D);

			MapCursor cursor = new MapCursor(b0, b1, (byte) (rotation & 0xF), MapCursor.Type.WHITE_POINTER.getValue(), true);

			cursors.addCursor(cursor);
		}

		// Add Tutorial Markers
		TutorialSession session = _tutorial.getTutorialSession(player);
		if (session != null)
		{
			if (session.getMapTargetLocation() != null)
			{
				Location point = session.getMapTargetLocation().clone();
				point.setX((((int)point.getX()) % TutorialWorldManager.BLOCKS_BETWEEN_TUTORIALS) + (point.getX() - ((int) point.getX())));
				point.setZ((((int)point.getZ()) % TutorialWorldManager.BLOCKS_BETWEEN_TUTORIALS) + (point.getZ() - ((int) point.getZ())));
				mapX = (point.getX() - tutorialMap.getX()) / zoom;
				mapZ = (point.getZ() - tutorialMap.getZ()) / zoom;

				// To make these appear at the edges of the map, just change it from 64 to something like 128 for double the map size
				if (mapX > -64 && mapX < 64 && mapZ > -64 && mapZ < 64)
				{
					byte b0 = (byte) (int) Math.min(127, (double) (mapX * 2.0F) + 0.5D);
					byte b1 = (byte) (int) Math.max(-127, (double) (mapZ * 2.0F) + 0.5D);

					byte cursorType = 4; // http://i.imgur.com/wpH6PT8.png
					// Those are byte 5 and 6
					byte rotation = (byte) ((int) Math.floor(System.currentTimeMillis() / 1000D) % 16);

					MapCursor cursor = new MapCursor(b0, b1, rotation, cursorType, true);

					cursors.addCursor(cursor);
				}
			}
		}
	}

	private byte attemptDraw(boolean colorAll, byte color, Color color1, Color color2, int xRegion, int zRegion, MinMaxArea area, int mapX, int mapZ)
	{
		if (xRegion >= area.MinX && xRegion <= area.MaxX && zRegion >= area.MinZ && zRegion <= area.MaxZ)
		{
			if (xRegion == area.MinX || xRegion == area.MaxX || zRegion == area.MinZ || zRegion == area.MaxZ)
			{
				// Outer Ring
				Color cColor = MapPalette.getColor(color);
				double clans = colorAll ? 1 : 0.8;// 0.65;

				//Use clanColor2 no matter what for admins
				Color drawColor = color1;
				if (color2 != null)
				{
					drawColor = color2;
					clans = 1;
				}

				double base = 1 - clans;

				int r = (int) ((cColor.getRed() * base) + (drawColor.getRed() * clans));
				int b = (int) ((cColor.getBlue() * base) + (drawColor.getBlue() * clans));
				int g = (int) ((cColor.getGreen() * base) + (drawColor.getGreen() * clans));

				color = MapPalette.matchColor(r, g, b);
			}
			else
			{
				// Inside
				Color cColor = MapPalette.getColor(color);

				double clans = 0.065;

				//Stripes
				boolean checker = (mapX + (mapZ % 4)) % 4 == 0;
				Color drawColor = color1;
//				if (colorAll)
//				{
//					clans = 0.8;
//				}
				if (checker && color2 != null && !colorAll)
				{
					drawColor = color2;
					clans = 1;
				}

				double base = 1 - clans;

				if (clans != 1 && (color == 0 || color == 1 || color == 2 || color == 3))
					return color;

				int r = (int) ((cColor.getRed() * base) + (drawColor.getRed() * clans));
				int b = (int) ((cColor.getBlue() * base) + (drawColor.getBlue() * clans));
				int g = (int) ((cColor.getGreen() * base) + (drawColor.getGreen() * clans));

				color = MapPalette.matchColor(r, g, b);
			}
		}

		return color;
	}

	private MinMaxArea getArea(TutorialRegion region, ClansMainTutorial.Bounds bounds)
	{
		MinMaxArea area = new MinMaxArea();
		List<Location> shopLocations = region.getLocationMap().getGoldLocations(bounds.getDataLocColor());
		area.MinX = Math.min(shopLocations.get(0).getBlockX(), shopLocations.get(1).getBlockX());
		area.MaxX = Math.max(shopLocations.get(0).getBlockX(), shopLocations.get(1).getBlockX());
		area.MinZ = Math.min(shopLocations.get(0).getBlockZ(), shopLocations.get(1).getBlockZ());
		area.MaxZ = Math.max(shopLocations.get(0).getBlockZ(), shopLocations.get(1).getBlockZ());

		return area;
	}

	private class MinMaxArea
	{
		public int MinX;
		public int MaxX;
		public int MinZ;
		public int MaxZ;
	}
}
