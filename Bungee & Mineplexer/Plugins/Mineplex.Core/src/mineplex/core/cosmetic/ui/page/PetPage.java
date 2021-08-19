package mineplex.core.cosmetic.ui.page;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.button.PetButton;
import mineplex.core.cosmetic.ui.button.RenamePetButton;
import mineplex.core.cosmetic.ui.button.activate.ActivatePetButton;
import mineplex.core.cosmetic.ui.button.deactivate.DeactivatePetButton;
import mineplex.core.donation.DonationManager;
import mineplex.core.pet.PetExtra;
import mineplex.core.pet.PetType;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.AnvilContainer;
import mineplex.core.shop.page.ShopPageBase;

public class PetPage extends ShopPageBase<CosmeticManager, CosmeticShop>
{
    public PetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player)
    {
        super(plugin, shop, clientManager, donationManager, name, player, 54);
        
        buildPage();
    }
    
    protected void buildPage()
    {
        int slot = 10;

		PetType[] pets = PetType.values();
		Arrays.sort(pets, Comparator.comparing(type -> type.getEntityType().getTypeId()));

        for (PetType pet : pets)
        {
        	List<String> itemLore = new ArrayList<>();
        	
        	itemLore.add(C.cBlack);
			if (pet.getLore().isPresent())
			{
				Collections.addAll(itemLore, UtilText.splitLineToArray(C.cGray + pet.getLore().get(), LineFormat.LORE));
			}
			else
			{
				itemLore.add(C.cGray + "Your very own " + pet.getName() + "!");
			}
        	//Chest Unlocks
        	if (!getPlugin().getPetManager().Get(getPlayer()).getPets().containsKey(pet))
        	{
        		if (pet.getPrice() == -1)
            	{
            		//Nothing
            	}

        		else if (pet.getPrice() == -2 || pet.getPrice() > 0)
            	{
            		itemLore.add(C.cBlack);
            		itemLore.add(C.cBlue + "Found in Treasure Chests");
            	}
            	else if (pet.getPrice() == -3)
            	{
            		itemLore.add(C.cBlack);
            		itemLore.add(C.cBlue + "Found in Winter Holiday Treasure");
            	}
            	else if (pet.getPrice() == -4)
            	{
            		itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Earned by defeating the Pumpkin King");
					itemLore.add(C.cBlue + "in the 2014 Christmas Chaos Event.");
            	}
            	else if (pet.getPrice() == -5)
            	{
            		itemLore.add(C.cBlack);
            		itemLore.add(C.cBlue + "Found in Easter Holiday Treasure");
            	}
				else if (pet.getPrice() == -8)
				{
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Pumpkin's Revenge");
				}
				else if (pet.getPrice() == -9)
				{
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Haunted Chests");
				}
				else if (pet.getPrice() == -18)
				{
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in St Patrick's Chests");
				}
				else if (pet.getPrice() == -19)
				{
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Spring Chests");
				}
				else if (pet.getPrice() == -14)
				{
					itemLore.add(C.cBlack);
					YearMonth yearMonth = pet.getYearMonth();
					if (yearMonth != null)
					{
						int year = yearMonth.getYear();
						Month month = yearMonth.getMonth();
						String monthName = month.getDisplayName(TextStyle.FULL, Locale.US);
						itemLore.addAll(UtilText.splitLine(C.cBlue + "Monthly Power Play Club Reward for " + monthName + " " + year, LineFormat.LORE));
					}
					else
					{
						itemLore.add(C.cBlue + "Bonus Item Unlocked with Power Play Club");
					}
				}

        		//Rank Unlocks
            	else if (pet.getPrice() == -10)
            	{
            		itemLore.add(C.cBlack);
            		itemLore.add(C.cAqua + "Unlocked with Ultra Rank");
            	}
            	else if (pet.getPrice() == -11)
            	{
            		itemLore.add(C.cBlack);
            		itemLore.add(C.cPurple + "Unlocked with Hero Rank");
            	}
            	else if (pet.getPrice() == -12)
            	{
            		itemLore.add(C.cBlack);
            		itemLore.add(C.cGreen + "Unlocked with Legend Rank");
            	}
            	else if (pet.getPrice() == -13)
            	{
            		itemLore.add(C.cBlack);
            		itemLore.add(C.cRed + "Unlocked with Titan Rank");
            	}
        	}	
        	
        	//Owned
        	if (getPlugin().getPetManager().Get(getPlayer()).getPets().containsKey(pet))
        	{
        	    String petName = getPlugin().getPetManager().Get(getPlayer()).getPets().get(pet);
        	    if (petName == null)
        	    {
        	        petName = pet.getName();
        	    }
        	    
        		if (getPlugin().getPetManager().hasActivePet(getPlayer().getName()) && getPlugin().getPetManager().getActivePetType(getPlayer().getName()) == pet)
        		{
        			itemLore.add(C.cBlack);
            		itemLore.add(C.cGreen + "Click to Disable");

					ItemStack item = pet.getDisplayItem();
					ItemMeta itemMeta = item.getItemMeta();
					itemMeta.setDisplayName(C.cGreen + C.Bold + pet.getName() + C.cGreen + " (" + C.cWhite + petName + C.cGreen + ")");
					itemMeta.setLore(itemLore);
					item.setItemMeta(itemMeta);

        			addButton(slot, new ShopItem(item, false, false).hideInfo(), new DeactivatePetButton(this, getPlugin().getPetManager()));

                	addGlow(slot);
        		}
        		else
        		{
        			itemLore.add(C.cBlack);
            		itemLore.add(C.cGreen + "Click to Enable");

					ItemStack item = pet.getDisplayItem();
					ItemMeta itemMeta = item.getItemMeta();
					itemMeta.setDisplayName(C.cGreen + C.Bold + pet.getName() + C.cGreen + " (" + C.cWhite + petName + C.cGreen + ")");
					itemMeta.setLore(itemLore);
					item.setItemMeta(itemMeta);

        			addButton(slot, new ShopItem(item, false, false).hideInfo(), new ActivatePetButton(pet, this));
        			//addButton(slot, new ShopItem(petItem, false, false), iButton);
        		}
        	}
        	//Not Owned
        	else
        	{
        		if (pet.getPrice() > 0)
    			{
        			itemLore.add(C.cBlack);
        			itemLore.add(C.cWhiteB + "Cost: " + C.cAqua + pet.getPrice() + " Treasure Shards");
    			}

        		if (pet.getPrice() > 0 && getDonationManager().Get(getPlayer()).getBalance(GlobalCurrency.TREASURE_SHARD) >= pet.getPrice())
        		{
        			itemLore.add(C.cBlack);
        			itemLore.add(C.cGreen + "Click to Purchase");
        			
        			addButton(slot, new ShopItem(Material.INK_SACK, (byte) 8, pet.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false), new PetButton(pet, this));
        		}
        		else if (pet.getPrice() > 0)
        		{
        			itemLore.add(C.cBlack);
        			itemLore.add(C.cRed + "Not enough Treasure Shards.");
        			
        			setItem(slot, new ShopItem(Material.INK_SACK, (byte)8, pet.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false));
        		}
        		else
        		{
        			setItem(slot, new ShopItem(Material.INK_SACK, (byte)8, pet.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false));
        		}
        	}            
        	
        	slot++;

			if (slot % 9 == 8)
			{
				slot += 2;
			}
        }
        
        slot = 49;
        for (PetExtra petExtra : PetExtra.values())
        {
        	List<String> itemLore = new ArrayList<String>();

			if (getPlugin().getPunishManager().GetClient(_player.getName()).IsMuted())
			{
				itemLore.add(C.cRed + "You may not rename pets while muted!");
				getInventory().setItem(slot, new ShopItem(petExtra.getMaterial(), (byte)0, C.cRed + petExtra.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false).getHandle());
			}
        	else if (!getPlugin().getPetManager().hasActivePet(getPlayer().getName()))
        	{
                itemLore.add(C.cWhite + "You must have an active pet to use this!");
                getInventory().setItem(slot, new ShopItem(petExtra.getMaterial(), (byte)0, C.cRed + petExtra.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false).getHandle());
        	}
			// Silverfish = Wither disguised
			// Villager = Elf
			// Zombie = Pumpkin
        	else if (getPlugin().getPetManager().getActivePet(getPlayer().getName()).getType() != EntityType.SILVERFISH
					|| getPlugin().getPetManager().getActivePet(getPlayer().getName()).getType() != EntityType.VILLAGER
					|| getPlugin().getPetManager().getActivePet(getPlayer().getName()).getType() != EntityType.ZOMBIE)
        	{
        		addButton(slot, new ShopItem(petExtra.getMaterial(), (byte) 0, "Rename " + getPlugin().getPetManager().getActivePet(getPlayer().getName()).getCustomName() + " for " + C.cYellow + petExtra.getPrice(), itemLore.toArray(new String[itemLore.size()]), 1, false, false), new RenamePetButton(this));
        	}
            
        	slot++;
        }

        // Custom pet
		/*addButton(50, new ShopItem(Material.GLASS, C.cGreen + "Custom", new String[]{}, 1, false), (player, clickType) ->
				getShop().openPageForPlayer(getPlayer(), new CustomPetBasePage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Custom Pet", player)));*/

        
		addButton(4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), (player, clickType) ->
				getShop().openPageForPlayer(getPlayer(), new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player)));
    }
    
	public void purchasePet(final Player player, final PetType petType)
	{
		renamePet(player, petType, true);
	}
	
	public void renamePet(Player player, PetType pet, boolean petPurchase)
	{
		playAcceptSound(player);
		
		PetTagPage petTagPage = new PetTagPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Repairing", getPlayer(), pet, petPurchase);
        EntityPlayer entityPlayer = ((CraftPlayer) getPlayer()).getHandle();
        int containerCounter = entityPlayer.nextContainerCounter();
       	UtilPlayer.sendPacket(player, new PacketPlayOutOpenWindow(containerCounter, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name", new Object[0])));
        entityPlayer.activeContainer = new AnvilContainer(entityPlayer.inventory, petTagPage.getInventory());
        entityPlayer.activeContainer.windowId = containerCounter;
        entityPlayer.activeContainer.addSlotListener(entityPlayer);
        UtilPlayer.sendPacket(player, new PacketPlayOutSetSlot(containerCounter, 0, new net.minecraft.server.v1_8_R3.ItemStack(Items.NAME_TAG)));
        
        getShop().setCurrentPageForPlayer(getPlayer(), petTagPage);
	}

	public void deactivatePet(Player player)
	{
		playAcceptSound(player);
		getPlugin().getPetManager().removePet(player, true);
		refresh();
	}
}
