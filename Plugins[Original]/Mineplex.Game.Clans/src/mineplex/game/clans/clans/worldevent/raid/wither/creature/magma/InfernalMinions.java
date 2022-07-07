package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import org.bukkit.Location;

import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.six.ChallengeSix;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadKnight;

public class InfernalMinions extends Cataclysm
{
	public InfernalMinions(ChallengeSix challenge, Magmus magmus)
	{
		super(challenge, magmus);
	}

	@Override
	protected String getAnnouncement()
	{
		return "Infernal minions have flocked to Magmus' aid!";
	}

	@Override
	protected void onStart()
	{
		for (int i = 0; i < 20; i++)
		{
			Location loc = UtilMath.randomElement(Magmus.getChallenge().getRaid().getPlayers()).getLocation();
			if (UtilMath.r(2) == 1)
			{
				Challenge.getRaid().registerCreature(new UndeadKnight(Challenge, loc));
			}
			else
			{
				Challenge.getRaid().registerCreature(new BlazeMinion(Challenge, loc));
			}
		}
		end();
	}
	
	@Override
	protected void onEnd() {}

	@Override
	protected void tick() {}
}