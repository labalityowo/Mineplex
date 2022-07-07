package mineplex.core.gadget.types;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetDisableEvent;
import mineplex.core.gadget.event.GadgetEnableEvent;

public abstract class GadgetSet implements Listener
{

	private final String _name;
	private final String[] _bonus;

	private final Gadget[] _gadgets;
	protected final GadgetManager Manager;

	protected final Set<Player> _active = new HashSet<>();

	public GadgetSet(GadgetManager manager, String name, String bonus, Gadget... gadgets)
	{
		Manager = manager;
		_gadgets = gadgets;

		_name = name;
		_bonus = UtilText.splitLineToArray(bonus, LineFormat.LORE);

		for (Gadget gadget : gadgets)
		{
			gadget.setSet(this);
		}

		UtilServer.RegisterEvents(this);
	}

	private void checkPlayer(Player player, Gadget gadget, boolean enable, boolean message)
	{
		boolean wasActive = isActive(player);

		for (Gadget g : _gadgets)
		{
			if (!g.isActive(player) || g.equals(gadget))
			{
				if (enable && g.equals(gadget)) continue;

				_active.remove(player);
				if (wasActive)
					customDisable(player);
				return;
			}
		}
		_active.add(player);
		if (!wasActive)
		{
			customEnable(player, message);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onActivate(GadgetEnableEvent event)
	{
		checkPlayer(event.getPlayer(), event.getGadget(), true, event.canShowMessage());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeativate(GadgetDisableEvent event)
	{
		checkPlayer(event.getPlayer(), event.getGadget(), false, true);
	}

	public boolean isActive(Player player)
	{
		return _active.contains(player);
	}

	public void customEnable(Player player, boolean message)
	{
		if (message)
		{
			player.sendMessage(F.main("Gadget", "Set Enabled: " + F.elem(C.cGreen + getName())));
		}
	}

	public void customDisable(Player player)
	{
		player.sendMessage(F.main("Gadget", "Set Disabled: " + F.elem(C.cRed + getName())));
	}

	public Gadget[] getGadgets()
	{
		return _gadgets;
	}

	public boolean isPartOfSet(Gadget gadget)
	{
		for (Gadget setGadget : _gadgets)
		{
			if (setGadget.equals(gadget))
			{
				return true;
			}
		}

		return false;
	}

	public String getName()
	{
		return _name;
	}

	public String[] getBonus()
	{
		return _bonus;
	}


}
