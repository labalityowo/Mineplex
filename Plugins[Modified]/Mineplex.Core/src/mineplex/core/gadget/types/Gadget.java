package mineplex.core.gadget.types;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.Donor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetAppliedEvent;
import mineplex.core.gadget.event.GadgetDisableEvent;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.inventory.ClientInventory;
import mineplex.core.shop.item.SalesPackageBase;

public abstract class Gadget extends SalesPackageBase implements Listener
{

	public final GadgetManager Manager;

	private final GadgetType _gadgetType;
	private final String[] _alternativePackageNames;

	private YearMonth _yearMonth;
	private ItemStack _displayItem;
	private boolean _hidden;
	private int _maxActive = Integer.MAX_VALUE;

	private GadgetSet _set;

	protected final Set<Player> _active = new HashSet<>();

	public Gadget(GadgetManager manager, GadgetType gadgetType, String name, String[] desc, int cost, Material mat, byte data)
	{
		this(manager, gadgetType, name, desc, cost, mat, data, 1);
	}

	public Gadget(GadgetManager manager, GadgetType gadgetType, String name, String[] desc, int cost, Material mat, byte data, int quantity, String... alternativePackageNames)
	{
		this(manager, gadgetType, name, desc, cost, mat, data, quantity, false, alternativePackageNames);
	}

	public Gadget(GadgetManager manager, GadgetType gadgetType, String name, String[] desc, int cost, Material mat, byte data, int quantity, boolean free, String... alternativePackageNames)
	{
		super(name, mat, data, desc, cost, quantity);

		Manager = manager;

		_gadgetType = gadgetType;
		Free = free;
		KnownPackage = false;

		_alternativePackageNames = alternativePackageNames;

		UtilServer.RegisterEvents(this);
	}

	public GadgetType getGadgetType()
	{
		return _gadgetType;
	}

	public Set<Player> getActive()
	{
		return _active;
	}

	public boolean isActive(Player player)
	{
		return _active.contains(player);
	}

	protected void setPPCYearMonth(YearMonth yearMonth)
	{
		_yearMonth = yearMonth;
	}

	public YearMonth getYearMonth()
	{
		return _yearMonth;
	}

	public void setHidden(boolean hidden)
	{
		_hidden = hidden;
	}

	public boolean isHidden()
	{
		return _hidden;
	}

	public void setMaxActive(int maxActive)
	{
		_maxActive = maxActive;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		disable(event.getPlayer());
	}

	public void enable(Player player)
	{
		enable(player, true);
	}

	public void enable(Player player, boolean message)
	{
		GadgetEnableEvent gadgetEvent = new GadgetEnableEvent(player, this);
		gadgetEvent.setShowMessage(message);
		UtilServer.CallEvent(gadgetEvent);

		if (gadgetEvent.isCancelled())
		{
			if (message)
			{
				UtilPlayer.message(player, F.main(Manager.getName(), F.name(getName()) + " is not enabled."));
			}

			return;
		}

		if (_active.size() + 1 >= _maxActive)
		{
			player.sendMessage(F.main(Manager.getName(), "Sorry there is a limit to how many people can use " + F.name(getName()) + " at one time in a lobby."));
			return;
		}

		enableCustom(player, message);
		Manager.setActive(player, this);
		UtilServer.CallEvent(new GadgetAppliedEvent(player, this));
	}

	public void disableForAll()
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			disable(player);
		}
	}

	public void disable(Player player)
	{
		disable(player, true);
	}

	public void disable(Player player, boolean message)
	{
		if (isActive(player))
		{
			GadgetDisableEvent event = new GadgetDisableEvent(player, this);
			UtilServer.CallEvent(event);
			disableCustom(player, message);
			Manager.removeActive(player, this);
		}
	}

	public void enableCustom(Player player, boolean message)
	{
		Manager.removeGadgetType(player, _gadgetType, this);
		_active.add(player);

		if (message)
		{
			UtilPlayer.message(player, F.main(Manager.getName(), "You enabled " + F.elem(getName()) + "."));
		}
	}

	public void disableCustom(Player player, boolean message)
	{
		if (_active.remove(player) && message)
		{
			UtilPlayer.message(player, F.main(Manager.getName(), "You disabled " + F.elem(getName()) + "."));
		}
	}

	public boolean ownsGadget(Player player)
	{
		Donor donor = Manager.getDonationManager().Get(player);
		ClientInventory inventory = Manager.getInventoryManager().Get(player);

		if (isFree() || donor.ownsUnknownSalesPackage(getName()) || inventory.getItemCount(getName()) > 0)
		{
			return true;
		}

		for (String alt : _alternativePackageNames)
		{
			if (donor.ownsUnknownSalesPackage(alt) || inventory.getItemCount(alt) > 0)
			{
				return true;
			}
		}

		return false;
	}

	public void setSet(GadgetSet set)
	{
		_set = set;
	}

	public GadgetSet getSet()
	{
		return _set;
	}

	public String[] getAlternativePackageNames()
	{
		return _alternativePackageNames;
	}

	public void setDisplayItem(ItemStack displayItem)
	{
		_displayItem = displayItem;
	}

	public boolean hasDisplayItem()
	{
		return _displayItem != null;
	}

	public ItemStack getDisplayItem()
	{
		return _displayItem;
	}
}
