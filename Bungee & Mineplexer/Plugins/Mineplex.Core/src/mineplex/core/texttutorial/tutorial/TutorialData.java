package mineplex.core.texttutorial.tutorial;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;

public class TutorialData
{
	private Player _player;
	private Phase _phase;
	private int _phaseStep;
	private int _textStep;
	private long _sleep;

	public TutorialData(Player player, Phase startPhase)
	{
		_player = player;
		_phase = startPhase;
		_phaseStep = 0;
		_textStep = 0;
		_sleep = System.currentTimeMillis() + 3000;
	}

	public boolean tick()
	{
		if (!_player.getLocation().equals(_phase.getLocation()))
			_player.teleport(_phase.getLocation());

		if (System.currentTimeMillis() < _sleep)
			return false;

		if (_textStep >= _phase.getText().length)
		{
			// No more text to display, move to next phase
			_phaseStep++;
			_sleep = System.currentTimeMillis() + 2000;

			return true;
		}

		// Display Text
		String text = _phase.getText()[_textStep];

		UtilPlayer.message(_player, " ");
		UtilPlayer.message(_player, " ");
		UtilPlayer.message(_player, " ");
		UtilPlayer.message(_player, C.cGreen + C.Strike + C.Bold + "========================================");
		UtilPlayer.message(_player, C.cGold + C.Bold + _phase.getHeader());
		UtilPlayer.message(_player, " ");

		for (int i=0 ; i<=_textStep ; i++)
			UtilPlayer.message(_player, "  " + _phase.getText()[i]);

		for (int i=_textStep ; i<=5 ; i++)
			UtilPlayer.message(_player, " ");

		UtilPlayer.message(_player, C.cGreen + C.Strike + C.Bold + "========================================");

		if (text.length() > 0)
		{
			_player.playSound(_player.getLocation(), Sound.ORB_PICKUP, 2f, 1.5f);
			_sleep = System.currentTimeMillis() + 1000 + (50*text.length());
		}
		else
		{
			_sleep = System.currentTimeMillis() + 600;
		}

		_textStep++;

		return false;
	}

	public void setNextPhase(Phase phase)
	{
		_phase = phase;
		_textStep = 0;
		_player.teleport(_phase.getLocation());
	}

	public Phase getPhase()
	{
		return _phase;
	}

	public long getSleep()
	{
		return _sleep;
	}

	public int getPhaseStep()
	{
		return _phaseStep;
	}

	public Player getPlayer()
	{
		return _player;
	}
}
