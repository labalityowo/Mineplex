package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.GoldPotHelper;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphGoldPot extends MorphGadget
{

	private Map<Player, GoldPotHelper> _helpers = new HashMap<>();

	public MorphGoldPot(GadgetManager manager)
	{
		super(manager, "Gold Pot Morph", UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "They say at the end of every rainbow a leprechaun has a pot filled with gold.",
								C.blankLine,
								C.cWhite + "Stand still to hide in place and fill up with treasure. Players who find you will earn a reward!",
						}, LineFormat.LORE),
				-14,
				Material.CAULDRON_ITEM, (byte) 0);

		setPPCYearMonth(YearMonth.of(2017, Month.MARCH));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		_helpers.put(player, new GoldPotHelper(player, Manager, this));

		DisguiseBlock disguiseBlock = new DisguiseBlock(player, Material.CAULDRON, (byte) 0);
		UtilMorph.disguise(player, disguiseBlock, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		if (_helpers.containsKey(player))
		{
			_helpers.get(player).unsolidifyPlayer();
			_helpers.get(player).cleanItems(true);
			_helpers.remove(player);
		}

		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		for (GoldPotHelper goldPotHelper : _helpers.values())
		{
			boolean solid = goldPotHelper.updatePlayer(event.getType() == UpdateType.SEC, event.getType() == UpdateType.TICK);
			if (solid)
			{
				goldPotHelper.solififyPlayer();
			}
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			for (GoldPotHelper goldPotHelper : _helpers.values())
			{
				goldPotHelper.performRightClick(event.getPlayer(), event.getClickedBlock());
			}
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event)
	{
		for (GoldPotHelper goldPotHelper : _helpers.values())
		{
			if (goldPotHelper.getItems().contains(event.getItem()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;

		if (_helpers.containsKey(event.getPlayer()))
		{
			if (!_helpers.get(event.getPlayer()).isSolid())
				return;
		}

		Location from = event.getFrom(), to = event.getTo();
		double xFrom = from.getX(), yFrom = from.getY(), zFrom = from.getZ(),
				xTo = to.getX(), yTo = to.getY(), zTo = to.getZ();
		if (xFrom != xTo || yFrom != yTo || zFrom != zTo)
		{
			if (_helpers.containsKey(event.getPlayer()))
			{
				_helpers.get(event.getPlayer()).unsolidifyPlayer();
			}
			event.getPlayer().setExp(0f);
		}
	}

}