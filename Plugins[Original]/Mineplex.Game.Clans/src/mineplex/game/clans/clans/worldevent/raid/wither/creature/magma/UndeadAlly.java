package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import mineplex.game.clans.clans.worldevent.raid.wither.challenge.six.ChallengeSix;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadMage;

public class UndeadAlly extends Cataclysm
{
	public UndeadAlly(ChallengeSix challenge, Magmus magmus)
	{
		super(challenge, magmus);
	}

	@Override
	protected String getAnnouncement()
	{
		return "Magmus has summoned an undead ally!";
	}

	@Override
	protected void onStart()
	{
		Challenge.getRaid().registerCreature(new UndeadMage(Challenge, Challenge.getRaid().getWorldData().getCustomLocs("C_SIX_C3S").get(0), false, null, null));
		end();
	}
	
	@Override
	protected void onEnd() {}

	@Override
	protected void tick() {}
}