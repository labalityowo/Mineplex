package nautilus.game.arcade.game.games.christmasnew.section.three;

import org.bukkit.Location;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;

public class Section3 extends Section
{

	private final CaveIn _challenge;

	public Section3(ChristmasNew host, Location sleighTarget, Location... presents)
	{
		super(host, sleighTarget);

		_challenge = new CaveIn(host, presents[0], this);

		registerChallenges(_challenge);

		setTimeSet(9000);
	}

	@Override
	public void onRegister()
	{

	}

	@Override
	public void onUnregister()
	{
		_host.sendSantaMessage("Let's go!", ChristmasNewAudio.SANTA_LETS_GO);
	}

	@Override
	public boolean isComplete()
	{
		return super.isComplete() && _challenge.isCleared();
	}
}
