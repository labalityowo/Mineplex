package mineplex.game.clans.clans.map;

import java.awt.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansUtility;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.api.EventState;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.tutorial.TutorialManager;

public class ItemMapRenderer extends MapRenderer
{
	private ItemMapManager _manager;
	private TutorialManager _tutorial;
	private WorldEventManager _eventManager;

	public ItemMapRenderer(ItemMapManager itemMapManager, WorldEventManager eventManager, TutorialManager tutorial)
	{
		super(true);

		_manager = itemMapManager;
		_tutorial = tutorial;
		_eventManager = eventManager;
	}

	@Override
	public void render(MapView mapView, MapCanvas canvas, Player player)
	{
//		if (_tutorial.inTutorial(player))
//		{
//			renderTutorialMap(mapView, canvas, player);
//		}
//		else
//		{
		try
		{
			renderNormalMap(mapView, canvas, player);
		}
		catch (Throwable t)
		{
			System.out.println("Error while rendering map");
			t.printStackTrace();
		}
//		}
	}

	private void renderTutorialMap(MapView mapView, MapCanvas canvas, Player player)
	{

	}


	private void renderNormalMap(MapView mapView, MapCanvas canvas, Player player)
	{
		MapInfo info = _manager.getMap(player);

		if (info == null)
		{
			return;
		}

		int scale = info.getScale();
		int zoom = _manager.getZoom(scale);

		ClanInfo clan = _manager.getClansUtility().getClanByPlayer(player);

		Byte[][] map = _manager.getMap(scale);

		int centerX = info.getX() / zoom;
		int centerZ = info.getZ() / zoom;

		// We have this cooldown to squeeze out every single bit of performance from the server.
		if (UtilTime.elapsed(info.getLastRendered(), 4000))
		{
			info.setLastRendered();

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

						ClaimLocation chunk = ClaimLocation.of("world", (int) Math.floor(blockX / 16D), (int) Math.floor(blockZ / 16D));

						ClanInfo owningClan = _manager.getClansUtility().getOwner(chunk);

						if (owningClan != null)
						{
							boolean colorAll = scale > 0;
							Color clanColor = null;
							Color clanColor2 = null;

							if (owningClan == clan)
							{
								clanColor = Color.CYAN;
							}
							else
							{
								ClansUtility.ClanRelation relation = _manager.getClansUtility().rel(clan, owningClan);

								if (owningClan.isAdmin())
								{
									colorAll = false;

									if (owningClan.getName().equals("Shops"))
									{
										clanColor = Color.WHITE;
										clanColor2 = new Color(50, 150, 255);

										if (_manager.getClansUtility().relPT(player, chunk) == ClansUtility.ClanRelation.SAFE)
											clanColor2 = new Color(50, 150, 255);
									}
									else if (owningClan.getName().equals("Spawn"))
									{
										clanColor = Color.WHITE;
										clanColor2 = new Color(0, 255, 100);
									}
									else if (owningClan.getName().equals("Fields"))
									{
										clanColor = Color.WHITE;
										clanColor2 = new Color(255, 120, 0);
									}
									else
									{
										clanColor = Color.WHITE;
										clanColor2 = Color.GRAY;
									}
								}
								else if (relation == ClansUtility.ClanRelation.WAR_LOSING)
								{
									clanColor = Color.RED;
								}
								else if (relation == ClansUtility.ClanRelation.WAR_WINNING)
								{
									clanColor = Color.MAGENTA;
								}
								else if (relation == ClansUtility.ClanRelation.ALLY || relation == ClansUtility.ClanRelation.ALLY_TRUST)
								{
									clanColor = Color.GREEN;
								}
								else if (relation == ClansUtility.ClanRelation.SELF)
								{
									clanColor = Color.CYAN;
								}
								else
								{
									// Neutral
									clanColor = Color.YELLOW;
								}
							}

							if (clanColor != null)
							{
								if(! ((color <= -113 || color >= 0) && color <= 127))
								{
									color = (byte) 0;
									System.out.println(String.format("Tried to draw invalid color %s, player: %s, mapX: %s, mapZ: %s",
											color, player.getName(), mapX, mapZ));
								}
								else
								{
									int chunkBX = blockX & 0xF;
									int chunkBZ = blockZ & 0xF;
									int chunkX1 = (int) Math.floor(blockX / 16D);
									int chunkZ1 = (int) Math.floor(blockZ / 16D);

									//Border
									if (colorAll ||

											((chunkBX == 0 || zoom == 13) &&

													owningClan != _manager.getClansUtility().getOwner(ClaimLocation.of("world", chunkX1 - 1, chunkZ1)))

											|| ((chunkBZ == 0 || zoom == 13) &&

											owningClan != _manager.getClansUtility().getOwner(ClaimLocation.of("world", chunkX1, chunkZ1 - 1)))

											|| ((chunkBX + zoom > 15 || zoom == 13) &&

											owningClan != _manager.getClansUtility().getOwner(ClaimLocation.of("world", chunkX1 + 1, chunkZ1)))

											|| ((chunkBZ + zoom > 15 || zoom == 13) &&

											owningClan != _manager.getClansUtility().getOwner(ClaimLocation.of("world", chunkX1, chunkZ1 + 1))))
									{
										Color cColor = MapPalette.getColor(color);
										double clans = colorAll ? 1 : 0.8;// 0.65;

										//Use clanColor2 no matter what for admins
										Color drawColor = clanColor;
										if (owningClan.isAdmin() && clanColor2 != null)
										{
											drawColor = clanColor2;
											clans = 1;
										}

										double base = 1 - clans;

										int r = (int) ((cColor.getRed() * base) + (drawColor.getRed() * clans));
										int b = (int) ((cColor.getBlue() * base) + (drawColor.getBlue() * clans));
										int g = (int) ((cColor.getGreen() * base) + (drawColor.getGreen() * clans));

										color = MapPalette.matchColor(r, g, b);
									}


									//Inside
									else
									{
										Color cColor = MapPalette.getColor(color);

										double clans = 0.065;

										//Stripes
										boolean checker = (mapX + (mapZ % 4)) % 4 == 0;
										Color drawColor = clanColor;
										if (checker && owningClan.isAdmin() && clanColor2 != null)
										{
											drawColor = clanColor2;
											clans = 1;
										}

										double base = 1 - clans;

										int r = (int) ((cColor.getRed() * base) + (drawColor.getRed() * clans));
										int b = (int) ((cColor.getBlue() * base) + (drawColor.getBlue() * clans));
										int g = (int) ((cColor.getGreen() * base) + (drawColor.getGreen() * clans));

										color = MapPalette.matchColor(r, g, b);
									}
								}
							}
						}
					}
					else
					{
						color = (byte) 0;
					}

					canvas.setPixel(mapX, mapZ, color);
				}
			}
		}

		if (info.isSendMap())
		{
			player.sendMap(mapView);
		}

		MapCursorCollection cursors = canvas.getCursors();

		while (cursors.size() > 0)
		{
			cursors.removeCursor(cursors.getCursor(0));
		}

		for (WorldEvent event : _eventManager.getEvents())
		{
			if (event.getState() != EventState.LIVE)
				continue;

			Location point = event.getCenterLocation();
			double mapX = (point.getX() - info.getX()) / zoom;
			double mapZ = (point.getZ() - info.getZ()) / zoom;

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

		for (Player other : Bukkit.getOnlinePlayers())
		{
			if (player.canSee(other) && other.isValid())
			{
				Location l = other.getLocation();

				double mapX = (l.getX() - info.getX()) / zoom;
				double mapZ = (l.getZ() - info.getZ()) / zoom;

				if (mapX > -64 && mapX < 64 && mapZ > -64 && mapZ < 64)
				{
					ClanInfo otherClan = _manager.getClansUtility().getClanByPlayer(other);

					MapCursor.Type cursorDisplay;

					if (player == other)
					{
						cursorDisplay = MapCursor.Type.WHITE_POINTER;
					}
					else if (otherClan == null || clan == null)
					{
						continue;
					}
					else if (otherClan == clan)
					{
						cursorDisplay = MapCursor.Type.BLUE_POINTER;
					}
					else if (otherClan.isAlly(clan.getName()))
					{
						cursorDisplay = MapCursor.Type.GREEN_POINTER;
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
