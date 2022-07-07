package mineplex.core.imagemap.objects;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.imagemap.CustomItemFrames;

public class PlayerMapImage extends MapImage
{

	private final CustomItemFrames _itemFramesManager;
	private final List<ItemStack> _itemMaps;

	public PlayerMapImage(CustomItemFrames itemFramesManager, BufferedImage image, List<ItemFrame> itemFrames, int width, int height)
	{
		super(image, itemFrames, width, height);

		_itemFramesManager = itemFramesManager;
		_itemMaps = new ArrayList<>();
	}

	@Override
	public void create()
	{
		World world = _itemFrames.get(0).getWorld();

		for (int x = 0; x < _width; x++)
		{
			for (int y = 0; y < _height; y++)
			{
				_itemMaps.add(getMapItem(world, x, y));
			}
		}
	}

	public List<ItemFrame> getItemFrames()
	{
		return _itemFrames;
	}

	public void addViewer(Player player, boolean sendMap)
	{
		if (sendMap)
		{
//			//FIXME
//			int slot = 8;
//			for (ItemStack itemStack : _itemMaps)
//			{
//				player.getInventory().setItem(slot++, itemStack);
//			}
//
//			UtilServer.runSyncLater(() -> player.getInventory().removeItem(_itemMaps.toArray(new ItemStack[0])), 5);

			_mapViews.forEach(player::sendMap);
		}

		for (int i = 0; i < _itemMaps.size(); i++)
		{
			ItemFrame itemFrame = _itemFrames.get(i);
			ItemStack itemStack = _itemMaps.get(i);

			_itemFramesManager.setItem(player, itemFrame, itemStack);
		}
	}
}
