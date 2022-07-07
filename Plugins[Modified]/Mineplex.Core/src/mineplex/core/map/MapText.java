package mineplex.core.map;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapText
{
	private ArrayList<String> split(String text)
	{
		ArrayList<String> returns = new ArrayList<String>();
		int lineWidth = 0;
		String current = "";

		for (String word : text.split("(?<= )"))
		{
			int length = 0;

			for (char c : word.toCharArray())
			{
				length += UtilText.getImage(c).getWidth();
			}

			if (lineWidth + length >= 127)
			{
				lineWidth = 0;
				returns.add(current);
				current = "";
			}

			current += word;
			lineWidth += length;
		}

		returns.add(current);

		return returns;
	}

	public ItemStack getMap(boolean sendToServer, String... text)
	{
		BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		int height = 1;

		for (String string : text)
		{
			for (String line : split(string))
			{
				int length = 1;

				for (char c : line.toCharArray())
				{
					BufferedImage img = UtilText.getImage(c);

					g.drawImage(img, length, height, null);

					length += img.getWidth();
				}

				height += 8;
			}
		}

		MapView map = Bukkit.createMap(Bukkit.getWorlds().get(0));

		for (MapRenderer r : map.getRenderers())
		{
			map.removeRenderer(r);
		}

		map.addRenderer(new ImageMapRenderer(image));

		ItemStack item = new ItemStack(Material.MAP);

		item.setDurability(map.getId());

		if (sendToServer)
		{
			for (Player player : UtilServer.getPlayers())
				player.sendMap(map);
		}

		return item;
	}
}
