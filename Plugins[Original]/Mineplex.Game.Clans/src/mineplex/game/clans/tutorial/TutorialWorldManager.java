package mineplex.game.clans.tutorial;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.generator.VoidGenerator;
import mineplex.game.clans.tutorial.TutorialRegion;

public class TutorialWorldManager extends MiniPlugin
{
	public static final int BLOCKS_BETWEEN_TUTORIALS = 1200;

	private final World _tutorialWorld;
	private final Schematic _schematic;
	private LinkedList<TutorialRegion> _regionStack;
	private TutorialRegion _centerRegion;

	public TutorialWorldManager(JavaPlugin plugin, String worldName, String schematicName) throws IOException
	{
		super("Tutorial World", plugin);

		log("Creating Tutorial World");
		WorldCreator creator = new WorldCreator(worldName);
		creator.generator(new VoidGenerator());
		_tutorialWorld = Bukkit.createWorld(creator);
		_tutorialWorld.setDifficulty(Difficulty.EASY);
		_tutorialWorld.setGameRuleValue("doDaylightCycle", "false");
		_tutorialWorld.setTime(6000);
		_tutorialWorld.setAutoSave(false);
		_tutorialWorld.setAmbientSpawnLimit(0);
		_tutorialWorld.setMonsterSpawnLimit(0);
		_tutorialWorld.setWaterAnimalSpawnLimit(0);
		log("Tutorial World Created!");

		log("Loading schematic - " + schematicName);
		_schematic = UtilSchematic.loadSchematic(new File(schematicName));
		log("Finished loading schematic");

		log("Populating Region Stack...");
		populateRegionStack();
		log("Finished loading Tutorial World Manager!");
	}

	private void populateRegionStack()
	{
		_regionStack = new LinkedList<>();

		// Populate the stack with 100 available tutorial regions
		for (int x = 0; x < 10; x++)
		{
			for (int z = 0; z < 10; z++)
			{
				long time = System.currentTimeMillis();
				double xLoc = (x) * BLOCKS_BETWEEN_TUTORIALS; // 1000x1000 regions
				double zLoc = (z) * BLOCKS_BETWEEN_TUTORIALS;

				TutorialRegion region = new TutorialRegion(_schematic, new Location(_tutorialWorld, xLoc, 30, zLoc));
				if (x == 0 && z == 0) _centerRegion = region;
				_regionStack.add(region);
				System.out.println("Finished Generating Region: " + ((x * 10) + z) + ". Took " + (System.currentTimeMillis() - time) + " ms");
			}
		}
	}

	/**
	 * Get the next TutorialRegion in the stack. This region is prepared and ready for use in a tutorial
	 * @return
	 */
	public TutorialRegion getNextRegion()
	{
		return _regionStack.pop();
	}

	/**
	 * Notify the TutorialWorldManager that this TutorialRegion is no longer needed and should be cleaned up.
	 * The TutorialRegion should not be used after calling this method
	 */
	public void returnRegion(TutorialRegion region)
	{
		region.reset();
		_regionStack.push(region);
		log("Returned " + region.toString());
	}

	public World getTutorialWorld()
	{
		return _tutorialWorld;
	}

	public TutorialRegion getCenterRegion()
	{
		return _centerRegion;
	}
}
