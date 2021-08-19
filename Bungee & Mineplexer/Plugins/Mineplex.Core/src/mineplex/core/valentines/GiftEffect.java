package mineplex.core.valentines;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;

public class GiftEffect
{
	private static final int ANIMATION_LENGTH = 20 * 5;
	private static final double CIRCLE_RADIUS = 3;
	private static final double CIRCLE_HEIGHT = 3;

	private Player _from;
	private Player _to;
	private String _fromGift;
	private String _toGift;
	private Location _centerLocation;
	private boolean _finished;
	private int _ticks;

	public GiftEffect(Player from, Player to, String fromGift, String toGift, Location centerLocation)
	{
		_from = from;
		_to = to;
		_fromGift = fromGift;
		_toGift = toGift;
		_centerLocation = centerLocation;
		_finished = false;
		_ticks = 0;

//		_to.setWalkSpeed(0.0F);
//		_from.setWalkSpeed(0.0F);
	}

	public void tick()
	{
		_ticks++;

		if (_ticks == 1)
		{
			if (_to.isOnline())
			{
				UtilTextMiddle.display("", C.cYellow + _from.getName() + C.cPurple + " is sharing a Gift with you", _to);
				_to.playSound(_to.getLocation(), Sound.CAT_MEOW, 1f, 1f);;
			}
			if (_from.isOnline())
			{
				UtilTextMiddle.display("", C.cPurple + "You are sharing a Gift with " + C.cYellow + _to.getName(), _from);
				_from.playSound(_from.getLocation(), Sound.CAT_MEOW, 1f, 1f);
			}
		}
		else if (_ticks == 40)
		{
			if (_to.isOnline())
			{
				UtilTextMiddle.display("", C.cRed + "3", _to);
				_to.playSound(_to.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - 0.3f);
			}
			if (_from.isOnline())
			{
				UtilTextMiddle.display("", C.cRed + "3", _from);
				_from.playSound(_from.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - 0.3f);
			}
		}
		else if (_ticks == 60)
		{
			if (_to.isOnline())
			{
				UtilTextMiddle.display("", C.cGold + "2", _to);
				_to.playSound(_to.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - 0.2f);
			}
			if (_from.isOnline())
			{
				UtilTextMiddle.display("", C.cGold + "2", _from);
				_from.playSound(_from.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - 0.2f);
			}
		}
		else if (_ticks == 80)
		{
			if (_to.isOnline())
			{
				UtilTextMiddle.display("", C.cGreen + "1", _to);
				_to.playSound(_to.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - 0.1f);
			}
			if (_from.isOnline())
			{
				UtilTextMiddle.display("", C.cGreen + "1", _from);
				_from.playSound(_from.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - 0.1f);
			}
		}
		else if (_ticks == 100)
		{
			if (_to.isOnline())
			{
				UtilTextMiddle.display("", C.cPurple + "You received " + _toGift, _to);
				_to.playSound(_to.getEyeLocation(), Sound.LEVEL_UP, 1F, 1F);
				UtilPlayer.message(_to, F.main("Gift", "You received " + F.elem(_toGift) + "!"));
				UtilPlayer.message(_to, F.main("Gift", F.name(_from.getName()) + " received " + F.elem(_fromGift) + "!"));
			}
			if (_from.isOnline())
			{
				UtilTextMiddle.display("", C.cPurple + "You received " + _fromGift, _from);
				_from.playSound(_from.getEyeLocation(), Sound.LEVEL_UP, 1F, 1F);
				UtilPlayer.message(_from, F.main("Gift", "You received " + F.elem(_fromGift) + "!"));
				UtilPlayer.message(_from, F.main("Gift", F.name(_to.getName()) + " received " + F.elem(_toGift) + "!"));
			}
		}

		double yAdd = CIRCLE_HEIGHT * ((double) _ticks) / ANIMATION_LENGTH;
		double xAdd = CIRCLE_RADIUS * Math.sin(_ticks / 10.0 * Math.PI);
		double zAdd = CIRCLE_RADIUS * Math.cos(_ticks / 10.0 * Math.PI);

		UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.HEART, _centerLocation.clone().add(xAdd, yAdd, zAdd), 0.5f, 0.5f, 0.5f, 0, 5, UtilParticle.ViewDist.NORMAL);

		if (_ticks % 10 == 0)
			UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.FIREWORKS_SPARK, _centerLocation.clone().add(0, 1, 0), 3f, 3f, 3f, 0, 10, UtilParticle.ViewDist.NORMAL);

		if (_ticks >= ANIMATION_LENGTH)
		{
			UtilFirework.playFirework(_centerLocation.clone().add(0, 3, 0), FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.RED).withFade(Color.WHITE).withFlicker().build());
			_finished = true;
//			if (_to.isOnline()) _to.setWalkSpeed(0.2F);
//			if (_from.isOnline()) _from.setWalkSpeed(0.2F);
		}
	}

	public boolean isFinished()
	{
		return _finished;
	}
}
