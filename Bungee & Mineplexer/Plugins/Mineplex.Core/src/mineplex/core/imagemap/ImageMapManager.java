package mineplex.core.imagemap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.imagemap.objects.MapImage;
import mineplex.core.imagemap.objects.PlayerMapBoard;
import mineplex.core.imagemap.objects.PlayerMapImage;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

@ReflectivelyCreateMiniPlugin
public class ImageMapManager extends MiniPlugin
{

	private static final String IMAGE_DIR = ".." + File.separator + ".." + File.separator + "update" + File.separator + "files";
	private static final int PIXELS_PER_MAP = 128;

	public static int getMapDimension()
	{
		return PIXELS_PER_MAP;
	}

	private final CustomItemFrames _itemFrames;

	private final Map<String, BufferedImage> _imageCache;
	private List<PlayerMapBoard> _boards;

	private ImageMapManager()
	{
		super("Image Map");

		_itemFrames = require(CustomItemFrames.class);

		_imageCache = new HashMap<>();
		_boards = new ArrayList<>();
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		runSyncLater(() -> _boards.forEach(board ->
		{
			if (board.isHandleJoin())
			{
				board.onPlayerJoin(player);
			}
		}), 40);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_boards.forEach(board -> board.onPlayerQuit(player));
	}

	@EventHandler
	public void refreshBoards(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC)
		{
			return;
		}

		_boards.forEach(PlayerMapBoard::onRefresh);
	}

	public MapImage createBoard(Location topLeft, BlockFace direction, int width, int height, String image)
	{
		BufferedImage bufferedImage = getImage(new File(IMAGE_DIR + File.separator + image));
		return createBoard(topLeft, direction, width, height, bufferedImage);
	}

	public MapImage createBoard(Location topLeft, BlockFace direction, int width, int height, File image)
	{
		BufferedImage bufferedImage = getImage(image);
		return createBoard(topLeft, direction, width, height, bufferedImage);
	}

	public MapImage createBoard(Location topLeft, BlockFace direction, int width, int height, URL image)
	{
		BufferedImage bufferedImage = getImage(image);
		return createBoard(topLeft, direction, width, height, bufferedImage);
	}

	public MapImage createBoard(Location topLeft, BlockFace direction, int width, int height, BufferedImage image)
	{
		List<ItemFrame> itemFrames = createItemFrames(topLeft, direction, width, height);
		MapImage mapImage = new MapImage(image, itemFrames, width, height);
		mapImage.create();

		return mapImage;
	}

	public PlayerMapBoard createPlayerBoard(Location topLeft, BlockFace direction, int width, int height, String... images)
	{
		return createPlayerBoard(topLeft, direction, width, height, true, images);
	}

	public PlayerMapBoard createPlayerBoard(Location topLeft, BlockFace direction, int width, int height, boolean handleJoin, String... images)
	{
		BufferedImage[] bufferedImages = new BufferedImage[images.length];

		for (int i = 0; i < images.length; i++)
		{
			bufferedImages[i] = getImage(new File(IMAGE_DIR + File.separator + images[i]));
		}

		return createPlayerBoard(topLeft, direction, width, height, handleJoin, bufferedImages);
	}

	public PlayerMapBoard createPlayerBoard(Location topLeft, BlockFace direction, int width, int height, File... images)
	{
		return createPlayerBoard(topLeft, direction, width, height, true, images);
	}

	public PlayerMapBoard createPlayerBoard(Location topLeft, BlockFace direction, int width, int height, boolean handleJoin, File... images)
	{
		BufferedImage[] bufferedImages = new BufferedImage[images.length];

		for (int i = 0; i < images.length; i++)
		{
			bufferedImages[i] = getImage(images[i]);
		}

		return createPlayerBoard(topLeft, direction, width, height, handleJoin, bufferedImages);
	}

	public PlayerMapBoard createPlayerBoard(Location topLeft, BlockFace direction, int width, int height, URL... images)
	{
		return createPlayerBoard(topLeft, direction, width, height, true, images);
	}

	public PlayerMapBoard createPlayerBoard(Location topLeft, BlockFace direction, int width, int height, boolean handleJoin, URL... images)
	{
		BufferedImage[] bufferedImages = new BufferedImage[images.length];

		for (int i = 0; i < images.length; i++)
		{
			bufferedImages[i] = getImage(images[i]);
		}

		return createPlayerBoard(topLeft, direction, width, height, handleJoin, bufferedImages);
	}

	public PlayerMapBoard createPlayerBoard(Location topLeft, BlockFace direction, int width, int height, boolean handleJoin, BufferedImage... images)
	{
		List<ItemFrame> itemFrames = createItemFrames(topLeft, direction, width, height);
		List<PlayerMapImage> mapImageList = new ArrayList<>();

		for (BufferedImage image : images)
		{
			PlayerMapImage mapImage = new PlayerMapImage(_itemFrames, image, itemFrames, width, height);
			mapImage.create();

			mapImageList.add(mapImage);
		}

		PlayerMapBoard board = new PlayerMapBoard(topLeft, mapImageList, handleJoin);

		if (handleJoin)
		{
			runSyncLater(() -> Bukkit.getOnlinePlayers().forEach(board::onPlayerJoin), 50);
		}

		_boards.add(board);
		return board;
	}

	private List<ItemFrame> createItemFrames(Location topLeft, BlockFace direction, int width, int height)
	{
		List<ItemFrame> itemFrames = new ArrayList<>();

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Location location = modLocation(topLeft, direction, x, y);

				Block opposite = location.getBlock().getRelative(direction.getOppositeFace());

				if (!UtilBlock.solid(opposite))
				{
					opposite.setType(Material.SMOOTH_BRICK);
				}

				ItemFrame itemFrame = location.getWorld().spawn(location, ItemFrame.class);
				itemFrame.setFacingDirection(direction, true);
				itemFrames.add(itemFrame);
			}
		}

		return itemFrames;
	}

	private Location modLocation(Location location, BlockFace direction, int x, int y)
	{
		int modX = 0;
		int modZ = 0;

		switch (direction)
		{
			case NORTH:
				modX = -x;
				break;
			case SOUTH:
				modX = x;
				break;
			case WEST:
				modZ = x;
				break;
			case EAST:
				modZ = -x;
				break;
		}

		return new Location(location.getWorld(), location.getBlockX() + modX, location.getBlockY() - y, location.getBlockZ() + modZ);
	}

	private BufferedImage getImage(File file)
	{
		if (_imageCache.containsKey(file.getName()))
		{
			return _imageCache.get(file.getName());
		}

		try
		{
			BufferedImage image = ImageIO.read(file);
			_imageCache.put(file.getName(), image);
			return image;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private BufferedImage getImage(URL url)
	{
		if (_imageCache.containsKey(url.toString()))
		{
			return _imageCache.get(url.toString());
		}

		try
		{
			BufferedImage image = ImageIO.read(url);
			_imageCache.put(url.toString(), image);
			return image;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void cleanupBoard(PlayerMapBoard board)
	{
		_boards.remove(board);

		//TODO Fix when multiple boards are needed at one time
		File dataDir = new File(board.getLocation().getWorld().getName() + File.separator + "data");
		if (dataDir.isDirectory())
		{
			File[] files = dataDir.listFiles();

			if (files != null)
			{
				for (File file : files)
				{
					if (file.getName().startsWith("map"))
					{
						file.delete();
					}
				}
			}
		}

		board.cleanup();
	}
}
