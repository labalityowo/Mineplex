package mineplex.gemhunters.tutorial;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.texttutorial.tutorial.Phase;
import mineplex.core.texttutorial.tutorial.Tutorial;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.spawn.SpawnModule;
import mineplex.gemhunters.world.WorldDataModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GemHuntersTutorial extends Tutorial implements Listener
{

	private final SpawnModule _spawn;
	private final WorldDataModule _worldData;
	
	private final int[][] _locations = {
			{
				92, 69, 148, 53, 15
			},
			{
				-125, 76, 94, 90, 0
			},
			{
				-220, 69, -280, -135, 20
			},
			{
				-611, 78, -19, -135, 20
			},
			{
				-454, 68, 231, 0, 0
			},
			{
				300, 88, 45, 125, 0
			}
	};
	
	public GemHuntersTutorial()
	{
		super("Gem Hunters", "gemhunterstutorial", 0);
		
		_spawn = Managers.require(SpawnModule.class);
		_worldData = Managers.require(WorldDataModule.class);
		
		addPhase(new Phase(getLocation(0), "Welcome", new String[] {
				"Welcome To " + C.cGreen + "Gem Hunters",
		}));
		addPhase(new Phase(getLocation(1), "PVP", new String[] {
				"Players start with " + C.cGreen + EconomyModule.GEM_START_COST + C.cWhite + " Gems and must survive in the city.",
				"Killing another player will gift you " + C.cYellow + "50%" + C.cWhite + " of their total gems.",
		}));
		addPhase(new Phase(getLocation(2), "Safezones", new String[] {
				"In Safe Zones you cannot take damage and can purchase items like", 
				"Food, Weapons, and Gear with the Gems you earn in the world.",
		}));
		addPhase(new Phase(getLocation(3), "Items", new String[] {
				"Collect items from chests and powerup.",
				"You can find anything from Weapons to Cosmetics.",
				"If you find something you want to keep you can",
				C.cGreen + "Cash Out" + C.cWhite + " at any time by right clicking the " + C.cGreen + "Emerald" + C.cWhite + " in your inventory.",
		}));
		addPhase(new Phase(getLocation(4), "Cashing Out", new String[] {
				"Cashing out will reset your progress in the world,",
				"adds any special items you had like",
				"Gems, Treasure Shards, Cosmetics or Rank Upgrades to your account!",
		}));
		addPhase(new Phase(getLocation(5), "Good luck", new String[] {
				"Stay safe! Anarchy rules in the world of " + C.cGreen + "Gem Hunters" + C.cWhite + "!"
		}));

		UtilServer.RegisterEvents(this);
	}
	
	@Override
	public void startTutorial(Player player)
	{
		super.startTutorial(player);

		player.setAllowFlight(true);
		player.setFlying(true);
	}
	
	@Override
	public void stopTutorial(Player player)
	{
		super.stopTutorial(player);
		
		player.setFlying(false);
		player.setAllowFlight(false);
		
		UtilServer.runSyncLater(() -> _spawn.teleportToSpawn(player), 10);
	}
	
	private Location getLocation(int phase)
	{
		int[] locations = _locations[phase];
		
		return new Location(_worldData.World, locations[0] + 0.5, locations[1] + 0.5, locations[2] + 0.5, locations[3], locations[4]);
	}

	@EventHandler
	public void updateFlight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (isInTutorial(player) && !player.isFlying())
			{
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}
	}

}
