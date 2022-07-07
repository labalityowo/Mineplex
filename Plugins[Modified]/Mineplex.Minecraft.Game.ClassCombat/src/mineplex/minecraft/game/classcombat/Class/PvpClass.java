package mineplex.minecraft.game.classcombat.Class;

import java.util.HashSet;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilGear;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.Skill.ISkill;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class PvpClass implements IPvpClass
{
	private ClassType _type;
	private CustomBuildToken _customBuild;
	private int _salesPackageId;
	private String[] _desc;
	private int _cost;
	private boolean _free;
	
	private Material _head;
	private Material _chest;
	private Material _legs;
	private Material _boots;
	
	private Color _leatherColor = null;

	private HashSet<ISkill> _skillSet;
	
	private ClassManager _classes;
	
	public PvpClass(ClassManager classes, int salesPackageId, ClassType type, CustomBuildToken customBuild, String[] desc, Material head, Material chest, Material legs, Material boots, Color leatherColor)
	{
		_classes = classes;
		_salesPackageId = salesPackageId;
		_cost = 5000;
		_desc = desc;
		
		_type = type;
		
		_customBuild = customBuild;
		_customBuild.PvpClass = type.name();

		_head = head;
		_chest = chest;
		_legs = legs;
		_boots = boots;

		_skillSet = new HashSet<ISkill>();
		_skillSet.addAll(_classes.GetSkillFactory().GetSkillsFor(this));
		_skillSet.addAll(_classes.GetSkillFactory().GetGlobalSkillsFor(this));
		
		_leatherColor = leatherColor;
	}

	@Override
	public String GetName() 
	{
		return _type.name();
	}

	@Override
	public ClassType GetType()
	{
		return _type;
	}

	@Override
	public Material GetHead() 
	{
		return _head;
	}

	@Override
	public Material GetChestplate() 
	{
		return _chest;
	}

	@Override
	public Material GetLeggings() 
	{
		return _legs;
	}

	@Override
	public Material GetBoots() 
	{
		return _boots;
	}

	@Override
	public HashSet<ISkill> GetSkills() 
	{
		return _skillSet;
	}
	
	public void checkEquip()
	{
		for (Player cur : Bukkit.getOnlinePlayers())
		{
			ClientClass client = _classes.Get(cur);
			
			//Check Unequip
			if (client.GetGameClass() != null)
				if (client.GetGameClass().GetType() == _type)
				{
					PlayerInventory inv = cur.getInventory();
		
					//Helm
					if (_head != null)
						if (!UtilGear.isMat(inv.getHelmet(), _head))
						{
							Unequip(cur);
							continue;
						}
		
					//Chest
					if (_chest != null)
						if (!UtilGear.isMat(inv.getChestplate(), _chest))
						{
							Unequip(cur);
							continue;
						}
		
					//Legs
					if (_legs != null)
						if (!UtilGear.isMat(inv.getLeggings(), _legs))
						{
							Unequip(cur);
							continue;
						}
		
					//Boots
					if (_boots != null)
						if (!UtilGear.isMat(inv.getBoots(), _boots))
						{
							Unequip(cur);
							continue;
						}
					
					if (_leatherColor != null)
					{
						if (!((LeatherArmorMeta)inv.getHelmet().getItemMeta()).getColor().equals(_leatherColor) ||
							!((LeatherArmorMeta)inv.getChestplate().getItemMeta()).getColor().equals(_leatherColor)|| 
							!((LeatherArmorMeta)inv.getLeggings().getItemMeta()).getColor().equals(_leatherColor) ||
							!((LeatherArmorMeta)inv.getBoots().getItemMeta()).getColor().equals(_leatherColor))
						{
							Unequip(cur);
							continue;
						}
					}
				}

			//Check Equip
			if (client.GetGameClass() == null || client.GetGameClass().GetType() == null || 
					(_leatherColor != null && client.GetGameClass().GetType() != this.GetType()))
			{
				PlayerInventory inv = cur.getInventory();

				//Helm
				if (_head != null)
					if (!UtilGear.isMat(inv.getHelmet(), _head))
						continue;

				//Chest
				if (_chest != null)
					if (!UtilGear.isMat(inv.getChestplate(), _chest))
						continue;

				//Legs
				if (_legs != null)
					if (!UtilGear.isMat(inv.getLeggings(), _legs))
						continue;

				//Boots
				if (_boots != null)
					if (!UtilGear.isMat(inv.getBoots(), _boots))
						continue;

				if (_leatherColor != null)
				{
					if (!((LeatherArmorMeta)inv.getHelmet().getItemMeta()).getColor().equals(_leatherColor) ||
						!((LeatherArmorMeta)inv.getChestplate().getItemMeta()).getColor().equals(_leatherColor)|| 
						!((LeatherArmorMeta)inv.getLeggings().getItemMeta()).getColor().equals(_leatherColor) ||
						!((LeatherArmorMeta)inv.getBoots().getItemMeta()).getColor().equals(_leatherColor))
						continue;
				}
				
				if (_classes.getGadgetManager() != null && _classes.getGadgetManager().getActive(cur, GadgetType.COSTUME) != null)
					continue;
			
				Equip(cur, _classes.getMessageSuppressedCallback(cur.getName()) == null);
				Callback<String> callback = _classes.getMessageSuppressedCallback(cur.getName());
				if (callback != null) callback.run(cur.getName());
			}
		}
	}

	public void Equip(Player player, boolean inform)
	{		
		ClientClass client = _classes.Get(player);
		
		CustomBuildToken customBuild = client.GetActiveCustomBuild(this);
		
		if (customBuild != null)
		{
			client.EquipCustomBuild(customBuild, inform, true);
		}
		else
		{
			client.SetGameClass(this);
			client.EquipCustomBuild(client.GetCustomBuilds(this).get(0), inform, true);
		}
		
		//Ensure Sneak Removed
		player.setSneaking(false);
	}

	public void Unequip(Player player)
	{
		_classes.Get(player).SetGameClass(null);

		// UtilPlayer.message(player, F.main("Class", "Armor Class: " + F.oo("None", false)));
	}

    @Override
    public int GetSalesPackageId()
    {
        return _salesPackageId;
    }

	@Override
	public Integer GetCost() 
	{
		return _cost;
	}

	@Override
	public String[] GetDesc()
	{
		return _desc;
	}
	
	@Override
	public boolean IsFree()
	{
		return _free;
	}

	@Override
	public void ApplyArmor(Player caller) 
	{
		ItemStack head = ItemStackFactory.Instance.CreateStack(GetHead(), 1);
		ItemStack chest = ItemStackFactory.Instance.CreateStack(GetChestplate(), 1);
		ItemStack legs = ItemStackFactory.Instance.CreateStack(GetLeggings(), 1);
		ItemStack boots = ItemStackFactory.Instance.CreateStack(GetBoots(), 1);
		
		if (_leatherColor != null)
		{ 
			LeatherArmorMeta meta;
			
			//Head
			meta = (LeatherArmorMeta)head.getItemMeta();
			meta.setColor(_leatherColor);
			head.setItemMeta(meta);
			
			//Chest
			meta = (LeatherArmorMeta)chest.getItemMeta();
			meta.setColor(_leatherColor);
			chest.setItemMeta(meta);
			
			//Legs
			meta = (LeatherArmorMeta)legs.getItemMeta();
			meta.setColor(_leatherColor);
			legs.setItemMeta(meta);
			
			//Boots
			meta = (LeatherArmorMeta)boots.getItemMeta();
			meta.setColor(_leatherColor);
			boots.setItemMeta(meta);
		}
		
		caller.getInventory().setHelmet(head);
		caller.getInventory().setChestplate(chest);
		caller.getInventory().setLeggings(legs);
		caller.getInventory().setBoots(boots);
	}

	@Override
	public CustomBuildToken getDefaultBuild()
	{
		return _customBuild;
	}
}
