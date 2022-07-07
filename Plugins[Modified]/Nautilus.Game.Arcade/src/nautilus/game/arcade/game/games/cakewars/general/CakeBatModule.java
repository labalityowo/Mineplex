package nautilus.game.arcade.game.games.cakewars.general;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.cakewars.CakeModule;
import nautilus.game.arcade.game.games.cakewars.CakeWars;

public class CakeBatModule extends CakeModule
{

	private static final int DAMAGE_SECONDS = 20;

	private final Map<Player, Integer> _unsafeSeconds;
	private final Set<Bat> _bats;
	private int _minY;

	public CakeBatModule(CakeWars game)
	{
		super(game);

		_unsafeSeconds = new HashMap<>();
		_bats = new HashSet<>();
	}

	@Override
	public void cleanup()
	{
		_unsafeSeconds.clear();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		List<Location> locations = _game.WorldData.GetDataLocs("BROWN");

		if (!locations.isEmpty())
		{
			_minY = locations.get(0).getBlockY();
		}
	}

	@EventHandler
	public void updateBats(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_game.IsLive() || _minY == 0)
		{
			return;
		}

		_bats.removeIf(bat ->
		{
			if (bat.getTicksLived() > 50)
			{
				bat.remove();
				return true;
			}

			return false;
		});

		for (Player player : _game.GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}

			Location location = player.getLocation();

			if (location.getY() >= _minY && _game.isInsideMap(player))
			{
				continue;
			}

			Integer ticks = _unsafeSeconds.get(player);

			if (ticks == null)
			{
				_unsafeSeconds.put(player, 0);
				continue;
			}

			_unsafeSeconds.put(player, ticks + 1);

			if (ticks < DAMAGE_SECONDS)
			{
				if (UtilEnt.onBlock(player) && ticks % 8 == 0)
				{
					player.sendMessage(F.main("Game", "Return to an island! If you don't bats will begin to attack you!"));
					UtilTextBottom.display(C.cRedB + "Return to an island!", player);
					player.playSound(location, Sound.NOTE_STICKS, 1, 0.5F);
				}
			}
			else
			{
				if (Recharge.Instance.use(player, "Bat Inform", 8000, false, false))
				{
					UtilTextMiddle.display(C.cRedB + "STOP CAMPING", "Bats are attacking you!", 0, 20, 10, player);
					player.sendMessage(F.main("Game", "Get back to an island, bats are attacking you!"));
					player.playSound(location, Sound.NOTE_PLING, 1, 0.5F);
				}

				_game.CreatureAllowOverride = true;

				Bat bat = location.getWorld().spawn(location.add(Math.random() - 0.5, 1.2, Math.random() - 0.5), Bat.class);
				bat.setAwake(true);
				_bats.add(bat);

				_game.getArcadeManager().GetDamage().NewDamageEvent(player, bat, null, DamageCause.ENTITY_ATTACK, 1 + (ticks - DAMAGE_SECONDS) / 5, false, true, true, bat.getName(), "Camping");

				_game.CreatureAllowOverride = false;
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_unsafeSeconds.remove(event.getPlayer());
	}

}
