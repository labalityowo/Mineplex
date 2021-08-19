package nautilus.game.arcade.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.KitAvailability;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.PlayerKitApplyEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;

public abstract class Kit implements Listener
{

	public final ArcadeManager Manager;
	private final GameKit _gameKit;
	private final Perk[] _perks;

	public Kit(ArcadeManager manager, GameKit gameKit, Perk... perks)
	{
		Manager = manager;
		_gameKit = gameKit;
		_perks = perks;
		
		for (Perk perk : _perks)
		{
			if (perk != null)
			{
				perk.SetHost(this);
			}
		}
	}

	public GameKit getGameKit()
	{
		return _gameKit;
	}

	public String GetFormattedName()
	{
		return _gameKit.getFormattedName();
	}
	
	public String GetName()
	{	
		return _gameKit.getDisplayName();
	}

	public KitAvailability GetAvailability()
	{
		return _gameKit.getAvailability();
	}
	
	public String[] GetDesc()
	{
		return _gameKit.getDescription();
	}
	
	public Perk[] GetPerks()
	{
		return _perks;
	}

	public boolean HasKit(Player player)
	{
		return Manager.GetGame() != null && Manager.GetGame().HasKit(player, this);
	}
	
	public void ApplyKit(Player player)
	{
		PlayerKitApplyEvent applyEvent = new PlayerKitApplyEvent(Manager.GetGame(), this, player);
		UtilServer.CallEvent(applyEvent);

		if (applyEvent.isCancelled())
		{
			return;
		}

		UtilInv.Clear(player);

		for (Perk perk : _perks)
		{
			if (perk != null)
			{
				perk.Apply(player);
			}
		}
		
		GiveItemsCall(player);
		
		UtilInv.Update(player);
	}
	
	public void GiveItemsCall(Player player)
	{
		GiveItems(player);
		
		//Event
		PlayerKitGiveEvent kitEvent = new PlayerKitGiveEvent(Manager.GetGame(), this, player);
		UtilServer.getServer().getPluginManager().callEvent(kitEvent);
	}
	
	public abstract void GiveItems(Player player);

	public int GetCost()
	{
		return _gameKit.getCost();
	}

	public void Deselected(Player player) { }
	
	public void Selected(Player player) { }

	public void registerEvents()
	{
	}

	public void unregisterEvents()
	{
	}

}
