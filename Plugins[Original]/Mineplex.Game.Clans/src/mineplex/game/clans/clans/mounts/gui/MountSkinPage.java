package mineplex.game.clans.clans.mounts.gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.mounts.Mount.SkinType;
import mineplex.game.clans.clans.mounts.MountManager;
import mineplex.game.clans.clans.mounts.MountToken;

public class MountSkinPage extends ShopPageBase<MountManager, MountShop>
{
	private MountOverviewPage _overview;
	private MountToken _token;
	
	public MountSkinPage(MountOverviewPage overview, Player player, MountToken token)
	{
		super(overview.getPlugin(), overview.getShop(), overview.getClientManager(), overview.getDonationManager(), "Skin Management", player, 54);
		
		_overview = overview;
		_token = token;
		buildPage();
	}
	
	private List<SkinType> getAllowed()
	{
		List<SkinType> allowed = new LinkedList<>();
		
		for (SkinType type : SkinType.values())
		{
			if (Arrays.asList(type.getPossibleTypes()).contains(_token.Type))
			{
				allowed.add(type);
			}
		}
		
		return allowed;
	}
	
	private boolean hasUnlocked(Player player, SkinType type)
	{
		if (getClientManager().Get(player).hasPermission(MountManager.Perm.MOUNT_SKIN_UNLOCK))
		{
			return true;
		}
		
		return getDonationManager().Get(player).ownsUnknownSalesPackage(type.getPackageName());
	}
	
	private boolean hasUnlocked(SkinType type)
	{
		return hasUnlocked(getPlayer(), type);
	}
	
	private ItemStack buildResetItem()
	{
		ItemBuilder builder = new ItemBuilder(Material.PAPER);
		
		builder.setTitle(C.cYellow + "Default Skin");
		if (_token.Skin == null)
		{
			builder.setLore(C.cRed, C.cGreenB + "SELECTED");
		}
		
		return builder.build();
	}
	
	private ItemStack buildSkinItem(SkinType type)
	{
		ItemBuilder builder = new ItemBuilder(type.getBaseDisplay());
		
		builder.setTitle(type.getDisplayName());
		builder.setLore(C.cRed);
		if (_token.Skin == type)
		{
			builder.addLore(C.cGreenB + "SELECTED");
		}
		else if (hasUnlocked(type))
		{
			builder.addLore(C.cGreenB + "UNLOCKED");
		}
		else
		{
			builder.addLore(C.cGray + "Available at http://www.mineplex.com/shop");
			builder.addLore(C.cRedB + "LOCKED");
		}
		
		return builder.build();
	}

	@Override
	protected void buildPage()
	{
		addButton(0, new ItemBuilder(Material.BED).setTitle(C.cGreen + "Back").build(), (player, clickType) ->
		{
			_overview.refresh();
			getShop().openPageForPlayer(player, _overview);
		});
		addButton(13, buildResetItem(), (player, clickType) ->
		{
			if (_token.Skin == null)
			{
				playDenySound(player);
			}
			else
			{
				playAcceptSound(player);
				_token.Skin = null;
				getPlugin().getRepository().saveMount(getClientManager().Get(player).getAccountId(), _token, getPlugin().Get(player).getStatToken(_token));
				refresh();
			}
		});
		int[] skinSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42};
		List<SkinType> allowed = getAllowed();
		for (int i = 0; i < skinSlots.length && i < allowed.size(); i++)
		{
			SkinType type = allowed.get(i);
			addButton(skinSlots[i], buildSkinItem(type), (player, clickType) ->
			{
				if (hasUnlocked(player, type) && _token.Skin != type)
				{
					playAcceptSound(player);
					_token.Skin = type;
					getPlugin().getRepository().saveMount(getClientManager().Get(player).getAccountId(), _token, getPlugin().Get(player).getStatToken(_token));
					refresh();
				}
				else
				{
					playDenySound(player);
				}
			});
		}
	}
}