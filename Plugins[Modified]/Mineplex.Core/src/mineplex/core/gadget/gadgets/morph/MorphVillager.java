package mineplex.core.gadget.gadgets.morph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphVillager extends MorphGadget implements IThrown
{

	private final Set<Item> _gems = new HashSet<>();

	public MorphVillager(GadgetManager manager)
	{
		super(manager, "Villager Morph", UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "HURRRR! MURR HURRR!",
								C.blankLine,
								"#" + C.cWhite + "Left Click to use Gem Throw",
								C.blankLine,
								"#" + C.cRed + C.Bold + "WARNING: " + ChatColor.RESET + "Gem Throw uses 20 Gems"
						}, LineFormat.LORE),
				12000,
				Material.EMERALD, (byte) 0);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseVillager disguise = new DisguiseVillager(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (Manager.getDonationManager().Get(player).getBalance(GlobalCurrency.GEM) < 20)
		{
			UtilPlayer.message(player, F.main("Gadget", "You do not have enough Gems."));
			return;
		}

		if (!Recharge.Instance.use(player, getName(), 800, false, false, "Cosmetics"))
			return;

		player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_IDLE, 1f, 1f);

		//Item
		Item gem = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), new ItemStack(Material.EMERALD));
		UtilAction.velocity(gem, player.getLocation().getDirection(), 1, false, 0, 0.2, 1, false);

		//Throw
		Manager.getProjectileManager().AddThrow(gem, player, this, -1, true, true, true, true,
				null, 1.4f, 0.8f, null, null, 0, UpdateType.TICK, 0.5f);

		Manager.getDonationManager().rewardCurrency(GlobalCurrency.GEM, player, this.getName() + " Throw", -20);

		gem.setPickupDelay(40);

		_gems.add(gem);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target == null || !Manager.selectEntity(this, target))
		{
			return;
		}

		//Pull
		UtilAction.velocity(target,
				UtilAlg.getTrajectory(data.getThrown().getLocation(), target.getEyeLocation()),
				1, false, 0, 0.2, 0.8, true);

		UtilAction.velocity(data.getThrown(),
				UtilAlg.getTrajectory(target, data.getThrown()),
				0.5, false, 0, 0, 0.8, true);

		//Effect
		target.playEffect(EntityEffect.HURT);
	}

	@Override
	public void Idle(ProjectileUser data)
	{

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
	public void Pickup(PlayerPickupItemEvent event)
	{
		if (_gems.contains(event.getItem()))
		{
			Manager.getDonationManager().rewardCurrency(GlobalCurrency.GEM, event.getPlayer(), getName() + " Pickup", 16 * event.getItem().getItemStack().getAmount());

			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 2f);

			event.setCancelled(true);
			event.getItem().remove();
		}
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<Item> gemIterator = _gems.iterator();

		while (gemIterator.hasNext())
		{
			Item gem = gemIterator.next();

			if (!gem.isValid() || gem.getTicksLived() > 1200)
			{
				gem.remove();
				gemIterator.remove();
			}
		}
	}
}