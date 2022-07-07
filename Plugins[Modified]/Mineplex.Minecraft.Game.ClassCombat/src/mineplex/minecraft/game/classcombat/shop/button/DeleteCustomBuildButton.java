package mineplex.minecraft.game.classcombat.shop.button;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import mineplex.core.shop.item.IButton;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.shop.page.CustomBuildPage;

public class DeleteCustomBuildButton implements IButton
{
	private final CustomBuildPage _page;
	private final CustomBuildToken _customBuild;
	
	public DeleteCustomBuildButton(CustomBuildPage page, CustomBuildToken customBuild)
	{
		_page = page;
		_customBuild = customBuild;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_page.getShop().openPageForPlayer(player, new ConfirmationPage<>(player, _page, new ConfirmationProcessor()
		{
			@Override
			public void init(Inventory inventory)
			{

			}

			@Override
			public void process(ConfirmationCallback callback)
			{
				_page.DeleteCustomBuild(_customBuild);
				callback.resolve("Deleted.");
			}
		}, new ItemBuilder(Material.TNT)
				.setTitle(C.cRedB + "Delete Build")
				.addLore("Confirming will delete your", "build, forever. That's a really", "long time.")
				.build()));
	}
}
