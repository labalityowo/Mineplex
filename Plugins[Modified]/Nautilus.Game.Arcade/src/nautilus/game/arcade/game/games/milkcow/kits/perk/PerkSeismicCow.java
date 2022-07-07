package nautilus.game.arcade.game.games.milkcow.kits.perk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkSeismicCow extends Perk
{

	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(6);
	private static final long LEAP_TIME = TimeUnit.SECONDS.toMillis(1);
	private static final int DAMAGE = 4;
	private static final int RANGE = 6;

	private final Map<Player, Long> _live = new HashMap<>();

	public PerkSeismicCow()
	{
		super("Body Slam", new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " with Shovel to " + C.cGreen + "Body Slam"
				});
	}

	@Override
	public void unregisteredEvents()
	{
		_live.clear();
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Block block = event.getClickedBlock();

		if (UtilBlock.usable(block))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!UtilItem.isSpade(itemStack) || !hasPerk(player) || !Recharge.Instance.use(player, GetName(), COOLDOWN, true, true))
		{
			return;
		}

		Vector direction = player.getLocation().getDirection();

		if (direction.getY() < 0)
		{
			direction.setY(direction.getY() * -1);
		}

		UtilAction.velocity(player, direction);

		_live.put(player, System.currentTimeMillis());

		player.sendMessage(F.main(Manager.getName(), "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void updateSlam(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!UtilEnt.isGrounded(player))
			{
				continue;
			}

			Long last = _live.get(player);

			if (last == null || !UtilTime.elapsed(last, LEAP_TIME))
			{
				return;
			}

			_live.remove(player);

			Location location = player.getLocation();

			UtilEnt.getInRadius(location, RANGE).forEach((entity, scale) ->
			{
				if (entity.equals(player) || entity instanceof Cow)
				{
					return;
				}

				Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, DAMAGE * scale + 0.5, false, true, false, player.getName(), GetName());

				UtilAction.velocity(entity, UtilAlg.getTrajectory2d(location, entity.getLocation()), 1.8 * scale, true, 0, 0.4 + 1.0 * scale, 1.6, true);

				Manager.GetCondition().Factory().Falling(GetName(), entity, player, 10, false, true);

				if (entity instanceof Player)
				{
					player.sendMessage(F.main(Manager.getName(), F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));
				}
			});

			location.getWorld().playSound(location, Sound.ZOMBIE_WOOD, 2f, 0.2f);

			UtilBlock.getBlocksInRadius(player.getLocation(), 4).forEach(block ->
			{
				if (UtilBlock.airFoliage(block.getRelative(BlockFace.UP)) && !UtilBlock.airFoliage(block))
				{
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
				}
			});
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_live.remove(event.getPlayer());
	}
}
