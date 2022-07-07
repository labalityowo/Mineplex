package mineplex.game.nano.game.games.bawkbawk;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class BawkBawk extends SoloGame
{

	private static final int PLATFORM_HEIGHT = 5;

	private List<Location> _possiblePlatforms;
	private Location _lastPlatform;

	private int _rounds;
	private long _roundTime = TimeUnit.SECONDS.toMillis(9), _roundEndTime = TimeUnit.SECONDS.toMillis(3);
	private long _lastRoundStart, _lastRoundEnd;
	private boolean _roundOver;
	private int _platfromSize = 2;

	public BawkBawk(NanoManager manager)
	{
		super(manager, GameType.BAWK_BAWK, new String[]
				{
						C.cYellow + "Bawk Bawk" + C.Reset + " is angry!",
						"Get to the " + C.cRed + "Platform" + C.Reset + " before the time runs out!",
						C.cRed + "PvP" + C.Reset + " is enabled at " + C.cGreen + "Round 3" + C.Reset + "!",
						C.cYellow + "Last player" + C.Reset + " standing wins!"
				});

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_endComponent.setTimeout(TimeUnit.SECONDS.toMillis(150));
	}

	@Override
	protected void parseData()
	{
		_possiblePlatforms = _mineplexWorld.getIronLocations("RED");
		_roundOver = true;
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		Player[] players = getAlivePlayers().toArray(new Player[0]);

		if (_roundOver)
		{
			// Round over
			if (UtilTime.elapsed(_lastRoundEnd, _roundEndTime))
			{
				generatePlatform();

				_rounds++;
				_lastRoundStart = System.currentTimeMillis();
				_roundOver = false;

				if (_roundTime > 5000)
				{
					_roundTime -= 250;
				}

				if (_rounds % 6 == 0 && _platfromSize > 0)
				{
					_platfromSize--;
				}

				if (_rounds == 3)
				{
					_damageComponent.setPvp(true);
					announce(F.main(getManager().getName(), F.color("PvP", C.cRedB) + " is now " + F.greenElem("Enabled") + "!"));
					UtilTextMiddle.display(null, C.cRed + "PvP is enabled!", 10, 20, 10, players);
				}
				else
				{
					for (Player player : players)
					{
						player.playSound(player.getLocation(), Sound.CHICKEN_IDLE, 1, 1);
					}

					UtilTextMiddle.display(null, C.cGreen + "Get to the Platform", 10, 20, 10, players);
				}
			}
		}
		else
		{
			long diff = System.currentTimeMillis() - _lastRoundStart;

			if (diff > _roundTime)
			{
				setWalls(_lastPlatform, false);

				for (Player player : players)
				{
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0.2F);
				}

				UtilTextBottom.display(C.cRedB + "BAWK BAWK", players);

				getManager().runSyncLater(() ->
				{
					double size = _platfromSize + 0.6;
					Location a = _lastPlatform.clone().add(size, PLATFORM_HEIGHT, size), b = _lastPlatform.clone().subtract(size, 0, size);

					for (Player player : players)
					{
						Location location = player.getLocation();

						if (!UtilAlg.inBoundingBox(location, a, b))
						{
							location.getWorld().strikeLightningEffect(location);
							_manager.getDamageManager().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, getGameType().getName(), "Lightning");
						}
					}
				}, 40);

				_lastRoundEnd = System.currentTimeMillis();
				_roundOver = true;
			}
			else
			{
				diff = _roundTime - diff;

				UtilTextBottom.displayProgress("Round " + _rounds, (double) diff / _roundTime, UtilTime.MakeStr(Math.max(0, diff)), players);
			}
		}
	}

	private void generatePlatform()
	{
		while (true)
		{
			Location location = UtilAlg.Random(_possiblePlatforms);

			if (location == null)
			{
				return;
			}

			if (_lastPlatform != null)
			{
				if (UtilMath.offsetSquared(_lastPlatform, location) < 25)
				{
					continue;
				}

				setPlatform(_lastPlatform, true);
				setWalls(_lastPlatform, true);
			}

			setPlatform(location, false);
			_lastPlatform = location;

			break;
		}
	}

	private void setPlatform(Location center, boolean air)
	{
		for (Block block : UtilBlock.getInBoundingBox(center.clone().add(_platfromSize, 0, _platfromSize), center.clone().subtract(_platfromSize, 0, _platfromSize), false))
		{
			Location location = block.getLocation();

			MapUtil.QuickChangeBlockAt(location, air ? Material.AIR : Material.STAINED_CLAY, (byte) 14);
			MapUtil.QuickChangeBlockAt(location.add(0, 5, 0), air ? Material.AIR : Material.WOOD_STEP);
		}
	}

	private void setWalls(Location center, boolean air)
	{
		int size = _platfromSize + 1;

		for (Block block : UtilBlock.getInBoundingBox(center.clone().add(size, PLATFORM_HEIGHT, size), center.clone().subtract(size, 0, size), false, false, true, false))
		{
			MapUtil.QuickChangeBlockAt(block.getLocation(), air ? Material.AIR : Material.STAINED_GLASS, (byte) 14);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.CUSTOM)
		{
			return;
		}

		event.AddMod(getGameType().getName(), -event.GetDamage() + 0.1);
		event.AddKnockback(getGameType().getName(), 3);
	}
}
