package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;

import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.game.games.christmasnew.section.SectionRegister;
import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public abstract class BossAttack implements SectionRegister, Listener
{

	protected final BossPhase _phase;
	protected LivingEntity _boss;
	protected long _start;
	private boolean _allowsMovement;

	BossAttack(BossPhase phase)
	{
		_phase = phase;
	}

	public abstract boolean isComplete();

	public void start()
	{
		_start = System.currentTimeMillis();
		_boss = _phase.getBoss();
		UtilServer.RegisterEvents(this);
		onRegister();
	}

	public void stop()
	{
		onUnregister();
		UtilServer.Unregister(this);
	}

	public void setAllowsMovement(boolean allowsMovement)
	{
		_allowsMovement = allowsMovement;
	}

	public boolean isAllowingMovement()
	{
		return _allowsMovement;
	}
}
