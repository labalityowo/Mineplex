package mineplex.game.clans.items.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilCollections;
import mineplex.core.common.util.UtilUI;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.ItemType;
import mineplex.game.clans.items.RareItemFactory;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.attributes.armor.ConqueringArmorAttribute;
import mineplex.game.clans.items.attributes.armor.LavaAttribute;
import mineplex.game.clans.items.attributes.armor.PaddedAttribute;
import mineplex.game.clans.items.attributes.armor.ReinforcedAttribute;
import mineplex.game.clans.items.attributes.armor.SlantedAttribute;
import mineplex.game.clans.items.attributes.bow.HeavyArrowsAttribute;
import mineplex.game.clans.items.attributes.bow.HuntingAttribute;
import mineplex.game.clans.items.attributes.bow.InverseAttribute;
import mineplex.game.clans.items.attributes.bow.LeechingAttribute;
import mineplex.game.clans.items.attributes.bow.RecursiveAttribute;
import mineplex.game.clans.items.attributes.bow.ScorchingAttribute;
import mineplex.game.clans.items.attributes.bow.SlayingAttribute;
import mineplex.game.clans.items.attributes.weapon.ConqueringAttribute;
import mineplex.game.clans.items.attributes.weapon.FlamingAttribute;
import mineplex.game.clans.items.attributes.weapon.FrostedAttribute;
import mineplex.game.clans.items.attributes.weapon.HasteAttribute;
import mineplex.game.clans.items.attributes.weapon.JaggedAttribute;
import mineplex.game.clans.items.attributes.weapon.SharpAttribute;
import mineplex.game.clans.items.legendaries.AlligatorsTooth;
import mineplex.game.clans.items.legendaries.DemonicScythe;
import mineplex.game.clans.items.legendaries.GiantsBroadsword;
import mineplex.game.clans.items.legendaries.HyperAxe;
import mineplex.game.clans.items.legendaries.KnightLance;
import mineplex.game.clans.items.legendaries.LegendaryItem;
import mineplex.game.clans.items.legendaries.MagneticMaul;
import mineplex.game.clans.items.legendaries.MeridianScepter;
import mineplex.game.clans.items.legendaries.WindBlade;
import mineplex.game.clans.items.rares.RareItem;
import mineplex.game.clans.items.rares.RunedPickaxe;

public class GearPage extends ShopPageBase<GearManager, GearShop>
{
	private int _stage = 0;
	
	private RareItemFactory _factory;
	
	private final IButton _nextButton;
	private final IButton _backButton;
	
	private Class<? extends ItemAttribute> _superPrefix;
	private Class<? extends ItemAttribute> _prefix;
	private Class<? extends ItemAttribute> _suffix;
	
	private List<Class<? extends ItemAttribute>> _armorSuperPrefixes;
	private List<Class<? extends ItemAttribute>> _armorPrefixes;
	private List<Class<? extends ItemAttribute>> _armorSuffixes;
	
	private List<Class<? extends ItemAttribute>> _weaponSuperPrefixes;
	private List<Class<? extends ItemAttribute>> _weaponPrefixes;
	private List<Class<? extends ItemAttribute>> _weaponSuffixes;
	
	private List<Class<? extends ItemAttribute>> _bowSuperPrefixes;
	private List<Class<? extends ItemAttribute>> _bowPrefixes;
	private List<Class<? extends ItemAttribute>> _bowSuffixes;
	
	private List<Class<? extends LegendaryItem>> _legendaryItems;
	private List<Class<? extends RareItem>> _rareItems;
	
	private List<Material> _weaponTypes;
	private List<Material> _armorTypes;
	
