package mineplex.minecraft.game.classcombat.shop.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.shop.item.SalesPackageBase;
import mineplex.core.shop.item.SalesPackageProcessor;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import mineplex.minecraft.game.classcombat.item.Item;
import mineplex.minecraft.game.classcombat.shop.ClassCombatShop;
import mineplex.minecraft.game.classcombat.shop.ClassShopManager;
import mineplex.minecraft.game.classcombat.shop.button.DeselectItemButton;
import mineplex.minecraft.game.classcombat.shop.button.PurchaseItemButton;
import mineplex.minecraft.game.classcombat.shop.button.PurchaseSkillButton;
import mineplex.minecraft.game.classcombat.shop.button.SelectItemButton;
import mineplex.minecraft.game.classcombat.shop.button.SelectSkillButton;
import mineplex.minecraft.game.classcombat.shop.salespackage.ItemSalesPackage;
import mineplex.minecraft.game.classcombat.shop.salespackage.SkillSalesPackage;

public class SkillPage extends ShopPageBase<ClassShopManager, ClassCombatShop>
{
	private IPvpClass _pvpClass;

	public SkillPage(ClassShopManager plugin, ClassCombatShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, IPvpClass pvpClass)
	{
		super(plugin, shop, clientManager, donationManager, "    Select Skills", player);

		_pvpClass = pvpClass;

		buildPage();
	}

	public void playerClosed()
	{
		super.playerClosed();

		if (!getShop().skillOnly())
		{
			if (getPlayer() != null && getPlayer().isOnline())
			{
				for (int i = 9; i < 36; i++)
				{
					getPlayer().getInventory().setItem(i, null);
				}
			}
		}
	}

	@Override
	protected void buildPage()
	{
		getButtonMap().clear();
		clear();

		ClientClass clientClass = getPlugin().GetClassManager().Get(getPlayer());
		
		BuildClassSkills(_pvpClass, clientClass);
		BuildGlobalSkills(clientClass);
		
		if (!getShop().skillOnly())
			BuildItems(_pvpClass, clientClass);
	}

	private void BuildItems(IPvpClass gameClass, ClientClass clientClass)
	{
		if (clientClass.GetSavingCustomBuild().ItemTokens > 0)
			addItem(62, new ShopItem(Material.IRON_INGOT, clientClass.GetSavingCustomBuild().ItemTokens + " Item Tokens", null, clientClass.GetSavingCustomBuild().ItemTokens, true, true));
		else
			addItem(62, new ShopItem(Material.REDSTONE_BLOCK, "0 Item Tokens", null, 1, true, true));
		
		
		int slotNumber = 54;
		
		int swordSlotNumber = 72;
		int axeSlotNumber = 73;
		int bowSlotNumber = 74;

		for (Item item : getPlugin().GetItemFactory().GetItems())
		{
			if (item.GetName().contains("Sword"))
			{
				slotNumber = swordSlotNumber;
				swordSlotNumber -= 9;
			}
			else if (item.GetName().contains("Axe"))
			{
				slotNumber = axeSlotNumber;
				axeSlotNumber -= 9;
			}
			else if (item.GetName().contains("Bow"))
			{
				if (gameClass.GetType() != ClassType.Assassin && gameClass.GetType() != ClassType.Ranger)
					continue;
				
				slotNumber = bowSlotNumber;
				bowSlotNumber -= 9;
			}
			else
			{
				if (gameClass.GetType() != ClassType.Assassin && gameClass.GetType() != ClassType.Ranger && item.GetName().contains("Arrow"))
					continue;
				
				if (gameClass.GetType() == ClassType.Assassin && item.GetName().contains("Ranger"))
					continue;
				
				if (gameClass.GetType() == ClassType.Ranger && item.GetName().contains("Assassin"))
					continue;
				
				if (item.GetType() == Material.ARROW)
					slotNumber = 65;
				else if (item.GetType() == Material.MUSHROOM_SOUP)
					slotNumber = 67;
				else if (item.GetType() == Material.TNT)
					slotNumber = 68;
				else if (item.GetType() == Material.WEB)
					slotNumber = 69;
				else if (item.GetType() == Material.POTION)
					slotNumber = 76;
				else if (item.GetType() == Material.REDSTONE_LAMP_OFF)
					slotNumber = 77;
			}

			BuildItem(item, slotNumber, clientClass);
			
			for (int i = 0; i < 9; i++)
			{
				ItemStack itemStack = getPlayer().getInventory().getItem(i);
				
				if (itemStack != null && itemStack.getType() == item.GetType() && itemStack.getAmount() == item.GetAmount())
				{
					getButtonMap().put(81 + i, new DeselectItemButton(this, item, i));
				}
			}
		}
	}

