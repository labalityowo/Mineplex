package mineplex.minecraft.game.classcombat.Class;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.Donor;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Class.event.ClassEquipEvent;
import mineplex.minecraft.game.classcombat.Class.repository.token.ClientClassToken;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.Class.repository.token.SlotToken;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import mineplex.minecraft.game.classcombat.Skill.ISkill.SkillType;
import mineplex.minecraft.game.classcombat.Skill.Knight.AxeThrow;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.shop.ClassShopManager;

public class ClientClass
{	
	private ClassManager _classFactory;
	private SkillFactory _skillFactory;
	private ItemFactory _itemFactory;
	private CoreClient _client;
	private Donor _donor;
	
	private IPvpClass _gameClass;
	private NautHashMap<SkillType, ISkill> _skillMap = new NautHashMap<SkillType, ISkill>();
	
	private IPvpClass _lastClass;
	private NautHashMap<Integer, ItemStack> _lastItems = new NautHashMap<Integer, ItemStack>();
	private ItemStack[] _lastArmor = new ItemStack[4];
	private NautHashMap<SkillType, Entry<ISkill, Integer>> _lastSkillMap = new NautHashMap<SkillType, Entry<ISkill, Integer>>();
	
    private NautHashMap<IPvpClass, HashMap<Integer, CustomBuildToken>> _customBuilds;
    private NautHashMap<IPvpClass, CustomBuildToken> _activeCustomBuilds;
    
    private CustomBuildToken _savingCustomBuild;
    
    public ClientClass(ClassManager classFactory, SkillFactory skillFactory, ItemFactory itemFactory, CoreClient client, Donor donor, ClientClassToken token)
    {
    	_classFactory = classFactory;
    	_skillFactory = skillFactory;
    	_itemFactory = itemFactory;
    	_client = client;
    	_donor = donor;
    	
    	Load(token);
    }
    
