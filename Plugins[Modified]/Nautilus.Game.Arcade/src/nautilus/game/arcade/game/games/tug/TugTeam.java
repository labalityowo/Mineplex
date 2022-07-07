package nautilus.game.arcade.game.games.tug;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.tug.entities.TugEntity;
import nautilus.game.arcade.game.games.tug.entities.TugSheep;

public class TugTeam
{

	private final GameTeam _gameTeam;
	private final List<Location> _crops, _spawns;
	private final List<TugEntity> _entities;

	private TugTeam _enemy;

	TugTeam(GameTeam gameTeam, List<Location> crops)
	{
		_gameTeam = gameTeam;
		_crops = new ArrayList<>(crops);
		_spawns = new ArrayList<>(crops);
		_entities = new ArrayList<>();
	}

	public GameTeam getGameTeam()
	{
		return _gameTeam;
	}

	public List<Location> getCrops()
	{
		return _crops;
	}

	public List<Location> getSpawns()
	{
		return _spawns;
	}

	public List<TugEntity> getEntities()
	{
		return _entities;
	}

	public boolean isEntity(LivingEntity entity)
	{
		for (TugEntity tugEntity : getEntities())
		{
			if (tugEntity.getEntity().equals(entity))
			{
				return true;
			}
		}

		return false;
	}

	public void setEnemy(TugTeam enemy)
	{
		_enemy = enemy;
	}

	public TugTeam getEnemy()
	{
		return _enemy;
	}
}