	private void BuildClassSkills(IPvpClass gameClass, ClientClass clientClass)
	{
		if (clientClass.GetSavingCustomBuild().SkillTokens > 0)
			getInventory().setItem(8, new ShopItem(Material.GOLD_INGOT, clientClass.GetSavingCustomBuild().SkillTokens + " Skill Tokens", null, clientClass.GetSavingCustomBuild().SkillTokens, true, true).getHandle());
		else
			getInventory().setItem(8, new ShopItem(Material.REDSTONE_BLOCK, "0 Skill Tokens", null, 1, true, true).getHandle());
		
		getInventory().setItem(0, new ShopItem(Material.IRON_SWORD, "Sword Skills", null, 1, true, true).getHandle());
		getInventory().setItem(9, new ShopItem(Material.IRON_AXE, "Axe Skills", null, 1, true, true).getHandle());
		getInventory().setItem(18, new ShopItem(Material.BOW, "Bow Skills", null, 1, true, true).getHandle());
		getInventory().setItem(27, new ShopItem(Material.INK_SACK, (byte)1, "Class Passive A Skills", null, 1, true, true).getHandle());
		getInventory().setItem(36, new ShopItem(Material.INK_SACK, (byte)14, "Class Passive B Skills", null, 1, true, true).getHandle());  

		int slotNumber = 53;

		int swordSlotNumber = 1;
		int axeSlotNumber = 10;
		int bowSlotNumber = 19;
		int passiveASlotNumber = 28;
		int passiveBSlotNumber = 37;

		for (ISkill skill : getPlugin().GetSkillFactory().GetSkillsFor(gameClass))
		{    
			switch (skill.GetSkillType())
			{
				case Sword:
					slotNumber = swordSlotNumber;
					swordSlotNumber++;
					break;
				case Axe:
					slotNumber = axeSlotNumber;
					axeSlotNumber++;
					break;
				case Bow:
					slotNumber = bowSlotNumber;
					bowSlotNumber++;
					break;
				case PassiveA:
					slotNumber = passiveASlotNumber;
					passiveASlotNumber++;
					break;
				case PassiveB:
					slotNumber = passiveBSlotNumber;
					passiveBSlotNumber++;
					break;
	
				default:
					continue;
			}

			BuildSkillItem(skill, slotNumber, clientClass);
		}
	}

	private void BuildGlobalSkills(ClientClass clientClass)
	{
		getInventory().setItem(45, new ShopItem(Material.INK_SACK, (byte)11, "Global Passive Skills", null, 1, true, true).getHandle());

		int slotNumber = 46;

		for (ISkill skill : getPlugin().GetSkillFactory().GetGlobalSkillsFor(clientClass.GetGameClass()))
		{                
			BuildSkillItem(skill, slotNumber++, clientClass);
		}
	}

	protected void BuildSkillItem(ISkill skill, int slotNumber, ClientClass clientClass)
	{	
		List<String> skillLore = new ArrayList<String>();

		boolean achievementLocked = skill.isAchievementSkill() && !getPlugin().hasAchievements(getPlayer());
		boolean locked = isSkillLocked(skill) || achievementLocked;
		Material material = locked ? Material.EMERALD : (clientClass.GetSavingCustomBuild().hasSkill(skill) ? Material.WRITTEN_BOOK : Material.BOOK); 
		boolean hasSkill = clientClass.GetSavingCustomBuild().hasSkill(skill);
		int level = hasSkill ? clientClass.GetSavingCustomBuild().getLevel(skill) : 1;
		String name = (locked ? ChatColor.RED + skill.GetName() + " (Locked)" : skill.GetName() + 
				ChatColor.RESET + C.Bold + " - " + ChatColor.GREEN + C.Bold + "Level " + (hasSkill ? level : 0) + "/" + skill.getMaxLevel());

		
		if (locked && !skill.isAchievementSkill())
		{
			skillLore.add(C.cYellow + skill.GetGemCost() + " Gems");
			skillLore.add(C.cBlack);
		}

		//Add Lore
		skillLore.addAll(java.util.Arrays.asList(skill.GetDesc(hasSkill ? level : 0)));

		//Add Select Information
		skillLore.add("");
		skillLore.add("");
		if (!hasSkill || level < skill.getMaxLevel())
		{
			skillLore.add(C.cYellow + "Skill Token Cost: " + C.cWhite + skill.GetTokenCost());
			skillLore.add(C.cYellow + "Skill Tokens Remaining: " + C.cWhite + clientClass.GetSavingCustomBuild().SkillTokens + "/" + CustomBuildToken.MAX_SKILL_TOKENS);
			skillLore.add("");

			if (clientClass.GetSavingCustomBuild().SkillTokens >= skill.GetTokenCost())
			{
				if (hasSkill)
					skillLore.add(C.cGreen + "Left-Click to Upgrade to Level " + (level + 1));
				else
					skillLore.add(C.cGreen + "Left-Click to " + (locked && !achievementLocked ? "Purchase" : "Select"));
			}
			else
			{
				skillLore.add(C.cRed + "You don't have enough Skill Tokens.");
			}
		}
		else
		{
			skillLore.add(C.cGold + "You have the maximum Level.");
		}

		//Color Gray
		for (int i = 0; i < skillLore.size(); i++)
		{
			skillLore.set(i, C.cGray + skillLore.get(i));
		}


		ShopItem skillItem = null;
		
		if (achievementLocked)
		{
			skillLore.add(C.cGray + "   ");
			skillLore.add(C.cPurple + "To use this skill, you must have all");
			skillLore.add(C.cPurple + "Champions Achievements unlocked!");
			skillItem = new ShopItem(Material.DIAMOND, (byte)0, name, skillLore.toArray(new String[skillLore.size()]), level, achievementLocked, true);
		}
		else
			skillItem = new ShopItem(material, name, skillLore.toArray(new String[skillLore.size()]), level, locked, true);
	
		if (achievementLocked)
			addItem(slotNumber, skillItem);
		else if (locked)
			addButton(slotNumber, skillItem, new PurchaseSkillButton(this, skill));
		else
			addButton(slotNumber, skillItem, new SelectSkillButton(this, skill, Math.min((hasSkill ? level + 1 : level), skill.getMaxLevel()), clientClass.GetSavingCustomBuild().SkillTokens >= skill.GetTokenCost()));
	}
	
