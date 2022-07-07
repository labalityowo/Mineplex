package mineplex.gemhunters.map;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilTime;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.safezone.SafezoneModule;
import mineplex.gemhunters.supplydrop.SupplyDrop;
import mineplex.gemhunters.supplydrop.SupplyDropModule;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

import java.awt.*;
import java.util.Set;
import java.util.UUID;

/**
 * <b>All item map code was adapted from Clans.</b><br>
 */
public class ItemMapRenderer extends MapRenderer
{

	private static final int RENDER_COOLDOWN = 10000;
	private static final int STANDARD_Y = 70;

	private final ItemMapModule _itemMap;
	private final EconomyModule _economy;
	private final LootModule _loot;
	private final SafezoneModule _safezone;
	private final SupplyDropModule _supply;
	private final WorldEventModule _worldEvent;

	private final PartyManager _party;

	public ItemMapRenderer()
	{
		super(true);

		_itemMap = Managers.require(ItemMapModule.class);
		_economy = Managers.require(EconomyModule.class);
		_loot = Managers.require(LootModule.class);
		_safezone = Managers.require(SafezoneModule.class);
		_supply = Managers.require(SupplyDropModule.class);
		_worldEvent = Managers.require(WorldEventModule.class);
		_party = Managers.require(PartyManager.class);
	}

	@Override
	public void render(MapView mapView, MapCanvas canvas, Player player)
	{
		try
		{
			renderNormalMap(mapView, canvas, player);
		}
		catch (Throwable t)
		{
			System.out.println("Error while rendering map");
			t.printStackTrace();
		}
	}

