package mineplex.gemhunters.quest.types;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.loot.LootItem;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.quest.Quest;
import mineplex.gemhunters.safezone.SafezoneModule;

public class WalkingQuest extends Quest
{
	
	private final SafezoneModule _safezone;
	private final LootModule _loot;
	
	private final Map<UUID, Location> _last;
	private final int _distance;
	private final Material _itemReward;
	
	public WalkingQuest(int id, String name, String description, int startCost, int completeReward, int distance, Material itemReward)
	{
		super(id, name, description, startCost, completeReward);
	
		_safezone = Managers.require(SafezoneModule.class);
		_loot = Managers.require(LootModule.class);
		
		_last = new HashMap<>();
		_distance = distance;
		_itemReward = itemReward;
	}
	
	@Override
	public void onStart(Player player)
	{
		super.onStart(player);
		
		_last.put(player.getUniqueId(), player.getLocation());
	}
	
	@Override
	public void onReward(Player player)
	{
		for (LootItem lootItem : _loot.getChestItems("RED"))
		{
			if (lootItem.getItemStack().getType() == _itemReward)
			{
				player.getInventory().addItem(lootItem.getItemStack());
				break;
			}
		}
		
		super.onReward(player);
	}
	
	@Override
	public String getRewardString()
	{
		return C.cAqua + ItemStackFactory.Instance.GetName(_itemReward, (byte) 0, false) + " Mount";
	}
	
	@Override
	public float getProgress(Player player)
	{
		return (float) get(player) / (float) _distance;
	}
	
	@Override
	public int getGoal()
	{
		return _distance;
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (!isActive(player) || _safezone.isInSafeZone(player.getLocation()))
			{
				continue;
			}
			
			int distance = (int) UtilMath.offset(_last.get(player.getUniqueId()), player.getLocation());
			int total = getAndIncrement(player, distance);
			
			if (total >= _distance)
			{
				_last.remove(player.getUniqueId());
				onReward(player);
				return;
			}
			
			_last.put(player.getUniqueId(), player.getLocation());
		}
	}
}
