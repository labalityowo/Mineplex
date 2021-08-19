package nautilus.game.arcade.game.games.minestrike.items.guns;

import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItemType;

import org.bukkit.Material;
import org.bukkit.Sound;

public enum GunStats
{
	//Pistols XXX
	CZ75(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "CZ75-Auto", new String[] 
			{
			
			}, 
			500, 0, 			//Cost, Gem Cost
			12, 1, 				//Clip Size, Spare Ammo
			80, 2700, 			//ROF, Reload Time
			35, 0.006, 0.77, 	//Damage, Dropoff, Armor Penetration
			0.02, 0.2,				//COF Min, COF Max 
			0.06, 				//COF Inc per Bullet
			Material.IRON_HOE, Sound.ENDERMAN_DEATH, false, 1),
			
	DEAGLE(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "Desert Eagle",  new String[] 
			{
			
			},
			800, 0, 			//Cost, Gem Cost
			7, 5, 				//Clip Size, Spare Ammo
			300, 2200, 			//ROF, Reload Time
			68, 0.007, 0.85, 	//Damage, Dropoff, Armor Penetration
			0, 0.12,			//COF Min, COF Max 
			0.12,				//COF Inc per Bullet
			Material.GOLD_HOE, Sound.BAT_DEATH, false, 1),
				
	GLOCK_18(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "Glock 18", new String[] 
			{
			
			}, 
			200, 0, 			//Cost, Gem Cost
			20, 6, 				//Clip Size, Spare Ammo
			120, 2200, 			//ROF, Reload Time
			28, 0.008, 0.47, 	//Damage, Dropoff, Armor Penetration
			0.01, 0.06,			//COF Min, COF Max 
			0.06, 				//COF Inc per Bullet
			Material.STONE_HOE, Sound.BAT_LOOP, false, 1),
			
	P2000(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "P2000",  new String[] 
			{
			
			},
			200, 0, 			//Cost, Gem Cost
			13, 4, 				//Clip Size, Spare Ammo
			130, 2200, 			//ROF, Reload Time
			35, 0.008, 0.50, 	//Damage, Dropoff, Armor Penetration
			0.01, 0.06,			//COF Min, COF Max 
			0.03, 				//COF Inc per Bullet
			Material.WOOD_HOE, Sound.GHAST_SCREAM2, false, 1),
	
	P250(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "P250",  new String[] 
			{
			
			},
			300, 0, 			//Cost, Gem Cost
			13, 4, 				//Clip Size, Spare Ammo
			130, 2200, 			//ROF, Reload Time
			35, 0.005, 0.77, 	//Damage, Dropoff, Armor Penetration
			0, 0.05,			//COF Min, COF Max 
			0.03,				//COF Inc per Bullet
			Material.DIAMOND_HOE, Sound.SILVERFISH_KILL, false, 1),
	
	
	//Rifles XXX
	FAMAS(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "FAMAS",  new String[] 
			{
			
			},
			2250, 5000, 		//Cost, Gem Cost
			25, 4, 				//Clip Size, Spare Ammo
			80, 3300, 			//ROF, Reload Time
			30, 0.004, 0.7, 	//Damage, Dropoff, Armor Penetration
			0, 0.17,			//COF Min, COF Max 
			0.06, 				//COF Inc per Bullet
			Material.WOOD_PICKAXE, Sound.WITHER_DEATH, false, 1),
	
	GALIL(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "Galil AR",  new String[] 
			{
			
			},
			2000, 5000, 		//Cost, Gem Cost
			35, 3, 				//Clip Size, Spare Ammo
			80, 2600, 			//ROF, Reload Time
			30, 0.004, 0.75, 	//Damage, Dropoff, Armor Penetration
			0, 0.18,			//COF Min, COF Max 
			0.065, 				//COF Inc per Bullet
			Material.STONE_PICKAXE, Sound.WITHER_SHOOT, false, 1),
			
			
	AK47(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "AK-47",  new String[] 
			{
			
			},
			2700, 5000, 		//Cost, Gem Cost
			30, 3, 				//Clip Size, Spare Ammo
			80, 2500, 			//ROF, Reload Time
			36, 0.004, 0.78, 	//Damage, Dropoff, Armor Penetration
			0, 0.15,			//COF Min, COF Max 
			0.055,				//COF Inc per Bullet
			Material.WOOD_SPADE, Sound.BURP, false, 1),
	