    public void Load(ClientClassToken token)
    {
        _customBuilds = new NautHashMap<>();
        _activeCustomBuilds = new NautHashMap<>();
        
	    for (IPvpClass pvpClass : _classFactory.GetAllClasses())
	    {
	    	_customBuilds.put(pvpClass, new HashMap<>());
	    	_customBuilds.get(pvpClass).put(0, pvpClass.getDefaultBuild());
	    }
	    
	    if (token == null)
	    	return;
	    
	    for (CustomBuildToken buildToken : token.CustomBuilds)
	    {
	    	IPvpClass pvpClass = _classFactory.GetClass(buildToken.PvpClass);
	    	
	    	ISkill swordSkill = _skillFactory.GetSkill(buildToken.SwordSkill);
	    	ISkill axeSkill = _skillFactory.GetSkill(buildToken.AxeSkill);
	    	ISkill bowSkill = _skillFactory.GetSkill(buildToken.BowSkill);
	    	ISkill classPassiveASkill = _skillFactory.GetSkill(buildToken.ClassPassiveASkill);
	    	ISkill classPassiveBSkill = _skillFactory.GetSkill(buildToken.ClassPassiveBSkill);
	    	ISkill globalPassive = _skillFactory.GetSkill(buildToken.GlobalPassiveSkill);
	    	
	    	int skillTokenUsage = 0;
	    	int itemTokenUsage = 0;
	    	
	    	if (!buildToken.SwordSkill.isEmpty())
	    	{
	    		if (!ValidSkill(buildToken.SwordSkill, swordSkill, SkillType.Sword))
	    			continue;
	    		else
	    			skillTokenUsage += swordSkill.GetTokenCost() * buildToken.SwordSkillLevel;
	    	}
	    	
	    	if (!buildToken.AxeSkill.isEmpty())
	    	{
	    		if (!ValidSkill(buildToken.AxeSkill, axeSkill, SkillType.Axe))
	    			continue;
	    		else
	    			skillTokenUsage += axeSkill.GetTokenCost() * buildToken.AxeSkillLevel;
	    	}		
	    	
	    	if (!buildToken.BowSkill.isEmpty())
	    	{
	    		if (!ValidSkill(buildToken.BowSkill, bowSkill, SkillType.Bow))
	    			continue;
	    		else
	    			skillTokenUsage += bowSkill.GetTokenCost() * buildToken.BowSkillLevel;
	    	}	
	    	
	    	if (!buildToken.ClassPassiveASkill.isEmpty())
	    	{
	    		if (!ValidSkill(buildToken.ClassPassiveASkill, classPassiveASkill, SkillType.PassiveA))
	    			continue;
	    		else
	    			skillTokenUsage += classPassiveASkill.GetTokenCost() * buildToken.ClassPassiveASkillLevel;
	    	}	
	    	
	    	if (!buildToken.ClassPassiveBSkill.isEmpty())
	    	{
	    		if (!ValidSkill(buildToken.ClassPassiveBSkill, classPassiveBSkill, SkillType.PassiveB)) 
	    			continue;
	    		else
	    			skillTokenUsage += classPassiveBSkill.GetTokenCost() * buildToken.ClassPassiveBSkillLevel;
	    	}		
	    	
	    	if (!buildToken.GlobalPassiveSkill.isEmpty())
	    	{
	    		if (!ValidSkill(buildToken.GlobalPassiveSkill, globalPassive, SkillType.GlobalPassive))
	    			continue;
	    		else
	    			skillTokenUsage += globalPassive.GetTokenCost() * buildToken.GlobalPassiveSkillLevel;
	    	}	

	    	for (SlotToken slotToken : buildToken.Slots)
	    	{
	    		if (slotToken == null)
	    			continue;

				if (slotToken.Name == null)
					continue;
	    		
	    		if (slotToken.Material == null)
	    			continue;
	    		
	    		if (slotToken.Material.isEmpty())
	    			continue;
	    		
	    		if (_itemFactory.GetItem("Cobweb".equalsIgnoreCase(slotToken.Name) ? "Web" : slotToken.Name) != null)
	    		{
	    			itemTokenUsage += _itemFactory.GetItem("Cobweb".equalsIgnoreCase(slotToken.Name) ? "Web" : slotToken.Name).getTokenCost();
	    		}
	    	}
	    	
	    	itemTokenUsage += buildToken.ItemTokens;
	    	skillTokenUsage += buildToken.SkillTokens;
	    	
	    	if (itemTokenUsage > CustomBuildToken.MAX_ITEM_TOKENS || skillTokenUsage > CustomBuildToken.MAX_SKILL_TOKENS)
	    	{
	    		System.out.println(buildToken.PvpClass + " " + buildToken.CustomBuildId  + "'s item tokens :" + itemTokenUsage + " skill tokens :" + skillTokenUsage);
	    		continue;
	    	}
	    	
	    	/*
	    	if (allEmpty)
	    	{
	    		buildToken.SkillTokens = CustomBuildToken.MAX_SKILL_TOKENS;
	    		buildToken.ItemTokens = CustomBuildToken.MAX_ITEM_TOKENS;
	    		
		    	if (!buildToken.SwordSkill.isEmpty() && ValidSkill(buildToken.SwordSkill, swordSkill, SkillType.Sword))
		    		buildToken.SkillTokens -= swordSkill.GetTokenCost();
		    	
		    	if (!buildToken.AxeSkill.isEmpty() && ValidSkill(buildToken.AxeSkill, axeSkill, SkillType.Axe))
		    		buildToken.SkillTokens -= axeSkill.GetTokenCost();
		    	
		    	if (!buildToken.BowSkill.isEmpty() && ValidSkill(buildToken.BowSkill, bowSkill, SkillType.Bow))
		    		buildToken.SkillTokens -= bowSkill.GetTokenCost();
		    	
		    	if (!buildToken.ClassPassiveASkill.isEmpty() && ValidSkill(buildToken.ClassPassiveASkill, classPassiveASkill, SkillType.PassiveA))
		    		buildToken.SkillTokens -= classPassiveASkill.GetTokenCost();
		    	
		    	if (!buildToken.ClassPassiveBSkill.isEmpty() && ValidSkill(buildToken.ClassPassiveBSkill, classPassiveBSkill, SkillType.PassiveB)) 
		    		buildToken.SkillTokens -= classPassiveBSkill.GetTokenCost();
		    	
		    	if (!buildToken.GlobalPassiveSkill.isEmpty() && ValidSkill(buildToken.GlobalPassiveSkill, globalPassive, SkillType.GlobalPassive))
		    		buildToken.SkillTokens -= globalPassive.GetTokenCost();
	    	}
	    	*/
	    	
	    	if (buildToken.CustomBuildNumber == 0)
	    	{
	    		_activeCustomBuilds.put(pvpClass, buildToken);
	    	}
	    	else
	    	{
	    		_customBuilds.get(pvpClass).put(buildToken.CustomBuildNumber, buildToken);
	    	}
	    }
    }
    
	public NautHashMap<Integer, ItemStack> GetDefaultItems()
	{
		return _lastItems;
	}

	public void SetDefaultHead(ItemStack armor)
	{
		_lastArmor[3] = armor;
	}

	public void SetDefaultChest(ItemStack armor)
	{
		_lastArmor[2] = armor;
	}

