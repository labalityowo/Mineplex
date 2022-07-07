package mineplex.core.gadget.gadgets.mount.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.particleeffects.RainbowTrailEffect;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountStPatricksHorse extends HorseMount
{

	private final HashSet<Item> _items = new HashSet<>();
	private final Map<SingleEntityMountData<Horse>, RainbowTrailEffect> _trailMap = new HashMap<>();

	public MountStPatricksHorse(GadgetManager manager)
	{
		super(manager,
				"Rainbow Horse",
				UtilText.splitLineToArray(C.cGray + "You know the cow that jumped over the moon? Total show off.", LineFormat.LORE),
				CostConstants.FOUND_IN_ST_PATRICKS_CHESTS,
				Material.CAULDRON_ITEM,
				(byte) 0,
				Color.WHITE,
				Style.WHITE,
				Variant.HORSE,
				1,
				Material.GOLD_BARDING
		);
	}

	@Override
	public SingleEntityMountData<Horse> spawnMount(Player player)
	{
		SingleEntityMountData<Horse> data = super.spawnMount(player);
		Horse horse = data.getEntity();

		RainbowTrailEffect effect = new RainbowTrailEffect(horse, _items);
		effect.start();
		_trailMap.put(data, effect);

		return data;
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		_trailMap.entrySet().removeIf(entry ->
		{
			if (entry.getKey().ownsMount(player))
			{
				entry.getValue().stop();
				return true;
			}

			return false;
		});

		super.disableCustom(player, message);
	}

	@EventHandler
	public void horseJump(HorseJumpEvent event)
	{
		for (SingleEntityMountData<Horse> horseData : _trailMap.keySet())
		{
			if (horseData.getEntity().equals(event.getEntity()) && Recharge.Instance.use(horseData.getOwner(), getName(), 2500, false, false, "Cosmetics"))
			{
				_trailMap.get(horseData).setJumping(true);
			}
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event)
	{
		if (_items.contains(event.getItem()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		cleanItems();
	}

	private void cleanItems()
	{
		Iterator<Item> iterator = _items.iterator();

		while (iterator.hasNext())
		{
			Item item = iterator.next();

			if (item.getTicksLived() >= 20)
			{
				item.remove();
				iterator.remove();
			}
		}
	}

}