	protected void BuildItem(Item item, int slotNumber, ClientClass clientClass)
	{	
		List<String> itemLore = new ArrayList<String>();

		boolean locked = isItemLocked(item);
		Material material = locked ? Material.EMERALD : item.GetType();
		boolean hasItem = locked ? false : clientClass.GetSavingCustomBuild().hasItem(material, item.GetName());
		
		String name = (locked ? ChatColor.RED + item.GetName() + " (Locked)" : item.GetName());
		
		if (locked)
		{
			itemLore.add(C.cYellow + item.GetGemCost() + " Gems");
			itemLore.add(C.cBlack);
		}

		//Add Lore
		itemLore.addAll(java.util.Arrays.asList(item.GetDesc()));

		//Add Select Information
		itemLore.add("");
		itemLore.add("");

		itemLore.add(C.cYellow + "Item Token Cost: " + C.cWhite + item.getTokenCost());
		itemLore.add(C.cYellow + "Item Tokens Remaining: " + C.cWhite + clientClass.GetSavingCustomBuild().ItemTokens + "/" + 
		        (CustomBuildToken.MAX_ITEM_TOKENS - (_pvpClass.GetType() == ClassType.Assassin || _pvpClass.GetType() == ClassType.Ranger ? 0 : 2)));
		itemLore.add("");

		if (clientClass.GetSavingCustomBuild().ItemTokens >= item.getTokenCost())
		{
			itemLore.add(C.cGreen + "Left-Click to Select");
		}
		else
		{
			itemLore.add(C.cRed + "You don't have enough Item Tokens.");
		}

		//Color Gray
		for (int i = 0; i < itemLore.size(); i++)
		{
			itemLore.set(i, C.cGray + itemLore.get(i));
		}

		ShopItem itemGUI = new ShopItem(material, name, itemLore.toArray(new String[itemLore.size()]), item.GetAmount(), locked, true);
	
		if (locked)
			addButton(slotNumber, itemGUI, new PurchaseItemButton(this, item));
		else
			addButton(slotNumber, itemGUI, new SelectItemButton(this, item, clientClass.GetSavingCustomBuild().ItemTokens >= item.getTokenCost()));
	}
	
	public void SelectSkill(Player player, ISkill skill, int level)
	{
		ClientClass clientClass = getPlugin().GetClassManager().Get(player);
		ISkill existingSkill = clientClass.GetSkillByType(skill.GetSkillType());

		if (existingSkill != null)
		{
			clientClass.RemoveSkill(existingSkill);
		}

		if (level > 0)
		{
			clientClass.AddSkill(skill, level);
		}
		
		playAcceptSound(player);

		buildPage();
	}
	
	public void DeselectSkill(Player player, ISkill skill)
	{
		if (skill.getLevel(player) == 0)
			return;
		
		ClientClass clientClass = getPlugin().GetClassManager().Get(player);
		ISkill existingSkill = clientClass.GetSkillByType(skill.GetSkillType());

		if (existingSkill == null)
		{
			return;
		}
			
		int level = existingSkill.getLevel(player) -1;
		
		clientClass.RemoveSkill(existingSkill);

		if (level > 0)
		{
			clientClass.AddSkill(skill, level);
		}

		playRemoveSound(player);

		buildPage();
	}