	public GearPage(final GearManager gearManager, final GearShop shop, final CoreClientManager clientManager, final DonationManager donationManager, final String name, final Player player)
	{
		super(gearManager, shop, clientManager, donationManager, name, player);
		
		_nextButton = new IButton()
		{
			public void onClick(final Player player, final ClickType clickType)
			{
				performNext();
			}
		};
		
		_backButton = new IButton()
		{
			public void onClick(final Player player, final ClickType clickType)
			{
				performBack();
			}
		};
		
		_legendaryItems = Arrays.<Class<? extends LegendaryItem>> asList(MeridianScepter.class, AlligatorsTooth.class, WindBlade.class, GiantsBroadsword.class, HyperAxe.class, MagneticMaul.class, DemonicScythe.class, KnightLance.class);
		
		_rareItems = Arrays.<Class<? extends RareItem>> asList(RunedPickaxe.class);
		
		_armorSuperPrefixes = Arrays.<Class<? extends ItemAttribute>> asList(LavaAttribute.class);
		_armorPrefixes = Arrays.<Class<? extends ItemAttribute>> asList(PaddedAttribute.class, ReinforcedAttribute.class, SlantedAttribute.class);
		_armorSuffixes = Arrays.<Class<? extends ItemAttribute>> asList(ConqueringArmorAttribute.class);
		
		_weaponSuperPrefixes = Arrays.<Class<? extends ItemAttribute>> asList(FlamingAttribute.class, FrostedAttribute.class);
		_weaponPrefixes = Arrays.<Class<? extends ItemAttribute>> asList(JaggedAttribute.class, SharpAttribute.class);
		_weaponSuffixes = Arrays.<Class<? extends ItemAttribute>> asList(ConqueringAttribute.class, HasteAttribute.class);
		
		_bowSuperPrefixes = Arrays.<Class<? extends ItemAttribute>> asList(LeechingAttribute.class, ScorchingAttribute.class);
		_bowPrefixes = Arrays.<Class<? extends ItemAttribute>> asList(HeavyArrowsAttribute.class, HuntingAttribute.class, InverseAttribute.class, RecursiveAttribute.class);
		_bowSuffixes = Arrays.<Class<? extends ItemAttribute>> asList(SlayingAttribute.class);
		
		_armorTypes = Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
		_weaponTypes = Arrays.asList(Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.IRON_SWORD, Material.IRON_AXE, Material.GOLD_SWORD, Material.GOLD_AXE, Material.STONE_SWORD, Material.STONE_AXE);
		
		buildPage();
	}
	
