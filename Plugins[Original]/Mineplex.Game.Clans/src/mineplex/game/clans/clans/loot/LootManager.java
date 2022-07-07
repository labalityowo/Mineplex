package mineplex.game.clans.clans.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.weight.WeightSet;
import mineplex.game.clans.clans.mounts.Mount.MountType;
import mineplex.game.clans.items.GearManager;

public class LootManager
{
	private final GearManager _gearManager;

	private WeightSet<ILoot> _commonSet;
	private WeightSet<ILoot> _rareSet;
	private WeightSet<ILoot> _bossSet;
	private WeightSet<ILoot> _undeadCitySet;
	private WeightSet<ILoot> _raidSet;
	private WeightSet<ILoot> _capturePointSet;
	
	public LootManager(GearManager gearManager)
	{
		_gearManager = gearManager;

		_commonSet = new WeightSet<>();
		_rareSet = new WeightSet<>();
		_bossSet = new WeightSet<>();
		_undeadCitySet = new WeightSet<>();
		_raidSet = new WeightSet<>();
		_capturePointSet = new WeightSet<>();
		
		populateCommon();
		populateRare();
		populateBoss();
		populateCity();
		populateRaid();
		populateCapturePoint();
	}
	
	private void populateCommon()
	{
		// Food
		_commonSet.add(5, new ItemLoot(Material.CARROT, 1, 5));
		_commonSet.add(5, new ItemLoot(Material.APPLE, 1, 3));
		_commonSet.add(5, new ItemLoot(Material.COOKED_BEEF, 1, 3));
		_commonSet.add(5, new ItemLoot(Material.RAW_BEEF, 1, 4));
		_commonSet.add(5, new ItemLoot(Material.POTATO_ITEM, 1, 5));
		
		// Armor
		_commonSet.add(2, new ItemLoot(Material.IRON_HELMET, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.IRON_CHESTPLATE, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.IRON_LEGGINGS, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.IRON_BOOTS, 1, 1));
		
		_commonSet.add(2, new ItemLoot(Material.GOLD_HELMET, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.GOLD_CHESTPLATE, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.GOLD_LEGGINGS, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.GOLD_BOOTS, 1, 1));
		
		_commonSet.add(2, new ItemLoot(Material.LEATHER_HELMET, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.LEATHER_CHESTPLATE, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.LEATHER_LEGGINGS, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.LEATHER_BOOTS, 1, 1));
		
		_commonSet.add(2, new ItemLoot(Material.DIAMOND_HELMET, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.DIAMOND_CHESTPLATE, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.DIAMOND_LEGGINGS, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.DIAMOND_BOOTS, 1, 1));
		
		_commonSet.add(2, new ItemLoot(Material.CHAINMAIL_HELMET, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.CHAINMAIL_CHESTPLATE, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.CHAINMAIL_LEGGINGS, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.CHAINMAIL_BOOTS, 1, 1));
		
		_commonSet.add(2, new ItemLoot(Material.IRON_AXE, 1, 1));
		_commonSet.add(2, new ItemLoot(Material.IRON_SWORD, 1, 1));
		
		_commonSet.add(2, new CustomItemLoot(Material.STONE_SWORD, 1, 1, "Standard Sword"));
		_commonSet.add(2, new CustomItemLoot(Material.STONE_AXE, 1, 1, "Standard Axe"));
		_commonSet.add(2, new CustomItemLoot(Material.GOLD_SWORD, 1, 1, "Booster Sword"));
		_commonSet.add(2, new CustomItemLoot(Material.GOLD_AXE, 1, 1, "Booster Axe"));
		_commonSet.add(2, new CustomItemLoot(Material.DIAMOND_SWORD, 1, 1, "Power Sword"));
		_commonSet.add(2, new CustomItemLoot(Material.DIAMOND_AXE, 1, 1, "Power Axe"));
		
		_commonSet.add(1, new ItemLoot(Material.WOOD_SWORD, 1, 1));
		_commonSet.add(1, new ItemLoot(Material.WOOD_AXE, 1, 1));
		
		// Gear
		_commonSet.add(1, new GearLoot(_gearManager));
		
		// Gold
		// _commonSet.add(5, new GoldLoot(_goldManager, 100, 1000));
		_commonSet.add(1, new GoldTokenLoot(5000, 10000));
	}
	
