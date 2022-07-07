package nautilus.game.arcade.game.games.christmasnew.section.two;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;

class RockParkour extends SectionChallenge
{

	private static final int MAX_MOBS = 25;
	private static final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false);

	private final List<Location> _mobSpawns;

	private boolean _spawn;

	RockParkour(ChristmasNew host, Location present, Section section)
	{
		super(host, present, section);

		_mobSpawns = _worldData.GetDataLocs("YELLOW");
	}

	@Override
	public void onPresentCollect()
	{

	}

	@Override
	public void onRegister()
	{
		_host.getArcadeManager().runSyncLater(() -> _spawn = true, 400);
	}

	@Override
	public void onUnregister()
	{

	}

	@EventHandler
	public void updateMobs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_spawn || _entities.size() > MAX_MOBS)
		{
			return;
		}

		Spider spawned = spawn(UtilAlg.Random(_mobSpawns), Spider.class);
		spawned.setRemoveWhenFarAway(false);
		spawned.addPotionEffect(SPEED);

		_entities.forEach(entity ->
		{
			if (entity instanceof Spider)
			{
				Spider spider = (Spider) entity;

				if (spider.getTarget() == null)
				{
					spider.setTarget(UtilPlayer.getClosest(entity.getLocation()));
				}
			}
		});
	}
}
