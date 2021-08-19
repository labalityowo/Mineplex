package nautilus.game.arcade.game.games.survivalgames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor.Type;
import org.bukkit.map.MapPalette;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.worldmap.WorldMapRenderer;

public class SurvivalGamesMapRenderer extends WorldMapRenderer<SurvivalGamesNew>
{

	private int _borderSize;

	SurvivalGamesMapRenderer(SurvivalGamesNew game)
	{
		super(game);
	}

	@Override
	public void renderTick()
	{
		_borderSize = (int) _game.WorldData.World.getWorldBorder().getSize() / 2;
	}

	@Override
	protected void preRender(Player player)
	{

	}

	@Override
	protected byte renderBlock(Player player, byte color, int mapX, int mapZ, int blockX, int blockZ)
	{
		int scale = _manager.getScale() * 2, minX = -_borderSize, maxX = _borderSize, minZ = -_borderSize, maxZ = _borderSize;
		boolean bigX = blockX > maxX, smallX = blockX < minX, bigZ = blockZ > maxZ, smallZ = blockZ < minZ;

		if (bigX || smallX || bigZ || smallZ)
		{
			if (
					bigX && blockX - scale < maxX && !smallX && !bigZ && !smallZ ||
							smallX && blockX + scale > minX && !bigX && !bigZ && !smallZ ||
							bigZ && blockZ - scale < maxZ && !bigX && !smallX && !smallZ ||
							smallZ && blockZ + scale > minZ && !bigX && !smallX && !bigZ ||
							(mapX + (mapZ % 4)) % 4 == 0)
			{
				color = MapPalette.RED;
			}
		}

		return color;
	}

	@Override
	protected void renderCursors(MapCanvas canvas, Player player)
	{
		GameTeam team = _game.GetTeam(player);

		if (team == null)
		{
			return;
		}

		for (Player other : UtilServer.getPlayersCollection())
		{
			if (UtilPlayer.isSpectator(other))
			{
				continue;
			}

			Location location = other.getLocation();

			if (player.equals(other))
			{
				addCursor(canvas, location, Type.WHITE_POINTER);
			}
			else if (_game.TeamMode && team.HasPlayer(other))
			{
				addCursor(canvas, location, Type.GREEN_POINTER);
			}
			else if (!Recharge.Instance.usable(player, "Show All Players"))
			{
				addCursor(canvas, location, Type.RED_POINTER);
			}
		}

		Location supplyDrop = _game.getSupplyDrop().getCurrentDrop();

		if (supplyDrop != null)
		{
			addCursor(canvas, supplyDrop, Type.WHITE_CROSS);
		}
	}
}
