package nautilus.game.arcade.game.games.minecraftleague.data;

import java.util.concurrent.ConcurrentHashMap;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import nautilus.game.arcade.game.GameTeam;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;

public class TowerAlert
{
	private ConcurrentHashMap<GameTeam, Long> _alerts = new ConcurrentHashMap<GameTeam, Long>();
	//private ConcurrentHashMap<GameTeam, String> _alertType = new ConcurrentHashMap<GameTeam, String>();
	
	public void alert(GameTeam team, TeamTowerBase tower)
	{
		if (!UtilTime.elapsed(_alerts.getOrDefault(team, (long) 1), UtilTime.convert(7, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
		{
			for (Player player : team.GetPlayers(true))
				playSound(player);
			
			return;
		}
		
		_alerts.put(team, System.currentTimeMillis());
		
		String type = "";
		if (tower instanceof TeamTower)
		{
			int number = ((TeamTower)tower).Number;
			if (number == 1)
				/*_alertType.put(team, */type = "First Tower";//);
			else
				/*_alertType.put(team, */type = "Second Tower";//);
		}
		else
			/*_alertType.put(team, */type = "Crystal";//);
		
		showAlert(team, type);
	}
	
	private void showAlert(GameTeam team, String type)
	{
		/*for (GameTeam team : _alerts.keySet())
		{*/
			for (Player player : team.GetPlayers(true))
			{
				UtilTextMiddle.display("", C.cGold + "Your " + /*_alertType.get(team)*/type + " is under attack!", 0, 20 * 5, 0, player);
				playSound(player);
			}
			
			/*if (UtilTime.elapsed(_alerts.get(team), UtilTime.convert(5, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
			{
				_alerts.remove(team);
				_alertType.remove(team);
			}*/
		//}
	}
	
	private void playSound(Player player)
	{
		player.playNote(player.getLocation(), Instrument.PIANO, Note.sharp(1, Tone.A));
	}
}