	private void renderNormalMap(MapView mapView, MapCanvas canvas, Player player)
	{
		MapInfo info = _itemMap.getMap(player);

		if (info == null)
		{
			return;
		}

		int scale = info.getScale();
		int zoom = _itemMap.getZoom(scale);

		Byte[][] map = _itemMap.getMap(scale);

		int centerX = info.getX() / zoom;
		int centerZ = info.getZ() / zoom;

		// We have this cooldown to squeeze out every single bit of performance
		// from the server.
		if (UtilTime.elapsed(info.getLastRendered(), RENDER_COOLDOWN))
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

					if (!(pixelX < 0 || pixelZ < 0 || pixelX >= map.length || pixelZ >= map.length) && map[pixelX][pixelZ] != null)
					{
						color = map[pixelX][pixelZ];

						blockX *= zoom;
						blockZ *= zoom;

						Location location = new Location(mapView.getWorld(), blockX, STANDARD_Y, blockZ);

						boolean safezone = _safezone.isInSafeZone(location);

						if (safezone)
						{
							boolean colorAll = scale > 0;
							Color areaColor = Color.GREEN;

							if (areaColor != null)
							{
								if (!((color <= -113 || color >= 0) && color <= 127))
								{
									color = (byte) 0;
									System.out.println(String.format("Tried to draw invalid color %s, player: %s, mapX: %s, mapZ: %s", color, player.getName(), mapX, mapZ));
								}
								else
								{
									// int chunkBX = blockX & 0xF;
									// int chunkBZ = blockZ & 0xF;

									// Border
									if (_safezone.isInSafeZone(new Location(mapView.getWorld(), blockX - 1, STANDARD_Y, blockZ)) || _safezone.isInSafeZone(new Location(mapView.getWorld(), blockX, STANDARD_Y, blockZ - 1)) || _safezone.isInSafeZone(new Location(mapView.getWorld(), blockX + 16, STANDARD_Y, blockZ)) || _safezone.isInSafeZone(new Location(mapView.getWorld(), blockX, STANDARD_Y, blockZ + 1)))
									{
										Color cColor = MapPalette.getColor(color);
										double clans = colorAll ? 1 : 0.8;
										double base = 1 - clans;

										int r = (int) ((cColor.getRed() * base) + (areaColor.getRed() * clans));
										int b = (int) ((cColor.getBlue() * base) + (areaColor.getBlue() * clans));
										int g = (int) ((cColor.getGreen() * base) + (areaColor.getGreen() * clans));

										color = MapPalette.matchColor(r, g, b);
									}

									// Inside
									else
									{
										Color cColor = MapPalette.getColor(color);

										double clans = 0.065;

										// Stripes
										// boolean checker = (mapX + (mapZ % 4))
										// % 4 == 0;
										double base = 1 - clans;

										int r = (int) ((cColor.getRed() * base) + (areaColor.getRed() * clans));
										int b = (int) ((cColor.getBlue() * base) + (areaColor.getBlue() * clans));
										int g = (int) ((cColor.getGreen() * base) + (areaColor.getGreen() * clans));

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

		for (WorldEvent event : _worldEvent.getActiveEvents())
		{
			if (!event.isInProgress() || event.getEventLocations() == null)
			{
				continue;
			}

			for (Location point : event.getEventLocations())
			{
				double mapX = (point.getX() - info.getX()) / zoom;
				double mapZ = (point.getZ() - info.getZ()) / zoom;

				// To make these appear at the edges of the map, just change it
				// from
				// 64 to something like 128 for double the map size
				if (mapX > -64 && mapX < 64 && mapZ > -64 && mapZ < 64)
				{
					byte b0 = (byte) (int) Math.min(127, (double) (mapX * 2.0F) + 0.5D);
					byte b1 = (byte) (int) Math.max(-127, (double) (mapZ * 2.0F) + 0.5D);

					byte cursorType = 5; // http://i.imgur.com/wpH6PT8.png
					// Those are byte 5 and 6
					byte rotation = (byte) (int) ((point.getYaw() * 16D) / 360D);

					MapCursor cursor = new MapCursor(b0, b1, (byte) (rotation & 0xF), cursorType, true);

					cursors.addCursor(cursor);
				}
			}
		}

		SupplyDrop supplyDrop = _supply.getActive();

		if (_supply.isActive())
		{
			Location point = supplyDrop.getCurrentLocation();
			double mapX = (point.getX() - info.getX()) / zoom;
			double mapZ = (point.getZ() - info.getZ()) / zoom;

			// To make these appear at the edges of the map, just change it from
			// 64 to something like 128 for double the map size
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

		Party party = _party.getPartyByPlayer(player);
		Set<UUID> shownPlayers = _loot.getShownPlayers();

		for (Player other : Bukkit.getOnlinePlayers())
		{
			if (player.canSee(other) && other.isValid())
			{
				Location l = other.getLocation();

				double mapX = (l.getX() - info.getX()) / zoom;
				double mapZ = (l.getZ() - info.getZ()) / zoom;

				if (mapX > -64 && mapX < 64 && mapZ > -64 && mapZ < 64)
				{
					MapCursor.Type cursorDisplay = null;

					if (player.equals(other))
					{
						cursorDisplay = MapCursor.Type.WHITE_POINTER;
					}
					else if (shownPlayers.contains(other.getUniqueId()))
					{
						cursorDisplay = MapCursor.Type.BLUE_POINTER;
					}
					else if (party != null && party.isMember(other))
					{
						cursorDisplay = MapCursor.Type.GREEN_POINTER;
					}
					else if (other.equals(_economy.getMostValuablePlayer()))
					{
						cursorDisplay = MapCursor.Type.RED_POINTER;
					}

					if (cursorDisplay == null)
					{
						continue;
					}

					byte b0 = (byte) (int) Math.min(127, (mapX * 2.0F) + 0.5D);
					byte b1 = (byte) (int) Math.max(-127, (mapZ * 2.0F) + 0.5D);

					byte rotation = (byte) (int) ((l.getYaw() * 16D) / 360D);

					MapCursor cursor = new MapCursor(b0, b1, (byte) (rotation & 0xF), cursorDisplay.getValue(), true);

					cursors.addCursor(cursor);
				}
			}
		}
	}
}
