package nautilus.game.arcade.game.games.moba.modes;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.gadget.gadgets.gamemodifiers.moba.emblems.EmblemGadget;
import mineplex.core.game.GameDisplay;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;

public class MobaMap implements Listener
{

	protected final Moba _host;

	public MobaMap(Moba host)
	{
		_host = host;
	}

	@EventHandler
	public void spawnEmblems(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (GameTeam team : _host.GetTeamList())
		{
			List<Location> schematicSpawns = _host.WorldData.GetCustomLocs("EMBLEM " + team.GetName().toUpperCase());
			int index = 0;

			for (Player player : team.GetPlayers(true))
			{
				EmblemGadget gadget = (EmblemGadget) _host.getArcadeManager().getCosmeticManager().getGadgetManager().getGameCosmeticManager().getActiveCosmetic(
						player,
						GameDisplay.MOBA,
						"Emblems"
				);

				if (gadget == null)
				{
					continue;
				}
				else if (index == schematicSpawns.size())
				{
					break;
				}

				Location location = schematicSpawns.get(index++);
				gadget.buildAt(location);
			}
		}
	}
}
