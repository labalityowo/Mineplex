package nautilus.game.arcade.game.games.christmasnew.section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.lifetimes.SimpleLifetime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmas.Sleigh;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.present.Present;
import nautilus.game.arcade.world.WorldData;

public abstract class Section extends SimpleLifetime implements Listener, SectionRegister
{

	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.STAR)
			.withColor(Color.RED, Color.LIME)
			.withFade(Color.WHITE)
			.withFlicker()
			.withTrail()
			.build();
	private static final int SLEIGH_OFFSET_INFORM_SQUARED = 300;

	protected final ChristmasNew _host;
	protected final WorldData _worldData;

	private final Location _sleighTarget;
	private final List<SectionChallenge> _challenges;

	private int _timeSet;
	private boolean _informedTarget;
	private String _objectiveText;
	private double _objectivePercentage;

	public Section(ChristmasNew host, Location sleighTarget)
	{
		_host = host;
		_worldData = host.WorldData;
		_sleighTarget = sleighTarget;
		_challenges = new ArrayList<>(2);
		_challenges.forEach(this::register);
	}

	@Override
	public void start() throws IllegalStateException
	{
		super.start();
		onRegister();
		_challenges.forEach(SectionRegister::onRegister);
	}

	@Override
	public void end() throws IllegalStateException
	{
		super.end();
		onUnregister();
		_challenges.forEach(SectionRegister::onUnregister);
	}

	protected void registerChallenges(SectionChallenge... challenges)
	{
		List<SectionChallenge> challengesList = Arrays.asList(challenges);
		_challenges.addAll(challengesList);
		challengesList.forEach(this::register);
	}

	public boolean isComplete()
	{
		for (SectionChallenge challenge : _challenges)
		{
			if (challenge.getPresent() != null && !challenge.getPresent().isCollected())
			{
				return false;
			}
		}

		return true;
	}

	public void onPresentCollect(Player player, Present present)
	{
		_host.AddGems(player, 1, "Presents Collected", true, true);
		present.setCollected(player);

		Location location = present.getLocation();

		int left = 0;

		for (SectionChallenge challenge : _challenges)
		{
			Present otherPresent = challenge.getPresent();

			if (otherPresent == null)
			{
				continue;
			}

			if (present.equals(otherPresent) && present.isCollected())
			{
				present.deactivate();
				challenge.onPresentCollect();
			}

			left += otherPresent.getLeft();
		}

		_host.sendSantaMessage("Well done " + player.getName() + " you found a present!" + (left > 0 ? " Only " + left + " to go!" : ""), null);

		for (int i = 0; i < 2; i++)
		{
			UtilFirework.launchFirework(location, FIREWORK_EFFECT, null, UtilMath.r(3));
		}

		_host.getSleigh().AddPresent(location);
	}

	@EventHandler
	public void updateTarget(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _informedTarget)
		{
			return;
		}

		Sleigh sleigh = _host.getSleigh();

		if (UtilMath.offsetSquared(sleigh.GetLocation(), sleigh.getTarget()) < SLEIGH_OFFSET_INFORM_SQUARED)
		{
			_informedTarget = true;
			setObjective("Collect the presents");
			onSantaTarget();
		}
	}

	@EventHandler
	public void updateObjective(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !_host.IsLive())
		{
			return;
		}

		UtilTextTop.displayProgress(C.cYellowB + _objectiveText, _objectivePercentage, UtilServer.getPlayers());
	}

	public void onSantaTarget()
	{
	}

	public void setTimeSet(int timeSet)
	{
		_timeSet = timeSet;
	}

	public int getTimeSet()
	{
		return _timeSet;
	}

	public void setObjective(String objectiveText)
	{
		setObjective(objectiveText, 1);
	}

	public void setObjective(String objectiveText, double objectivePercentage)
	{
		_objectiveText = objectiveText;
		_objectivePercentage = objectivePercentage;
	}

	public String getObjectiveText()
	{
		return _objectiveText;
	}

	public Location getSleighTarget()
	{
		return _sleighTarget;
	}
}
