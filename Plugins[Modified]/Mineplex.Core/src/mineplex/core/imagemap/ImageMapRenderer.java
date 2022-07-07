package mineplex.core.imagemap;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageMapRenderer extends MapRenderer
{

	private Image _image;
	private boolean _rendered;

	public ImageMapRenderer(BufferedImage image, int x1, int y1)
	{
		int startX = Math.min(x1 * ImageMapManager.getMapDimension(), image.getWidth());
		startX = Math.max(image.getMinX(), startX);
		int startY = Math.min(y1 * ImageMapManager.getMapDimension(), image.getHeight());
		startY = Math.max(image.getMinY(), startY);

		if (startX + ImageMapManager.getMapDimension() > image.getWidth() || startY + ImageMapManager.getMapDimension() > image.getHeight())
		{
			return;
		}

		_image = image.getSubimage(startX, startY, ImageMapManager.getMapDimension(), ImageMapManager.getMapDimension());
	}

	@Override
	public void render(MapView view, MapCanvas canvas, Player player)
	{
		if (_image != null && !_rendered)
		{
			canvas.drawImage(0, 0, _image);
			_rendered = true;
		}
	}
}