	protected void buildPage()
	{
		clearPage();
		
		String stageTitle = "Fatal Error";
		Material stageMaterial = Material.WOOL;
		
		try
		{
			if (_stage == 0)
			{
				Pair<String, Material> stage1 = doStageOne();
				
				stageTitle = stage1.getLeft();
				if (stage1.getRight() != null)
				{
					stageMaterial = stage1.getRight();
				}
			}
			else if (_stage == 1)
			{
				Triple<Boolean, String, Material> stage2 = doStageTwo();
				
				if (stage2.getLeft())
				{
					return;
				}
				
				stageTitle = stage2.getMiddle();
				if (stage2.getRight() != null)
				{
					stageMaterial = stage2.getRight();
				}
			}
			else if (_stage == 2)
			{
				Triple<Boolean, String, Material> stage3 = doStageThree();
				
				if (stage3.getLeft())
				{
					return;
				}
				
				stageTitle = stage3.getMiddle();
				if (stage3.getRight() != null)
				{
					stageMaterial = stage3.getRight();
				}
			}
			else if (_stage == 3)
			{
				Pair<String, Material> stage4 = doStageFour();
				
				stageTitle = stage4.getLeft();
				if (stage4.getRight() != null)
				{
					stageMaterial = stage4.getRight();
				}
			}
			else if (_stage == 4)
			{
				Pair<String, Material> stage5 = doStageFive();
				
				stageTitle = stage5.getLeft();
				if (stage5.getRight() != null)
				{
					stageMaterial = stage5.getRight();
				}
			}
			else
			{
				finish();
				stageTitle = "The End";
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		addButton(4, stageMaterial, 0, stageTitle, (player, clickType) -> {}, false);
		
		addButton(45, Material.REDSTONE_BLOCK, 0, C.cRed + "Previous page", _backButton, false);
		addButton(53, Material.EMERALD_BLOCK, 0, C.cGreen + "Next page", _nextButton, false);
	}
	
	private Pair<String, Material> doStageOne()
	{
		String stageTitle;
		
		int[] indices = UtilUI.getIndicesFor(5, 1, 1);
		
		stageTitle = "1. Select Item Type";
		addButton(indices[0], Material.GOLD_RECORD, 0, C.cGold + "Legendary", new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				_factory = RareItemFactory.begin(ItemType.LEGENDARY);
				performNext();
				buildPage();
			}
		}, _factory != null && ItemType.LEGENDARY.equals(_factory.getItemType()), new String[] { _factory != null && ItemType.LEGENDARY.equals(_factory.getItemType()) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
		
		addButton(indices[1], Material.RECORD_7, 0, C.cAqua + "Rare", new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				_factory = RareItemFactory.begin(ItemType.RARE);
				performNext();
				buildPage();
			}
		}, _factory != null && ItemType.RARE.equals(_factory.getItemType()), new String[] { _factory != null && ItemType.RARE.equals(_factory.getItemType()) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
		
		addButton(indices[2], Material.DIAMOND_SWORD, 0, C.cDGreen + "Weapon", new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				_factory = RareItemFactory.begin(ItemType.WEAPON);
				performNext();
				buildPage();
			}
		}, _factory != null && ItemType.RARE.equals(_factory.getItemType()), new String[] { _factory != null && ItemType.RARE.equals(_factory.getItemType()) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
		
		addButton(indices[3], Material.DIAMOND_CHESTPLATE, 0, C.cDGreen + "Armor", new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				_factory = RareItemFactory.begin(ItemType.ARMOR);
				performNext();
				buildPage();
			}
		}, _factory != null && ItemType.ARMOR.equals(_factory.getItemType()), new String[] { _factory != null && ItemType.ARMOR.equals(_factory.getItemType()) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
		
		addButton(indices[4], Material.BOW, 0, C.cDGreen + "Bow", new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				_factory = RareItemFactory.begin(ItemType.BOW);
				performNext();
				buildPage();
			}
		}, _factory != null && ItemType.BOW.equals(_factory.getItemType()), new String[] { _factory != null && ItemType.BOW.equals(_factory.getItemType()) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
		
		return Pair.create(stageTitle, null);
	}
	
	private Triple<Boolean, String, Material> doStageTwo() throws InstantiationException, IllegalAccessException
	{
		String stageTitle = "Fatal Error";
		Material stageMaterial = null;
		boolean $return = false;
		
		if (_factory == null || _factory.getItemType() == null)
		{
			performBack();
			return Triple.of(true, stageTitle, stageMaterial);
		}
		
		if (_factory.getItemType().equals(ItemType.RARE))
		{
			stageTitle = "2. Select Rare Item";
			stageMaterial = _factory.getMaterial() == null ? stageMaterial : _factory.getMaterial();
			
			int[] indices = UtilUI.getIndicesFor(_rareItems.size(), 1);
			
			int index = 0;
			for (final Class<? extends RareItem> rare : _rareItems)
			{
				final RareItem item = rare.newInstance();
				
				List<String> lore = new ArrayList<>();
				
				lore.addAll(UtilCollections.toList(item.getDescription()));
				
				lore.add(" ");
				lore.add(item.getDisplayName().equals(_factory.getWrapper() == null ? null : _factory.getWrapper().getDisplayName()) ? C.cGreen + "Selected" : C.cRed + "Not Selected");
				
				addButton(indices[index], item.toItemStack().getType(), 0, C.cAqua + item.getDisplayName(), new IButton()
				{
					public void onClick(Player player, ClickType clickType)
					{
						_factory.setRare(rare);
						performNext();
						buildPage();
					}
				}, item.getDisplayName().equals(_factory.getWrapper() == null ? null : _factory.getWrapper().getDisplayName()), lore.toArray(new String[lore.size()]));
				
				index++;
			}
		}
		else if (_factory.getItemType().equals(ItemType.LEGENDARY))
		{
			stageTitle = "2. Select Legendary Item";
			stageMaterial = _factory.getMaterial() == null ? stageMaterial : _factory.getMaterial();
			
			int[] indices = UtilUI.getIndicesFor(_legendaryItems.size(), 1);
			
			int index = 0;
			for (final Class<? extends LegendaryItem> legendary : _legendaryItems)
			{
				final LegendaryItem item = legendary.newInstance();
				
				List<String> lore = new ArrayList<>();
				
				lore.addAll(Arrays.asList(item.getDescription()));
				
				lore.add(" ");
				lore.add(item.getDisplayName().equals(_factory.getWrapper() == null ? null : _factory.getWrapper().getDisplayName()) ? C.cGreen + "Selected" : C.cRed + "Not Selected");
				
				addButton(indices[index], item.toItemStack().getType(), 0, C.cGold + item.getDisplayName(), new IButton()
				{
					public void onClick(Player player, ClickType clickType)
					{
						_factory.setLegendary(legendary);
						performNext();
						buildPage();
					}
				}, item.getDisplayName().equals(_factory.getWrapper() == null ? null : _factory.getWrapper().getDisplayName()), lore.toArray(new String[lore.size()]));
				
				index++;
			}
		}
		else
		{
			switch (_factory.getItemType())
			{
				case WEAPON:
				{
					stageTitle = "Select Weapon Type";
					
					int[] indices = UtilUI.getIndicesFor(_weaponTypes.size(), 1);
					
					int index = 0;
					for (final Material type : _weaponTypes)
					{
						addButton(indices[index], type, 0, C.cGold + WordUtils.capitalizeFully(type.toString()), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_factory.setType(type);
								performNext();
								buildPage();
							}
						}, type.equals(_factory.getMaterial()), new String[] { type.equals(_factory.getMaterial()) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						index++;
					}
					break;
				}
				case ARMOR:
				{
					stageTitle = "Select Armor Type";
					
					int[] indices = UtilUI.getIndicesFor(_armorTypes.size(), 1, 4, 0);
					
					int index = 0;
					for (final Material type : _armorTypes)
					{
						addButton(indices[index], type, 0, C.cGold + WordUtils.capitalizeFully(type.toString()), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_factory.setType(type);
								performNext();
								buildPage();
							}
						}, type.equals(_factory.getMaterial()), new String[] { type.equals(_factory.getMaterial()) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						index++;
					}
					break;
				}
				default:
				{
					if (_factory.getMaterial() != null)
					{
						performBack();
						performBack();
						break;
					}
					
					/**
					 * The only time this code block should be reached is when
					 * _itemType is BOW, or if there are ever new item types
					 * added which don't have a case, since LEGENDARY is checked
					 * separately, and ARMOR, and WEAPON have their own cases.
					 */
					
					_factory.setType(Material.BOW);
					_stage = 2;
					buildPage();
					$return = true;
					
					break;
				}
			}
		}
		return Triple.of($return, stageTitle, stageMaterial);
	}
	
	private Triple<Boolean, String, Material> doStageThree() throws InstantiationException, IllegalAccessException
	{
		String stageTitle;
		
		if (_factory.getMaterial() == null)
		{
			performBack();
			return Triple.of(true, null, null);
		}
		
		Material stageMaterial = _factory.getMaterial();
		
		if (_factory.getItemType().equals(ItemType.LEGENDARY) || _factory.getItemType().equals(ItemType.RARE))
		{
			finish();
			stageTitle = "The End";
		}
		else
		{
			switch (_factory.getItemType())
			{
				case WEAPON:
				{
					stageTitle = "Select Super Prefix";
					
					int[] indices = UtilUI.getIndicesFor(_weaponSuperPrefixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _weaponSuperPrefixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], stageMaterial, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_superPrefix = (attribute.equals(_superPrefix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_superPrefix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_superPrefix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				case ARMOR:
				{
					stageTitle = "Select Super Prefix";
					int[] indices = UtilUI.getIndicesFor(_armorSuperPrefixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _armorSuperPrefixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], stageMaterial, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_superPrefix = (attribute.equals(_superPrefix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_superPrefix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_superPrefix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				case BOW:
				{
					stageTitle = "Select Super Prefix";
					
					int[] indices = UtilUI.getIndicesFor(_bowSuperPrefixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _bowSuperPrefixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], Material.BOW, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_superPrefix = (attribute.equals(_superPrefix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_superPrefix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_superPrefix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				default:
				{
					stageTitle = "Fatal Error";
					stageMaterial = Material.REDSTONE_BLOCK;
				}
			}
		}
		
		return Triple.of(false, stageTitle, stageMaterial);
	}
	
	private Pair<String, Material> doStageFour() throws InstantiationException, IllegalAccessException
	{
		String stageTitle;
		Material stageMaterial = _factory.getMaterial();
		
		if (_factory.getItemType().equals(ItemType.LEGENDARY))
		{
			finish();
			stageTitle = "The End";
		}
		else
		{
			switch (_factory.getItemType())
			{
				case WEAPON:
				{
					stageTitle = "Select Prefix";
					
					int[] indices = UtilUI.getIndicesFor(_weaponPrefixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _weaponPrefixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], stageMaterial, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_prefix = (attribute.equals(_prefix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_prefix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_prefix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				case ARMOR:
				{
					stageTitle = "Select Prefix";
					
					int[] indices = UtilUI.getIndicesFor(_armorPrefixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _armorPrefixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], stageMaterial, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_prefix = (attribute.equals(_prefix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_prefix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_prefix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				case BOW:
				{
					stageTitle = "Select Prefix";
					
					int[] indices = UtilUI.getIndicesFor(_bowPrefixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _bowPrefixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], Material.BOW, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_prefix = (attribute.equals(_prefix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_prefix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_prefix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				default:
				{
					stageTitle = "Fatal Error";
					stageMaterial = Material.REDSTONE_BLOCK;
				}
			}
		}
		
		return Pair.create(stageTitle, stageMaterial);
	}
	
	private Pair<String, Material> doStageFive() throws InstantiationException, IllegalAccessException
	{
		String stageTitle;
		Material stageMaterial = _factory.getMaterial();
		
		if (_factory.getItemType().equals(ItemType.LEGENDARY))
		{
			finish();
			stageTitle = "The End";
		}
		else
		{
			switch (_factory.getItemType())
			{
				case WEAPON:
				{
					stageTitle = "Select Suffix";
					
					int[] indices = UtilUI.getIndicesFor(_weaponSuffixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _weaponSuffixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], stageMaterial, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_suffix = (attribute.equals(_suffix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_suffix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_suffix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				case ARMOR:
				{
					stageTitle = "Select Suffix";
					
					int[] indices = UtilUI.getIndicesFor(_armorSuffixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _armorSuffixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], stageMaterial, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_suffix = (attribute.equals(_suffix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_suffix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_suffix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						index++;
					}
					break;
				}
				case BOW:
				{
					stageTitle = "Select Suffix";
					
					int[] indices = UtilUI.getIndicesFor(_bowSuffixes.size(), 1);
					
					int index = 0;
					for (final Class<? extends ItemAttribute> attribute : _bowSuffixes)
					{
						final ItemAttribute attrib = attribute.newInstance();
						
						addButton(indices[index], Material.BOW, 0, C.cGold + attrib.getDisplayName(), new IButton()
						{
							public void onClick(Player player, ClickType clickType)
							{
								_suffix = (attribute.equals(_suffix) ? null : attribute);
								performNext();
								buildPage();
							}
						}, attribute.equals(_suffix), new String[] { C.cWhite + attrib.getDescription(), "", attribute.equals(_suffix) ? C.cGreen + "Selected" : C.cRed + "Not Selected" });
						
						index++;
					}
					break;
				}
				default:
				{
					stageTitle = "Fatal Error";
					stageMaterial = Material.REDSTONE_BLOCK;
				}
			}
		}
		
		return Pair.create(stageTitle, stageMaterial);
	}
	
	private void addButton(final int index, final Material type, final int data, final String name, final IButton ibutton, final boolean dullEnchantment, final String... description)
	{
		ShopItem shopItem;
		
		if (dullEnchantment)
		{
			shopItem = new ShopItem(type, (byte) data, name, description, 1, ibutton == null, false).addGlow();
		}
		else
		{
			shopItem = new ShopItem(type, (byte) data, name, description, 1, ibutton == null, false);
		}
		
		addButton(index, shopItem, ibutton);
	}
	
	private void addButton(final int index, final ItemStack stack, final IButton ibutton, final String... description)
	{
		addButton(index, new ShopItem(stack, ibutton == null, false), ibutton);
	}
	
	private void performBack()
	{
		if (_stage > 0)
		{
			_stage--;
			buildPage();
		}
	}
	
	protected void finish()
	{
		clearPage();
		
		_factory.setSuperPrefix(_superPrefix);
		_factory.setPrefix(_prefix);
		_factory.setSuffix(_suffix);
		
		final ItemStack item = _factory.fabricate();
		
		addButton(9 + 4, item, new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				player.getInventory().addItem(item);
				getPlugin().getPlayerGear(player).updateCache(true);
			}
		}, new String[] { C.cWhite + "Click to get item" });
	}
	
	protected void performNext()
	{
		if (_stage <= 4)
		{
			_stage++;
		}
		buildPage();
	}
}