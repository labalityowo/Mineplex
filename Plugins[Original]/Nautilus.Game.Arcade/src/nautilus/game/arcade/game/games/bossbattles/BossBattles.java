package nautilus.game.arcade.game.games.bossbattles;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.EventState;
import mineplex.minecraft.game.core.boss.WorldEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.bossbattles.displays.BossDisplay;
import nautilus.game.arcade.game.games.champions.kits.KitAssassin;
import nautilus.game.arcade.game.games.champions.kits.KitBrute;
import nautilus.game.arcade.game.games.champions.kits.KitKnight;
import nautilus.game.arcade.game.games.champions.kits.KitMage;
import nautilus.game.arcade.game.games.champions.kits.KitRanger;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class BossBattles extends TeamGame
{
	private WorldEvent _currentBoss;
	private BattleBoss _chosenBoss =BattleBoss.SPIDER;
//	BattleBoss.values()[UtilMath.r(BattleBoss.values().length)];
	private ArrayList<BossDisplay> _displays = new ArrayList<BossDisplay>();

	public BossBattles(ArcadeManager manager)
	{ 
		super(manager, GameType.BossBattles, new Kit[]
		{
				new KitBrute(manager), new KitRanger(manager),
				new KitKnight(manager), new KitMage(manager),
				new KitAssassin(manager),
		}, new String[]
		{
			"Fight some bosses"
		});

		HungerSet = 20;
		CreatureAllowOverride = true;
		PrepareFreeze = false;

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);

//		registerChatStats(Kills);
//		Game giving constant errors when loading.
	}

	@EventHandler
	public void onSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != event.getSpawnReason().CUSTOM)
			event.setCancelled(true);
	}

	@Override
	public boolean isInsideMap(Player player)
	{
		return true;
	}

	@EventHandler
	public void checkBossIsDead(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (_currentBoss == null)
		{
			return;
		}

		if (GetPlayers(true).isEmpty())
		{
			_currentBoss.cancel();
		}

		if (_currentBoss.getState() != EventState.COMPLETE
				&& _currentBoss.getState() != EventState.CANCELLED)
		{
			return;
		}

		endCurrentBoss();
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (GetPlayers(true).size() <= 0 || _currentBoss == null
				|| _currentBoss.getState() == EventState.CANCELLED
				|| _currentBoss.getState() == EventState.COMPLETE)
		{
			endCurrentBoss();

			super.EndCheck();
		}
	}

	private void endCurrentBoss()
	{
		if (_currentBoss == null)
		{
			return;
		}

		HandlerList.unregisterAll(_currentBoss);

		// If the event was cancelled, we don't need to run a cleanup
		if (_currentBoss.getState() != EventState.CANCELLED)
			_currentBoss.cleanup();
	}

	public void setPicked(Player player, BattleBoss battleBoss)
	{
		_chosenBoss = battleBoss;

		Announce(C.cGold + C.Bold + player.getName() + " has chosen the boss "
				+ battleBoss.name() + "!");
	}

	@EventHandler
	public void onGameOver(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live
				|| event.GetState() == GameState.Prepare)
		{
			return;
		}

		if (_currentBoss == null)
		{
			return;
		}

		endCurrentBoss();
	}

	@EventHandler
	public void onGameLoadingOver(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Recruit)
		{
			return;
		}

		for (BossDisplay display : _displays)
		{
			HandlerList.unregisterAll(display);
			display.removeHologram();
			display.removeBoss();
		}

		_displays.clear();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPrepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		double distAway = Math.max(_currentBoss.getSchematic().getWidth(),
				_currentBoss.getSchematic().getLength()) / 2;

		distAway += Math.min(20, distAway * 0.2);

		for (Location loc : GetTeamList().get(0).GetSpawns())
		{
			double maxDist = loc.toVector().setY(0).length();

			Vector vec = UtilAlg.getTrajectory2d(new Vector(), loc.toVector());

			loc.setX(0);
			loc.setZ(0);

			loc.add(vec.multiply(Math.min(maxDist, distAway)));

			vec.normalize().multiply(-1);

			boolean foundSafe = false;

			for (int i = 0; i < 50; i++)
			{
				for (int y = 200; y > 0; y--)
				{
					loc.setY(y);

					if (UtilBlock.solid(loc.getBlock().getRelative(0, -1, 0)))
					{
						foundSafe = true;
						break;
					}
				}

				if (foundSafe)
				{
					break;
				}

				loc.add(vec);
			}
		}
	}

	@EventHandler
	public void onGamePrepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		int i = 0;
		ArrayList<Location> locations = UtilShapes.getPointsInCircle(
				new Location(UtilWorld.getWorld("world"), 0, 104, 0),
				BattleBoss.values().length, 4.5);

		for (BattleBoss boss : BattleBoss.values())
		{
			try
			{
				Location loc = locations.get(i).clone();

				loc.setDirection(UtilAlg.getTrajectory(loc, Manager.GetLobby()
						.getSpawn()));

				Constructor<? extends BossDisplay> constructor = boss
						.getBossDisplay().getConstructor(BossBattles.class,
								BattleBoss.class, Location.class);

				BossDisplay bossDisplay = constructor.newInstance(this,
						BattleBoss.values()[i], loc);

				bossDisplay.start();
				bossDisplay.spawnHologram();

				Bukkit.getPluginManager().registerEvents(bossDisplay,
						getArcadeManager().getPlugin());

				_displays.add(bossDisplay);

				System.out.print("Registered "
						+ bossDisplay.getClass().getSimpleName());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			i++;
		}
	}

	@EventHandler
	public void onGameStarted(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		setNewBoss(_chosenBoss);
	}

	@EventHandler
	public void onGameLive(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_currentBoss.start();

		Bukkit.getPluginManager().registerEvents(_currentBoss,
				getArcadeManager().getPlugin());
	}

	public WorldEvent createInstance(Class clazz, Location centerLocation)
	{
		WorldEvent worldEvent = null;

		try
		{
			for (Constructor<?> con : clazz.getConstructors())
			{

				worldEvent = (WorldEvent) con.newInstance(getArcadeManager()
						.GetDisguise(), getArcadeManager().GetProjectile(),
						getArcadeManager().GetDamage(), getArcadeManager()
								.GetBlockRestore(), getArcadeManager()
								.GetCondition(), centerLocation);
				break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return worldEvent;
	}

	private void setNewBoss(BattleBoss boss)
	{
		_currentBoss = createInstance(boss.getBoss(), new Location(
				WorldData.World, 0, 6, 0));

		_currentBoss.setArcadeGame(true);
		_currentBoss.setDifficulty(0.6);

		_currentBoss.loadMap();
	}
}
