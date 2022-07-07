package nautilus.game.arcade.game.games.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang.IllegalClassException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.hologram.Hologram;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.loot.ChestLoot;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.core.explosion.CustomExplosion;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.wizards.kit.KitMage;
import nautilus.game.arcade.game.games.wizards.kit.KitMystic;
import nautilus.game.arcade.game.games.wizards.kit.KitSorcerer;
import nautilus.game.arcade.game.games.wizards.kit.KitWitchDoctor;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClickBlock;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClickEntity;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import net.minecraft.server.v1_8_R3.EntityFireball;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems;

public class Wizards extends SoloGame
{
	private ArrayList<Item> _droppedWandsBooks = new ArrayList<Item>();
	private int _endgameMessageCounter;
	private double _accuracy = 0;
	private float _endgameSize = 1.5F;
	private float _fireballSpeed = 0.05F;
	private long _lastEndgameStrike;
	private long _lastGhastMoan;
	private int _lastGamePace;
	/**
	 * @0 for meteors
	 * @1 for lightning
	 */
	private int _endGameEvent;
	private int _nextEndgameStrike = 6000;
	private ChestLoot _chestLoot = new ChestLoot();
	private NautHashMap<SpellType, Spell> _spells = new NautHashMap<SpellType, Spell>();
	private WizardSpellMenu _wizard;
	private NautHashMap<String, Wizard> _wizards = new NautHashMap<String, Wizard>();
	private IPacketHandler _wizardSpellLevelHandler;