	M4A4(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "M4A4",  new String[] 
			{
			
			},
			2900, 5000, 		//Cost, Gem Cost
			30, 3, 				//Clip Size, Spare Ammo
			80, 3000, 			//ROF, Reload Time
			33, 0.004, 0.7, 	//Damage, Dropoff, Armor Penetration
			0, 0.14,			//COF Min, COF Max 
			0.05, 				//COF Inc per Bullet
			Material.STONE_SPADE, Sound.BAT_TAKEOFF, false, 1),
	
	SG553(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "SG553",  new String[] 
			{
			
			},
			3000, 5000, 		//Cost, Gem Cost
			30, 3, 				//Clip Size, Spare Ammo
			80, 3800, 			//ROF, Reload Time
			30, 0.004, 1.00, 	//Damage, Dropoff, Armor Penetration
			0, 0.13,			//COF Min, COF Max 
			0.045, 				//COF Inc per Bullet
			Material.IRON_PICKAXE, Sound.WITHER_SPAWN, true, 1),
			
	AUG(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "Steyr AUG",  new String[] 
			{
			
			},
			3300, 5000, 		//Cost, Gem Cost
			30, 3, 				//Clip Size, Spare Ammo
			80, 3800, 			//ROF, Reload Time
			28, 0.004, 0.9, 	//Damage, Dropoff, Armor Penetration
			0, 0.12,			//COF Min, COF Max 
			0.04,				//COF Inc per Bullet
			Material.GOLD_PICKAXE, Sound.VILLAGER_DEATH, true, 1),
	
	//Sniper XXX
	AWP(StrikeItemType.PRIMARY_WEAPON, GunType.SNIPER, "AWP",  new String[] 
			{
			
			},
			4750, 5000, 			//Cost, Gem Cost
			10, 3, 					//Clip Size, Spare Ammo
			1500, 3600, 			//ROF, Reload Time
			115, 0, 0.97, 			//Damage, Dropoff, Armor Penetration
			0.2, 0.2,				//COF Min, COF Max 
			0, 						//COF Inc per Bullet
			Material.GOLD_SPADE, Sound.DRINK, true, 1),
			
	SSG08(StrikeItemType.PRIMARY_WEAPON, GunType.SNIPER, "SSG 08",  new String[] 
			{
			
			},
			1700, 5000, 			//Cost, Gem Cost
			10, 6, 					//Clip Size, Spare Ammo
			1250, 3700, 			//ROF, Reload Time
			88, 0.001, 0.85, 		//Damage, Dropoff, Armor Penetration
			0.08, 0.08,				//COF Min, COF Max 
			0, 						//COF Inc per Bullet
			Material.DIAMOND_PICKAXE, Sound.WOLF_DEATH, true, 1),
	
	//Shotgun XXX
	NOVA(StrikeItemType.PRIMARY_WEAPON, GunType.SHOTGUN, "Nova",  new String[] 
			{
			
			},
			1200, 5000, 		//Cost, Gem Cost
			8, 4, 				//Clip Size, Spare Ammo
			1000, 600, 			//ROF, Reload Time
			20, 0.04, 0.5, 		//Damage, Dropoff, Armor Penetration
			0.1, 0.1,			//COF Min, COF Max 
			0, 					//COF Inc per Bullet
			Material.GOLD_AXE, Sound.BLAZE_DEATH, false, 9),
	
	XM1014(StrikeItemType.PRIMARY_WEAPON, GunType.SHOTGUN, "XM1014",  new String[] 
			{
			
			},
			2000, 5000, 		//Cost, Gem Cost
			7, 4, 				//Clip Size, Spare Ammo
			260, 600, 			//ROF, Reload Time
			16, 0.04, 0.7, 		//Damage, Dropoff, Armor Penetration
			0.12, 0.12,			//COF Min, COF Max 
			0, 					//COF Inc per Bullet
			Material.DIAMOND_AXE, Sound.SKELETON_DEATH, false, 6),
	
