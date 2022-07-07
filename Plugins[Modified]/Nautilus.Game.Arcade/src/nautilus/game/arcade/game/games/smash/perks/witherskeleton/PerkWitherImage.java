package nautilus.game.arcade.game.games.smash.perks.witherskeleton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import mineplex.core.velocity.VelocityFix;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkWitherImage extends SmashPerk
{

	private int _cooldown;
	private int _swapCooldown;
	private int _targetRadius;
	
	private Map<UUID, Skeleton> _skeletons = new HashMap<>();

	public PerkWitherImage()
	{
		super("Wither Image", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Wither Image", C.cYellow + "Double Right-Click" + C.cGray + " with Axe to " + C.cGreen
				+ "Wither Swap" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_swapCooldown = getPerkTime("Swap Cooldown");
		_targetRadius = getPerkInt("Target Radius");
	}

	@EventHandler
	public void activate(PlayerInteractEvent event)
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

		if (isSuperActive(player))
		{
			return;
		}

		if (!_skeletons.containsKey(player.getUniqueId()))
		{
			if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
			{
				return;
			}

			// Spawn
			Manager.GetGame().CreatureAllowOverride = true;

			Skeleton skel = UtilVariant.spawnWitherSkeleton(player.getEyeLocation());

			Manager.GetGame().CreatureAllowOverride = false;

			skel.getEquipment().setItemInHand(player.getItemInHand());
			skel.setMaxHealth(20);
			skel.setHealth(skel.getMaxHealth());

			for (Player other : UtilPlayer.getNearby(skel.getLocation(), _targetRadius))
			{
				if (player.equals(other) || UtilPlayer.isSpectator(other) || isTeamDamage(player, other))
				{
					continue;
				}
				
				skel.setTarget(other);
				break;
			}

			if (Manager.GetGame().GetTeamList().size() > 1)
			{
				skel.setCustomName(Manager.GetColor(player) + player.getName());
			}
			else
			{
				skel.setCustomName(C.cYellow + player.getName());
			}

			skel.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
			skel.setCustomNameVisible(true);

			UtilAction.velocity(skel, player.getLocation().getDirection(), 1.6, false, 0, 0.2, 10, true);

			_skeletons.put(player.getUniqueId(), skel);

			Recharge.Instance.use(player, "Wither Swap", _swapCooldown / 4, false, false);

			// Sound
			player.getWorld().playSound(player.getLocation(), Sound.WITHER_SPAWN, 1f, 1f);

			// Inform
			UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		}
		else
		{
			if (!Recharge.Instance.use(player, "Wither Swap", _swapCooldown, true, false))
			{
				return;
			}

			Skeleton skel = _skeletons.get(player.getUniqueId());
			Vector oldSkeletonVector = skel.getVelocity().clone();
			Vector oldPlayerVector = player.getVelocity().clone();

			Location loc = skel.getLocation();
			skel.teleport(player.getLocation());
			UtilAction.velocity(skel, oldPlayerVector);
			player.teleport(loc);
			// Not using UtilAction.velocity because it causes the player not to get the correct velocity
			player.setVelocity(oldSkeletonVector);

			// Sound
			player.getWorld().playSound(player.getLocation(), Sound.WITHER_SPAWN, 1f, 2f);

			// Inform
			UtilPlayer.message(player, F.main("Game", "You used " + F.skill("Wither Swap") + "."));
		}
	}

	@EventHandler
	public void entityTarget(EntityTargetEvent event)
	{
		if (!(event.getEntity() instanceof Skeleton))
		{
			return;
		}

		Player owner = getOwner((Skeleton) event.getEntity());

		if (owner == null || event.getTarget() == null)
		{
			return;
		}

		if (event.getTarget() instanceof Player && isTeamDamage((Player) event.getTarget(), owner) || event.getTarget().equals(owner))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (damagee == null)
		{
			return;
		}

		if (!_skeletons.containsKey(damagee.getUniqueId()))
		{
			return;
		}

		LivingEntity damager = event.GetDamagerEntity(false);

		if (damager == null)
		{
			return;
		}

		if (_skeletons.get(damagee.getUniqueId()).equals(damager))
		{
			event.SetCancelled("Wither Image");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void minionDamageTransfer(CustomDamageEvent event)
	{
		LivingEntity damager = event.GetDamagerEntity(true);

		if (damager == null)
			return;

		if (!_skeletons.containsValue(damager))
		{
			return;
		}

		for (UUID uuid : _skeletons.keySet())
		{
			Player player = UtilPlayer.searchExact(uuid);

			if (player == null)
			{
				return;
			}

			if (_skeletons.get(uuid).equals(damager))
			{
				event.SetDamager(player);
				event.setKnockbackOrigin(damager.getLocation());
				event.AddMod(GetName(), "Wither Image", -5.5, true);
			}
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;

		}
		Iterator<UUID> playerIterator = _skeletons.keySet().iterator();

		while (playerIterator.hasNext())
		{
			UUID uuid = playerIterator.next();
			Player player = UtilPlayer.searchExact(uuid);
			
			if (player == null)
			{
				playerIterator.remove();
				continue;
			}
			
			Skeleton skel = _skeletons.get(uuid);

			if (!player.isValid() || !skel.isValid() || skel.getTicksLived() > 160 || UtilBlock.liquid(skel.getLocation().getBlock()))
			{
				// Effect
				Manager.GetBlood().Effects(null, skel.getLocation().add(0, 0.5, 0), 12, 0.3, Sound.WITHER_HURT, 1f, 0.75f, Material.BONE, (byte) 0, 40, false);

				playerIterator.remove();
				skel.remove();
				continue;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void clean(PlayerDeathEvent event)
	{
		Skeleton skel = _skeletons.remove(event.getEntity().getUniqueId());

		if (skel != null)
		{
			// Effect
			Manager.GetBlood().Effects(null, skel.getLocation().add(0, 0.5, 0), 12, 0.3, Sound.WITHER_HURT, 1f, 0.75f, Material.BONE, (byte) 0, 40, false);

			skel.remove();
		}
	}

	private Player getOwner(Skeleton skel)
	{
		for (UUID uuid : _skeletons.keySet())
		{
			Skeleton other = _skeletons.get(uuid);

			if (other.equals(skel))
			{
				return UtilPlayer.searchExact(uuid);
			}
		}

		return null;
	}

}
