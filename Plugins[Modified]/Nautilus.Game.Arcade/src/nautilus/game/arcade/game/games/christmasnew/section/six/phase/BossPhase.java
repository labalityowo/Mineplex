package nautilus.game.arcade.game.games.christmasnew.section.six.phase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.BossAttack;

public abstract class BossPhase extends SectionChallenge
{

	private static final long DEFAULT_ATTACK_DELAY = TimeUnit.SECONDS.toMillis(2);
	private static final PotionEffect SLOW = new PotionEffect(PotionEffectType.SLOW, 120, 6, false, false);

	private final List<BossAttack> _attacks;
	private BossAttack _currentAttack;

	protected long _start;
	protected LivingEntity _boss;
	private Location _bossSpawn;

	protected long _attackDelay;
	private long _lastAttackComplete;

	BossPhase(ChristmasNew host, Section section)
	{
		super(host, null, section);

		_attacks = new ArrayList<>();
		_attackDelay = DEFAULT_ATTACK_DELAY;
	}

	public abstract boolean isComplete();

	public abstract void onAttack(BossAttack attack);

	@Override
	public void onPresentCollect()
	{
	}

	@Override
	public void activate()
	{
		super.activate();

		_start = System.currentTimeMillis();
	}

	@Override
	public void deactivate()
	{
		super.deactivate();

		if (_currentAttack != null)
		{
			_currentAttack.stop();
		}
	}

	protected void addAttacks(BossAttack... attacks)
	{
		_attacks.addAll(Arrays.asList(attacks));
	}

	protected void clearAttacks()
	{
		_attacks.clear();
	}

	@EventHandler
	public void updateAttackStart(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		if (_currentAttack == null && UtilTime.elapsed(_lastAttackComplete, _attackDelay))
		{
			_currentAttack = UtilAlg.Random(_attacks);

			if (_currentAttack == null)
			{
				return;
			}

			onAttack(_currentAttack);
			_currentAttack.start();
		}
		else if (_currentAttack != null && _currentAttack.isComplete())
		{
			_lastAttackComplete = System.currentTimeMillis();
			_currentAttack.stop();
			_currentAttack = null;
		}
	}

	protected void stopBossMovement()
	{
		_boss.addPotionEffect(SLOW);
	}

	public void setBoss(LivingEntity boss)
	{
		_boss = boss;
		_bossSpawn = boss.getLocation();
		_lastAttackComplete = System.currentTimeMillis();
	}

	public LivingEntity getBoss()
	{
		return _boss;
	}

	public Location getBossSpawn()
	{
		return _bossSpawn;
	}
}