	private void populateRare()
	{
		// Gear
		_rareSet.add(70, new GearLoot(_gearManager));
		_rareSet.add(10, new GoldTokenLoot(50000, 100000));
		_rareSet.add(20, new MountLoot(1, 3, MountType.values()));
	}
	
	private void populateBoss()
	{
		_bossSet.add(70, new GearLoot(_gearManager));
		_bossSet.add(10, new GoldTokenLoot(50000, 100000));
		_bossSet.add(20, new MountLoot(1, 3, MountType.values()));
	}
	
	private void populateCity()
	{
		// Food
		_undeadCitySet.add(5, new ItemLoot(Material.CARROT, 1, 5));
		_undeadCitySet.add(5, new ItemLoot(Material.APPLE, 1, 3));
		_undeadCitySet.add(5, new ItemLoot(Material.COOKED_BEEF, 1, 3));
		_undeadCitySet.add(5, new ItemLoot(Material.RAW_BEEF, 1, 4));
		_undeadCitySet.add(5, new ItemLoot(Material.POTATO_ITEM, 1, 5));
		
		// Armor
		_undeadCitySet.add(3, new ItemLoot(Material.IRON_HELMET, 1, 1));
		_undeadCitySet.add(3, new ItemLoot(Material.IRON_CHESTPLATE, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.IRON_LEGGINGS, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.IRON_BOOTS, 1, 1));
		
		_undeadCitySet.add(4, new ItemLoot(Material.GOLD_HELMET, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.GOLD_CHESTPLATE, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.GOLD_LEGGINGS, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.GOLD_BOOTS, 1, 1));
		
		_undeadCitySet.add(4, new ItemLoot(Material.LEATHER_HELMET, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.LEATHER_CHESTPLATE, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.LEATHER_LEGGINGS, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.LEATHER_BOOTS, 1, 1));
		
		_undeadCitySet.add(4, new ItemLoot(Material.DIAMOND_HELMET, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.DIAMOND_CHESTPLATE, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.DIAMOND_LEGGINGS, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.DIAMOND_BOOTS, 1, 1));
		
		_undeadCitySet.add(4, new ItemLoot(Material.CHAINMAIL_HELMET, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.CHAINMAIL_CHESTPLATE, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.CHAINMAIL_LEGGINGS, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.CHAINMAIL_BOOTS, 1, 1));
		
		_undeadCitySet.add(4, new ItemLoot(Material.IRON_AXE, 1, 1));
		_undeadCitySet.add(4, new ItemLoot(Material.IRON_SWORD, 1, 1));
		
		_undeadCitySet.add(3, new ItemLoot(Material.EMERALD, 10, 15));
		
		_undeadCitySet.add(2, new CustomItemLoot(Material.GOLD_SWORD, 1, 1, "Booster Sword"));
		_undeadCitySet.add(2, new CustomItemLoot(Material.GOLD_AXE, 1, 1, "Booster Axe"));
		_undeadCitySet.add(2, new CustomItemLoot(Material.DIAMOND_SWORD, 1, 1, "Power Sword"));
		_undeadCitySet.add(2, new CustomItemLoot(Material.DIAMOND_AXE, 1, 1, "Power Axe"));
		
		// Gear
		_undeadCitySet.add(1, new GearLoot(_gearManager));
		
		// Gold
		_undeadCitySet.add(1, new GoldTokenLoot(5000, 10000));
	}
	
	private void populateRaid()
	{
		_raidSet.add(70, new GearLoot(_gearManager));
		_raidSet.add(10, new GoldTokenLoot(100000, 200000));
		_raidSet.add(20, new MountLoot(2, 3, MountType.values()));
	}
	
