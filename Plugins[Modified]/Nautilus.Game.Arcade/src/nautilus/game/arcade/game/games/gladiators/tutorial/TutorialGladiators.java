package nautilus.game.arcade.game.games.gladiators.tutorial;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.gladiators.Arena;
import nautilus.game.arcade.game.games.gladiators.Gladiators;
import nautilus.game.arcade.gametutorial.GameTutorial;
import nautilus.game.arcade.gametutorial.TutorialPhase;

/**
 * Created by William (WilliamTiger).
 * 10/12/15
 */
public class TutorialGladiators extends GameTutorial
{

	private Gladiators _host;

	private Location _pink, _orange;
	private Zombie _zombie1, _zombie2;

	private boolean hasHit1, hasHit2;

	public TutorialGladiators(ArcadeManager manager)
	{
		super(manager, new TutorialPhase[]{
				new TutorialPhaseGladiators()
		});

		TeleportOnEnd = false;

		hasHit1 = false;
		hasHit2 = false;

		_host = (Gladiators) manager.GetGame();
	}

	public Location getPink()
	{
		return _pink;
	}

	public void setPink(Location pink)
	{
		_pink = pink;
	}

	public Location getOrange()
	{
		return _orange;
	}

	public void setOrange(Location orange)
	{
		_orange = orange;
	}

	public Zombie getZombie1()
	{
		return _zombie1;
	}

	public void setZombie1(Zombie zombie1)
	{
		_zombie1 = zombie1;
	}

	public Zombie getZombie2()
	{
		return _zombie2;
	}

	public void setZombie2(Zombie zombie2)
	{
		_zombie2 = zombie2;
	}

	public boolean isHasHit1()
	{
		return hasHit1;
	}

	public void setHasHit1(boolean hasHit1)
	{
		this.hasHit1 = hasHit1;
	}

	public boolean isHasHit2()
	{
		return hasHit2;
	}

	public void setHasHit2(boolean hasHit2)
	{
		this.hasHit2 = hasHit2;
	}

	@Override
	public void onEnd()
	{
		Gladiators game = (Gladiators) Manager.GetGame();

		for (Player p : game.GetPlayers(true))
		{
			p.teleport(game.GetTeam(p).GetSpawn().clone());
		}

		if (_zombie1 != null)
			_zombie1.remove();

		if (_zombie2 != null)
			_zombie2.remove();

		Arena gateArena = _host.getArenaByMid(getOrange());
		for (Location loc : gateArena.getDoorBlocks())
			loc.getBlock().setType(Material.FENCE); // Manual door close.

		// Spawns

		_host.GetTeamList().get(0).GetSpawns().clear();

		for (Arena a : _host.getGameArenaSet())
		{
			if (a.getCapacity() <= 0)
				continue;

			for (Location l : a.capacitySpawns())
				_host.GetTeamList().get(0).GetSpawns().add(l);
		}

		for (Player p : _host.GetPlayers(true))
		{
			_host.GetTeam(p).SpawnTeleport(p);
		}
	}
}