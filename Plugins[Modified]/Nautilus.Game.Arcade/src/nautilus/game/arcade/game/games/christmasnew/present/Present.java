package nautilus.game.arcade.game.games.christmasnew.present;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.lifetimes.Component;

public class Present implements Component
{

	private static final ItemStack PRESENT = SkinData.PRESENT.getSkull();
	private static final int ROTATION_ITERATIONS = 20;
	private static final double ROTATION_DELTA_Y = 1D / ROTATION_ITERATIONS;
	private static final float ROTATION_DELTA_YAW = 360F / ROTATION_ITERATIONS;
	private static final int TO_COLLECT = 2;

	private final Location _location;
	private final ArmorStand _stand;
	private final List<Player> _players;

	private int _iterations;
	private boolean _down;

	public Present(Location location)
	{
		_location = location.clone();
		_location.setYaw(UtilMath.r(360));
		_stand = _location.getWorld().spawn(_location.clone().add(0, 0.25, 0), ArmorStand.class);
		_stand.setVisible(false);
		_stand.setGravity(false);
		_stand.setHelmet(PRESENT);
		_stand.setRemoveWhenFarAway(false);
		UtilEnt.setTickWhenFarAway(_stand, true);
		_players = new ArrayList<>(TO_COLLECT);

		MapUtil.QuickChangeBlockAt(_location, Material.SNOW_BLOCK);
	}

	@Override
	public void activate()
	{
		MapUtil.QuickChangeBlockAt(_location, Material.STAINED_GLASS, (byte) (Math.random() < 0.5 ? 14 : 5));
	}

	@Override
	public void deactivate()
	{
		_stand.remove();
		MapUtil.QuickChangeBlockAt(_location.clone().subtract(0, 1, 0), Material.COAL_BLOCK);
	}

	public void updateRotation()
	{
		Location location = _stand.getLocation();

		location.add(0, _down ? -ROTATION_DELTA_Y : ROTATION_DELTA_Y, 0);
		location.setYaw(location.getYaw() + ROTATION_DELTA_YAW);

		if (++_iterations == 20)
		{
			_iterations = 0;
			_down = !_down;
		}

		_stand.teleport(location);
	}

	public ArmorStand getStand()
	{
		return _stand;
	}

	public Location getLocation()
	{
		return _location;
	}

	public boolean isColliding(Player player, boolean containsCheck)
	{
		return (!containsCheck || !_players.contains(player)) && UtilMath.offsetSquared(player, _stand) < 4;
	}

	public void setCollected(Player player)
	{
		_players.add(player);
	}

	public int getLeft()
	{
		return TO_COLLECT - _players.size();
	}

	public boolean isCollected()
	{
		return _players.size() == TO_COLLECT;
	}
}
