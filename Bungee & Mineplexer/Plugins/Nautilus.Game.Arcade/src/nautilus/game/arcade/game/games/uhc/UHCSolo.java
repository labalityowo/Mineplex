package nautilus.game.arcade.game.games.uhc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;

public class UHCSolo extends UHC
{
	
	public UHCSolo(ArcadeManager manager)
	{
		this(manager, GameType.UHCSolo);
	}

	public UHCSolo(ArcadeManager manager, GameType type)
	{
		this(manager, type, false);
	}
	
	public UHCSolo(ArcadeManager manager, GameType type, boolean speedMode)
	{
		super(manager, type, speedMode);
		
		DamageTeamSelf = true;
		SpawnNearAllies = false;
	}
	
	@EventHandler
	public void playerTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}
		
		_teamList = new ArrayList<>(Arrays.asList(_teamList.get(0)));
		
		for (GameTeam team : _teamList)
		{
			team.SetName("Players");
			team.SetColor(ChatColor.YELLOW);
		}
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			List<Player> places = GetTeamList().get(0).GetPlacements(true);

			if (places.isEmpty() || !places.get(0).isOnline())
				return Arrays.asList();
			else
				return Arrays.asList(places.get(0));
		}
		else
			return null;
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
			return null;

		List<Player> losers = GetTeamList().get(0).GetPlayers(false);

		losers.removeAll(winners);

		return losers;
	}

	@Override
	public String GetMode()
	{
		return "UHC Solo";
	}

}
