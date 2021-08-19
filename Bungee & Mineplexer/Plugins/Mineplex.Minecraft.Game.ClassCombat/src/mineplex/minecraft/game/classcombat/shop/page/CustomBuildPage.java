package mineplex.minecraft.game.classcombat.shop.page;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.Class.event.ClassSetupEvent;
import mineplex.minecraft.game.classcombat.Class.event.ClassSetupEvent.SetupType;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.shop.ClassCombatShop;
import mineplex.minecraft.game.classcombat.shop.ClassShopManager;
import mineplex.minecraft.game.classcombat.shop.button.DeleteCustomBuildButton;
import mineplex.minecraft.game.classcombat.shop.button.EditAndSaveCustomBuildButton;
import mineplex.minecraft.game.classcombat.shop.button.SelectCustomBuildButton;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CustomBuildPage extends ShopPageBase<ClassShopManager, ClassCombatShop>
{
	private IPvpClass _pvpClass;
	
	public CustomBuildPage(ClassShopManager shopManager, ClassCombatShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{        
		super(shopManager, shop, clientManager, donationManager, "       Custom Build", player);
		_pvpClass = getPlugin().GetClassManager().Get(player).GetGameClass();
				
		buildPage();
	}
	
	public CustomBuildPage(ClassShopManager shopManager, ClassCombatShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, IPvpClass kit)
	{
		super(shopManager, shop, clientManager, donationManager, "       Custom Build", player);
		_pvpClass = kit;
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
    	int slot = 9;
    	
		for (int i=0; i < 5; i++)
		{
			byte itemData;
			String[] lockedText = new String[] { };
			boolean locked = false;

			switch (i)
			{
				case 0:
					itemData = 1;
					break;
				case 1:
					itemData = 14;
					break;
				case 2:
					itemData = 11;
	
//					if (!Client.GetRank().Has(Rank.ULTRA) && !_donationManager.Get(Player.getName()).OwnsUnknownPackage("Competitive ULTRA"))
//					{
//						locked = true;
//						lockedText = new String[] { "§rGet Ultra rank to access this slot" };
//					}
					break;
				case 3:
					itemData = 2;
	
//					if (!Client.GetRank().Has(Rank.ULTRA) && !_donationManager.Get(Player.getName()).OwnsUnknownPackage("Competitive ULTRA"))
//					{
//						locked = true;
//						lockedText = new String[] { "§rGet Ultra rank to access this slot" };
//					}
					break;
				default:
					itemData = 4;
	
//					if (!Client.GetRank().Has(Rank.ULTRA) && !_donationManager.Get(Player.getName()).OwnsUnknownPackage("Competitive ULTRA"))
//					{
//						locked = true;
//						lockedText = new String[] { "§rGet Ultra rank to access this slot" };
//					}
					break;
			}

			ClientClass clientClass = getPlugin().GetClassManager().Get(getPlayer());
			
			CustomBuildToken customBuild = clientClass.GetCustomBuilds(_pvpClass).get(i);
			
			if (customBuild != null)
			{
				String[] skills = new String[7];
				
				skills[0] = C.cYellow + " ";
				
				skills[1] = C.cYellow + "Sword: " + ChatColor.RESET + 
						((customBuild.SwordSkillLevel != null && customBuild.SwordSkillLevel > 0) ? 
								(customBuild.SwordSkill + " " + customBuild.SwordSkillLevel) : "None");
				
				skills[2] = C.cYellow + "Axe: " + ChatColor.RESET + 
						((customBuild.AxeSkillLevel != null && customBuild.AxeSkillLevel > 0) ? 
								(customBuild.AxeSkill + " " + customBuild.AxeSkillLevel) : "None");
				
				skills[3] = C.cYellow + "Bow: " + ChatColor.RESET + 
						((customBuild.BowSkillLevel != null && customBuild.BowSkillLevel > 0) ? 
								(customBuild.BowSkill + " " + customBuild.BowSkillLevel) : "None");
				
				skills[4] = C.cYellow + "Passive A: " + ChatColor.RESET + 
						((customBuild.ClassPassiveASkillLevel != null && customBuild.ClassPassiveASkillLevel > 0) ? 
								(customBuild.ClassPassiveASkill + " " + customBuild.ClassPassiveASkillLevel) : "None");
				
				skills[5] = C.cYellow + "Passive B: " + ChatColor.RESET + 
						((customBuild.ClassPassiveBSkillLevel != null && customBuild.ClassPassiveBSkillLevel > 0) ? 
								(customBuild.ClassPassiveBSkill + " " + customBuild.ClassPassiveBSkillLevel) : "None");
				
				skills[6] = C.cYellow + "Passive C: " + ChatColor.RESET + 
						((customBuild.GlobalPassiveSkillLevel != null && customBuild.GlobalPassiveSkillLevel > 0) ? 
								(customBuild.GlobalPassiveSkill + " " + customBuild.GlobalPassiveSkillLevel) : "None");
				
				addButton(slot, new ShopItem(Material.INK_SACK, itemData, "Apply " + customBuild.Name, skills, 1, locked, true), new SelectCustomBuildButton(this, customBuild));
			}
			else
			{
				getInventory().setItem(slot, new ShopItem(Material.INK_SACK, (byte)8, locked ? "Locked Build" : "Unsaved Build", lockedText, 1, locked, true).getHandle());
			}
			
			if (!locked)
			{
				if (customBuild == null)
				{
					customBuild = new CustomBuildToken(_pvpClass.GetType());
					customBuild.CustomBuildNumber = i;
					customBuild.Name = "Build " + (i + 1);
					customBuild.PvpClass = _pvpClass.GetName();
				}
				
				if (i != 0)
		        {
					addButton(slot + 18, new ShopItem(Material.ANVIL, "Edit Build", new String[]{}, 1, locked, true), new EditAndSaveCustomBuildButton(this, customBuild));
		        	addButton(slot + 36, new ShopItem(Material.TNT, "Delete Build", new String[]{"§rIt will never come back..."}, 1, locked, true), new DeleteCustomBuildButton(this, customBuild));
		        }
			}
			else
			{
		        getInventory().setItem(slot + 18, new ShopItem(Material.ANVIL, "Edit Build", new String[] { }, 1, locked, true).getHandle());
		        getInventory().setItem(slot + 36, new ShopItem(Material.TNT, "Delete Build", new String[] { "§rIt will never come back..."}, 1, locked, true).getHandle());
			}

			slot += 2;
		}
	}
	
	public void EditAndSaveCustomBuild(CustomBuildToken customBuild)
	{
		ClientClass clientClass = getPlugin().GetClassManager().Get(getPlayer());
		clientClass.SetActiveCustomBuild(_pvpClass, customBuild);
		
		ClassSetupEvent event = new ClassSetupEvent(getPlayer(), SetupType.SaveEditCustomBuild, _pvpClass.GetType(), customBuild.CustomBuildNumber, customBuild);
		getPlugin().getPlugin().getServer().getPluginManager().callEvent(event);

		if (event.IsCancelled())
			return;

		clientClass.EquipCustomBuild(customBuild, false, getShop().skillOnly());
		clientClass.SetSavingCustomBuild(_pvpClass, customBuild);
		
		getShop().openPageForPlayer(getPlayer(), new SkillPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getPlayer(), _pvpClass));
	}
	
	public void SelectCustomBuild(CustomBuildToken customBuild)
	{
		ClientClass clientClass = getPlugin().GetClassManager().Get(getPlayer());
		clientClass.SetActiveCustomBuild(_pvpClass, customBuild);

		ClassSetupEvent event = new ClassSetupEvent(getPlayer(), SetupType.ApplyCustomBuild, _pvpClass.GetType(), customBuild.CustomBuildNumber + 1, customBuild);
		getPlugin().getPluginManager().callEvent(event);
		
		if (event.IsCancelled())
			return;
		
		clientClass.EquipCustomBuild(customBuild, true, getShop().skillOnly());

		getPlayer().closeInventory();
	}
	
	@SuppressWarnings("deprecation")
	public void DeleteCustomBuild(CustomBuildToken customBuild)
	{
		ClientClass clientClass = getPlugin().GetClassManager().Get(getPlayer());
		
		//Event
		ClassSetupEvent event = new ClassSetupEvent(getPlayer(), SetupType.DeleteCustomBuild, _pvpClass.GetType(), customBuild.CustomBuildNumber + 1, customBuild);
		getPlugin().getPlugin().getServer().getPluginManager().callEvent(event);
		
		if (event.IsCancelled())
			return;
		
		clientClass.GetCustomBuilds(_pvpClass).remove(customBuild.CustomBuildNumber);

		buildPage();
		getPlayer().updateInventory();
	}
}
