package nautilus.game.arcade.game.games.survivalgames.modules;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerDeathOutEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.modules.Module;

public class BorderModule extends Module
{

	private static final int RATE_CONSTANT = 60;
	private static final int PLAYER_CONSTANT = 24;
	private static final long FINAL_BORDER_TIME = TimeUnit.MINUTES.toMillis(3);
	private static final int FINAL_BORDER_MOVE_TIME = 300;

	private double _initialBorder, _initialPlayers, _deathmatchSize = 61;
	private boolean _finalBorderCheck, _finalBorderMove;
	private long _finalBorderTime;

	@Override
	protected void setup()
	{
		Game game = getGame();
		Location center = game.GetSpectatorLocation();
		List<Double> borders = Arrays.asList
				(
						game.WorldData.MaxX - center.getX(),
						center.getX() - game.WorldData.MinX,
						game.WorldData.MaxZ - center.getZ(),
						center.getZ() - game.WorldData.MinZ
				);

		borders.sort(Comparator.naturalOrder());
		double largestBorder = borders.get(borders.size() - 1);
		WorldBorder border = game.WorldData.World.getWorldBorder();

		_initialBorder = largestBorder * 2;
		border.setCenter(center);
		border.setSize(_initialBorder);
		border.setDamageAmount(0.1);
		border.setDamageBuffer(0);
		border.setWarningDistance(10);

		List<Location> deathmatchPoint = game.WorldData.GetDataLocs("BROWN");

		if (!deathmatchPoint.isEmpty())
		{
			_deathmatchSize = UtilMath.offset(center, deathmatchPoint.get(0)) * 2;
		}
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		int players = getGame().GetPlayers(true).size();
		_initialPlayers = players;
		updateBorderSize(players);
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		getGame().WorldData.World.getWorldBorder().setSize(Integer.MAX_VALUE);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerDeathOut(PlayerDeathOutEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		updateBorderSize(getGame().GetPlayers(true).size() - 1);
	}

	private void updateBorderSize(int players)
	{
		WorldBorder border = getGame().WorldData.World.getWorldBorder();

		border.setSize(border.getSize());
		border.setSize(_deathmatchSize, (long) ((border.getSize() / _initialBorder) * (players * (PLAYER_CONSTANT / _initialPlayers)) * RATE_CONSTANT));
	}

	@EventHandler
	public void borderDamage(CustomDamageEvent event)
	{
		Player player = event.GetDamageePlayer();

		if (event.GetCause() != DamageCause.SUFFOCATION || player == null)
		{
			return;
		}

		event.SetCancelled("Border Pre-Event");
		if (Recharge.Instance.use(player, "World Border Damage", 1000, false, false))
		{
			getGame().getArcadeManager().GetDamage().NewDamageEvent(event.GetDamageeEntity(), null, null, DamageCause.CUSTOM, 2, false, true, true, getGame().GetName(), "World Border");
		}
	}

	@EventHandler
	public void updateEndGame(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !getGame().IsLive() || _deathmatchSize != getGame().WorldData.World.getWorldBorder().getSize())
		{
			return;
		}

		if (!_finalBorderCheck)
		{
			_finalBorderTime = System.currentTimeMillis();
			_finalBorderCheck = true;
		}
		else if (!_finalBorderMove && UtilTime.elapsed(_finalBorderTime, FINAL_BORDER_TIME))
		{
			getGame().WorldData.World.getWorldBorder().setSize(1, FINAL_BORDER_MOVE_TIME);
			_finalBorderMove = true;
		}

		Location center = UtilAlg.getAverageLocation(getGame().GetTeamList().get(0).GetSpawns());

		if (center == null)
		{
			return;
		}

		double maxY = center.getY() + 6;

		for (Player player : getGame().GetPlayers(true))
		{
			if (player.getLocation().getY() > maxY)
			{
				getGame().GetTeam(player).SpawnTeleport(player);
			}
		}
	}
}
