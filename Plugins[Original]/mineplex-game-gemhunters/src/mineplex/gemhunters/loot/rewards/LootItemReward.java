package mineplex.gemhunters.loot.rewards;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;

public abstract class LootItemReward
{

	private String _name;

	private long _firstItemPickup;
	private long _cashOutDelay;

	protected Player _player;
	private ItemStack _itemStack;
	
	public LootItemReward(String name, long cashOutDelay, ItemStack itemStack)
	{
		_name = name;
		_firstItemPickup = 0;
		_cashOutDelay = cashOutDelay;
		_itemStack = itemStack;
	}

	public abstract void onCollectItem();

	public abstract void onSuccessful();

	public abstract void onDeath();

	public final void collectItem(Player player)
	{
		if (_player != null && player.equals(_player))
		{
			return;
		}
		
		if (_firstItemPickup == 0)
		{
			String title = C.cYellow + player.getName();
			String subtitle = C.cGray + "Collected a " + F.elem(_name) + " reward. Killing them will drop it!";
			String chatMessage = F.main("Game", title + " " + subtitle + " They will not be able to quit out of the game for " + F.time(UtilTime.MakeStr(_cashOutDelay) + "."));

			UtilTextMiddle.display(title, subtitle, 20, 60, 20, UtilServer.getPlayers());
			UtilServer.broadcast(chatMessage);
			
			_firstItemPickup = System.currentTimeMillis();
		}
		else
		{
			String message = F.main("Game", F.name(player.getName()) + " now has the " + F.elem(_name) + " reward!");
			
			UtilServer.broadcast(message);
		}
		
		Recharge.Instance.useForce(player, "Cash Out", _cashOutDelay, false);
		_player = player;
		onCollectItem();
	}

	public final void success()
	{
		onSuccessful();
	}

	public final void death(PlayerDeathEvent event)
	{
		_player = null;
	}
	
	public boolean isFirstPickup()
	{
		return _firstItemPickup == 0;
	}
		
	public Player getPlayer()
	{
		return _player;
	}
	
	public ItemStack getItemStack()
	{
		return _itemStack;
	}

}
