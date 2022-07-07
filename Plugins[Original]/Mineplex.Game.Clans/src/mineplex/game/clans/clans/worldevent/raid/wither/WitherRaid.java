package mineplex.game.clans.clans.worldevent.raid.wither;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.RaidManager;
import mineplex.game.clans.clans.worldevent.raid.RaidType;
import mineplex.game.clans.clans.worldevent.raid.RaidWorldEvent;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.five.ChallengeFive;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.four.ChallengeFour;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.one.ChallengeOne;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.seven.ChallengeSeven;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.six.ChallengeSix;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.three.ChallengeThree;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.two.ChallengeTwo;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class WitherRaid extends RaidWorldEvent
{
	private List<Class<? extends RaidChallenge<WitherRaid>>> _challenges;
	private RaidChallenge<WitherRaid> _currentChallenge;
	
	public WitherRaid(mineplex.game.clans.clans.worldevent.raid.WorldData data, RaidManager manager)
	{
		super(RaidType.CHARLES_WITHERTON.getRaidName(), data, manager);
	}
	
	private void nextChallenge()
	{
		_currentChallenge = null;
		
		if (_challenges.isEmpty())
		{
			return;
		}
		
		Class<? extends RaidChallenge<WitherRaid>> clazz = _challenges.remove(0);
		try
		{
			_currentChallenge = clazz.getConstructor(WitherRaid.class).newInstance(this);
			_currentChallenge.start();
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
	}
	
	private void teleportToAltar()
	{
		Location altar = WorldData.getCustomLocs("MAIN_ALTAR").get(0);
		getPlayers().forEach(player -> player.teleport(altar));
	}

	@Override
	protected void customLoad()
	{
		WorldData.World.setGameRuleValue("doDaylightCycle", "false");
		WorldData.World.setTime(14000);
		_challenges = new LinkedList<>();
		_challenges.add(ChallengeOne.class);
		_challenges.add(ChallengeTwo.class);
		_challenges.add(ChallengeThree.class);
		_challenges.add(ChallengeFour.class);
		_challenges.add(ChallengeFive.class);
		_challenges.add(ChallengeSix.class);
		_challenges.add(ChallengeSeven.class);
	}
	
	@Override
	protected void afterTeleportIn()
	{
		nextChallenge();
	}

	@Override
	protected void customTick()
	{
		if (_currentChallenge != null)
		{
			if (_currentChallenge.isComplete())
			{
				if (_challenges.size() < 5)
				{
					teleportToAltar();
				}
				nextChallenge();
			}
		}
	}

	@Override
	protected void customStop()
	{
		if (_currentChallenge != null)
		{
			_currentChallenge.complete(false);
		}
	}
	
	@EventHandler
	public void onBlockToss(SkillTriggerEvent event)
	{
		if (event.GetSkillName().equals("Block Toss") && getPlayers().contains(event.GetPlayer()))
		{
			event.SetCancelled(true);
		}
	}
}