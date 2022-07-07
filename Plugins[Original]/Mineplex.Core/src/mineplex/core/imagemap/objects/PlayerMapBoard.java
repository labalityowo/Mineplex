package mineplex.core.imagemap.objects;

import mineplex.core.common.util.UtilMath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerMapBoard
{

	private static final int VIEW_DIST_SQUARED = 28 * 28;

	private final Location _location;
	private final List<PlayerMapImage> _images;
	private final Map<Player, Integer> _viewers;
	private final boolean _handleJoin;

	public PlayerMapBoard(Location location, List<PlayerMapImage> images, boolean handleJoin)
	{
		_location = location;
		_images = images;
		_viewers = new HashMap<>();
		_handleJoin = handleJoin;
	}

	public void goTo(Player player, boolean next)
	{
		if (!_viewers.containsKey(player))
		{
			return;
		}

		int index = _viewers.get(player);

		if (next && _images.size() - 1 == index)
		{
			index = -1;
		}
		else if (!next && index == 0)
		{
			index = _images.size();
		}

		int newIndex = next ? index + 1 : index - 1;
		goTo(player, newIndex);
	}

	public void goTo(Player player, int index)
	{
		_viewers.put(player, index);
		_images.get(index).addViewer(player, true);
	}

	public void onPlayerJoin(Player player)
	{
		_viewers.put(player, 0);
		_images.get(0).addViewer(player, true);
	}

	public void onPlayerQuit(Player player)
	{
		_viewers.remove(player);
	}

	public void onRefresh()
	{
		Bukkit.getOnlinePlayers().forEach(player ->
		{
			if (player.getWorld().equals(_location.getWorld()) && UtilMath.offset2dSquared(player.getLocation(), _location) < VIEW_DIST_SQUARED && _viewers.containsKey(player))
			{
				int index = _viewers.get(player);
				_images.get(index).addViewer(player, false);
			}
		});
	}

	public void cleanup()
	{
		_viewers.clear();
		_images.forEach(image ->
		{
			image.getItemFrames().forEach(Entity::remove);
			image.getItemFrames().clear();
		});
	}

	public Location getLocation()
	{
		return _location;
	}

	public boolean isHandleJoin()
	{
		return _handleJoin;
	}
}
