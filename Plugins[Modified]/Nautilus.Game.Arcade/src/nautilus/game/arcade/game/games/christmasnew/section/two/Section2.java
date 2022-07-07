package nautilus.game.arcade.game.games.christmasnew.section.two;

import org.bukkit.Location;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;

public class Section2 extends Section
{

	public Section2(ChristmasNew host, Location sleighTarget, Location... presents)
	{
		super(host, sleighTarget);

		registerChallenges(
				new IceMaze(host, presents[0], this),
				new RockParkour(host, presents[1], this)
		);

		setTimeSet(6000);
	}

	@Override
	public void onRegister()
	{

	}

	@Override
	public void onUnregister()
	{
		_host.sendSantaMessage("Good job!", ChristmasNewAudio.SANTA_GOOD_JOB);
	}

	@Override
	public void onSantaTarget()
	{
		_host.sendSantaMessage("Oh no! Those spiders looks particularly nasty.", ChristmasNewAudio.SANTA_SPIDERS);
		_host.getArcadeManager().runSyncLater(() -> _host.sendSantaMessage("There’s another present hidden within that ice maze.", ChristmasNewAudio.SANTA_ICE_MAZE), 150);
	}
}
