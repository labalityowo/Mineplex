package nautilus.game.arcade.game.games.moba.kit.bardolf;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.kit.bardolf.HeroBardolf.WolfData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SkillWolfPounce extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Dash into the air",
			"Your wolves will follow you."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);
	private static final int COOLDOWN = 7000;

	private final Set<PounceData> _data = new HashSet<>(2);

	public SkillWolfPounce(int slot)
	{
		super("Wolf Pounce", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(COOLDOWN);
		setSneakActivate(true);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_data.removeIf(data -> data.Leader.equals(event.getPlayer()));
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		useSkill(player);
	}

	@EventHandler
	public void toggleSneak(PlayerToggleSneakEvent event)
	{
		if (!isSkillSneak(event))
		{
			return;
		}

		Player player = event.getPlayer();

		useSkill(player);
	}

	@Override
	public void useSkill(Player player)
	{
		super.useSkill(player);

		WolfData data = ((HeroBardolf) Kit).getWolfData(player);

		if (data == null)
		{
			return;
		}

		for (Wolf wolf : data.getWolves())
		{
			data.getOverrideTarget().put(wolf, player.getLocation());
		}

		Vector direction = player.getLocation().getDirection();
		direction.setY(Math.max(0.8, direction.getY()));
		_data.add(new PounceData(player, data, direction));

		UtilAction.velocity(player, direction);

		player.getWorld().playSound(player.getLocation(), Sound.WOLF_BARK, 2, 1F);
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, player.getLocation().add(0, 0.6, 0), 0.5F, 0.5F, 0.5F, 0.1F, 15, ViewDist.LONG);
	}

	@EventHandler
	public void updateWolves(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		Iterator<PounceData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			PounceData data = iterator.next();

			for (Wolf wolf : data.WolfData.getWolves())
			{
				// Wolf has already leaped
				if (data.LeapedWolves.contains(wolf) || UtilMath.offsetSquared(data.Target, wolf.getLocation()) > 4)
				{
					continue;
				}

				data.LeapedWolves.add(wolf);
				wolf.setVelocity(data.Direction);
				wolf.getWorld().playSound(wolf.getLocation(), Sound.WOLF_WHINE, 1, 1.5F);
				data.WolfData.getOverrideTarget().remove(wolf);
			}

			if (UtilTime.elapsed(data.Time, COOLDOWN - 500))
			{
				iterator.remove();
				data.WolfData.getOverrideTarget().clear();
			}
		}
	}

	private class PounceData
	{

		Player Leader;
		WolfData WolfData;
		Location Target;
		Vector Direction;
		long Time;
		List<Wolf> LeapedWolves;

		PounceData(Player leader, WolfData wolfData, Vector direction)
		{
			Leader = leader;
			WolfData = wolfData;
			Target = leader.getLocation();
			Direction = direction.multiply(1.5);
			Time = System.currentTimeMillis();
			LeapedWolves = new ArrayList<>(5);
		}
	}
}

