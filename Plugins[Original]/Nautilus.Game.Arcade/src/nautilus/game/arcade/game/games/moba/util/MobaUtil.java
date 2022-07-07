package nautilus.game.arcade.game.games.moba.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.common.geom.Point2D;
import mineplex.core.common.geom.Polygon2D;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.hp.MobaHPRegenEvent;

public class MobaUtil
{

	public static LivingEntity getBestEntityTarget(Moba host, GameTeam owner, LivingEntity source, Location location, int targetRange, boolean targetPlayers)
	{
		LivingEntity highest = null;
		double bestDist = 0;

		for (Entry<LivingEntity, Double> entry : UtilEnt.getInRadius(location, targetRange).entrySet())
		{
			LivingEntity entity = entry.getKey();
			double dist = entry.getValue();

			if (source.equals(entity))
			{
				continue;
			}

			// Check for team entities
			if (isTeamEntity(entity, owner))
			{
				continue;
			}

			// Ignore players on the same team
			if (entity instanceof Player)
			{
				if (!targetPlayers || owner.equals(host.GetTeam((Player) entity)))
				{
					continue;
				}
			}

			if (bestDist < dist)
			{
				highest = entity;
				bestDist = dist;
			}
		}

		return highest;
	}

	public static boolean isInBoundary(GameTeam owner, LivingEntity source, Location center, Polygon2D boundaries, LivingEntity target)
	{
		if (boundaries == null)
		{
			return true;
		}

		return getEntitiesInBoundary(owner, source, center, boundaries).contains(target);
	}

	public static LivingEntity getBestEntityTarget(GameTeam owner, LivingEntity source, Location center, Polygon2D boundaries)
	{
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;

		for (LivingEntity entity : getEntitiesInBoundary(owner, source, center, boundaries))
		{
			double dist = UtilMath.offsetSquared(entity.getLocation(), center);

			if (dist < bestDist)
			{
				best = entity;
				bestDist = dist;
			}
		}

		return best;
	}

	public static Set<LivingEntity> getEntitiesInBoundary(GameTeam owner, LivingEntity source, Location center, Polygon2D boundaries)
	{
		List<Player> ignored = new ArrayList<>();

		if (owner != null)
		{
			// Add teammates to ignored players
			ignored.addAll(owner.GetPlayers(true));
		}

		Set<LivingEntity> result = new HashSet<>();

		for (Entity cur : center.getWorld().getEntities()) // TODO: narrow down to specific chunks contained in the polygon?
		{
			if (!(cur instanceof LivingEntity) || UtilPlayer.isSpectator(cur))
				continue;

			LivingEntity ent = (LivingEntity)cur;

			if (boundaries.contains(Point2D.of(ent.getLocation())))
			{
				if ((owner != null && isTeamEntity(ent, owner)) || source.equals(ent) || ignored.contains(ent))
				{
					continue;
				}
				result.add(ent);
			}
		}

		return result;
	}

	public static String getHealthBar(LivingEntity entity, int bars)
	{
		return getHealthBar(entity, entity.getHealth(), bars);
	}

	public static String getHealthBar(LivingEntity entity, double newHealth, int bars)
	{
		return getProgressBar(newHealth, entity.getMaxHealth(), bars);
	}

	public static String getProgressBar(double value, double max, int bars)
	{
		StringBuilder out = new StringBuilder();
		double progress = value / max;
		String colour = getColour(progress);

		for (int i = 0; i < bars; i++)
		{
			double cur = i * (1D / (double) bars);

			if (cur < progress)
			{
				out.append(colour).append("|");
			}
			else
			{
				out.append(C.cGrayB).append("|");
			}
		}

		return out.toString();
	}

	public static String getProgressBar(double valueA, double valueB, double max, int bars)
	{
		StringBuilder out = new StringBuilder();
		double progressA = valueA / max;
		double progressB = valueB / max;

		for (int i = 0; i < bars; i++)
		{
			double cur = i * (1D / (double) bars);

			if (cur < progressA)
			{
				out.append(C.cAqua).append("|");
			}
			else if (cur < progressB)
			{
				out.append(C.cGreen).append("|");
			}
			else
			{
				out.append(C.cGray).append("|");
			}
		}

		return out.toString();
	}

	public static String getColour(double percentage)
	{
		if (percentage < 0.25)
		{
			return C.cRedB;
		}
		else if (percentage < 0.5)
		{
			return C.cGoldB;
		}
		else if (percentage < 0.75)
		{
			return C.cYellowB;
		}

		return C.cGreenB;
	}

	public static void setTeamEntity(LivingEntity entity, GameTeam team)
	{
		UtilEnt.SetMetadata(entity, MobaConstants.TEAM_METADATA, team.GetName());
	}

	public static boolean isTeamEntity(LivingEntity entity, GameTeam team)
	{
		String metadata = UtilEnt.GetMetadata(entity, MobaConstants.TEAM_METADATA);

		return metadata != null && metadata.equals(team.GetName());
	}


	public static void heal(LivingEntity entity, Player source, double health)
	{
		if (entity instanceof Player)
		{
			MobaHPRegenEvent regenEvent = new MobaHPRegenEvent((Player) entity, source, health, false);
			UtilServer.CallEvent(regenEvent);

			if (regenEvent.isCancelled())
			{
				return;
			}

			health = regenEvent.getHealth();
		}

		entity.setHealth(Math.min(entity.getHealth() + health, entity.getMaxHealth()));
	}

	public static double scaleDamageWithBow(ItemStack itemStack, double initialDamage)
	{
		// Set a base damage
		initialDamage += 5;

		// Scale damage with the damage on the player's bow
		if (itemStack != null && itemStack.getType() == Material.BOW)
		{
			initialDamage += initialDamage * itemStack.getEnchantmentLevel(Enchantment.ARROW_DAMAGE) * 0.25;
		}

		return initialDamage;
	}
}
