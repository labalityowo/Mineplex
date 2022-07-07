package mineplex.core.gadget.gadgets.morph;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;

public abstract class MorphAbilityGadget extends MorphGadget
{

	private static final int ACTIVE_SLOT = 2;

	private final ItemStack _abilityItem;
	private final String _abilityName;
	private final long _recharge;

	public MorphAbilityGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, ItemStack abilityItem, String abilityName, long recharge)
	{
		super(manager, name, desc, cost, mat, data);

		_abilityName = abilityName;
		_abilityItem = abilityItem;
		_recharge = recharge;
	}

	@Override
	public void applyArmor(Player player, boolean message)
	{
		super.applyArmor(player, message);

		player.getInventory().setItem(ACTIVE_SLOT, _abilityItem);
	}

	@Override
	public void removeArmor(Player player)
	{
		super.removeArmor(player);

		player.getInventory().setItem(ACTIVE_SLOT, null);
	}

	public abstract void onAbilityActivate(Player player);

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem();

		if (!isActive(player) || itemStack == null || !itemStack.equals(_abilityItem))
		{
			return;
		}

		event.setCancelled(true);

		if (!Manager.selectLocation(this, player.getLocation()))
		{
			Manager.informNoUse(player);
			return;
		}

		if (!Recharge.Instance.use(player, _abilityName, _recharge, true, true, "Cosmetics"))
		{
			return;
		}

		onAbilityActivate(player);
	}
}
