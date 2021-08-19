package nautilus.game.arcade.game.games.castlesiegenew.perks;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.castlesiegenew.CastleSiegeNew;
import nautilus.game.arcade.kit.Perk;

public class PerkMobPotions extends Perk
{

	private static final long MAX_TIME = TimeUnit.SECONDS.toMillis(16);
	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(2);
	private static final int HEALTH = 20;
	private static final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false);

	private final MobPotion[] _mobPotions;
	private final Set<SummonedEntity> _entities = new HashSet<>();
	private final long _cooldown;

	public PerkMobPotions(long cooldown, MobPotion... mobPotions)
	{
		super("Mob Egg");

		_cooldown = cooldown;
		_mobPotions = mobPotions;
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();

		if (!hasPerk(player))
		{
			return;
		}

		MobPotion clickedPotion = null;
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null)
		{
			return;
		}

		for (MobPotion potion : _mobPotions)
		{
			if (potion.getItemStack().isSimilar(itemStack))
			{
				clickedPotion = potion;
				break;
			}
		}

		if (clickedPotion == null)
		{
			return;
		}

		event.setCancelled(true);

		if (!Recharge.Instance.use(player, GetName(), COOLDOWN, true, false) || !Recharge.Instance.use(player, ChatColor.stripColor(clickedPotion.getItemStack().getItemMeta().getDisplayName()), _cooldown, true, true))
		{
			return;
		}

		Manager.GetGame().CreatureAllowOverride = true;

		Location location = player.getEyeLocation();

		for (int i = 0; i < clickedPotion.getAmount(); i++)
		{
			LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(location, clickedPotion.getEntityType());
			entity.setVelocity(location.getDirection().add(new Vector(Math.random() - 0.5, 0, Math.random() - 0.5)));
			entity.addPotionEffect(SPEED);
			entity.setCustomName(player.getName() + "'s Minion");
			entity.setCustomNameVisible(true);
			entity.setMaxHealth(HEALTH);
			entity.setHealth(HEALTH);

			if (entity instanceof Slime)
			{
				((Slime) entity).setSize(1);
			}

			_entities.add(new SummonedEntity(entity, player));
		}

		Manager.GetGame().CreatureAllowOverride = false;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_entities.removeIf(entity ->
		{
			if (UtilTime.elapsed(entity.SpawnedAt, MAX_TIME))
			{
				entity.Summoned.setHealth(0);
				return true;
			}

			return entity.Summoned.isDead();
		});
	}

	@EventHandler
	public void entityTarget(EntityTargetEvent event)
	{
		for (SummonedEntity entity : _entities)
		{
			if (!event.getEntity().equals(entity.Summoned))
			{
				continue;
			}

			LivingEntity target = getNewTarget(entity);

			if (target == null)
			{
				event.setCancelled(true);
				return;
			}

			event.setTarget(target);
			return;
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		for (SummonedEntity entity : _entities)
		{
			if (!event.getEntity().equals(entity.Summoned))
			{
				continue;
			}

			event.setDroppedExp(0);
			event.getDrops().clear();
			return;
		}
	}

	@EventHandler
	public void entityDamage(CustomDamageEvent event)
	{
		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(false);

		for (SummonedEntity entity : _entities)
		{
			if (entity.Summoned.equals(damagee))
			{
				Player damagerPlayer = event.GetDamagerPlayer(true);
				CastleSiegeNew game = (CastleSiegeNew) Manager.GetGame();

				if (damager == null || !game.getUndead().HasPlayer(damagerPlayer))
				{
					return;
				}

				event.SetCancelled("Team Mob");
				return;
			}
			else if (entity.Summoned.equals(damager))
			{
				event.setDamager(entity.Owner);
				return;
			}
		}
	}

	@EventHandler
	public void updateDamage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (SummonedEntity entity : _entities)
		{
			// Slimes are weird and don't damage players normally.
			if (entity.Summoned instanceof Slime)
			{
				LivingEntity target = entity.Summoned;

				for (Player player : UtilPlayer.getNearby(target.getLocation(), 2))
				{
					if (player.equals(entity.Owner))
					{
						return;
					}

					Manager.GetDamage().NewDamageEvent(player, entity.Owner, null, DamageCause.CUSTOM, 2, true, true, false, entity.Summoned.getCustomName(), "Minion");
				}
			}
		}
	}

	public MobPotion[] getMobPotions()
	{
		return _mobPotions;
	}

	private LivingEntity getNewTarget(SummonedEntity entity)
	{
		return UtilPlayer.getClosest(entity.Summoned.getLocation(), 10, entity.OwnerTeam.GetPlayers(true));
	}

	private class SummonedEntity
	{

		LivingEntity Summoned;
		Player Owner;
		GameTeam OwnerTeam;
		long SpawnedAt;

		SummonedEntity(LivingEntity summoned, Player owner)
		{
			Summoned = summoned;
			Owner = owner;
			OwnerTeam = Manager.GetGame().GetTeam(owner);
			SpawnedAt = System.currentTimeMillis();
		}
	}


}