	//Smg XXX
	P90(StrikeItemType.PRIMARY_WEAPON, GunType.SMG, "P90", new String[] 
			{
			
			},
			2350, 5000, 		//Cost, Gem Cost
			50, 2, 				//Clip Size, Spare Ammo
			35, 3300, 			//ROF, Reload Time
			17, 0.006, 0.65, 	//Damage, Dropoff, Armor Penetration
			0.05, 0.13,			//COF Min, COF Max 
			0.03, 				//COF Inc per Bullet
			Material.STONE_AXE, Sound.CREEPER_DEATH, false, 1),
	
	PPBIZON(StrikeItemType.PRIMARY_WEAPON, GunType.SMG, "PP-Bizon", new String[] 
			{
			
			},
			1400, 5000, 		//Cost, Gem Cost
			64, 2, 				//Clip Size, Spare Ammo
			60, 2400, 			//ROF, Reload Time
			27, 0.007, 0.47, 	//Damage, Dropoff, Armor Penetration
			0.04, 0.15,				//COF Min, COF Max 
			0.04,				//COF Inc per Bullet
			Material.WOOD_AXE, Sound.SHEEP_SHEAR, false, 1);
	
	
	private GunType _gunType;
	private StrikeItemType _itemType;
	
	private String _name;
	private String[] _desc;
	
	private int _cost;
	private int _gemCost;
	
	private int _clipSize;
	private int _clipReserve;
	private long _fireRate;
	private long _reloadTime;
	private double _damage;
	private double _dropOffPerBlock;
	private double _armorPen;
	
	private Material _skin;
	private Sound _fireSound;

	private double _coneMin;
	private double _coneMax;
	private double _coneIncreaseRate;
	
	private boolean _scope = false;
	private int _pellets;
	
	GunStats(StrikeItemType type, GunType gunType, String name, String[] desc, 
			int cost, int gemCost, 
			int clipSize, int clipReserve, 
			long fireRate, long reloadTime, 
			double damage, double dropOffPerBlock, double armorPen, 
			double coneMin, double coneMax,	double coneIncrease, 
			Material skin, Sound sound, boolean scope, int pellets)
	{
		_itemType = type;
		_gunType = gunType;
		
		_name = name;
		_desc = desc;
		
		_cost = cost;
		_gemCost = gemCost;
		
		_clipSize = clipSize;
		_clipReserve = clipReserve;
		_fireRate = fireRate;
		_reloadTime = reloadTime;
		_damage = damage;
		_dropOffPerBlock = dropOffPerBlock;
		_armorPen = armorPen;

		_skin = skin;
		_fireSound = sound;

		_coneMin = coneMin;
		_coneMax = coneMax;
		_coneIncreaseRate = coneIncrease;
		
		_scope = scope;
		_pellets = pellets;
	}

	public StrikeItemType getItemType()
	{
		return _itemType;
	}

	public GunType getGunType()
	{
		return _gunType;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String[] getDesc()
	{
		return _desc;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public int getGemCost()
	{
		return _gemCost;
	}
	
	public int getClipSize()
	{
		return _clipSize;
	}
	
	public int getClipReserve()
	{
		return _clipReserve;
	}
	
	public long getFireRate()
	{
		return _fireRate;
	}
	
	public long getReloadTime()
	{
		return _reloadTime;
	}
	
	public double getDamage()
	{
		return _damage;
	}
	
	public double getDropoff()
	{
		return _dropOffPerBlock;
	}
	
	public double getArmorPen()
	{
		return _armorPen;
	}
	
	public Material getSkin()
	{
		return _skin;
	}
	
	public Sound getFireSound()
	{
		return _fireSound;
	}
	
	public double getConeMin()
	{
		return _coneMin;
	}
	
	public double getConeMax()
	{
		return _coneMax;
	}
	
	public double getConeReduceRate()
	{
		return _gunType.getRecoilReduction();
	}
	
	public double getConeIncreaseRate()
	{
		return _coneIncreaseRate * GunModule.RECOIL;
	}
	
	public boolean getScope()
	{
		return _scope;
	}
	
	public int getPellets()
	{
		return _pellets;
	}
}
