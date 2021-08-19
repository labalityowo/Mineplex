package nautilus.game.arcade.stats;

import java.util.ArrayList;
import java.util.List;

import mineplex.minecraft.game.classcombat.Skill.Mage.LightningOrb.LightningOrbEvent;
import nautilus.game.arcade.game.Game;

import nautilus.game.arcade.game.GameTeam;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class ElectrocutionStatTracker extends StatTracker<Game>
{
	public ElectrocutionStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onLightningOrb(LightningOrbEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;
		
		List<Player> viable = new ArrayList<Player>();

		GameTeam playerTeam = getGame().GetTeam(event.getPlayer());
		
		for (LivingEntity en : event.getStruck())
		{
			if (en instanceof Player)
			{
				Player struckPlayer = (Player) en;

				GameTeam struckTeam = getGame().GetTeam(struckPlayer);

				if (struckTeam != null && playerTeam != null && struckTeam == playerTeam)
				{
					continue;
				}

				viable.add(struckPlayer);
			}
		}
		
		if (viable.size() >= 4)
		{
			addStat(event.getPlayer(), "MassElectrocution", 1, true, false);
		}
	}
}
