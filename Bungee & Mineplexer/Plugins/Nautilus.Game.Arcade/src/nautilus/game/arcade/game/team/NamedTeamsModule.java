package nautilus.game.arcade.game.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilBlock;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.Module;

public class NamedTeamsModule extends Module
{

	private boolean _teamPerSpawn;

	private TeamColors _currentColor = TeamColors.first();
	private int _nameIndex;

	@Override
	protected void setup()
	{
		getGame().TeamMode = true;
	}

	public NamedTeamsModule setTeamPerSpawn(boolean teamPerSpawn)
	{
		_teamPerSpawn = teamPerSpawn;
		return this;
	}

	@EventHandler
	public void generateCustomTeams(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		List<Location> spawns = getGame().GetTeamList().get(0).GetSpawns();
		getGame().GetTeamList().clear();

		if (_teamPerSpawn)
		{
			for (Location location : spawns)
			{
				List<Location> newSpawns = new ArrayList<>();

				// Add nearby non block spawns
				for (Block block : UtilBlock.getInBoundingBox(location.clone().subtract(2, 0, 2), location.clone().add(2, 0, 2), false))
				{
					if (UtilBlock.airFoliage(block) && UtilBlock.airFoliage(block.getRelative(BlockFace.UP)))
					{
						newSpawns.add(block.getLocation().add(0.5, 0, 0.5));
					}
				}

				// Give them a good old shuffle
				Collections.shuffle(newSpawns);

				createTeam(newSpawns);
			}
		}
		else
		{
			int playersPerTeam = getGame().getTeamModule().getPlayersPerTeam();
			int teams = getGame().getArcadeManager().GetPlayerFull() / playersPerTeam;

			for (int i = 0; i < teams; i++)
			{
				createTeam(spawns);
			}
		}
	}

	private void createTeam(List<Location> spawns)
	{
		// Create the team
		GameTeam team = new GameTeam(getGame(), _currentColor._names[_nameIndex], _currentColor._color, spawns, true);
		getGame().AddTeam(team);

		_currentColor = _currentColor.next();

		// If there is no next colour reset and increment name index
		if (_currentColor == null)
		{
			_currentColor = TeamColors.first();
			_nameIndex++;
		}
	}

	enum TeamColors
	{

		YELLOW(ChatColor.YELLOW, new String[]{"Banana", "Mopple", "Custard", "Sponge", "Star", "Giraffe", "Lego", "Light"}),
		GREEN(ChatColor.GREEN, new String[]{"Creepers", "Alien", "Seaweed", "Emerald", "Grinch", "Shrub", "Snake", "Leaf"}),
		AQUA(ChatColor.AQUA, new String[]{"Diamond", "Ice", "Pool", "Kraken", "Aquatic", "Ocean"}),
		RED(ChatColor.RED, new String[]{"Heart", "Tomato", "Ruby", "Jam", "Rose", "Apple", "TNT"}),
		GOLD(ChatColor.GOLD, new String[]{"Mango", "Foxes", "Sunset", "Nuggets", "Lion", "Desert", "Gapple"}),
		LIGHT_PURPLE(ChatColor.LIGHT_PURPLE, new String[]{"Dream", "Cupcake", "Cake", "Candy", "Unicorn"}),
		DARK_BLUE(ChatColor.DARK_BLUE, new String[]{"Squid", "Lapis", "Sharks", "Galaxy", "Empoleon"}),
		DARK_RED(ChatColor.DARK_RED, new String[]{"Rose", "Apple", "Twizzler", "Rocket", "Blood"}),
		WHITE(ChatColor.WHITE, new String[]{"Ghosts", "Spookies", "Popcorn", "Seagull", "Rice", "Snowman", "Artic"}),
		BLUE(ChatColor.BLUE, new String[]{"Sky", "Whale", "Lake", "Birds", "Bluebird", "Piplup"}),
		DARK_GREEN(ChatColor.DARK_GREEN, new String[]{"Forest", "Zombies", "Cactus", "Slime", "Toxic", "Poison"}),
		DARK_PURPLE(ChatColor.DARK_PURPLE, new String[]{"Amethyst", "Slugs", "Grape", "Witch", "Magic", "Zula"}),
		DARK_AQUA(ChatColor.DARK_AQUA, new String[]{"Snorlax", "Aquatic", "Clam", "Fish"});

		static TeamColors first()
		{
			return TeamColors.values()[0];
		}

		private final ChatColor _color;
		private final String[] _names;

		TeamColors(ChatColor color, String[] names)
		{
			_color = color;
			_names = names;
		}

		TeamColors next()
		{
			if (ordinal() == values().length - 1)
			{
				return null;
			}

			return values()[ordinal() + 1];
		}
	}
}
