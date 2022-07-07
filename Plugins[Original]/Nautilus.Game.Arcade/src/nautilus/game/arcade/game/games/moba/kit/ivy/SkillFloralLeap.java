package nautilus.game.arcade.game.games.moba.kit.ivy;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.moba.kit.HeroSkill;

public class SkillFloralLeap extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Leap up into the air.",
			"Landing creates a bed of flowers",
			"that damage and slow enemy players."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);
	private static final DustSpellColor[] COLOURS = {
			new DustSpellColor(Color.RED),
			new DustSpellColor(Color.CYAN),
			new DustSpellColor(Color.MAGENTA)
	};
	private static final long DURATION = TimeUnit.SECONDS.toMillis(5);
	private static final int DAMAGE_RADIUS = 5;
	private static final int BLOCK_RADIUS = 3;
	private static final PotionEffect DEBUFF = new PotionEffect(PotionEffectType.SLOW, 50, 2, false, false);
	private static final int DAMAGE = 2;

	private final Set<FlowerBedData> _data = new HashSet<>();

	public SkillFloralLeap(int slot)
	{
		super("Floral Leap", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(11000);
		setSneakActivate(true);
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

		Vector direction = player.getLocation().getDirection();
		direction.setY(1.2);

		UtilAction.velocity(player, direction);
		_data.add(new FlowerBedData(player));

		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, player.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.1F, 20, ViewDist.LONG);
		player.getWorld().playSound(player.getLocation(), Sound.DIG_GRASS, 1, 1);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_data.removeIf(data ->
		{
			Player player = data.Owner;

			if (player == null || !player.isOnline())
			{
				return true;
			}

			// Has landed
			if (data.Active)
			{
				if (UtilTime.elapsed(data.Start, DURATION))
				{
					return true;
				}
				else if (!UtilTime.elapsed(data.LastDamage, 1000))
				{
					return false;
				}

				data.LastDamage = System.currentTimeMillis();

				for (LivingEntity nearby : UtilEnt.getInRadius(data.Center, DAMAGE_RADIUS).keySet())
				{
					if (isTeamDamage(player, nearby))
					{
						continue;
					}

					nearby.addPotionEffect(DEBUFF);
					Manager.GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, DAMAGE, false, true, false, player.getName(), GetName());
				}
			}
			// Has just landed
			else if (UtilTime.elapsed(data.Leap, 500) && UtilEnt.isGrounded(player))
			{
				Location location = player.getLocation();

				data.Active = true;
				data.Center = location;
				data.Start = System.currentTimeMillis();

				for (Block block : UtilBlock.getInRadius(location, BLOCK_RADIUS).keySet())
				{
					if (block.getType() != Material.AIR || UtilBlock.airFoliage(block.getRelative(BlockFace.DOWN)))
					{
						continue;
					}

					Pair<Material, Byte> flower = getRandomFlower();
					Manager.GetBlockRestore().add(block, flower.getLeft().getId(), flower.getRight(), DURATION + (UtilMath.rRange(-500, 500)));

					if (Math.random() > 0.7)
					{
						block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.STEP_SOUND, flower.getLeft());
					}
				}
			}
			// Hasn't landed
			else
			{
				Location location = player.getLocation();

				for (int i = 0; i < 2; i++)
				{
					for (DustSpellColor colour : COLOURS)
					{
						new ColoredParticle(ParticleType.RED_DUST, colour, UtilAlg.getRandomLocation(location, 0.3, 0, 0.3))
								.display(ViewDist.LONG);
					}
				}
			}

			return false;
		});
	}

	@EventHandler
	public void blockPhysics(BlockPhysicsEvent event)
	{
		if (event.getChangedType() == Material.RED_ROSE)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_data.removeIf(data -> data.Owner.equals(event.getEntity()));
	}

	private Pair<Material, Byte> getRandomFlower()
	{
		return Pair.create(Material.RED_ROSE, (byte) (UtilMath.r(8) + 1));
	}

	private class FlowerBedData
	{

		Player Owner;
		long Leap;
		boolean Active;
		Location Center;
		long Start;
		long LastDamage;

		FlowerBedData(Player owner)
		{
			Owner = owner;
			Leap = System.currentTimeMillis();
		}
	}
}

