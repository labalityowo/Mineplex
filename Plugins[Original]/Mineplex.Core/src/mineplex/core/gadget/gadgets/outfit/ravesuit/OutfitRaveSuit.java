package mineplex.core.gadget.gadgets.outfit.ravesuit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class OutfitRaveSuit extends OutfitGadget
{
	private Map<UUID, Integer> _colorPhase = new HashMap<>();

	public OutfitRaveSuit(GadgetManager manager, String name,
			int cost, ArmorSlot slot, Material mat, byte data)
	{
		super(manager, name, 
				UtilText.splitLineToArray(C.cGray + "There's nothing more suitable for celebration than this high tech flashing outfit!", LineFormat.LORE), 
				cost, slot, mat, data);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		super.enableCustom(player, message);

		_colorPhase.put(player.getUniqueId(), -1);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		_colorPhase.remove(player.getUniqueId());
	}

	@EventHandler
	public void updateColor(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!isActive(player))
				continue;

			//Get Item
			ItemStack stack;

			if (getSlot() == ArmorSlot.HELMET)
			{
				stack = player.getInventory().getHelmet();

				if (!UtilGear.isMat(stack, getDisplayMaterial()))
				{
					disable(player);
					continue;
				}
			}
			else if (getSlot() == ArmorSlot.CHEST)
			{
				stack = player.getInventory().getChestplate();

				if (!UtilGear.isMat(stack, getDisplayMaterial()))
				{
					disable(player);
					continue;
				}
			}
			else if (getSlot() == ArmorSlot.LEGS)
			{
				stack = player.getInventory().getLeggings();

				if (!UtilGear.isMat(stack, getDisplayMaterial()))
				{
					disable(player);
					continue;
				}
			}
			else if (getSlot() == ArmorSlot.BOOTS)
			{
				stack = player.getInventory().getBoots();

				if (!UtilGear.isMat(stack, getDisplayMaterial()))
				{
					disable(player);
					continue;
				}
			}
			else
			{
				continue;
			}

			//Rainbow
			int phase = _colorPhase.get(player.getUniqueId());

			LeatherArmorMeta meta = (LeatherArmorMeta)stack.getItemMeta();

			if (phase == -1)
			{
				meta.setColor(Color.fromRGB(250, 0, 0));
				_colorPhase.put(player.getUniqueId(), 0);
			}
			//Red > Yellow
			else if (phase == 0)
			{
				meta.setColor(Color.fromRGB(250, Math.min(250, meta.getColor().getGreen() + 25), 0));

				if (meta.getColor().getGreen() >= 250)
					_colorPhase.put(player.getUniqueId(), 1);
			}
			//Yellow > Green
			else if (phase == 1)
			{
				meta.setColor(Color.fromRGB(Math.max(0, meta.getColor().getRed() - 25), 250, 0));

				if (meta.getColor().getRed() <= 0)
					_colorPhase.put(player.getUniqueId(), 2);
			}
			//Green > Blue
			else if (phase == 2)
			{
				meta.setColor(Color.fromRGB(0, Math.max(0, meta.getColor().getGreen() - 25), Math.min(250, meta.getColor().getBlue() + 25)));

				if (meta.getColor().getGreen() <= 0)
					_colorPhase.put(player.getUniqueId(), 3);
			}
			//Blue > Red
			else if (phase == 3)
			{
				meta.setColor(Color.fromRGB(Math.min(250, meta.getColor().getRed() + 25), 0, Math.max(0, meta.getColor().getBlue() - 25)));

				if (meta.getColor().getBlue() <= 0)
					_colorPhase.put(player.getUniqueId(), 0);
			}

			stack.setItemMeta(meta);
		}
	}
	
	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_colorPhase.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void setBonus(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (getSlot() != ArmorSlot.HELMET)
			return;
		
		for (Player player : UtilServer.getPlayers())
			if (getSet() != null && getSet().isActive(player))
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 39, 4, true, false), true);
	}
}