	public void SetDefaultLegs(ItemStack armor)
	{
		_lastArmor[1] = armor;
	}

	public void SetDefaultFeet(ItemStack armor)
	{
		_lastArmor[0] = armor;
	}
    
	public void SaveActiveCustomBuild() 
	{
		_savingCustomBuild.PlayerName = _client.getName();
		
		_classFactory.GetRepository().SaveCustomBuild(_savingCustomBuild);
		_savingCustomBuild = null;
	}

	public void SetSavingCustomBuild(IPvpClass pvpClass, CustomBuildToken customBuild)
	{
		_savingCustomBuild = customBuild;
		_savingCustomBuild.PvpClass = pvpClass.GetName();
		
		_customBuilds.get(pvpClass).put(_savingCustomBuild.CustomBuildNumber, _savingCustomBuild);
	}
	
	public void SetActiveCustomBuild(IPvpClass pvpClass, CustomBuildToken customBuild)
	{
		customBuild.Active = true;
		_activeCustomBuilds.put(pvpClass, customBuild);
	}
	
	public CustomBuildToken GetActiveCustomBuild(IPvpClass pvpClass)
	{
		return _activeCustomBuilds.get(pvpClass);
	}
	
	public CustomBuildToken GetSavingCustomBuild() 
	{
		return _savingCustomBuild;
	}

	public boolean IsSavingCustomBuild() 
	{
		return _savingCustomBuild != null;
	}
	
    public HashMap<Integer, CustomBuildToken> GetCustomBuilds(IPvpClass pvpClass)
    {
    	return _customBuilds.get(pvpClass);
    }
    
	public void EquipCustomBuild(CustomBuildToken customBuild) 
	{
		EquipCustomBuild(customBuild, true);
	}
    
	public void EquipCustomBuild(CustomBuildToken customBuild, boolean notify) 
	{
		EquipCustomBuild(customBuild, true, false);
	}
	
	public void EquipCustomBuild(CustomBuildToken customBuild, boolean notify, boolean skillsOnly) 
	{
		_lastClass = _classFactory.GetClass(customBuild.PvpClass);

		if (_lastClass == null)
			return;
		
		_lastSkillMap.remove(SkillType.Class);

		SetDefaultHead(ItemStackFactory.Instance.CreateStack(_lastClass.GetHead()));
		SetDefaultChest(ItemStackFactory.Instance.CreateStack(_lastClass.GetChestplate()));
		SetDefaultLegs(ItemStackFactory.Instance.CreateStack(_lastClass.GetLeggings()));
		SetDefaultFeet(ItemStackFactory.Instance.CreateStack(_lastClass.GetBoots()));
		
		if (!customBuild.SwordSkill.isEmpty())
			_lastSkillMap.put(SkillType.Sword, new AbstractMap.SimpleEntry<ISkill, Integer>(_skillFactory.GetSkill(customBuild.SwordSkill), customBuild.SwordSkillLevel));
		else
			_lastSkillMap.remove(SkillType.Sword);

		if (!customBuild.AxeSkill.isEmpty())
			_lastSkillMap.put(SkillType.Axe, new AbstractMap.SimpleEntry<ISkill, Integer>(_skillFactory.GetSkill(customBuild.AxeSkill), customBuild.AxeSkillLevel));
		else
			_lastSkillMap.remove(SkillType.Axe);

		if (!customBuild.BowSkill.isEmpty())
			_lastSkillMap.put(SkillType.Bow, new AbstractMap.SimpleEntry<ISkill, Integer>(_skillFactory.GetSkill(customBuild.BowSkill), customBuild.BowSkillLevel));
		else
			_lastSkillMap.remove(SkillType.Bow);

		if (!customBuild.ClassPassiveASkill.isEmpty())
			_lastSkillMap.put(SkillType.PassiveA, new AbstractMap.SimpleEntry<ISkill, Integer>(_skillFactory.GetSkill(customBuild.ClassPassiveASkill), customBuild.ClassPassiveASkillLevel));
		else
			_lastSkillMap.remove(SkillType.PassiveA);

		if (!customBuild.ClassPassiveBSkill.isEmpty())
			_lastSkillMap.put(SkillType.PassiveB, new AbstractMap.SimpleEntry<ISkill, Integer>(_skillFactory.GetSkill(customBuild.ClassPassiveBSkill), customBuild.ClassPassiveBSkillLevel));
		else
			_lastSkillMap.remove(SkillType.PassiveB);

		if (!customBuild.GlobalPassiveSkill.isEmpty())
			_lastSkillMap.put(SkillType.GlobalPassive, new AbstractMap.SimpleEntry<ISkill, Integer>(_skillFactory.GetSkill(customBuild.GlobalPassiveSkill), customBuild.GlobalPassiveSkillLevel));
		else
			_lastSkillMap.remove(SkillType.GlobalPassive);
		
		for (int i = 0; i < 9; i++)
		{
			SlotToken token = customBuild.Slots.get(i);
			
			if (token == null || token.Material == null || token.Material.isEmpty())
			{
				_lastItems.put(i, null);
				continue;
			}
			
			ItemStack itemStack = ItemStackFactory.Instance.CreateStack(Enum.valueOf(Material.class, token.Material), (byte)0, token.Amount, token.Name);
			
			if (token.Name.contains("Booster"))
				itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			
			_lastItems.put(i, itemStack);
		}
		
		ResetToDefaults(!skillsOnly, !skillsOnly);
		
		// Event
		ClassEquipEvent event = new ClassEquipEvent(this, customBuild, _client.GetPlayer());
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		if (notify)
		{
			ListSkills(_client.GetPlayer());
			_client.GetPlayer().playSound(_client.GetPlayer().getLocation(), Sound.LEVEL_UP, 1f, 1f);

			_client.GetPlayer().sendMessage(F.main("Class", "You equipped " + F.skill(customBuild.Name) + "."));
		}
		
		UtilServer.runSyncLater(() -> validateClassSkills(_client.GetPlayer()), 20L);
	}

