package mineplex.core.map;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

class ImageMapRenderer extends MapRenderer
{
	private BufferedImage _image;
	private boolean _first = true;

	ImageMapRenderer(BufferedImage image)
	{
		_image = image;
	}

	@Override
	public void render(MapView view, MapCanvas canvas, Player player)
	{
		if (_image != null && _first)
		{
			canvas.drawImage(0, 0, _image);

			_first = false;
		}
	}
}