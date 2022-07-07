package nautilus.game.arcade.game.games.smash.perks.golem;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.data.FissureData;

public class PerkFissure extends SmashPerk
{

	private int _cooldown;

	private Set<FissureData> _active = new HashSet<>();

	public PerkFissure()
	{
		super("Fissure", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Fissure" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Game", "You cannot use " + F.skill(GetName()) + " while airborne."));
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		Location location = player.getLocation();
		FissureData data = new FissureData(this, player, location.getDirection(), location.add(location.getDirection()).add(0, -0.4, 0));
		_active.add(data);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Set<FissureData> remove = new HashSet<>();

		for (FissureData data : _active)
		{
			if (data.Update())
			{
				remove.add(data);
			}
		}

		for (FissureData data : remove)
		{
			_active.remove(data);
			data.Clear();
		}
	}
}