	public void ListSkills(Player caller) 
	{
		UtilPlayer.message(caller, F.main("Skill", "Listing Class Skills:"));

		for (SkillType type : SkillType.values())
			if ((caller != null && caller.isOp()) || type != SkillType.Class)
				if (_skillMap.containsKey(type))
					UtilPlayer.message(caller, F.desc(type.toString(), _skillMap.get(type).GetName() + " Lvl" + _skillMap.get(type).getLevel(caller)));
	}
	
	public void ResetSkills(Player player)
	{
		for (ISkill skill : GetSkills())
		{
			skill.Reset(player);
		}
	}
	
	public void ResetItems()
	{
		_client.GetPlayer().getInventory().clear();
		
		for (Entry<Integer, ItemStack> defaultItem : GetDefaultItems().entrySet())
		{
			_client.GetPlayer().getInventory().setItem(defaultItem.getKey(), defaultItem.getValue());
		}
		
		for (ISkill skill : _skillMap.values())
		{
			if (skill instanceof AxeThrow)
			{
				((AxeThrow)skill).Reset(_client.GetPlayer());
			}
		}
	}
	
	public void ResetToDefaults(boolean equipItems, boolean equipDefaultArmor)
	{
		if (_lastClass == null)
		{
			_lastClass = _classFactory.GetClass("Knight");
			
			_lastArmor[3] = ItemStackFactory.Instance.CreateStack(_lastClass.GetHead());
			_lastArmor[2] = ItemStackFactory.Instance.CreateStack(_lastClass.GetChestplate());
			_lastArmor[1] = ItemStackFactory.Instance.CreateStack(_lastClass.GetLeggings());
			_lastArmor[0] = ItemStackFactory.Instance.CreateStack(_lastClass.GetBoots());
			
			EquipCustomBuild(_customBuilds.get(_lastClass).get(0));
		}	
		
		SetGameClass(_lastClass);
		
		if (equipDefaultArmor)
		{
			if (_lastArmor[3] != null)
				_client.GetPlayer().getInventory().setHelmet(_lastArmor[3].clone());
	
			if (_lastArmor[2] != null)
				_client.GetPlayer().getInventory().setChestplate(_lastArmor[2].clone());
	
			if (_lastArmor[1] != null)
				_client.GetPlayer().getInventory().setLeggings(_lastArmor[1].clone());
	
			if (_lastArmor[0] != null)
				_client.GetPlayer().getInventory().setBoots(_lastArmor[0].clone());
		}
		
		if (equipItems)
		{			
			ResetItems();
		}

		ClearSkills();
		
		if (_skillFactory.GetSkill(_gameClass.GetName() + " Class") != null)
		{
			AddSkill(_skillFactory.GetSkill(_gameClass.GetName() + " Class"), 1);
		}
		
		for (Entry<ISkill, Integer> skill : _lastSkillMap.values())
		{
			AddSkill(skill.getKey(), skill.getValue());
		}
	}
    
	public void ClearSkills()
	{
		if (_skillMap != null)
		{
			for (ISkill skill : _skillMap.values())
			{
				skill.RemoveUser(_client.GetPlayer());
			}
		}

		_skillMap.clear();
	}
	
	public void ClearDefaultSkills()
	{
		_lastSkillMap = new NautHashMap<SkillType, Entry<ISkill, Integer>>();
	}
    
