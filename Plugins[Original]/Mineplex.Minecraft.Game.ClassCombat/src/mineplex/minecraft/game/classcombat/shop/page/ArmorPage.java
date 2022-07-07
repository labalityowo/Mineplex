package mineplex.minecraft.game.classcombat.shop.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.shop.ClassCombatShop;
import mineplex.minecraft.game.classcombat.shop.ClassShopManager;
import mineplex.minecraft.game.classcombat.shop.button.SelectClassButton;
import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.common.util.C;

public class ArmorPage extends ShopPageBase<ClassShopManager, ClassCombatShop>
{
	public ArmorPage(ClassShopManager shopManager, ClassCombatShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{        
		super(shopManager, shop, clientManager, donationManager, "       Armor", player);
		
		buildPage();
	}
	
	public void SelectClass(Player player, IPvpClass pvpClass)
	{			
		ClientClass clientClass = getPlugin().GetClassManager().Get(player);
		
		player.getInventory().clear();
		
		clientClass.SetGameClass(pvpClass);
		clientClass.ClearDefaults();
		
		getShop().openPageForPlayer(getPlayer(), new CustomBuildPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
	}

	@Override
	protected void buildPage()
	{
        int slot = 9;
        
        for (IPvpClass gameClass : getPlugin().GetClassManager().GetGameClasses())
        {
        	BuildArmorSelectPackage(gameClass, slot);
            
            slot += 2;
        }
	}
	
    private void BuildArmorSelectPackage(IPvpClass gameClass, int slot)
    {
    	List<String> lockedClassDesc = new ArrayList<String>();
    	List<String> unlockedClassDesc = new ArrayList<String>();
    	
    	lockedClassDesc.add(C.cBlack);
    	unlockedClassDesc.add(C.cBlack);
    	
    	lockedClassDesc.addAll(Arrays.asList(gameClass.GetDesc()));
    	unlockedClassDesc.addAll(Arrays.asList(gameClass.GetDesc()));
    	
    	for (int i = 1; i < lockedClassDesc.size(); i++)
    	{
    		lockedClassDesc.set(i, C.cGray + lockedClassDesc.get(i));
    	}
    	
    	for (int i = 1; i < unlockedClassDesc.size(); i++)
    	{
    		unlockedClassDesc.set(i, C.cGray + unlockedClassDesc.get(i));
    	}
    	
    	addButton(slot, new ShopItem(gameClass.GetHead(), gameClass.GetName(), 1, false), new SelectClassButton(this, gameClass));
    	addButton(slot + 9, new ShopItem(gameClass.GetChestplate(), gameClass.GetName(), 1, false), new SelectClassButton(this, gameClass));
    	addButton(slot + 18, new ShopItem(gameClass.GetLeggings(), gameClass.GetName(), 1, false), new SelectClassButton(this, gameClass));
    	addButton(slot + 27, new ShopItem(gameClass.GetBoots(), gameClass.GetName(), 1, false), new SelectClassButton(this, gameClass));
    }
}
