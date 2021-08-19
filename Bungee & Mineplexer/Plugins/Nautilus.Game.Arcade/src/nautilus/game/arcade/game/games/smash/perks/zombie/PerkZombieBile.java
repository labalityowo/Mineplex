package nautilus.game.arcade.game.games.smash.perks.zombie;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkZombieBile extends SmashPerk implements IThrown
{

	private int _cooldown;
	private int _items;
	private int _damage;
	private int _knockbackMagnitude;
	
	private Map<UUID, Long> _active = new HashMap<>();

	public PerkZombieBile()
	{
		super("Spew Bile", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Spew Bile" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_items = getPerkInt("Items");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
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

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		_active.put(player.getUniqueId(), System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void deactivateDeath(PlayerDeathEvent event)
	{
		if (!hasPerk(event.getEntity()))
		{
			return;
		}

		_active.remove(event.getEntity().getUniqueId());
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<UUID> activeIter = _active.keySet().iterator();

		while (activeIter.hasNext())
		{
			UUID uuid = activeIter.next();
			Player player = UtilPlayer.searchExact(uuid);

			// Expire
			if (UtilTime.elapsed(_active.get(player.getUniqueId()), 2000))
			{
				activeIter.remove();
				continue;
			}

			// Sound
			if (Math.random() > 0.85)
			{
				player.getWorld().playSound(player.getLocation(), Sound.BURP, 1f, (float) (Math.random() + 0.5));
			}

			// Projectiles
			for (int i = 0; i < _items; i++)
			{
				Vector rand = new Vector((Math.random() - 0.5) * 0.525, (Math.random() - 0.5) * 0.525, (Math.random() - 0.5) * 0.525);

				Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()).subtract(0, 0.5, 0), new ItemStack(Material.ROTTEN_FLESH));
				UtilAction.velocity(ent, player.getLocation().getDirection().add(rand), 0.8, false, 0, 0.2, 10, false);
				Manager.GetProjectile().AddThrow(ent, player, this, 2000, true, true, true, false, 0.5f);
			}
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		_active.remove(event.getEntity().getUniqueId());
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		data.getThrown().remove();

		if (target == null)
		{
			return;
		}
		
		if (UtilPlayer.isSpectator(target))
		{
			return;
		}
			
		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.CUSTOM, _damage, true, false, false, UtilEnt.getName(data.getThrower()), GetName());

		data.getThrown().remove();
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}