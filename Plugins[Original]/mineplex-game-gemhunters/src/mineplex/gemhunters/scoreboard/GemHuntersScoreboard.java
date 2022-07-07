package mineplex.gemhunters.scoreboard;

import mineplex.gemhunters.progression.ProgressionModule;
import mineplex.gemhunters.progression.ProgressionTitle;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.scoreboard.WritableMineplexScoreboard;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.playerstatus.PlayerStatus;
import mineplex.gemhunters.playerstatus.PlayerStatusModule;
import mineplex.gemhunters.supplydrop.SupplyDropModule;

public class GemHuntersScoreboard extends WritableMineplexScoreboard
{
	
	private final EconomyModule _economy;
	private final PlayerStatusModule _playerStatus;
	private final ProgressionModule _progression;
	private final SupplyDropModule _supplyDrop;
	
	public GemHuntersScoreboard(Player player)
	{
		super(player);

		_economy = Managers.get(EconomyModule.class);
		_playerStatus = Managers.get(PlayerStatusModule.class);
		_progression = Managers.get(ProgressionModule.class);
		_supplyDrop = Managers.get(SupplyDropModule.class);
	}

	public void writeContent(Player player)
	{
		writeNewLine();

		write(C.cGreenB + "Gems Earned");
		write(String.valueOf(_economy.getGems(player)));
		
		writeNewLine();

		write(C.cGoldB + "Supply Drop");
		if (_supplyDrop.isActive())
		{
			write(_supplyDrop.getActive().getName() + " - " + (int) UtilMath.offset(player.getLocation(), _supplyDrop.getActive().getChestLocation()) + "m");
		}
		else
		{
			write(UtilTime.MakeStr(_supplyDrop.getLastSupplyDrop() + _supplyDrop.getSequenceTimer() - System.currentTimeMillis()));
		}
		
		writeNewLine();
		
		write(C.cYellowB + "Player Status");
		PlayerStatus status = _playerStatus.Get(player);
		write(status.getStatusType().getName() + (status.getLength() > 0 ? " (" + (status.getStart() + status.getLength() - System.currentTimeMillis()) / 1000 + ")" : ""));
		
		writeNewLine();
	}

	public int getUndernameScore(Player player)
	{
		return _economy.getGems(player);
	}

	public String getPrefix(Player perspective, Player subject)
	{
		return _progression.getTitle(_economy.getGems(subject)).getTitle() + " " + C.cYellow;
	}
}
