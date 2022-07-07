package nautilus.game.arcade.game.games.smash.perks.witherskeleton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.kits.KitWitherSkeleton;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashWitherSkeleton extends SmashUltimate
{

	public SmashWitherSkeleton()
	{
		super("Wither Form", new String[] {}, Sound.WITHER_SPAWN, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		if (Kit instanceof KitWitherSkeleton)
		{
			KitWitherSkeleton kit = (KitWitherSkeleton) Kit;
			kit.disguise(player, DisguiseWither.class);
			kit.giveSmashItems(player);
		}
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		SmashKit kit = (SmashKit) Kit;
		kit.disguise(player);

		player.setFlying(false);
	}

	@EventHandler
	public void witherBump(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (UUID uuid : getLastUltimate().keySet())
		{
			Player player = UtilPlayer.searchExact(uuid);

			if (player == null)
			{
				continue;
			}

			List<Location> collisions = new ArrayList<>();

			// Bump
			for (Block block : UtilBlock.getInRadius(player.getLocation().add(0, 0.5, 0), 1.5d).keySet())
			{
				if (!UtilBlock.airFoliage(block))
				{
					collisions.add(block.getLocation().add(0.5, 0.5, 0.5));
				}
			}

			Vector vec = UtilAlg.getAverageBump(player.getLocation(), collisions);

			if (vec == null)
			{
				continue;
			}
			
			UtilAction.velocity(player, vec, 0.6, false, 0, 0.4, 10, true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void witherMeleeCancel(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Player player = event.GetDamagerPlayer(true);

		if (player == null)
		{
			return;
		}

		if (!isUsingUltimate(player))
		{
			return;
		}

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		event.SetCancelled("Wither Form Melee Cancel");
	}

	@EventHandler
	public void witherFlight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (UUID uuid : getLastUltimate().keySet())
		{
			Player player = UtilPlayer.searchExact(uuid);

			if (player == null)
			{
				continue;
			}

			if (player.isFlying())
			{
				continue;
			}

			player.setAllowFlight(true);
			player.setFlying(true);
		}
	}
}
