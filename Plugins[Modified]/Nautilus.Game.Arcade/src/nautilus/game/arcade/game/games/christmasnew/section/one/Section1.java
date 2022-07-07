package nautilus.game.arcade.game.games.christmasnew.section.one;

import org.bukkit.Location;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;

public class Section1 extends Section
{

	public Section1(ChristmasNew host, Location sleighTarget, Location... presents)
	{
		super(host, sleighTarget);

		registerChallenges(
				new CaveMaze(host, presents[0], this),
				new SectionChallenge(host, presents[1], this)
				{
					@Override
					public void onPresentCollect()
					{

					}

					@Override
					public void onRegister()
					{

					}

					@Override
					public void onUnregister()
					{

					}
				}
		);
	}

	@Override
	public void onRegister()
	{

	}

	@Override
	public void onUnregister()
	{
		_host.sendSantaMessage("Follow me.", ChristmasNewAudio.SANTA_FOLLOW_ME);
	}

	@Override
	public void onSantaTarget()
	{
		_host.sendSantaMessage("Look! I see one up in those trees. Follow the ornaments in the trees to find the present.", ChristmasNewAudio.SANTA_TREES);
		_host.getArcadeManager().runSyncLater(() -> _host.sendSantaMessage("There’s another one in the mine over there. Watch out for the monsters.", ChristmasNewAudio.SANTA_MINE), 150);
	}
}
