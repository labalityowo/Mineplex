package mineplex.core.gadget.gadgets.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemPaintbrush extends ItemGadget
{
	private NautHashMap<String, Byte> _brushColor = new NautHashMap<String, Byte>();
	private NautHashMap<String, Location> _brushPrevious = new NautHashMap<String, Location>();
	
	private NautHashMap<String, Location> _playerLocation = new NautHashMap<String, Location>();
	
	public ItemPaintbrush(GadgetManager manager) 
	{
		super(manager, "Paintbrush", 
				UtilText.splitLineToArray(C.cWhite + "Unleash your inner Bob Ross! Happy little trees!", LineFormat.LORE),
				CostConstants.NO_LORE,
				Material.WOOD_SWORD, (byte)0,
				200, new Ammo("Paint", "100 Pixels", Material.INK_SACK, (byte)0, new String[] { C.cWhite + "100 Pixels worth of Paint!" }, 500, 100));

		setHidden(true);
	}
	
	@Override
	public void ApplyItem(Player player, boolean inform)
	{
		Manager.removeGadgetType(player, GadgetType.ITEM);

		_active.add(player);

		List<String> itemLore = new ArrayList<String>();
		itemLore.addAll(Arrays.asList(getDescription()));

		player.getInventory().setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(getDisplayMaterial(), getDisplayData(), 1, getName()));
		
		if (inform)
			UtilPlayer.message(player, F.main("Gadget", "You equipped " + F.elem(getName()) + "."));
	}
	
	@Override
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		
	}

	@Override
	public void ActivateCustom(Player player)
	{
		
	}
	
	@Override
	public void enableCustom(Player player, boolean message)
	{
		ApplyItem(player, message);
		
		_brushColor.put(player.getName(), (byte)15);
		_playerLocation.put(player.getName(), player.getLocation());
	}
	
	
	@Override
	public void disableCustom(Player player, boolean message)
	{
		_brushColor.remove(player.getName());
		_brushPrevious.remove(player.getName());
		_playerLocation.remove(player.getName());
		
		RemoveItem(player, message);
	}
	
	@EventHandler
	public void colorSelect(PlayerInteractEvent event)
	{		
		if (!isActive(event.getPlayer()))
			return;
				
		Player player = event.getPlayer();
		
		if (!UtilGear.isMat(player.getItemInHand(), Material.WOOD_SWORD))
			return;

		Block block = player.getTargetBlock((HashSet<Byte>)null, 100);
		if (block == null || block.getType() != Material.STAINED_CLAY)	
			return;

		_brushColor.put(player.getName(), block.getData());

		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2f, 1f);
	}
	
	
	@EventHandler
	public void disableDistance(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!getActive().contains(player))
				continue;
			
			Location loc = _playerLocation.get(player.getName());
			
			if (loc == null || UtilMath.offset(player.getLocation(), loc) > 12) 
			{
				disable(player);
			}
		}
	}
	
	@EventHandler
	public void paint(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : getActive())
		{
			if (!UtilGear.isMat(player.getItemInHand(), Material.WOOD_SWORD))
				continue;
			
			if (!player.isBlocking())
			{
				_brushPrevious.remove(player.getName());
				continue;
			}
				
			Block block = player.getTargetBlock((HashSet<Byte>)null, 100);
			if (block == null || block.getType() != Material.WOOL)	
				continue;

			//Color
			Manager.getBlockRestore().add(block, 35, _brushColor.get(player.getName()), 30000);
						
			//Join Dots
			if (_brushPrevious.containsKey(player.getName()))
			{				
				while (UtilMath.offset(_brushPrevious.get(player.getName()), block.getLocation().add(0.5, 0.5, 0.5)) > 0.5)
				{					
					_brushPrevious.get(player.getName()).add(UtilAlg.getTrajectory(_brushPrevious.get(player.getName()), block.getLocation().add(0.5, 0.5, 0.5)).multiply(0.5));

					Block fixBlock = _brushPrevious.get(player.getName()).getBlock();
					
					if (fixBlock.getType() != Material.WOOL)
						continue;
			
					Manager.getBlockRestore().add(fixBlock, 35, _brushColor.get(player.getName()), 30000);
				}
			}
			
			player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 0.25f, 2f);
			

			_brushPrevious.put(player.getName(), block.getLocation().add(0.5, 0.5, 0.5));
		}
	}
	
	@EventHandler
	public void enableInteract(PlayerInteractEntityEvent event)
	{		
		if (isActive(event.getPlayer()))
			return;
				
		if (!(event.getRightClicked() instanceof Villager))
			return;

		Villager villager = (Villager)event.getRightClicked();
		
		if (villager.getCustomName() != null && villager.getCustomName().contains("Bob Ross"))
		{
			enable(event.getPlayer());
		}
	}
}
