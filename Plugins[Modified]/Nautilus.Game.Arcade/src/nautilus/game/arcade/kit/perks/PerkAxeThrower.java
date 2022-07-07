package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.kit.Perk;

public class PerkAxeThrower extends Perk implements IThrown
{

	private final Map<LivingEntity, Long> _lastAxe;

	public PerkAxeThrower()
	{
		super("Axe Thrower", new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " with Axes to " + C.cGreen + "Throw Axe",
				});

		_lastAxe = new HashMap<>();
	}

	@Override
	public void unregisteredEvents()
	{
		_lastAxe.clear();
	}

	@EventHandler
	public void Throw(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		Player player = event.getPlayer();

		if (!UtilItem.isAxe(player.getItemInHand()) || !Kit.HasKit(player) || !Recharge.Instance.usable(player, GetName(), true))
		{
			return;
		}

		event.setCancelled(true);

		Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(player.getItemInHand().getType()));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 10, false);
		Manager.GetProjectile().AddThrow(ent, player, this, -1, true, false, false, true, 0.7f);

		//Remove Axe
		player.setItemInHand(null);
		UtilInv.Update(player);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (!Manager.GetGame().IsAlive(target))
			return;

		Item item = (Item) data.getThrown();
		LivingEntity thrower = data.getThrower();

		int damage = 4;

		switch (item.getItemStack().getType())
		{
			case STONE_AXE:
				damage = 5;
				break;
			case IRON_AXE:
				damage = 6;
				break;
			case DIAMOND_AXE:
				damage = 7;
				break;
		}

		Long last = _lastAxe.get(thrower);

		if (last == null)
		{
			_lastAxe.put(thrower, System.currentTimeMillis());
		}
		else
		{
			long delta = System.currentTimeMillis() - last;
			damage /= Math.max(1, (2000 - delta) / 500);
		}

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, thrower, null,
				DamageCause.CUSTOM, damage, true, true, false,
				UtilEnt.getName(thrower), GetName());

		//Effect
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.ZOMBIE_WOOD, 1f, 1.6f);

		UtilAction.zeroVelocity(item);
		item.setPickupDelay(60);
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		if (Math.random() < 0.15)
		{
			Player thrower = (Player) data.getThrower();

			thrower.sendMessage(F.main("Skill", "Your " + F.item("Axe") + " returned to your inventory."));
			thrower.getInventory().addItem(((Item) data.getThrown()).getItemStack());
			data.getThrown().remove();
		}
	}

	@Override
	public void Expire(ProjectileUser data)
	{

	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_lastAxe.remove(event.getPlayer());
	}
}