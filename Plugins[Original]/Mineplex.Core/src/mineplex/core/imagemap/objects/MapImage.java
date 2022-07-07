package mineplex.core.imagemap.objects;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import mineplex.core.imagemap.ImageMapRenderer;

public class MapImage
{

	private final BufferedImage _image;
	protected final List<ItemFrame> _itemFrames;
	protected final List<MapView> _mapViews;
	protected final int _width, _height;

	public MapImage(BufferedImage image, List<ItemFrame> itemFrames, int width, int height)
	{
		_image = image;
		_itemFrames = itemFrames;
		_mapViews = new ArrayList<>(itemFrames.size());
		_width = width;
		_height = height;
	}

	public void create()
	{
		int i = 0;

		for (int x = 0; x < _width; x++)
		{
			for (int y = 0; y < _height; y++)
			{
				ItemFrame frame = _itemFrames.get(i++);

				frame.setItem(getMapItem(frame.getWorld(), x, y));
			}
		}
	}

	protected ItemStack getMapItem(World world, int x, int y)
	{
		ItemStack item = new ItemStack(Material.MAP);

		MapView map = Bukkit.getServer().createMap(world);
		map.getRenderers().forEach(map::removeRenderer);

		map.addRenderer(new ImageMapRenderer(_image, x, y));
		item.setDurability(map.getId());
		_mapViews.add(map);

		return item;
	}
}