	public Wizards(ArcadeManager manager)
	{
		super(manager, GameType.Wizards, new Kit[0], new String[]
			{

					"Find loot and spells in chests",

					"Right click wands to assign spells",

					"Left click with wands to cast magic",

					"The last wizard alive wins!"

			});

		setKits(new Kit[]
			{
					new KitMage(manager),

					new KitSorcerer(manager),

					new KitMystic(manager),

					new KitWitchDoctor(manager)
			});

		_wizard = new WizardSpellMenu(this);

		AnnounceStay = false;
		BlockBreak = true;
		BlockPlace = true;
		ItemPickup = true;
		ItemDrop = true;
		InventoryOpenBlock = true;
		InventoryOpenChest = true;
		InventoryClick = true;
		DisableKillCommand = false;
		SoupEnabled = false;
		DamageTeamSelf = true;
		AllowParticles = false;

		registerChatStats(
				Kills,
				Assists,
				BlankLine,
				DamageTaken,
				DamageDealt,
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);

		Manager.getCosmeticManager().setHideParticles(true);
		// Manager.GetDamage().GetCombatManager().setUseWeaponName(AttackReason.DefaultWeaponName);

		createLoot();

		_wizardSpellLevelHandler = new IPacketHandler()
		{
			@Override
			public void handle(PacketInfo packetInfo)
			{
				if (packetInfo.getPacket() instanceof PacketPlayOutWindowItems
						|| packetInfo.getPacket() instanceof PacketPlayOutSetSlot)
				{
					Inventory inv = packetInfo.getPlayer().getOpenInventory().getTopInventory();

					if (inv.getType() == InventoryType.CHEST)
					{
						if (packetInfo.getPacket() instanceof PacketPlayOutWindowItems)
						{
							Wizard wizard = getWizard(packetInfo.getPlayer());

							if (wizard != null)
							{
								PacketPlayOutWindowItems packet = (PacketPlayOutWindowItems) packetInfo.getPacket();
								boolean ownPacket = false;

								ItemStack[] items = new ItemStack[packet.b.length];

								for (int i = 0; i < items.length; i++)
								{
									items[i] = CraftItemStack.asBukkitCopy(packet.b[i]);

									ItemStack item = items[i];

									if (item != null && item.getType() != Material.AIR)
									{
										SpellType spellType = getSpell(item);

										if (spellType != null)
										{
											if (wizard.getSpellLevel(spellType) < spellType.getMaxLevel())
											{
												item.setAmount(wizard.getSpellLevel(spellType) + 1);
											}
											else
											{
												item.setAmount(0);
											}

											ownPacket = true;
										}
									}
								}

								if (ownPacket)
								{
									List list = new ArrayList();

									for (ItemStack item : items)
									{
										list.add(CraftItemStack.asNMSCopy(item));
									}

									packetInfo.setCancelled(true);

									packet = new PacketPlayOutWindowItems(packet.a, list);

									packetInfo.getVerifier().bypassProcess(packet);
								}
							}
						}
						else
						{
							PacketPlayOutSetSlot packet = (PacketPlayOutSetSlot) packetInfo.getPacket();

							ItemStack item = null;

							try
							{
								item = CraftItemStack.asBukkitCopy(packet.c);
							}
							catch (Exception ex)
							{
								ex.printStackTrace();
							}

							if (item != null && item.getType() != Material.AIR)
							{
								SpellType spellType = getSpell(item);

								if (spellType != null)
								{
									Wizard wizard = getWizard(packetInfo.getPlayer());

									if (wizard != null)
									{
										if (wizard.getSpellLevel(spellType) < spellType.getMaxLevel())
										{
											item.setAmount(wizard.getSpellLevel(spellType) + 1);
										}
										else
										{
											item.setAmount(0);
										}

										packetInfo.setCancelled(true);

										packet = new PacketPlayOutSetSlot(packet.a, packet.b, CraftItemStack.asNMSCopy(item));

										packetInfo.getVerifier().bypassProcess(packet);
									}
								}
							}
						}
					}
				}
			}
		};


		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	@EventHandler
	public void onFireballDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Fireball)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onWandHit(CustomDamageEvent event)
	{
		// Damager is ENTITY
		if (!event.isCancelled() && event.GetDamagerEntity(true) != null)
		{
			if (event.GetReason() == null)
			{
				if (event.GetDamagerPlayer(false) != null)
				{
					Player damager = event.GetDamagerPlayer(false);

					if (damager.getInventory().getHeldItemSlot() < 5)
					{
						Wizard wizard = getWizard(damager);

						if (wizard != null)
						{
							String reason = damager.getInventory().getHeldItemSlot() < wizard.getWandsOwned() ? "Wand" : "Fist";

							event.AddMod(reason, reason, 0, true);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (!event.IgnoreArmor())
		{
			double percentProtected = 0;

			if (event.GetDamageePlayer() == null)
			{
				return;
			}

			for (ItemStack item : event.GetDamageePlayer().getInventory().getArmorContents())
			{
				if (item != null)
				{
					double percent = 0;
					String name = item.getType().name();

					if (name.contains("LEATHER"))
					{
						percent = 10;
					}
					else if (name.contains("GOLD") || name.contains("CHAINMAIL"))
					{
						percent = 15;
					}
					else if (name.contains("IRON"))
					{
						percent = 19;
					}
					else if (name.contains("DIAMOND"))
					{
						percent = 25;
					}

					if (name.contains("BOOTS"))
					{
						percent /= 3;
					}
					else if (name.contains("LEGGINGS"))
					{
						percent /= 1.5;
					}
					else if (name.contains("CHESTPLATE"))
					{
					}
					else if (name.contains("HELMET"))
					{
						percent /= 2;
					}

					percentProtected += (percent / 100);
				}
			}

			if (percentProtected > 0)
			{
				event.SetIgnoreArmor(true);
				event.AddMult("Armor Rebalancing", "Armor Rebalancing", 1 - percentProtected, false);
			}
		}
	}

	public String buildTime()
	{
		String s = "";

		for (char c : ("" + System.nanoTime()).toCharArray())
		{
			s += "§" + c;
		}

		return s;
	}

	@EventHandler
	public void spellCooldown(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (!IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			int heldSlot = player.getInventory().getHeldItemSlot();
			Wizard wizard = getWizard(player);

			for (int i = 0; i < 5; i++)
			{
				if (i == heldSlot)
					continue;

				ItemStack item = player.getInventory().getItem(i);

				if (item != null)
				{
					SpellType spell = wizard.getSpell(i);

					if (spell != null)
					{
						int timeLeft = (int) Math.ceil(getUsableTime(wizard, spell).getKey());

						timeLeft = Math.max(0, Math.min(63, timeLeft)) + 1;

						if (timeLeft != item.getAmount())
						{
							item.setAmount(timeLeft);

							player.getInventory().setItem(i, item);
						}
					}
				}
			}
		}
	}

	public void castSpell(Player player, Wizard wizard, SpellType spell, Object interacted)
	{
		int spellLevel = wizard.getSpellLevel(spell);

		if (spellLevel > 0)
		{

			if (wizard.getCooldown(spell) == 0)
			{

				if (wizard.getMana() >= spell.getManaCost(wizard))
				{

					Spell sp = _spells.get(spell);

					if (interacted instanceof Block && sp instanceof SpellClickBlock)
					{
						((SpellClickBlock) sp).castSpell(player, (Block) interacted);
					}

					if (wizard.getCooldown(spell) == 0 && interacted instanceof Entity && sp instanceof SpellClickEntity)
					{
						((SpellClickEntity) sp).castSpell(player, (Entity) interacted);
					}

					if (wizard.getCooldown(spell) == 0 && sp instanceof SpellClick)
					{
						((SpellClick) sp).castSpell(player);
					}
				}
				else
				{
					player.playSound(player.getLocation(), Sound.FIZZ, 300, 1);
					player.sendMessage(ChatColor.BLUE + "The spell sputters and dies.");
					player.sendMessage(ChatColor.BLUE + "You do not have enough mana!");
				}
			}
			else
			{

				player.playSound(player.getLocation(), Sound.FIZZ, 300, 1);
				player.sendMessage(ChatColor.BLUE + "The spell hasn't recharged yet!");

			}

		}
	}

	public void changeWandsType(Player player, int oldSlot, int newSlot)
	{
		PlayerInventory inv = player.getInventory();
		Wizard wizard = getWizard(player);

		if (oldSlot >= 0 && oldSlot < 5)
		{
			SpellType spell = wizard.getSpell(oldSlot);

			if (spell != null)
			{
				int timeLeft = (int) Math.ceil(getUsableTime(wizard, spell).getKey());

				timeLeft = Math.max(0, Math.min(63, timeLeft)) + 1;

				ItemStack item = inv.getItem(oldSlot);

				item.setType(spell.getSpellItem().getType());

				item.setDurability(spell.getSpellItem().getDurability());

				item.setAmount(timeLeft);

				inv.setItem(oldSlot, item);
			}
		}

		if (newSlot >= 0 && newSlot < 5)
		{
			SpellType spell = wizard.getSpell(newSlot);

			if (spell != null)
			{
				ItemStack item = inv.getItem(newSlot);

				item.setDurability((short) 0);

				item.setType(spell.getWandType().getMaterial());

				item.setAmount(1);

				inv.setItem(newSlot, item);
			}
		}
	}

	public void changeWandsTitles(Player player)
	{
		PlayerInventory inv = player.getInventory();
		Wizard wizard = getWizard(player);

		for (int slot = 0; slot < 5; slot++)
		{
			if (slot < wizard.getWandsOwned())
			{
				ItemStack item = inv.getItem(slot);
				SpellType type = wizard.getSpell(slot);
				String display;

				if (type != null)
				{
					display = C.cYellow + "Mana " + ChatColor.RESET + type.getManaCost(wizard)

					+ "      " +

					C.cYellow + "Cooldown " + ChatColor.RESET

					+ UtilTime.convertString((long) (type.getSpellCooldown(wizard) * 1000), 1, TimeUnit.FIT);
				}
				else
				{
					display = C.cWhite + "Right click to set a spell";
				}

				ItemMeta meta = item.getItemMeta();

				if (!meta.hasDisplayName() || !meta.getDisplayName().equals(display))
				{
					meta.setDisplayName(display);
					item.setItemMeta(meta);
				}
			}
		}
	}

	@EventHandler
	public void checkPickupBooks(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK && IsLive())
		{
			Iterator<Item> itel = _droppedWandsBooks.iterator();

			while (itel.hasNext())
			{
				Item item = itel.next();

				if (item.isValid())
				{
					Player player = UtilPlayer.getClosest(item.getLocation(), (Entity) null);

					if (player != null && player.getLocation().distance(item.getLocation()) < 1.7)
					{
						onPickup(new PlayerPickupItemEvent(player, item, 0));
					}
				}

				if (!item.isValid())
				{
					itel.remove();
				}
			}
		}
	}

	private void CreateChestCraftEnchant()
	{
		ArrayList<Location> chests = WorldData.GetCustomLocs("54");

		System.out.println("Map Chest Locations: " + chests.size());

		int spawn = 0;
		Location spawnPoint = UtilWorld.averageLocation(GetTeamList().get(0).GetSpawns());

		// Chests
		System.out.println("Chests: " + Math.min(250, chests.size()));
		for (int i = 0; i < 250 && !chests.isEmpty(); i++)
		{
			Location loc = chests.remove(UtilMath.r(chests.size()));

			fillWithLoot(loc.getBlock());
			if (UtilMath.offset2d(loc, spawnPoint) < 8)
				spawn++;
		}

		for (Location loc : chests)
		{
			if (spawn < 10 && UtilMath.offset(loc, spawnPoint) < 8)
			{
				spawn++;
				fillWithLoot(loc.getBlock());
				continue;
			}

			loc.getBlock().setType(Material.AIR);
		}
	}

	@EventHandler
	public void handleEntityPacket(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			getArcadeManager().getPacketHandler().addPacketHandler(
					_wizardSpellLevelHandler, PacketPlayOutSetSlot.class,
					PacketPlayOutWindowItems.class);
		}
		else if (event.GetState() == GameState.Dead)
		{
			getArcadeManager().getPacketHandler().removePacketHandler(_wizardSpellLevelHandler);
		}
	}

	private void createLoot()
	{
		for (SpellType spellType : SpellType.values())
		{
			_chestLoot.addLoot(spellType.getSpellBook(this), spellType.getItemAmount());
		}

		_chestLoot.addLoot(
				new ItemBuilder(makeBlankWand()).setTitle(C.cWhite + "Spell Wand" + buildTime())
						.addEnchantment(UtilInv.getDullEnchantment(), 1).build(), 4);

		_chestLoot.addLoot(Material.CARROT_ITEM, 15, 1, 2);
		_chestLoot.addLoot(Material.COOKED_BEEF, 15, 1, 2);
		_chestLoot.addLoot(Material.BREAD, 15, 1, 2);
		_chestLoot.addLoot(new ItemBuilder(Material.COOKED_CHICKEN).setTitle(C.cWhite + "Cheese").build(), 15, 1, 2);

		_chestLoot.addLoot(Material.WHEAT, 5, 1, 2);
		_chestLoot.addLoot(Material.WOOD, 5, 1, 10);

		_chestLoot.addLoot(Material.GOLD_INGOT, 5, 1, 2);
		_chestLoot.addLoot(Material.IRON_INGOT, 5, 1, 2);
		_chestLoot.addLoot(Material.DIAMOND, 3, 1, 1);

		_chestLoot.addLoot(Material.LEATHER_BOOTS, 6, 1, 1);
		_chestLoot.addLoot(Material.LEATHER_LEGGINGS, 6, 1, 1);
		_chestLoot.addLoot(Material.LEATHER_CHESTPLATE, 6, 1, 1);
		_chestLoot.addLoot(Material.LEATHER_HELMET, 6, 1, 1);

		_chestLoot.addLoot(Material.GOLD_BOOTS, 5, 1, 1);
		_chestLoot.addLoot(Material.GOLD_CHESTPLATE, 5, 1, 1);
		_chestLoot.addLoot(Material.GOLD_HELMET, 5, 1, 1);
		_chestLoot.addLoot(Material.GOLD_LEGGINGS, 5, 1, 1);

		_chestLoot.addLoot(Material.IRON_BOOTS, 2, 1, 1);
		_chestLoot.addLoot(Material.IRON_CHESTPLATE, 2, 1, 1);
		_chestLoot.addLoot(Material.IRON_HELMET, 2, 1, 1);
		_chestLoot.addLoot(Material.IRON_LEGGINGS, 2, 1, 1);
	}

	@EventHandler
	public void CreateRandomChests(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		HashSet<Material> ignore = new HashSet<Material>();

		ignore.add(Material.LEAVES);

		int xDiff = WorldData.MaxX - WorldData.MinX;
		int zDiff = WorldData.MaxZ - WorldData.MinZ;

		int done = 0;

		while (done < 40)
		{

			Block block = UtilBlock.getHighest(WorldData.World, WorldData.MinX + UtilMath.r(xDiff),
					WorldData.MinZ + UtilMath.r(zDiff), ignore);

			if (!UtilBlock.airFoliage(block) || !UtilBlock.solid(block.getRelative(BlockFace.DOWN)))
				continue;

			block.setTypeIdAndData(54, (byte) UtilMath.r(4), true);
			fillWithLoot(block);
			done++;
		}
	}
	
	private void displayProgress(String progressColor, String prefix, double amount, String suffix, Player... players)
	{
		// Generate Bar
		int bars = 24;
		String progressBar = C.cGreen + "";
		boolean colorChange = false;
		for (int i = 0; i < bars; i++)
		{
			if (!colorChange && (float) i / (float) bars >= amount)
			{
				progressBar += progressColor;// C.cRed;
				colorChange = true;
			}

			progressBar += "▌";
		}

		UtilTextBottom.display((prefix == null ? "" : prefix + ChatColor.RESET + " ") + progressBar
				+ (suffix == null ? "" : ChatColor.RESET + " " + suffix), players);

	}

	public void drawUtilTextBottom(Player player)
	{
		int heldSlot = player.getInventory().getHeldItemSlot();

		if (heldSlot >= 0 && heldSlot < 5)
		{
			Wizard wizard = getWizard(player);

			if (wizard != null)
			{

				SpellType type = wizard.getSpell(heldSlot);

				if (type != null)
				{
					Entry<Double, Boolean> entry = getUsableTime(wizard, type);

					double usableTime = entry.getKey();// Time in seconds till usable

					if (usableTime > 0)
					{
						usableTime = UtilMath.trim(1, usableTime);

						double maxSeconds = Math.max(type.getSpellCooldown(wizard),
								type.getManaCost(wizard) / (wizard.getManaPerTick() * 20));

						displayProgress(C.cRed, C.cRed + type.getSpellName(), 1f - (usableTime / maxSeconds),

						(entry.getValue() ?

						UtilTime.convertString((long) (usableTime * 1000), 1, TimeUnit.FIT)

						:

						usableTime + (usableTime < 60 ? "s" : "m") + " for mana"),

						player);

					}
					else
					{
						UtilTextBottom.display(C.cGreen + C.Bold + type.getSpellName(), player);
					}
				}
				else
				{
					UtilTextBottom.display("Spell Wand", player);
				}
			}
		}
	}

	private Entry<Double, Boolean> getUsableTime(Wizard wizard, SpellType type)
	{
		double usableTime = 0;

		if (wizard.getMana() < type.getManaCost(wizard))
		{
			usableTime = (type.getManaCost(wizard) - wizard.getMana()) / (20 * wizard.getManaPerTick());
		}

		double cooldown = wizard.getCooldown(type) != 0 ? (double) (wizard.getCooldown(type) - System.currentTimeMillis()) / 1000D
				: 0;
		boolean displayCooldown = false;

		if (cooldown > 0 && cooldown > usableTime)
		{
			usableTime = cooldown;
			displayCooldown = true;
		}

		return new HashMap.SimpleEntry(usableTime, displayCooldown);
	}

	private ArrayList<ItemStack> getItems(Player player)
	{
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		PlayerInventory inv = player.getInventory();

		for (int i = 5; i < inv.getSize(); i++)
		{
			ItemStack item = inv.getItem(i);

			if (item != null && item.getType() != Material.AIR)
			{
				items.add(item.clone());
			}
		}

		for (ItemStack item : inv.getArmorContents())
		{
			if (item != null && item.getType() != Material.AIR)
			{
				items.add(item.clone());
			}
		}

		ItemStack cursorItem = player.getItemOnCursor();

		if (cursorItem != null && cursorItem.getType() != Material.AIR)
			items.add(cursorItem.clone());

		return items;
	}
	
	private void dropSpells(Player player)
	{
		HashSet<SpellType> spells = new HashSet<SpellType>();
		ArrayList<ItemStack> itemsToDrop = new ArrayList<ItemStack>();

		Wizard wizard = getWizard(player);

		for (int i = 0; i < 5; i++)
		{
			SpellType type = wizard.getSpell(i);

			if (type != null && type != SpellType.ManaBolt)
			{
				spells.add(type);
			}
		}

		for (SpellType spell : wizard.getKnownSpells())
		{
			if (spell != SpellType.ManaBolt && UtilMath.random.nextInt(5) == 0)
			{
				spells.add(spell);
			}
		}

		for (ItemStack item : getItems(player))
		{
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}

		for (SpellType type : spells)
		{
			ItemStack item = type.getSpellBook(this);

			UtilInv.addDullEnchantment(item);

			itemsToDrop.add(item);
		}

		if (wizard.getWandsOwned() > 3 || UtilMath.random.nextBoolean())
		{
			itemsToDrop.add(makeBlankWand());
		}

		itemsToDrop.add(new ItemBuilder(Material.NETHER_STAR).setTitle(buildTime()).build());

		Collections.shuffle(itemsToDrop, new Random());

		double beginnerAngle = Math.random() * 360;

		for (ItemStack itemstack : itemsToDrop)
		{
			Item item = player.getWorld().dropItem(player.getLocation(), itemstack);
			item.setPickupDelay(60);

			beginnerAngle += 360D / itemsToDrop.size();
			double angle = (((2 * Math.PI) / 360) * beginnerAngle) % 360;
			double x = 0.2 * Math.cos(angle);
			double z = 0.2 * Math.sin(angle);

			item.setVelocity(new Vector(x, 0.3, z));
		}
	}

	private void fillWithLoot(Block block)
	{
		BlockState state = block.getState();

		if (state instanceof InventoryHolder)
		{
			InventoryHolder holder = (InventoryHolder) state;
			Inventory inv = holder.getInventory();
			boolean containsSpell = false;

			for (int i = 0; i < 5 || !containsSpell; i++)
			{

				ItemStack item = _chestLoot.getLoot();

				SpellType spellType = getSpell(item);

				// Every chest has a spell.
				if (i > 5 && spellType == null)
				{
					continue;
				}

				if (spellType != null)
				{
					containsSpell = true;
					UtilInv.addDullEnchantment(item);
				}
				
				if (UtilItem.isArmor(item))
					UtilItem.makeUnbreakable(item);
				
				inv.setItem(UtilMath.r(inv.getSize()), item);
			}

			state.update();
		}
	}

	private SpellType getSpell(ItemStack item)
	{
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
		{
			String title = item.getItemMeta().getDisplayName();

			if (title.contains(" "))
			{
				title = ChatColor.stripColor(title.substring(title.split(" ")[0].length() + 1));

				for (SpellType spell : SpellType.values())
				{
					if (spell.getSpellName().equals(title))
						return spell;
				}
			}
		}

		return null;
	}

	public Wizard getWizard(org.bukkit.entity.Player player)
	{
		return _wizards.get(player.getName());
	}

	@EventHandler
	public void increaseGamePace(UpdateEvent event)
	{

		if (event.getType() != UpdateType.SEC)
			return;

		if (!IsLive())
			return;

		int timesShouldIncrease = (int) Math.floor((System.currentTimeMillis() - getGameLiveTime()) / 60000D);

		if (timesShouldIncrease < 10 && timesShouldIncrease != _lastGamePace
				&& (timesShouldIncrease % 2 == 0 || timesShouldIncrease == 9))
		{
			_lastGamePace = timesShouldIncrease;

			Announce(C.cYellow + C.Bold + "Power surges through the battlefield!");
			Announce(C.cYellow + C.Bold + "Mana cost and spell cooldown has been lowered!", false);

			for (Player player : GetPlayers(true))
			{
				Wizard wizard = getWizard(player);

				wizard.decreaseCooldown();

				changeWandsTitles(player);
			}
		}
	}

	public ItemStack makeBlankWand()
	{
		ItemBuilder builder = new ItemBuilder(Material.BLAZE_ROD);

		builder.setTitle(C.cWhite + "Right click to set a spell" + buildTime());

		builder.addLore(C.cGreen + C.Bold + "Left-Click" + C.cWhite + " Bind to Wand");

		builder.addLore(C.cGreen + C.Bold + "Right-Click" + C.cWhite + " Quickcast Spell");

		return builder.build();
	}

	private void onCastSpell(Player player, Object obj)
	{
		ItemStack item = player.getItemInHand();

		if (IsLive() && IsAlive(player) && item != null && player.getInventory().getHeldItemSlot() < 5)
		{
			Wizard wizard = getWizard(player);

			SpellType spell = wizard.getSpell(player.getInventory().getHeldItemSlot());

			if (spell != null)
			{
				castSpell(player, wizard, spell, obj);
			}
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null)
		{
			return;
		}
		InventoryHolder holder = event.getClickedInventory().getHolder();
		// DoubleChests aren't a BlockState as they represent more than one block
		if (!(holder instanceof BlockState || holder instanceof DoubleChest))
		{
			return;
		}

		ItemStack item = event.getCurrentItem();

		if (item != null)
		{
			Player p = (Player) event.getWhoClicked();

			if (event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest)
			{

				SpellType spell = getSpell(item);

				if (spell != null)
				{
					onSpellLearn(p, spell);

					event.setCancelled(true);
					event.setCurrentItem(new ItemStack(Material.AIR));

					p.playSound(p.getLocation(), Sound.NOTE_STICKS, 0.7F, 0);
				}

			}

			if (item.getType() == Material.BLAZE_ROD
					&& (event.getClickedInventory().getType() != InventoryType.PLAYER || event.getSlot() > 4))
			{

				if (onGainWand(p))
				{
					event.setCancelled(true);
					event.setCurrentItem(new ItemStack(Material.AIR));

					p.playSound(p.getLocation(), Sound.NOTE_STICKS, 0.7F, 0);
				}
			}

		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (IsAlive(event.getDamager()))
		{
			onCastSpell((Player) event.getDamager(), event.getEntity());

			if (event.getDamage() > 0.5)
			{
				event.setDamage(0.5);

				if (event.getEntity() instanceof Player)
				{
					UtilParticle.PlayParticle(ParticleType.HEART,
							((LivingEntity) event.getEntity()).getEyeLocation(), 0, 0, 0, 0, 1,
							ViewDist.MAX, (Player) event.getDamager());
				}
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		Player p = event.getEntity();

		if (IsLive() && IsAlive(p))
		{
			dropSpells(p);
		}

		_wizards.remove(p.getName());
	}

	@EventHandler
	public void onDropItem(ItemSpawnEvent event)
	{
		ItemStack item = event.getEntity().getItemStack();

		SpellType spell = getSpell(item);

		if (spell != null)
		{
			Hologram holo = new Hologram(getArcadeManager().getHologramManager(),

			event.getEntity().getLocation().add(0, 1, 0),

			C.cDPurple + C.Bold + "Spell",

			spell.getElement().getColor() + spell.getSpellName());

			holo.setFollowEntity(event.getEntity());

			holo.setRemoveOnEntityDeath();

			holo.setViewDistance(16);

			holo.start();

			_droppedWandsBooks.add(event.getEntity());

		}
		else if (item.getType() == Material.BLAZE_ROD)
		{
			item.removeEnchantment(UtilInv.getDullEnchantment());

			Hologram holo = new Hologram(getArcadeManager().getHologramManager(),

			event.getEntity().getLocation().add(0, 1, 0),

			C.Bold + "Spell Wand");

			holo.setFollowEntity(event.getEntity());

			holo.setRemoveOnEntityDeath();

			holo.setViewDistance(16);

			holo.start();

			_droppedWandsBooks.add(event.getEntity());
		}
		else if (item.getType() == Material.NETHER_STAR)
		{
			Hologram holo = new Hologram(getArcadeManager().getHologramManager(),

			event.getEntity().getLocation().add(0, 1, 0),

			C.Bold + "Wizard Soul");

			holo.setFollowEntity(event.getEntity());

			holo.setRemoveOnEntityDeath();

			holo.start();

			_droppedWandsBooks.add(event.getEntity());
		}
		else if (item.getType() == Material.BOOK || item.getType() == Material.STICK)
		{
			event.getEntity().remove();
		}
	}

	@EventHandler
	public void onDropItem(PlayerDropItemEvent event)
	{
		// Check to see if they item they're dropping is from one of the first five slots(wand slots)
		for (int i = 0; i < 5; i++)
		{
			if (event.getPlayer().getInventory().getItem(i) == null)
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEndOrPrepare(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			for (Player player : GetPlayers(true))
			{
				Kit kit = GetKit(player);

				if (kit instanceof KitMage)
				{
					Wizard wizard = getWizard(player);

					for (int a = 0; a < 2; a++)
					{
						for (int i = 0; i < 100; i++)
						{
							SpellType spell = SpellType.values()[UtilMath.r(SpellType.values().length)];

							if (wizard.getSpellLevel(spell) == 0 && UtilMath.r(10) < spell.getItemAmount())
							{
								onSpellLearn(player, spell);
								break;
							}
						}
					}
				}
			}
			for (SpellType spells : SpellType.values())
			{
				try
				{
					Spell spell = spells.getSpellClass().newInstance();

					if (!(spell instanceof SpellClick || spell instanceof SpellClickBlock || spell instanceof SpellClickEntity))
						throw new IllegalClassException(spells.getSpellName() + "'s spell class doesn't extend a spell interface");

					spell.setSpellType(spells);
					spell.Wizards = this;

					_spells.put(spells, spell);

					Bukkit.getPluginManager().registerEvents(spell, getArcadeManager().getPlugin());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (event.GetState() == GameState.Dead || event.GetState() == GameState.End)
		{
			for (Spell spell : _spells.values())
			{
				HandlerList.unregisterAll(spell);
			}
			_spells.clear();
		}
	}

	private void onGainSoulStar(Player p)
	{
		Wizard wizard = getWizard(p);
		p.sendMessage(C.cGold + "Wizards" + C.cDGray + "> " + C.cGray + "Wizard Soul absorbed, mana regeneration increased");
		wizard.addSoulStar();
		drawUtilTextBottom(p);
	}

	private boolean onGainWand(Player p)
	{
		Wizard wizard = getWizard(p);

		int slot = wizard.getWandsOwned();

		if (slot >= 0 && slot < 5)
		{
			wizard.setWandsOwned(wizard.getWandsOwned() + 1);

			p.getInventory().setItem(slot, makeBlankWand());

			p.updateInventory();

			p.sendMessage(C.cGold + "Wizards" + C.cDGray + "> " + C.cGray + "Extra wand gained");
		}
		else
		{
			wizard.addMana(100);
			p.sendMessage(C.cGold + "Wizards" + C.cDGray + "> " + C.cGray + "Wand converted into mana");
		}

		return true;
	}

	@EventHandler
	public void onGameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End || event.GetState() == GameState.Dead)
		{
			HandlerList.unregisterAll(_wizard);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.getAction().name().contains("LEFT"))
		{
			onCastSpell(event.getPlayer(), event.getClickedBlock());
		}
	}

	@EventHandler
	public void onItemClick(InventoryClickEvent event)
	{
		int slot = event.getClick().isKeyboardClick() && event.getSlot() >= 5 ? event.getHotbarButton() : event.getSlot();

		if (slot >= 0 && slot < 5
				&& (event.getClickedInventory().getType() == InventoryType.PLAYER || event.getClick() == ClickType.NUMBER_KEY))
		{
			event.setCancelled(true);
			((Player) event.getWhoClicked()).updateInventory();
		}
	}

	public void setupWizard(Player player)
	{
		Kit kit = GetKit(player);
		Wizard wizard = new Wizard(kit instanceof KitWitchDoctor ? 150 : 100);

		_wizards.put(player.getName(), wizard);

		if (kit instanceof KitMystic)
		{
			wizard.setManaPerTick(wizard.getManaPerTick() * 1.1F);
		}

		wizard.setWandsOwned(kit instanceof KitSorcerer ? 3 : 2);

		for (int i = 0; i < 5; i++)
		{
			if (i < wizard.getWandsOwned())
			{
				player.getInventory().addItem(((Wizards) Manager.GetGame()).makeBlankWand());
			}
			else
			{
				player.getInventory().addItem(

				new ItemBuilder(Material.INK_SACK, 1, (short) 5)

				.setTitle(C.cGray + "Empty wand slot" + ((Wizards) Manager.GetGame()).buildTime())

				.addLore(C.cGray + C.Italics + "Wands can be found in chests and dead players")

				.build());
			}
		}

		changeWandsTitles(player);
	}

	@EventHandler
	public void onMeteorHit(ProjectileHitEvent event)
	{
		Projectile projectile = event.getEntity();

		if (projectile.hasMetadata("Meteor"))
		{
			projectile.remove();

			CustomExplosion explosion = new CustomExplosion(getArcadeManager().GetDamage(), getArcadeManager().GetExplosion(),
					projectile.getLocation(), _endgameSize, "Meteor");

			explosion.setBlockExplosionSize(_endgameSize * 1.4F);

			explosion.setFallingBlockExplosionAmount(20);

			explosion.setFallingBlockExplosion(true);

			explosion.setDropItems(false);

			explosion.setBlocksDamagedEqually(true);

			explosion.explode();
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event)
	{
		if (!IsLive())
		{
			event.setCancelled(true);
			return;
		}

		ItemStack item = event.getItem().getItemStack();
		Player p = event.getPlayer();

		if (IsAlive(p))
		{

			if (item.getType() == Material.BLAZE_ROD)
			{
				if (onGainWand(p))
				{
					event.setCancelled(true);
					event.getItem().remove();
				}

			}
			else if (getSpell(item) != null)
			{

				SpellType spell = getSpell(item);

				onSpellLearn(p, spell);

				event.setCancelled(true);
				event.getItem().remove();

			}
			else if (item.getType() == Material.NETHER_STAR)
			{
				onGainSoulStar(p);

				event.setCancelled(true);
				event.getItem().remove();
			}
			else
			{
				return;
			}

			p.playSound(p.getLocation(), Sound.NOTE_STICKS, 0.7F, 0);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player p = event.getPlayer();

		if (IsLive() && _wizards.containsKey(p.getName()))
		{
			dropSpells(p);
		}

		_wizards.remove(p.getName());
	}

	private void onSpellLearn(Player p, SpellType spell)
	{
		Wizard wiz = getWizard(p);

		int spellLevel = wiz.getSpellLevel(spell);

		if (spellLevel < spell.getMaxLevel())
		{
			wiz.learnSpell(spell);

			p.sendMessage(spell.getElement().getColor() + spell.getSpellName() + C.cDGray + "> " + C.cGray + "Leveled up to "
					+ getWizard(p).getSpellLevel(spell));
		}
		else
		{
			wiz.addMana(50);
			p.sendMessage(C.cGold + "Wizards" + C.cDGray + "> " + C.cGray + "Spellbook converted into mana");
		}
	}

	@EventHandler
	public void onSwapItem(PlayerItemHeldEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		Player p = event.getPlayer();

		if (!_wizards.containsKey(p.getName()))
		{
			return;
		}

		changeWandsType(p, event.getPreviousSlot(), event.getNewSlot());

		if (event.getNewSlot() >= 0 && event.getNewSlot() < 5)
		{
			drawUtilTextBottom(p);
		}
		else
		{
			// Get rid of the old wand message
			if (event.getPreviousSlot() >= 0 && event.getPreviousSlot() < 5)
			{
				UtilTextBottom.display(C.Bold, p);
			}
		}
	}

	@EventHandler
	public void onUnplaceablePlace(BlockPlaceEvent event)
	{
		if (event.getPlayer().getInventory().getHeldItemSlot() < 5)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onWeaponCraft(PrepareItemCraftEvent event)
	{
		Recipe recipe = event.getRecipe();

		ItemStack result = recipe != null ? recipe.getResult() : null;

		if (result != null)
		{
			Material mat = result.getType();

			if (mat.name().contains("_SWORD") || mat.name().contains("_AXE"))
			{
				event.getInventory().setResult(new ItemStack(Material.AIR));

				UtilPlayer.message(event.getViewers().get(0), C.cRed + "You may not craft weapons");
			}
			else if (mat == Material.STICK || mat == Material.BUCKET || mat.name().contains("_HOE"))
			{
				event.getInventory().setResult(new ItemStack(Material.AIR));

				UtilPlayer.message(event.getViewers().get(0), C.cRed + "You may not craft this item");
			}
			else
			{
				for (SpellType spell : SpellType.values())
				{
					if (mat == spell.getSpellItem().getType())
					{
						event.getInventory().setResult(new ItemStack(Material.AIR));

						UtilPlayer.message(event.getViewers().get(0), C.cRed + "You may not craft this item");
						break;
					}
				}
			}
		}
	}


	@Override
	public void ParseData()
	{
		CreateChestCraftEnchant();
		_endGameEvent = UtilMath.r(2);

		System.out.print("Endgame event: " + (_endGameEvent == 0 ? "Meteors" : "Lightning"));
	}

	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		GetScoreboard().reset();

		GetScoreboard().writeNewLine();

		GetScoreboard().write(C.cYellow + C.Bold + "Wizards");
		GetScoreboard().write(C.cWhite + GetPlayers(true).size());

		GetScoreboard().writeNewLine();

		double time = UtilTime.convert((

		(getGameLiveTime() == 0 ? System.currentTimeMillis() : getGameLiveTime())

		+ (10 * 60 * 1000)) - System.currentTimeMillis(), 1, TimeUnit.MINUTES);

		GetScoreboard().write((time >= 0 ? C.cYellow : C.cRed) + C.Bold + (time >= 0 ? "Time Left" : "Overtime"));
		GetScoreboard().write(C.cWhite + Math.abs(time) + " Minute" + (Math.abs(time) != 1 ? "s" : ""));

		GetScoreboard().draw();
	}

	@EventHandler
	public void endGameEvent(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (System.currentTimeMillis() <= getGameLiveTime() + (10 * 60 * 1000))
		{
			return;
		}

		if (_endgameMessageCounter <= 6)
		{
			if (event.getType() != UpdateType.SEC)
			{
				return;
			}

			if (_endGameEvent == 0)
			{
				if (_endgameMessageCounter == 0)
				{
					Announce(C.cYellow + C.Bold + "Broken is the cage, the skies scream with rage!");
				}
				else if (_endgameMessageCounter == 2)
				{
					Announce(C.cYellow + C.Bold + "The ground trembles with fear, your doom is here!");
				}
				else if (_endgameMessageCounter == 4)
				{
					Announce(C.cYellow + C.Bold + "Where the wizards stand, meteors strike the land!");
				}
				else if (_endgameMessageCounter == 6)
				{
					Announce(C.cYellow + C.Bold + "Fight to the death! Fight with your dying breath!");
				}
			}
			else if (_endGameEvent == 1)
			{
				if (_endgameMessageCounter == 0)
				{
					Announce(C.cYellow + C.Bold + "Storm rumbles through the sky, birds fly high!");
				}
				else if (_endgameMessageCounter == 2)
				{
					Announce(C.cYellow + C.Bold + "Lightning strikes the earth, terror given birth!");
				}
				else if (_endgameMessageCounter == 4)
				{
					Announce(C.cYellow + C.Bold + "Lightning flickering through the air, doom is here!");
				}
				else if (_endgameMessageCounter == 6)
				{
					Announce(C.cYellow + C.Bold + "Fight to the death! Fight with your dying breath!");
				}
			}

			if (_endgameMessageCounter == 6)
			{
				WorldTimeSet = 0;
				WorldData.World.setTime(15000);
			}

			_endgameMessageCounter++;
		}
		else
		{
			if (event.getType() != UpdateType.TICK)
			{
				return;
			}

			if (_nextEndgameStrike > 750)
			{
				_nextEndgameStrike -= 2;
			}

			if (UtilTime.elapsed(_lastEndgameStrike, _nextEndgameStrike))
			{
				_lastEndgameStrike = System.currentTimeMillis();

				if (_endGameEvent == 0)
				{
					makeMeteor();
				}
				else if (_endGameEvent == 1)
				{
					makeLightning();
				}
			}

			if (_lastGhastMoan < System.currentTimeMillis())
			{
				Sound sound = null;

				switch (UtilMath.r(3))
				{
				case 0:
					sound = Sound.GHAST_MOAN;
					break;
				case 1:
					sound = Sound.CAT_HIT;
					break;
				case 2:
					sound = Sound.GHAST_SCREAM;
					break;
				default:
					break;
				}

				for (Player player : GetPlayers(false))
				{
					player.playSound(player.getLocation(), sound, 0.7F, 0 + (UtilMath.random.nextFloat() / 10));
				}

				_lastGhastMoan = System.currentTimeMillis() + 5000 + (UtilMath.r(8) * 1000);
			}
		}
	}

	private Location getEndgameLocation()
	{
		int chance = UtilMath.r(50) + 3;
		int accuracy = Math.max((int) (chance - (_accuracy * chance)), 1);
		_accuracy += 0.0001;

		ArrayList<Player> players = GetPlayers(true);

		for (int a = 0; a < 50; a++)
		{
			Player player = players.get(UtilMath.r(players.size()));

			Location location = player.getLocation().add(UtilMath.r(accuracy * 2) - accuracy, 0,
					UtilMath.r(accuracy * 2) - accuracy);

			location = WorldData.World.getHighestBlockAt(location).getLocation().add(0.5, 0, 0.5);

			if (location.getBlock().getType() == Material.AIR)
			{
				location.add(0, -1, 0);
			}

			if (location.getBlockY() > 0 && location.getBlock().getType() != Material.AIR)
			{
				return location;
			}
		}

		return null;
	}

	private void makeLightning()
	{

		Location loc = getEndgameLocation();

		if (loc == null)
		{
			return;
		}

		loc.getWorld().spigot().strikeLightningEffect(loc, true);
		loc.getWorld().playSound(loc, Sound.AMBIENCE_THUNDER, 5F, 0.8F + UtilMath.random.nextFloat());
		loc.getWorld().playSound(loc, Sound.EXPLODE, 2F, 0.9F + (UtilMath.random.nextFloat() / 3));

		UtilBlock.getExplosionBlocks(loc, 3 * _endgameSize, false);

		// Blocks
		ArrayList<Block> blocks = new ArrayList<Block>(UtilBlock.getInRadius(loc, 3 * _endgameSize).keySet());
		Collections.shuffle(blocks);

		while (blocks.size() > 20)
		{
			blocks.remove(0).setType(Material.AIR);
		}

		Manager.GetExplosion().BlockExplosion(blocks, loc, false);

		HashMap<LivingEntity, Double> inRadius = UtilEnt.getInRadius(loc, 4D * _endgameSize);

		// The damage done at ground zero
		double baseDamage = 6 * _endgameSize;

		for (LivingEntity entity : inRadius.keySet())
		{
			double damage = baseDamage * inRadius.get(entity);

			if (damage > 0)
			{
				getArcadeManager().GetDamage().NewDamageEvent(entity, null, null, DamageCause.LIGHTNING, damage, true, false,
						true, "Lightning", "Lightning");
			}
		}
	}

	@EventHandler
	public void preventEnchanting(EnchantItemEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void preventAnvil(InventoryClickEvent event)
	{
		if (event.getView().getTopInventory() instanceof AnvilInventory)
		{
			event.setCancelled(true);
		}
	}

	private void makeMeteor()
	{
		_fireballSpeed += 0.002;

		if (_endgameSize < 10)
		{
			_endgameSize += 0.04;
		}

		Location loc = getEndgameLocation();

		if (loc == null)
		{
			return;
		}

		summonMeteor(loc, 1.5F * _endgameSize);

	}

	private void summonMeteor(Location loc, float fireballSize)
	{
		Vector vector = new Vector(UtilMath.random.nextDouble() - 0.5D, 0.8, UtilMath.random.nextDouble() - 0.5D).normalize();

		vector.multiply(40);

		loc.add((UtilMath.random.nextDouble() - 0.5) * 7, 0, (UtilMath.random.nextDouble() - 0.5) * 7);

		loc.add(vector);

		final Fireball fireball = (Fireball) loc.getWorld().spawnEntity(loc, EntityType.FIREBALL);

		fireball.setMetadata("Meteor", new FixedMetadataValue(getArcadeManager().getPlugin(), fireballSize));

		new BukkitRunnable()
		{
			int i;

			public void run()
			{
				if (fireball.isValid() && IsLive())
				{
					UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, fireball.getLocation(), 0.3F, 0.3F, 0.3F, 0, 3,
							ViewDist.MAX, UtilServer.getPlayers());

					if (i++ % 6 == 0)
					{
						fireball.getWorld().playSound(fireball.getLocation(), Sound.CAT_HISS, 1.3F, 0F);
					}
				}
				else
				{
					cancel();
				}
			}
		}.runTaskTimer(getArcadeManager().getPlugin(), 0, 0);

		vector.normalize().multiply(-(0.04 + ((_fireballSpeed - 0.05) / 2)));

		// We can't call the bukkit methods because for some weird reason, it enforces a certain speed.
		EntityFireball eFireball = ((CraftFireball) fireball).getHandle();
		eFireball.dirX = vector.getX();
		eFireball.dirY = vector.getY();
		eFireball.dirZ = vector.getZ();

		fireball.setBounce(false);
		fireball.setYield(0);
		fireball.setIsIncendiary(true);
		fireball.setFireTicks(9999);
	}

	@EventHandler
	public void instantDeath(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (!IsLive())
			return;

		if (System.currentTimeMillis() > getGameLiveTime() + (20 * 60 * 1000))
		{
			ArrayList<Player> players = new ArrayList<Player>(GetPlayers(true));

			Collections.sort(players, new Comparator<Player>()
			{

				@Override
				public int compare(Player o1, Player o2)
				{
					// Compare them backwards so the lesser health people are last
					// Just so the bigger camper loses more.
					return new Double(o2.getHealth()).compareTo(o1.getHealth());
				}
			});

			Iterator<Player> itel = players.iterator();

			while (itel.hasNext())
			{
				Player player = itel.next();

				// Don't kill them if they are the last person in this list.
				if (itel.hasNext())
				{
					getArcadeManager().GetDamage().NewDamageEvent(player, null, null, DamageCause.ENTITY_EXPLOSION, 9999, false,
							true, true, "Magic", "Magic");
				}
			}
		}
	}

	@EventHandler
	public void updateMana(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (!IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			Wizard wizard = getWizard(player);

			float newMana = wizard.getMana();

			if (newMana < wizard.getMaxMana())
			{
				newMana = Math.min(newMana + wizard.getManaPerTick(), wizard.getMaxMana());
				wizard.setMana(newMana);
			}

			float percentage = Math.min(1, wizard.getMana() / wizard.getMaxMana());

			String text = (int) Math.floor(wizard.getMana()) + "/" + (int) wizard.getMaxMana() + " mana                    "
					+ UtilTime.convert((int) (wizard.getManaPerTick() * 20000), 1, TimeUnit.SECONDS) + "mps";

			UtilTextTop.displayTextBar(player, percentage, text);

			drawUtilTextBottom(player);
		}

	}
}
