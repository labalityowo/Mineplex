package nautilus.game.arcade.game.games.buildmavericks;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.mavericks.MavericksBuildRepository;
import mineplex.core.mavericks.MavericksBuildWrapper;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.games.build.Build;
import nautilus.game.arcade.game.games.build.BuildData;
import nautilus.game.arcade.game.modules.compass.CompassModule;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * A custom implementation of the Master Builders game in conjunction with the Mavericks basketball team.
 */
public class BuildMavericks extends Build
{
	private MavericksBuildRepository _repository;
	private List<Player> _places;
	private Map<Player, BuildData> _dataClone = new HashMap<>();
	

	public BuildMavericks(ArcadeManager manager)
	{
		super(manager, GameType.BuildMavericks);
		
		_words = new String[]
				{
						"Maverick", "Champ", "Basketball", "Net", "Court", "Referee", "Basket", "Scoreboard", "Jump ball", "Player", 
						"Coach", "Shot clock", "Backboard", "Rim", "Bench", "Dribble", "Dunk", "Defense", "Swish", "Cheerleaders", 
						"Fans", "Trophy", "All-star", "Jersey", "Uniform", "Basketball shoes", "MVP", "Basketball shorts", "Jumbotron", 
						"Block", "Team bus", "Team plane", "Victory"
				};
		
		_repository = new MavericksBuildRepository();
		
		_notifyFailure = false;

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}
	
	@Override
	public void AnnounceEnd(List<Player> places)
	{
		super.AnnounceEnd(places);
		_places = places;
	}
	
	@Override
	protected void tallyScores()
	{
		super.tallyScores();
		
		_dataClone.putAll(_data);
	}
	
	@Override
	public void SetState(GameState state)
	{
		super.SetState(state);
	
		if (state == GameState.End)
		{
			for (Entry<Player, BuildData> e : _dataClone.entrySet())
			{
				BuildData data = e.getValue();
				
				//TODO: Insert some real value here
				if (data.getPoints() < 2) continue;
				
				UUID uuid = e.getKey().getUniqueId();
				String name = e.getKey().getName();
				
				Schematic schematic = data.convertToSchematic();
				
				
				int place = _places.indexOf(e.getKey());
				if (place == -1) continue;
				
				MavericksBuildWrapper wrapper = new MavericksBuildWrapper(-1, uuid, name, _word, data.getPoints(), place, 
						System.currentTimeMillis(), UtilSchematic.getBytes(schematic), data.getParticlesBytes(), false);
				
				_repository.add(wrapper).thenCompose(BukkitFuture.accept(success ->
				{
					if (!success)
					{
						new RuntimeException("Unable to save build data and schematic for player '" + e.getKey().getName() + "': " + wrapper).printStackTrace();
					}
				}));
			}
		}
	}
	
	@Override
	protected void addBuildData(Player player, Location spawn)
	{
		Location center = UtilAlg.findClosest(player.getLocation(), WorldData.GetDataLocs("YELLOW"));

		_data.put(player, new BuildDataCylinder(player, spawn, center));
	}
}