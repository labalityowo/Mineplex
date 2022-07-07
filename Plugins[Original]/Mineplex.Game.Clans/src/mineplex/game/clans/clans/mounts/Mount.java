package mineplex.game.clans.clans.mounts;

import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.TriConsumer;
import mineplex.core.common.util.C;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.disguise.disguises.DisguiseCow;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.ClansManager;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.GenericAttributes;

public class Mount
{
	private static final long HIT_REGEN_COOLDOWN = 30000;
	
	private Player _owner;
	private CraftHorse _entity;
	private SkinType _skin;
	private final int _strength;
	private long _lastHit;
	private int _hits;
	
	public Mount(Player owner, CraftHorse entity, SkinType skin, int strength)
	{
		_owner = owner;
		_entity = entity;
		_skin = skin;
		_strength = strength;
		_lastHit = System.currentTimeMillis();
		_hits = 0;
	}
	
	public Player getOwner()
	{
		return _owner;
	}
	
	public CraftHorse getEntity()
	{
		return _entity;
	}
	
	public void update()
	{
		if (_skin != null)
		{
			_skin.onUpdate(_entity);
			if (_skin.needsJumpAssist())
			{
				EntityPlayer rider = null;
				if (_entity.getPassenger() != null && _entity.getPassenger() instanceof Player)
				{
					rider = ((CraftPlayer)_entity.getPassenger()).getHandle();
					try
					{
						boolean jumping = MountManager.JumpBooleanField.getBoolean(rider);
						
						if (jumping)
						{
							rider.i(false);
							if (UtilEnt.isGrounded(_entity))
							{
								MountManager.JumpFloatField.setFloat(_entity.getHandle(), 1.0f);
							}
						}
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
		if (UtilTime.elapsed(_lastHit, HIT_REGEN_COOLDOWN) && _hits > 0)
		{
			_hits--;
			_lastHit = System.currentTimeMillis();
		}
	}
	
	public void despawn(boolean forced)
	{
		UtilServer.CallEvent(new MountDespawnEvent(this, forced));
		_entity.getInventory().setSaddle(null);
		_entity.getInventory().setArmor(null);
		for (ItemStack item : _entity.getInventory().getContents())
		{
			if (item == null || item.getType() == Material.AIR)
			{
				continue;
			}
			_entity.getWorld().dropItem(_entity.getLocation(), item);
		}
		_entity.remove();
	}
	
	public void handleHit()
	{
		_hits++;
		if (_hits == _strength)
		{
			despawn(true);
		}
	}
	
	public static enum SkinType
	{
		INFERNAL_HORROR(1, "Clans Infernal Horror Mount Skin", C.cRed + "Infernal Horror", Material.BONE, Color.BLACK, Variant.SKELETON_HORSE, Style.BLACK_DOTS, false, horse -> {}, horse ->
		{
			UtilParticle.PlayParticleToAll(ParticleType.FLAME, horse.getLocation().add(0, 1, 0),
					0.25f, 0.25f, 0.25f, 0, 2,ViewDist.NORMAL);
		}, MountType.HORSE),
		GLACIAL_STEED(2, "Clans Glacial Steed Mount Skin", C.cGray + "Glacial Steed", Material.SNOW_BALL, Color.WHITE, Variant.HORSE, Style.WHITE, false, horse -> {}, horse ->
		{
			UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, horse.getLocation().add(0, 1, 0),
					0.25f, 0.25f, 0.25f, 0.1f, 4, ViewDist.NORMAL);
		}, MountType.HORSE),
		ZOMBIE_HORSE(3, "Clans Zombie Horse Mount Skin", C.cDGray + "Zombie Horse", Material.ROTTEN_FLESH, Color.BLACK, Variant.UNDEAD_HORSE, Style.BLACK_DOTS, false, horse -> {}, horse ->
		{
			UtilParticle.PlayParticleToAll(ParticleType.FOOTSTEP, horse.getLocation(),
					null, 0, 1, ViewDist.NORMAL);
		}, MountType.HORSE),
		@SuppressWarnings("deprecation")
		RAINBOW_SHEEP(4, "Clans Rainbow Sheep Mount Skin", C.cGreen + "Rainbow " + C.cAqua + "Sheep", new ItemBuilder(Material.WOOL).setData(DyeColor.RED.getWoolData()).build(),Color.WHITE, Variant.HORSE, Style.NONE, true, horse ->
		{
			DisguiseSheep disguise = new DisguiseSheep(horse);
			disguise.setName(horse.getCustomName());
			ClansManager.getInstance().getDisguiseManager().disguise(disguise);
			UtilEnt.SetMetadata(horse, "RainbowSheep.ActiveTicks", new EnclosedObject<>(0));
			UtilEnt.SetMetadata(horse, "RainbowSheep.DelayTicks", new EnclosedObject<>(0));
			UtilEnt.SetMetadata(horse, "RainbowSheep.ParticleColor", new EnclosedObject<>(java.awt.Color.RED));
		}, horse ->
		{
			EnclosedObject<Integer> activeTicks = null;
			EnclosedObject<Integer> delayTicks = null;
			EnclosedObject<java.awt.Color> color = UtilEnt.GetMetadata(horse, "RainbowSheep.ParticleColor");
			if ((activeTicks = UtilEnt.GetMetadata(horse, "RainbowSheep.ActiveTicks")) != null && (delayTicks = UtilEnt.GetMetadata(horse, "RainbowSheep.DelayTicks")) != null)
			{
				DisguiseBase base = ClansManager.getInstance().getDisguiseManager().getActiveDisguise(horse);
				if (base instanceof DisguiseSheep && (delayTicks.Get() % 10) == 0)
				{
					DisguiseSheep sheep = (DisguiseSheep) base;
					int mod = activeTicks.Get() % 4;
					activeTicks.Set(activeTicks.Get() + 1);
					
					if (mod == 0)
					{
						sheep.setColor(DyeColor.RED);
						color.Set(java.awt.Color.RED);
					}
					else if (mod == 1)
					{
						sheep.setColor(DyeColor.YELLOW);
						color.Set(java.awt.Color.YELLOW);
					}
					else if (mod == 2)
					{
						sheep.setColor(DyeColor.GREEN);
						color.Set(java.awt.Color.GREEN);
					}
					else if (mod == 3)
					{
						sheep.setColor(DyeColor.BLUE);
						color.Set(java.awt.Color.BLUE);
					}
					
					ClansManager.getInstance().getDisguiseManager().updateDisguise(base);
				}
				delayTicks.Set(delayTicks.Get() + 1);
			}
			ColoredParticle particle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(color.Get()), horse.getLocation().add(0, 1, 0));
			particle.display(3);
		}, MountType.HORSE),
		ROYAL_STEED(5, "Clans Royal Steed Mount Skin", C.cGold + "Royal Steed", Material.DIAMOND_BARDING, Color.WHITE, Variant.HORSE, Style.WHITE, false, horse ->
		{
			horse.getInventory().setArmor(new ItemBuilder(Material.DIAMOND_BARDING).setTitle(C.cGoldB + "Royal Armor").build());
		}, horse ->
		{
			UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.GOLD_BLOCK, 0), horse.getLocation().add(0, 1, 0),
					0.25f, 0.25f, 0.25f, 0, 3, ViewDist.NORMAL);
		}, MountType.HORSE),
		ROYAL_GUARD_STEED(6, "Clans Royal Guard Steed Mount Skin", C.cGray + "Royal Guard's Steed", Material.GOLD_BARDING, Color.BLACK, Variant.HORSE, Style.NONE, false, horse ->
		{
			horse.getInventory().setArmor(new ItemBuilder(Material.GOLD_BARDING).setTitle(C.cGoldB + "Guardian Armor").build());
		}, horse ->
		{
			UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.IRON_BLOCK, 0), horse.getLocation().add(0, 1, 0),
					0.25f, 0.25f, 0.25f, 0, 3, ViewDist.NORMAL);
		}, MountType.HORSE),
		KNIGHT_STEED(7, "Clans Knight Steed Mount Skin", C.cDRed + "Knight's Steed", Material.IRON_BARDING, Color.GRAY, Variant.HORSE, Style.NONE, false, horse ->
		{
			horse.getInventory().setArmor(new ItemBuilder(Material.IRON_BARDING).setTitle(C.cGoldB + "Knightly Armor").build());
		}, horse ->
		{
			ColoredParticle red = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(java.awt.Color.RED), horse.getLocation().add(0, 1, 0));
			red.display(3);
		}, MountType.HORSE),
		COW(8, "Clans Cow Mount Skin", C.cWhite + "Cow", Material.MILK_BUCKET, Color.WHITE, Variant.HORSE, Style.NONE, true, horse ->
		{
			DisguiseCow disguise = new DisguiseCow(horse);
			disguise.setName(horse.getCustomName());
			ClansManager.getInstance().getDisguiseManager().disguise(disguise);
		}, horse -> {}, MountType.HORSE),
		SHEEP(9, "Clans Sheep Mount Skin", C.cWhite + "Sheep", Material.WOOL, Color.WHITE, Variant.HORSE, Style.NONE, true, horse ->
		{
			DisguiseSheep disguise = new DisguiseSheep(horse);
			disguise.setName(horse.getCustomName());
			ClansManager.getInstance().getDisguiseManager().disguise(disguise);
		}, horse -> {}, MountType.HORSE),
		TRUSTY_MULE(10, "Clans Trusty Mule Mount Skin", C.cBlue + "Trusty Mule", Material.APPLE, Color.BROWN, Variant.MULE, Style.NONE, false, horse -> {}, horse -> {}, MountType.DONKEY),
		CAKE(11, "Clans Cake Mount Skin", C.cPurple + "Cake", Material.CAKE, Color.WHITE, Variant.HORSE, Style.NONE, true, horse ->
		{
			DisguiseBlock disguise = new DisguiseBlock(horse, Material.CAKE_BLOCK, 0);
			ClansManager.getInstance().getDisguiseManager().disguise(disguise);
		}, horse ->
		{
			ColoredParticle red = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(java.awt.Color.RED), horse.getLocation().add(0, 1, 0));
			red.display(2);
			ColoredParticle white = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(java.awt.Color.WHITE), horse.getLocation().add(0, 1, 0));
			white.display(3);
			
			horse.getWorld().playSound(horse.getLocation(), Sound.EAT, 1, 1);
		}, MountType.HORSE),
		MELON(12, "Clans Power Melon Mount Skin", C.cGreen + "Power Melon", Material.MELON, Color.WHITE, Variant.HORSE, Style.NONE, true, horse ->
		{
			DisguiseBlock disguise = new DisguiseBlock(horse, Material.MELON_BLOCK, 0);
			ClansManager.getInstance().getDisguiseManager().disguise(disguise);
			UtilEnt.addFlag(horse, "HelmetPacket.RiderMelon");
		}, horse ->
		{
			ColoredParticle red = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(java.awt.Color.RED), horse.getLocation().add(0, 1, 0));
			red.display(2);
			ColoredParticle green = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(java.awt.Color.GREEN), horse.getLocation().add(0, 1, 0));
			green.display(3);
		}, MountType.HORSE),
		;
		
		private final int _id;
		private final String _packageName;
		private final String _displayName;
		private final ItemStack _baseDisplayItem;
		private final Color _color;
		private final Variant _variant;
		private final Style _style;
		private final boolean _needsJumpAssist;
		private final Consumer<CraftHorse> _onSpawn, _onUpdate;
		private final MountType[] _possibleTypes;
		
		private SkinType(int id, String packageName, String displayName, Material displayType, Color color, Variant variant, Style style, boolean needsJumpAssist, Consumer<CraftHorse> onSpawn, Consumer<CraftHorse> onUpdate, MountType... possibleTypes)
		{
			this(id, packageName, displayName, new ItemStack(displayType), color, variant, style, needsJumpAssist, onSpawn, onUpdate, possibleTypes);
		}
		
		private SkinType(int id, String packageName, String displayName, ItemStack baseDisplayItem, Color color, Variant variant, Style style, boolean needsJumpAssist, Consumer<CraftHorse> onSpawn, Consumer<CraftHorse> onUpdate, MountType... possibleTypes)
		{
			_id = id;
			_packageName = packageName;
			_displayName = displayName;
			_baseDisplayItem = baseDisplayItem;
			_color = color;
			_variant = variant;
			_style = style;
			_needsJumpAssist = needsJumpAssist;
			_onSpawn = onSpawn;
			_onUpdate = onUpdate;
			_possibleTypes = possibleTypes;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getPackageName()
		{
			return _packageName;
		}
		
		public String getDisplayName()
		{
			return _displayName;
		}
		
		public ItemStack getBaseDisplay()
		{
			return _baseDisplayItem;
		}
		
		public Color getColor()
		{
			return _color;
		}
		
		public Variant getVariant()
		{
			return _variant;
		}
		
		public Style getStyle()
		{
			return _style;
		}
		
		public boolean needsJumpAssist()
		{
			return _needsJumpAssist;
		}
		
		public void onSpawn(CraftHorse horse)
		{
			_onSpawn.accept(horse);
		}
		
		public void onUpdate(CraftHorse horse)
		{
			_onUpdate.accept(horse);
		}
		
		public MountType[] getPossibleTypes()
		{
			return _possibleTypes;
		}
		
		public static SkinType getFromId(int id)
		{
			for (SkinType type : SkinType.values())
			{
				if (type.getId() == id)
				{
					return type;
				}
			}
			
			return null;
		}
	}
	
	public static enum MountType
	{
		HORSE(1, C.cWhite + "Horse", Material.IRON_BARDING, (owner, skin, stats) ->
		{
			CraftHorse horse = (CraftHorse) owner.getWorld().spawnEntity(owner.getLocation(), EntityType.HORSE);
			horse.setAdult();
			horse.setAgeLock(true);
			horse.setBreed(false);
			horse.setCustomNameVisible(true);
			horse.setCustomName(owner.getName() + "'s " + (skin == null ? "Horse" : ChatColor.stripColor(skin.getDisplayName())));
			if (skin != null)
			{
				horse.setVariant(skin.getVariant());
				horse.setColor(skin.getColor());
				horse.setStyle(skin.getStyle());
				skin.onSpawn(horse);
			}
			horse.setTamed(true);
			horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
			horse.setOwner(owner);
			horse.setJumpStrength(MountManager.getJump(stats.JumpStars));
			horse.getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(MountManager.getSpeed(stats.SpeedStars));
			//horse.setPassenger(owner);
			Mount mount = new Mount(owner, horse, skin, MountManager.getStrength(stats.StrengthStars));
			UtilServer.CallEvent(new MountSpawnEvent(mount));
		}),
		DONKEY(2, C.cWhite + "Donkey", Material.GOLD_BARDING, (owner, skin, stats) ->
		{
			CraftHorse horse = (CraftHorse) owner.getWorld().spawnEntity(owner.getLocation(), EntityType.HORSE);
			horse.setAdult();
			horse.setAgeLock(true);
			horse.setBreed(false);
			horse.setCustomNameVisible(true);
			horse.setCustomName(owner.getName() + "'s " + (skin == null ? "Donkey" : ChatColor.stripColor(skin.getDisplayName())));
			if (skin != null)
			{
				horse.setVariant(skin.getVariant());
				skin.onSpawn(horse);
			}
			else
			{
				horse.setVariant(Variant.DONKEY);
			}
			horse.setTamed(true);
			horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
			horse.setOwner(owner);
			horse.setJumpStrength(MountManager.getJump(stats.JumpStars));
			horse.getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(MountManager.getSpeed(stats.SpeedStars));
			horse.setCarryingChest(true);
			//horse.setPassenger(owner);
			Mount mount = new Mount(owner, horse, skin, MountManager.getStrength(stats.StrengthStars));
			UtilServer.CallEvent(new MountSpawnEvent(mount));
		})
		;
		
		private final int _id;
		private final String _displayName;
		private final Material _displayType;
		private final TriConsumer<Player, SkinType, MountStatToken> _spawnHandler;
		
		private MountType(int id, String displayName, Material displayType, TriConsumer<Player, SkinType, MountStatToken> spawnHandler)
		{
			_id = id;
			_displayName = displayName;
			_displayType = displayType;
			_spawnHandler = spawnHandler;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getDisplayName()
		{
			return _displayName;
		}
		
		public Material getDisplayType()
		{
			return _displayType;
		}
		
		public void spawn(Player owner, SkinType skinType, MountStatToken statToken)
		{
			_spawnHandler.accept(owner, skinType, statToken);
		}
		
		public static MountType getFromId(int id)
		{
			for (MountType type : MountType.values())
			{
				if (type.getId() == id)
				{
					return type;
				}
			}
			
			return null;
		}
	}
}