	private void populateCapturePoint()
	{
		// Food
		_capturePointSet.add(5, new ItemLoot(Material.CARROT, 32, 32));
		_capturePointSet.add(5, new ItemLoot(Material.APPLE, 32, 32));
		_capturePointSet.add(5, new ItemLoot(Material.COOKED_BEEF, 32, 32));
		_capturePointSet.add(5, new ItemLoot(Material.RAW_BEEF, 32, 32));
		_capturePointSet.add(5, new ItemLoot(Material.POTATO_ITEM, 32, 32));

		// Armor
		_capturePointSet.add(3, new ItemLoot(Material.IRON_HELMET, 1, 1));
		_capturePointSet.add(3, new ItemLoot(Material.IRON_CHESTPLATE, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.IRON_LEGGINGS, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.IRON_BOOTS, 1, 1));

		_capturePointSet.add(3, new ItemLoot(Material.GOLD_HELMET, 1, 1));
		_capturePointSet.add(3, new ItemLoot(Material.GOLD_CHESTPLATE, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.GOLD_LEGGINGS, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.GOLD_BOOTS, 1, 1));

		_capturePointSet.add(3, new ItemLoot(Material.LEATHER_HELMET, 1, 1));
		_capturePointSet.add(3, new ItemLoot(Material.LEATHER_CHESTPLATE, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.LEATHER_LEGGINGS, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.LEATHER_BOOTS, 1, 1));

		_capturePointSet.add(3, new ItemLoot(Material.DIAMOND_HELMET, 1, 1));
		_capturePointSet.add(3, new ItemLoot(Material.DIAMOND_CHESTPLATE, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.DIAMOND_LEGGINGS, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.DIAMOND_BOOTS, 1, 1));

		_capturePointSet.add(3, new ItemLoot(Material.CHAINMAIL_HELMET, 1, 1));
		_capturePointSet.add(3, new ItemLoot(Material.CHAINMAIL_CHESTPLATE, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.CHAINMAIL_LEGGINGS, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.CHAINMAIL_BOOTS, 1, 1));

		_capturePointSet.add(4, new ItemLoot(Material.IRON_AXE, 1, 1));
		_capturePointSet.add(4, new ItemLoot(Material.IRON_SWORD, 1, 1));

		_capturePointSet.add(4, new ItemLoot(Material.EMERALD, 10, 15));

		_capturePointSet.add(3, new CustomItemLoot(Material.GOLD_SWORD, 1, 1, "Booster Sword"));
		_capturePointSet.add(3, new CustomItemLoot(Material.GOLD_AXE, 1, 1, "Booster Axe"));
		_capturePointSet.add(3, new CustomItemLoot(Material.DIAMOND_SWORD, 1, 1, "Power Sword"));
		_capturePointSet.add(3, new CustomItemLoot(Material.DIAMOND_AXE, 1, 1, "Power Axe"));

		// Gold
		_capturePointSet.add(1, new GoldTokenLoot(10000, 50000));

		// Rune
		_capturePointSet.add(2, new RuneLoot());
	}
	
	public void dropCommon(Location location)
	{
		_commonSet.generateRandom().dropLoot(location);
	}
	
	public void dropRare(Location location)
	{
		_rareSet.generateRandom().dropLoot(location);
	}
	
	public void dropBoss(Location location)
	{
		_bossSet.generateRandom().dropLoot(location);
	}
	
	public void dropUndeadCity(Location location)
	{
		_undeadCitySet.generateRandom().dropLoot(location);
	}
	
	public void dropRaid(Location location)
	{
		_raidSet.generateRandom().dropLoot(location);
	}
	
	public void dropCapturePoint(Location location)
	{
		_capturePointSet.generateRandom().dropLoot(location);
	}
	
	public ItemStack getRareItemStack()
	{
		return _rareSet.generateRandom().getItemStack();
	}
}