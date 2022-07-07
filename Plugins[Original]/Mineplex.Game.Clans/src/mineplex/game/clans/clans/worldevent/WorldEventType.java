package mineplex.game.clans.clans.worldevent;

import java.util.function.Function;

import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemBoss;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonBoss;
import mineplex.game.clans.clans.worldevent.capturepoint.CapturePointEvent;
import mineplex.game.clans.clans.worldevent.undead.UndeadCity;

public enum WorldEventType
{
	//SLIME_KING("Slime King", SlimeBoss.class, 30),
	//KING_OF_THE_HILL("King of The Hill", KingHill.class, 30),
	//UNDEAD_CAMP("Undead Camp", UndeadCamp.class, 30),
	//IRON_WIZARD("Iron Wizard", GolemBoss.class, 30),
	//BROOD_MOTHER("Brood Mother", SpiderBoss.class, 30),
	//SKELETON_KING("Skeleton King", SkeletonBoss.class, 30)
	IRON_WIZARD("Iron Wizard", GolemBoss::new),
	SKELETON_KING("Skeleton King", SkeletonBoss::new),
	CAPTURE_POINT("Capture Point", CapturePointEvent::new),
	UNDEAD_CITY("Undead City", UndeadCity::new)
	;
	
	private String _name;
	private Function<WorldEventManager, WorldEvent> _creator;
	
	private WorldEventType(String name, Function<WorldEventManager, WorldEvent> creator)
	{
		_name = name;
		_creator = creator;
	}
	
	public WorldEvent createInstance(WorldEventManager eventManager)
	{
		WorldEvent worldEvent = null;
		
		if (_creator != null)
		{
			worldEvent = _creator.apply(eventManager);
		}
		
		return worldEvent;
	}

	public String getName()
	{
		return _name;
	}
}