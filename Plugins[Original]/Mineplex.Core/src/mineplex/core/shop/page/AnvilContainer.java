package mineplex.core.shop.page;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;

import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.InventoryLargeChest;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import net.minecraft.server.v1_8_R3.Slot;

public class AnvilContainer extends Container
{
	public IInventory _container;
	private CraftInventoryView _bukkitEntity = null;
	private PlayerInventory _playerInventory;

	public AnvilContainer(PlayerInventory playerInventory, IInventory anvilInventory)
	{
		_playerInventory = playerInventory;
		_container = anvilInventory;
		
		a(new Slot(anvilInventory, 0, 27, 47));
		a(new Slot(anvilInventory, 1, 76, 47));
		a(new Slot(anvilInventory, 2, 134, 47));
		
		for (int l = 0; l < 3; l++) 
		{
			for (int i1 = 0; i1 < 9; i1++) 
		  	{
				a(new Slot(playerInventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
		  	}
		}
		
		for (int l = 0; l < 9; l++)
			a(new Slot(playerInventory, l, 8 + l * 18, 142));
	}
	
	@Override
	public CraftInventoryView getBukkitView()
	{
		if (_bukkitEntity != null)
			return _bukkitEntity;
		
		CraftInventory inventory;
		if ((_container instanceof PlayerInventory)) 
		{
			inventory = new CraftInventoryPlayer((PlayerInventory)_container);
		}
		else
		{
			if ((_container instanceof InventoryLargeChest))
				inventory = new CraftInventoryDoubleChest((InventoryLargeChest)_container);
			else 
				inventory = new CraftInventory(_container);
		}
		
		_bukkitEntity = new CraftInventoryView(_playerInventory.player.getBukkitEntity(), inventory, this);
		
		return _bukkitEntity;
	}
	  
	@Override
	public boolean a(EntityHuman arg0)
	{
		return true;
	}

}
