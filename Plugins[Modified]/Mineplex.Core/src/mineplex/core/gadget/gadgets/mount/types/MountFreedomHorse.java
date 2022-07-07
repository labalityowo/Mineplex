package mineplex.core.gadget.gadgets.mount.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.particleeffects.FreedomTrailEffect;
import mineplex.core.recharge.Recharge;

public class MountFreedomHorse extends HorseMount
{

	private final Map<SingleEntityMountData<Horse>, FreedomTrailEffect> _trailMap = new HashMap<>();

	public MountFreedomHorse(GadgetManager manager)
	{
		super(manager,
				"Freedom Mount",
				UtilText.splitLineToArray(UtilText.colorWords("The British might be coming, but with this impressive mount you have nothing to fear.",
						ChatColor.RED, ChatColor.WHITE, ChatColor.BLUE), LineFormat.LORE),
				CostConstants.FOUND_IN_FREEDOM_CHESTS,
				Material.FIREWORK,
				(byte) 0,
				Color.WHITE,
				Style.WHITE,
				Variant.HORSE,
				1,
				null);
	}

	@Override
	public SingleEntityMountData<Horse> spawnMount(Player player)
	{
		SingleEntityMountData<Horse> data = super.spawnMount(player);
		Horse horse = data.getEntity();

		data.getEntity().getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));

		FreedomTrailEffect effect = new FreedomTrailEffect(horse);
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
			if (horseData.getEntity().equals(event.getEntity()))
			{
				if (!Recharge.Instance.use(horseData.getOwner(), getName(), 2500, false, false, "Cosmetics"))
				{
					event.setPower(0f);
					event.setCancelled(true);
					return;
				}

				_trailMap.get(horseData).setJumping(true);
			}
		}
	}

}
