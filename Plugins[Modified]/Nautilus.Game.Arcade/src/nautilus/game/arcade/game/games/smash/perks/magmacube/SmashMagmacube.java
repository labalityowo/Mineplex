package nautilus.game.arcade.game.games.smash.perks.magmacube;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.kit.perks.data.MeteorShowerData;

public class SmashMagmacube extends SmashUltimate
{

	private final List<MeteorShowerData> _meteors = new ArrayList<>();

	public SmashMagmacube()
	{
		super("Meteor Shower", new String[] {}, Sound.AMBIENCE_THUNDER, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		_meteors.add(new MeteorShowerData(player, UtilPlayer.getTarget(player, UtilBlock.blockPassSet, 128).getLocation(), getLength()));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_meteors.removeIf(MeteorShowerData::update);
	}
}