	public void PurchaseSkill(Player player, ISkill skill)
	{
		SalesPackageBase salesPackage = new SkillSalesPackage(skill);
		getShop().openPageForPlayer(player, new mineplex.core.shop.confirmation.ConfirmationPage<>(player, this, new SalesPackageProcessor(player, GlobalCurrency.GEM, salesPackage, getDonationManager(), this::buildPage), salesPackage.buildIcon()));
	}
	
	private boolean isSkillLocked(ISkill skill)
	{
		if (skill.IsFree() || getClientManager().Get(getPlayer()).hasPermission(ClassShopManager.Perm.SKILL_UNLOCK) || getDonationManager().Get(getPlayer()).ownsUnknownSalesPackage("Champions ULTRA") || getDonationManager().Get(getPlayer()).ownsUnknownSalesPackage("Champions " + skill.GetName()))
			return false;

		return true;
	}
	
	private boolean isItemLocked(Item item)
	{
		if (item.isFree() || getClientManager().Get(getPlayer()).hasPermission(ClassShopManager.Perm.SKILL_UNLOCK) || getDonationManager().Get(getPlayer()).ownsUnknownSalesPackage("Champions ULTRA") || getDonationManager().Get(getPlayer()).ownsUnknownSalesPackage("Champions " + item.GetName()))
			return false;

		return true;
	}

	public void PurchaseItem(Player player, Item item)
	{
		SalesPackageBase salesPackage = new ItemSalesPackage(item);
		getShop().openPageForPlayer(player, new mineplex.core.shop.confirmation.ConfirmationPage<>(player, this, new SalesPackageProcessor(player, GlobalCurrency.GEM, salesPackage, getDonationManager(), this::buildPage), salesPackage.buildIcon()));
	}

	public void SelectItem(Player player, Item item)
	{
		int index = -1;
		ClientClass clientClass = getPlugin().GetClassManager().Get(player);

		/*
		if (item.getName().contains("Sword") || item.getName().contains("Axe") || item.getName().contains("Bow"))
		{
			if (clientClass.GetSavingCustomBuild().hasItem(item.GetType(), item.getName()))
			{
				PlayDenySound(player);
				System.out.println("Denying because of matching material and name.");
				return;
			}
			
			if (item.getName().contains("Sword"))
			{
				System.out.println("Sword");
				if (clientClass.GetSavingCustomBuild().hasItemWithNameLike("Sword"))
					index = clientClass.GetSavingCustomBuild().getItemIndexWithNameLike("Sword");
			}
			else if (item.getName().contains("Axe"))
			{
				System.out.println("Axe");
				if (clientClass.GetSavingCustomBuild().hasItemWithNameLike("Axe"))
					index = clientClass.GetSavingCustomBuild().getItemIndexWithNameLike("Axe");
			}
			else if (item.getName().contains("Bow"))
			{
				System.out.println("Bow");
				if (clientClass.GetSavingCustomBuild().hasItemWithNameLike("Bow"))
					index = clientClass.GetSavingCustomBuild().getItemIndexWithNameLike("Bow");
			}
			
			if (index != -1)
			{
				System.out.println("Triggering ClickedRight on slot " + (81 + index));
				ButtonMap.get(81 + index).ClickedRight(player);
			}
		}
*/
		if (index == -1 && player.getInventory().firstEmpty() < 9)
			index = player.getInventory().firstEmpty();
		
		if (index != -1)
		{
			playAcceptSound(player);
			
			clientClass.GetSavingCustomBuild().addItem(item, index);
			
			ItemStack itemStack = ItemStackFactory.Instance.CreateStack(item.GetType(), (byte)0, item.GetAmount(), item.GetName());
			
			if (item.GetName().contains("Booster"))
				itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			
			player.getInventory().setItem(index, itemStack);
		}
		else
		{
			playDenySound(player);
		}
		
		buildPage();
	}
	
	public void DeselectItem(Player player, Item item)
	{
		DeselectItem(player, item, getPlugin().GetClassManager().Get(player).GetSavingCustomBuild().getLastItemIndexWithNameLike(item.GetName()));
	}
	
	public void DeselectItem(Player player, Item item, int index)
	{
		if (index != -1)
		{
			playAcceptSound(player);
			
			getPlugin().GetClassManager().Get(player).GetSavingCustomBuild().removeItem(item, index);
			player.getInventory().setItem(index, null);
		}
		else
		{
			playDenySound(player);
		}
		
		buildPage();
	}
}