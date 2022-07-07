package nautilus.game.arcade.game.games.smash.perks.guardian;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkWhirlpoolBlade extends Perk implements IThrown
{

	private int _cooldown;
	private int _expireTime;
	private float _velocity;
	private float _hitBox;
	private int _damage;
	
	private Set<Item> _items = new HashSet<>();
	
	public PerkWhirlpoolBlade()
	{
		super("Whirlpool Axe", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Whirlpool Axe" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_expireTime = getPerkTime("Expire Time");
		_velocity = getPerkFloat("Velocity");
		_hitBox = getPerkFloat("Hit Box");
		_damage = getPerkInt("Damage");
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
		
		player.playSound(player.getLocation(), Sound.DIG_SNOW, 1, 1);
		
		Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.PRISMARINE_SHARD));
		
		item.setVelocity(player.getLocation().getDirection().multiply(_velocity));
		Manager.GetProjectile().AddThrow(item, player, this, _expireTime, true, true, true, false, false, _hitBox);
		_items.add(item);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<Item> iterator = _items.iterator();
		
		while (iterator.hasNext())
		{
			Item item = iterator.next();
			
			if (!item.isValid())
			{
				iterator.remove();
				continue;
			}
			
			UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, item.getLocation(), 0, 0, 0, 0.01F, 1, ViewDist.LONG);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (!UtilBlock.airFoliage(block))
		{
			data.getThrown().remove();
		}
		
		if (target == null)
		{
			return;
		}
		
		CustomDamageEvent event = Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.CUSTOM, _damage, false, true, true, data.getThrower().getName(), GetName());
		if(event.IsCancelled())
		{
			return;
		}
		UtilAction.velocity(target, UtilAlg.getTrajectory(target, data.getThrower()).setY(0.5));
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
}