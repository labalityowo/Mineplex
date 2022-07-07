package mineplex.core.gadget.gadgets.outfit;

import java.util.HashMap;

import mineplex.core.gadget.event.GadgetAppliedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;

public class OutfitTeam extends OutfitGadget
{
	private boolean _enabled = true;
	
	private HashMap<String, Color> _colorSetting = new HashMap<String, Color>();

	public OutfitTeam(GadgetManager manager, String name,
			int cost, ArmorSlot slot, Material mat, byte data)
	{
		super(manager, name, new String[] {ChatColor.RESET + "Team up with other players!", ChatColor.RESET + "Equip by typing;", ChatColor.RESET + C.cGreen + "/team <red/yellow/green/blue>"}, cost, slot, mat, data);

		setHidden(true);
	}
	
	@Override
	public void enable(Player player)
	{
		GadgetEnableEvent gadgetEvent = new GadgetEnableEvent(player, this);
		Bukkit.getServer().getPluginManager().callEvent(gadgetEvent);

		if (gadgetEvent.isCancelled())
		{
			return;
		}
		
		enableCustom(player, true);
		Manager.setActive(player, this);
		Bukkit.getServer().getPluginManager().callEvent(new GadgetAppliedEvent(player, this));
	}
	
	@Override
	public void applyArmor(Player player, boolean message)
	{
		Manager.removeGadgetType(player, GadgetType.MORPH);
		
		Manager.removeOutfit(player, _slot);
		
		_active.add(player);
		
		if (_slot == ArmorSlot.HELMET)	player.getInventory().setHelmet(
				ItemStackFactory.Instance.CreateStack(getDisplayMaterial().getId(), getDisplayData(), 1, getName()));
		
		else if (_slot == ArmorSlot.CHEST)	player.getInventory().setChestplate(
				ItemStackFactory.Instance.CreateStack(getDisplayMaterial().getId(), getDisplayData(), 1, getName()));
		
		else if (_slot == ArmorSlot.LEGS)	player.getInventory().setLeggings(
				ItemStackFactory.Instance.CreateStack(getDisplayMaterial().getId(), getDisplayData(), 1, getName()));
		
		else if (_slot == ArmorSlot.BOOTS)	player.getInventory().setBoots(
				ItemStackFactory.Instance.CreateStack(getDisplayMaterial().getId(), getDisplayData(), 1, getName()));
	}
	
	@Override
	public void removeArmor(Player player, boolean message)
	{
		if (!_active.remove(player))
			return;
		
		if (_slot == ArmorSlot.HELMET)		player.getInventory().setHelmet(null);
		else if (_slot == ArmorSlot.CHEST)	player.getInventory().setChestplate(null);
		else if (_slot == ArmorSlot.LEGS)	player.getInventory().setLeggings(null);
		else if (_slot == ArmorSlot.BOOTS)	player.getInventory().setBoots(null);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);
		colorArmor(player);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player, message);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void setColor(PlayerCommandPreprocessEvent event)
	{
		if (!_enabled)
			return;
		
		Player player = event.getPlayer();

		if (!event.getMessage().toLowerCase().startsWith("/team "))
			return;

		event.setCancelled(true);

		String[] args = event.getMessage().toLowerCase().split(" ");

		if (args.length < 2)
		{
			disable(player);
			return;
		}
			
		
		//Will only display the message once
		if (getSlot() == ArmorSlot.LEGS)
		{
			if (!Recharge.Instance.use(player, "Set Team Color", 20000, true, false))
				return;
		}
		else
		{
			if (!Recharge.Instance.use(player, "Set Team Color " + getSlot(), 20000, false, false))
				return;
		}
		
		//Color
		if (args[1].equals("red"))
		{
			_colorSetting.put(player.getName(), Color.RED);

			if (getSlot() == ArmorSlot.LEGS) //Only Display Once
				UtilPlayer.message(player, F.main("Gadget", "You equipped " + F.elem(C.cRed + "Red Team Outfit") + "!"));
		}
		else if (args[1].equals("yellow"))
		{
			_colorSetting.put(player.getName(), Color.YELLOW);

			if (getSlot() == ArmorSlot.LEGS) //Only Display Once
				UtilPlayer.message(player, F.main("Gadget", "You equipped " + F.elem(C.cYellow + "Yellow Team Outfit") + "!"));
		}
		else if (args[1].equals("green"))
		{
			_colorSetting.put(player.getName(), Color.LIME);

			if (getSlot() == ArmorSlot.LEGS) //Only Display Once
				UtilPlayer.message(player, F.main("Gadget", "You equipped " + F.elem(C.cGreen + "Green Team Outfit") + "!"));
		}
		else if (args[1].equals("blue"))
		{
			_colorSetting.put(player.getName(), Color.AQUA);

			if (getSlot() == ArmorSlot.LEGS) //Only Display Once
				UtilPlayer.message(player, F.main("Gadget", "You equipped " + F.elem(C.cAqua + "Blue Team Outfit") + "!"));
		}
		else
			return;

		colorArmor(player);

		enable(player);
	}

	private void colorArmor(Player player)
	{
		if (!_colorSetting.containsKey(player.getName()))
			return;

		//Get Item
		ItemStack stack;

		if (getSlot() == ArmorSlot.HELMET)
		{
			stack = player.getInventory().getHelmet();

			if (!UtilGear.isMat(stack, getDisplayMaterial()))
			{
				disable(player);
				return;
			}
		}
		else if (getSlot() == ArmorSlot.CHEST)
		{
			stack = player.getInventory().getChestplate();

			if (!UtilGear.isMat(stack, getDisplayMaterial()))
			{
				disable(player);
				return;
			}
		}
		else if (getSlot() == ArmorSlot.LEGS)
		{
			stack = player.getInventory().getLeggings();

			if (!UtilGear.isMat(stack, getDisplayMaterial()))
			{
				disable(player);
				return;
			}
		}
		else if (getSlot() == ArmorSlot.BOOTS)
		{
			stack = player.getInventory().getBoots();

			if (!UtilGear.isMat(stack, getDisplayMaterial()))
			{
				disable(player);
				return;
			}
		}
		else
		{
			return;
		}


		//Set!
		LeatherArmorMeta meta = (LeatherArmorMeta)stack.getItemMeta();
		meta.setColor(_colorSetting.get(player.getName()));
		stack.setItemMeta(meta);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_colorSetting.remove(event.getPlayer().getName());
	}

	public Color getTeamColor(Player player) 
	{
		return _colorSetting.get(player.getName());
	}
	
	public void setEnabled(boolean var)
	{
		_enabled = var;
	}
}
