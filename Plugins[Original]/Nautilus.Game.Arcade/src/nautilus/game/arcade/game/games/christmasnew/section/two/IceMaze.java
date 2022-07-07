package nautilus.game.arcade.game.games.christmasnew.section.two;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;

class IceMaze extends SectionChallenge
{

	private final List<Location> _enterance;

	IceMaze(ChristmasNew host, Location present, Section section)
	{
		super(host, present, section);

		_enterance = _worldData.GetCustomLocs(String.valueOf(Material.GOLD_BLOCK.getId()));
		_enterance.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));

		List<Location> mazeOne = _worldData.GetCustomLocs(String.valueOf(Material.SLIME_BLOCK.getId()));
		List<Location> mazeTwo = _worldData.GetCustomLocs(String.valueOf(Material.COMMAND.getId()));
		boolean state = Math.random() < 0.5;

		mazeOne.forEach(location -> MapUtil.QuickChangeBlockAt(location, state ? Material.ICE : Material.AIR));
		mazeTwo.forEach(location -> MapUtil.QuickChangeBlockAt(location, state ? Material.AIR : Material.ICE));
	}

	@Override
	public void onPresentCollect()
	{
		_enterance.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.ICE));

		List<Location> corners = _worldData.GetDataLocs("LIGHT_BLUE");
		Location cornerA = corners.get(0);
		Location cornerB = corners.get(1);

		Location outside = _worldData.GetDataLocs("BLUE").get(0);
		outside.setYaw(-90);

		for (Player player : _host.GetPlayers(true))
		{
			if (UtilAlg.inBoundingBox(player.getLocation(), cornerA, cornerB))
			{
				player.teleport(outside);
			}
		}
	}

	@Override
	public void onRegister()
	{

	}

	@Override
	public void onUnregister()
	{

	}
}