	public void SetGameClass(IPvpClass gameClass) 
	{
		ClearSkills();

		_gameClass = gameClass;
	}
    
	public IPvpClass GetGameClass() 
	{
		return _gameClass;
	}

	public boolean IsGameClass(ClassType... types)
	{
		if (GetGameClass() == null)
			return false;

		for (ClassType type : types)
		{
			if (type == GetGameClass().GetType())
			{
				return true;
			}
		}

		return false;
	}
	
	public Collection<ISkill> GetSkills() 
	{
		if (_skillMap == null)
			_skillMap = new NautHashMap<SkillType, ISkill>();

		return _skillMap.values();
	}
	
	public Collection<Entry<ISkill, Integer>> GetDefaultSkills() 
	{
		return _lastSkillMap.values();
	}

	public ISkill GetSkillByType(SkillType skillType)
	{
		if (_skillMap == null)
			_skillMap = new NautHashMap<SkillType, ISkill>();

		if (_skillMap.containsKey(skillType))
			return _skillMap.get(skillType);

		return null;
	}
	
	public void AddSkill(ISkill skill, int level) 
	{
		if (skill == null)
			return;
		
		if (_skillMap == null)
			_skillMap = new NautHashMap<SkillType, ISkill>();

		if (_skillMap.get(skill.GetSkillType()) != null)
			_skillMap.get(skill.GetSkillType()).RemoveUser(_client.GetPlayer());

		_skillMap.put(skill.GetSkillType(), skill);
		_lastSkillMap.put(skill.GetSkillType(), new AbstractMap.SimpleEntry<ISkill, Integer>(skill, level));
		
		skill.AddUser(_client.GetPlayer(), level);
		
		if (IsSavingCustomBuild())
			_savingCustomBuild.setSkill(skill, level);
	}

	public void RemoveSkill(ISkill skill) 
	{
		if (skill == null)
			return;
		
		if (_skillMap == null)
			return;

		_skillMap.remove(skill.GetSkillType());
		_lastSkillMap.remove(skill.GetSkillType());
		
		if (IsSavingCustomBuild())
			_savingCustomBuild.removeSkill(skill);

		skill.RemoveUser(_client.GetPlayer());
	}

	public ItemStack[] GetDefaultArmor()
	{
		return _lastArmor;
	}

	public void ClearDefaults()
	{
		_lastItems.clear();
		_lastArmor = new ItemStack[4];
		_lastSkillMap.clear();
	}
	
	private boolean ValidSkill(String skillName, ISkill skill, SkillType expectedType)
	{
		try
		{
			if (skillName == null || skill == null || expectedType == null)
			{
				return false;
			}
			
			if (!skillName.isEmpty() && (skill.GetSkillType() != expectedType || !skill.IsFree() && !_donor.ownsUnknownSalesPackage("Champions " + skillName) && !_client.hasPermission(ClassShopManager.Perm.SKILL_UNLOCK_LEGACY) && !_donor.ownsUnknownSalesPackage("Competitive ULTRA")))
			{
				return false;
			}
		}
		catch (NullPointerException ex) 
		{
			System.out.println("Somehow a Nullpointer happens here if someone uses /disguise.\n" + 
					"shouldnt be a problem because Youtube+ can have all skills.");
		}
    	return true;
	}

	public void DisplaySkills(Player player)
	{
		String bar = "------------------------------------------";
		player.sendMessage(bar);
		for (SkillType type : _lastSkillMap.keySet())
		{
			if (_lastSkillMap.get(type).getKey() == null)
				continue;
			player.sendMessage(C.cGreen + type + ": " + C.cWhite + _lastSkillMap.get(type).getKey().GetName() + " " + _lastSkillMap.get(type).getValue());
		}
		player.sendMessage(bar);
	}
	
	//This is used to fix a dual class bug using Twitch broadcasting GUI (F6)
	public void validateClassSkills(Player player)
	{
		if (_gameClass == null || _skillMap == null)
			return;
		
		for (SkillType type : SkillType.values())
		{
			if (type == SkillType.GlobalPassive)
			{
				continue;
			}
			
			if (!_skillMap.containsKey(type))
			{
				continue;
			}
			
			ISkill skill = _skillMap.get(type);
			
			if (skill.GetClassType() != _gameClass.GetType())
			{
				skill.Reset(player);
				RemoveSkill(skill);
								
				System.out.println("[" + player.getName() + " / " + _gameClass.GetType() + "] Removed Invalid " + skill.GetClassType() + " Skill (" + skill.GetName() + ")");
			}
		}
	}
}