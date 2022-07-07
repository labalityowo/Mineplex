package nautilus.game.arcade.game.games.moba.kit.dana;

import mineplex.core.common.events.EntityVelocityChangeEvent;
import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaParticles;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SkillRally extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"You leap up into the air, and upon landing",
			"you plant a banner that heals nearby allies",
			"and minions."
	};

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);

	private Set<RallyData> _data = new HashSet<>();

	public SkillRally(int slot)
	{
		super("Rally", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(40000);
		setDropItemActivate(true);
	}

	@Override
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		Vector vector = player.getLocation().getDirection();

		for (RallyData data : _data)
		{
			if (data.Owner.equals(player))
			{
				return;
			}
		}

		vector.setY(1.5);

		UtilAction.velocity(player, vector);
		_data.add(new RallyData(player));
		broadcast(player);
	}

	@EventHandler
	public void updateLand(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<RallyData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			RallyData data = iterator.next();
			Player player = data.Owner;

			if (data.Landed)
			{
				if (UtilTime.elapsed(data.LandTime, 7000))
				{
					iterator.remove();
				}
				else
				{
					for (Player nearby : UtilPlayer.getNearby(data.Banner, 5))
					{
						// Only heal allies
						if (!isTeamDamage(nearby, player))
						{
							continue;
						}

						if (Math.random() > 0.75)
						{
							MobaParticles.healing(nearby, 1);
						}
						Manager.GetCondition().Factory().Regen(GetName(), nearby, data.Owner, 3, 1, false, true, false);
					}
				}
			}
			else if (UtilTime.elapsed(data.LaunchTime, 1000) && UtilEnt.isGrounded(data.Owner))
			{
				data.LandTime = System.currentTimeMillis();
				data.Landed = true;
				Location location = data.Owner.getLocation();
				data.Banner = location;

				useActiveSkill(player, 7000);

				GameTeam team = Manager.GetGame().GetTeam(player);
				Block block = location.getBlock();

				Manager.GetBlockRestore().add(block, Material.STANDING_BANNER.getId(), (byte) 0, 7500);

				Banner banner = (Banner) block.getState();
				banner.setBaseColor(team.GetColor() == ChatColor.RED ? DyeColor.RED : DyeColor.BLUE);
				banner.addPattern(getPattern(team));
				banner.update();

				for (Block nearby : UtilBlock.getBlocksInRadius(banner.getLocation(), 5))
				{
					if (UtilBlock.airFoliage(nearby))
					{
						continue;
					}

					Manager.GetBlockRestore().add(nearby, Material.STAINED_CLAY.getId(), team.GetColorData(), (long) (7000 + (Math.random() * 500)));
					if (Math.random() > 0.9)
					{
						nearby.getWorld().playEffect(nearby.getLocation(), Effect.STEP_SOUND, Material.STAINED_CLAY, team.GetColorData());
					}
				}

				for (LivingEntity nearby : UtilEnt.getInRadius(player.getLocation(), 3).keySet())
				{
					if (isTeamDamage(nearby, player))
					{
						continue;
					}

					Manager.GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, 7,  true, true, false, UtilEnt.getName(player), GetName());
				}
			}
		}
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (RallyData data : _data)
		{
			if (!data.Landed)
			{
				continue;
			}

			Location banner = data.Banner;

			for (int i = 0; i < 5; i++)
			{
				double x = 5 * Math.sin(data.ParticleTheta);
				double z = 5 * Math.cos(data.ParticleTheta);

				banner.add(x, 0.25, z);

				UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, banner, 0, 0, 0, 0.1F, 1, ViewDist.LONG);

				banner.subtract(x, 0.25, z);

				data.ParticleTheta += Math.PI / 100;
			}
		}
	}

	@EventHandler
	public void velocityChange(EntityVelocityChangeEvent event)
	{
		for (RallyData data : _data)
		{
			if (!data.Landed && data.Owner.equals(event.getEntity()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.BANNER)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_data.removeIf(rallyData -> rallyData.Owner.equals(event.getEntity()));
	}

	private Pattern getPattern(GameTeam team)
	{
		return team.GetColor() == ChatColor.RED ? new Pattern(DyeColor.WHITE, PatternType.CROSS) : new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE);
	}

	private class RallyData
	{

		Player Owner;
		Location Banner;
		boolean Landed;
		long LaunchTime;
		long LandTime;
		double ParticleTheta;

		RallyData(Player owner)
		{
			Owner = owner;
			LaunchTime = System.currentTimeMillis();
		}
	}

}
