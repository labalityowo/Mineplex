package mineplex.game.clans.shop.pvp.tnt;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.*;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.tntgenerator.TntGenerator;
import mineplex.game.clans.clans.tntgenerator.TntGeneratorManager;
import mineplex.game.clans.economy.GoldManager;
import mineplex.game.clans.shop.ClansShopItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class TNTGenPage extends ShopPageBase<ClansManager, TNTGenShop>
{
	public TNTGenPage(ClansManager plugin, TNTGenShop shop, CoreClientManager clientManager, DonationManager donationManager, org.bukkit.entity.Player player)
	{
		super(plugin, shop, clientManager, donationManager, "TNT Generator", player);
		
		buildPage();
	}
	
	@Override
	protected void buildPage()
	{
		ClanInfo clan = _plugin.getClan(_player);
		
		if (clan == null)
		{
			_player.closeInventory();
			_player.playSound(_player.getLocation(), Sound.NOTE_BASS, 1, 1);
			UtilPlayer.message(_player, F.main("Clans", "You must be in a clan to manage a TNT Generator."));
			return;
		}
		
		if (clan.getGenerator() == null)
		{
			addButton(13, new ItemBuilder(Material.BREWING_STAND_ITEM).setTitle(C.cRed + "Buy TNT Generator").setLore(" ", C.cYellow + ClansShopItem.TNT_GENERATOR.getBuyPrice() + " Gold").build(), (player, click) ->
			{
				clearPage();
				buildPage();
				
				// Check if generator is still null
				// Prevents someone from buying a generator at the same time as another clan member does.
				if (clan.getGenerator() != null)
				{
					_player.playSound(_player.getLocation(), Sound.NOTE_BASS, 1, 1);
					UtilPlayer.message(_player, F.main("Clans", "Your clan already has a TNT Generator."));
					return;
				}
				
				if (getPlugin().getGoldManager().Get(player).getBalance() >= ClansShopItem.TNT_GENERATOR.getBuyPrice())
				{
					GoldManager.getInstance().deductGold(success ->
					{
						if (success)
						{
							UtilPlayer.message(player, F.main("Clans", "You purchased a " + F.elem("TNT Generator") + " for your Clan. You can now access it from the " + F.elem("PvP Shop")  + "."));
							clan.inform(F.main("Clans", F.elem(player.getName()) + " purchased a " + F.elem("TNT Generator") + " for the Clan. You can now access it from the " + F.elem("PvP Shop")  + "."), player.getName());
							
							TntGenerator generator = new TntGenerator(player.getUniqueId().toString());
							clan.setGenerator(generator);
							_plugin.getClanDataAccess().updateGenerator(clan, null);
						}
						else
						{
							UtilPlayer.message(player, F.main("Clans", "You can not afford to purchase a " + F.elem("TNT Generator") + " for your Clan."));
							_player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
						}
					}, player, ClansShopItem.TNT_GENERATOR.getBuyPrice());
				}
				else
				{
					UtilPlayer.message(player, F.main("Clans", "You can not afford to purchase a " + F.elem("TNT Generator") + " for your Clan."));
					_player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
				}
				
				clearPage();
				buildPage();
			});
		}
		else
		{
			TntGenerator generator = clan.getGenerator();
			
			String nextTnt = "Never";
			
			if (generator.getStock() < 3)
			{
				nextTnt = UtilTime.MakeStr((TntGeneratorManager.SECONDS_PER_TNT - generator.getTicks()) * 1000);
			}
			
			if (clan.getMembers().containsKey(generator.getBuyer()))
			{
				addButton(13, new ItemBuilder(Material.BREWING_STAND_ITEM)
									.setTitle(C.cGreen + "TNT Generator")
									.setLore(
											" ",
											C.cWhite + "Purchased by " + F.elem(clan.getMembers().get(generator.getBuyer()).getPlayerName()),
											" ",
											C.cWhite + "TNT Available: " + F.elem(generator.getStock()),
											" ",
											C.cWhite + "Next TNT: " + F.elem(nextTnt)).build(), (player, click) ->
											{
												clearPage();
												buildPage();
											}
				);
			}
			else
			{
				addButton(13, new ItemBuilder(Material.BREWING_STAND_ITEM)
									.setTitle(C.cGreen + "TNT Generator")
									.setLore(
											" ",
											C.cWhite + "TNT Available: " + F.elem(generator.getStock()),
											" ",
											C.cWhite + "Next TNT: " + F.elem(nextTnt)).build(), (player, click) ->
											{
												clearPage();
												buildPage();
											}
				);
			}
			
			if (generator.getStock() == 0)
			{
				return;
			}
			
			int[] indices = UtilUI.getIndicesFor(generator.getStock(), 2);
			
			for (int index : indices)
			{
				addButton(index, new ItemBuilder(Material.TNT).setTitle(C.cGreen + "Retrieve TNT").setLore(" ", "Click to collect this TNT.").build(), (player, click) ->
				{
					clearPage();
					buildPage();
					
					if (generator.getStock() <= 0)
					{
						UtilPlayer.message(player, F.main("Clans", "Your " + F.elem("TNT Generator") + " no longer contains this piece of TNT."));
						_player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
						return;
					}
					
					if (UtilInv.HasSpace(player, Material.TNT, 1))
					{
						UtilPlayer.message(player, F.main("Clans", "You have successfully collected TNT from your " + F.elem("TNT Generator") + "."));
						clan.inform(F.main("Clans", F.elem(player.getName()) + " has collected TNT from the Clan's " + F.elem("TNT Generator") + "."), player.getName());
						player.getInventory().addItem(new ItemStack(Material.TNT, 1));
						generator.setStock(generator.getStock() - 1);
						_player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
					}
					else
					{
						UtilPlayer.message(player, F.main("Clans", "You do not have enough sufficient space in your inventory to collect TNT."));
						_player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
					}
					
					clearPage();
					buildPage();
					
				});
			}
		}
	}
}