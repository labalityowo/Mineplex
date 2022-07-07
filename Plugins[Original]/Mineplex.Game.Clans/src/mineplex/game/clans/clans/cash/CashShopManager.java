package mineplex.game.clans.clans.cash;

import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;

@ReflectivelyCreateMiniPlugin
public class CashShopManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		INVENTORY_COMMAND_PERMISSION,
	}
	
	private final CashShop _shop;
	
	private CashShopManager()
	{
		super("Online Store");
		
		_shop = new CashShop(this);
		
		addCommand(new CommandBase<CashShopManager>(this, Perm.INVENTORY_COMMAND_PERMISSION, "inventory")
		{
			public void Execute(Player caller, String[] args)
			{
				_shop.attemptShopOpen(caller);
			}
		});
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.INVENTORY_COMMAND_PERMISSION, true, true);
	}
}