package mineplex.game.clans.compensation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.amplifiers.AmplifierManager.AmplifierType;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.items.ItemType;
import mineplex.game.clans.items.RareItemFactory;
import mineplex.game.clans.items.attributes.armor.ConqueringArmorAttribute;
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
import mineplex.game.clans.items.economy.GoldToken;
import mineplex.game.clans.items.legendaries.AlligatorsTooth;
import mineplex.game.clans.items.legendaries.GiantsBroadsword;
import mineplex.game.clans.items.legendaries.HyperAxe;
import mineplex.game.clans.items.legendaries.MagneticMaul;
import mineplex.game.clans.items.legendaries.MeridianScepter;
import mineplex.game.clans.items.legendaries.WindBlade;

public class ClansCompensation extends JavaPlugin implements Listener
{
	public enum Perm implements Permission
	{
		COMPENSATION_COMMAND,
	}

	private final List<UUID> _compensating = new ArrayList<>();
	private boolean _debug;
	
	@Override
	public void onEnable()
	{
		System.out.println("[INFO] Enabling ClansCompensation");
		Bukkit.getPluginManager().registerEvents(this, this);
		PermissionGroup.PLAYER.setPermission(Perm.COMPENSATION_COMMAND, true, true);
		ClansManager.getInstance().addCommand(new CompensationCommand(ClansManager.getInstance(), this));
		loadUUIDs(uuids ->
		{
			_compensating.clear();
			_compensating.addAll(uuids);
		});
		try
		{
			_debug = new File(new File(".").getCanonicalPath() + File.separator + "DebugCompensation.dat").exists();
		}
		catch (IOException e)
		{
			_debug = false;
		}
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("[INFO] Disabling ClansCompensation");
		saveUUIDs(false);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (canClaim(event.getPlayer().getUniqueId()))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Compensation", "You have a compensation package ready to open! Run /compensation to get started!"));
		}
	}
	
	public boolean canClaim(UUID uuid)
	{
		if (_debug)
		{
			return true;
		}
		return _compensating.contains(uuid);
	}
	
	public void claim(Player player)
	{
		if (_debug || _compensating.remove(player.getUniqueId()))
		{
			Block[] possible = new Block[]
					{
						player.getLocation().getBlock().getRelative(1, 0, 0),
						player.getLocation().getBlock().getRelative(-1, 0, 0),
						player.getLocation().getBlock().getRelative(0, 0, 1),
						player.getLocation().getBlock().getRelative(0, 0, -1)
					};
			
			Block spawn = null;
			Block spawn2 = null;
			for (Block block : possible)
			{
				if (spawn != null && spawn2 != null)
				{
					break;
				}
				ClanTerritory claim = ClansManager.getInstance().getClanUtility().getClaim(block.getLocation());
				if (claim != null)
				{
					if (ClansManager.getInstance().getClan(player) == null)
					{
						continue;
					}
					ClanInfo clan = ClansManager.getInstance().getClan(player);
					if (!clan.getName().equals(claim.Owner))
					{
						continue;
					}
				}
				ClansManager.getInstance().getBlockRestore().restore(block);
				if (block.getType() != Material.AIR)
				{
					continue;
				}
				boolean overlap = false;
				for (int x = -1; x <= 1; x++)
				{
					for (int z = -1; z <= 1; z++)
					{
						Block check = block.getRelative(x, 0, z);
						if (check.getType() == Material.CHEST)
						{
							overlap = true;
						}
					}
				}
				if (overlap)
				{
					continue;
				}
				
				if (spawn == null)
				{
					spawn = block;
					continue;
				}
				if (spawn2 == null)
				{
					spawn2 = block;
					continue;
				}
			}
			
			if (spawn == null || spawn2 == null)
			{
				UtilPlayer.message(player, F.main("Compensation", "Try that again in a different spot!"));
				if (!_debug)
				{
					_compensating.add(player.getUniqueId());
				}
				return;
			}
			else
			{
				spawn.setType(Material.CHEST);
				spawn2.setType(Material.CHEST);
				Chest one = (Chest)spawn.getState();
				Chest two = (Chest)spawn2.getState();
				FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.MAROON).withFade(Color.NAVY).withFlicker().build();
				UtilFirework.playFirework(spawn.getLocation().add(0.5, 0.5, 0.5), effect);
				UtilFirework.playFirework(spawn2.getLocation().add(0.5, 0.5, 0.5), effect);
				
				List<ItemStack> items = new ArrayList<>();
				{
					UtilPlayer.message(player, F.main("Compensation", "You received two " + F.name(AmplifierType.SIXTY.getDisplayName() + "s") + "!"));
					ClansManager.getInstance().getInventoryManager().addItemToInventory(player, AmplifierType.SIXTY.getFullItemName(), 2);
					for (int i = 0; i < 10; i++)
					{
						items.add(new GoldToken(50000).toItemStack());
					}
					items.add(ItemStackFactory.Instance.CreateStack(Material.BEACON, (byte) 0, 1, C.cGold + "Supply Drop"));
					items.add(new ItemStack(Material.DIAMOND_HELMET, 5));
					items.add(new ItemStack(Material.DIAMOND_CHESTPLATE, 5));
					items.add(new ItemStack(Material.DIAMOND_LEGGINGS, 5));
					items.add(new ItemStack(Material.DIAMOND_BOOTS, 5));
					items.add(new ItemStack(Material.IRON_HELMET, 5));
					items.add(new ItemStack(Material.IRON_CHESTPLATE, 5));
					items.add(new ItemStack(Material.IRON_LEGGINGS, 5));
					items.add(new ItemStack(Material.IRON_BOOTS, 5));
					items.add(new ItemStack(Material.GOLD_HELMET, 5));
					items.add(new ItemStack(Material.GOLD_CHESTPLATE, 5));
					items.add(new ItemStack(Material.GOLD_LEGGINGS, 5));
					items.add(new ItemStack(Material.GOLD_BOOTS, 5));
					items.add(new ItemStack(Material.CHAINMAIL_HELMET, 5));
					items.add(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 5));
					items.add(new ItemStack(Material.CHAINMAIL_LEGGINGS, 5));
					items.add(new ItemStack(Material.CHAINMAIL_BOOTS, 5));
					items.add(new ItemStack(Material.LEATHER_HELMET, 5));
					items.add(new ItemStack(Material.LEATHER_CHESTPLATE, 5));
					items.add(new ItemStack(Material.LEATHER_LEGGINGS, 5));
					items.add(new ItemStack(Material.LEATHER_BOOTS, 5));
					items.add(new ItemStack(Material.DIAMOND_SWORD, 5));
					items.add(new ItemStack(Material.DIAMOND_AXE, 5));
					items.add(new ItemStack(Material.GOLD_SWORD, 5));
					items.add(new ItemStack(Material.GOLD_AXE, 5));
					items.add(new ItemStack(Material.IRON_SWORD, 5));
					items.add(new ItemStack(Material.IRON_AXE, 5));
					items.add(new ItemStack(Material.BOW, 5));
					items.add(new ItemBuilder(Material.ENCHANTMENT_TABLE).setTitle(C.cGreenB + "Class Shop").build());
					items.add(new ItemStack(Material.ANVIL, 3));
					Random rand = new Random();
					for (int i = 0; i < 3; i++)
					{
						int picked = rand.nextInt(24 * 3);
						RareItemFactory factory;
						switch (picked)
						{
						case 0:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(WindBlade.class);
							factory.setSuffix(HasteAttribute.class);
							items.add(factory.fabricate());
							break;
						case 1:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_AXE);
							factory.setSuperPrefix(FrostedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 2:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_SWORD);
							factory.setSuperPrefix(FlamingAttribute.class);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 3:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(AlligatorsTooth.class);
							items.add(factory.fabricate());
							break;
						case 4:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setSuperPrefix(LeechingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 5:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_AXE);
							factory.setPrefix(JaggedAttribute.class);
							factory.setSuffix(ConqueringAttribute.class);
							items.add(factory.fabricate());
							break;
						case 6:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(GiantsBroadsword.class);
							items.add(factory.fabricate());
							break;
						case 7:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setSuffix(HasteAttribute.class);
							items.add(factory.fabricate());
							break;
						case 8:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 9:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(MagneticMaul.class);
							factory.setSuffix(HasteAttribute.class);
							items.add(factory.fabricate());
							break;
						case 10:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.GOLD_SWORD);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 11:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_AXE);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 12:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(MeridianScepter.class);
							items.add(factory.fabricate());
							break;
						case 13:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setPrefix(JaggedAttribute.class);
							factory.setSuffix(ConqueringAttribute.class);
							items.add(factory.fabricate());
							break;
						case 14:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_SWORD);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 15:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(AlligatorsTooth.class);
							items.add(factory.fabricate());
							break;
						case 16:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setSuperPrefix(FrostedAttribute.class);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 17:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.GOLD_AXE);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 18:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(WindBlade.class);
							factory.setSuffix(ConqueringAttribute.class);
							items.add(factory.fabricate());
							break;
						case 19:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_SWORD);
							factory.setSuperPrefix(FrostedAttribute.class);
							factory.setSuffix(HasteAttribute.class);
							items.add(factory.fabricate());
							break;
						case 20:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_AXE);
							factory.setSuperPrefix(FrostedAttribute.class);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 21:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(GiantsBroadsword.class);
							factory.setSuffix(HasteAttribute.class);
							items.add(factory.fabricate());
							break;
						case 22:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setSuffix(ConqueringAttribute.class);
							items.add(factory.fabricate());
							break;
						case 23:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setPrefix(RecursiveAttribute.class);
							items.add(factory.fabricate());
							break;
						case 24:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(HyperAxe.class);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 25:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setPrefix(HeavyArrowsAttribute.class);
							items.add(factory.fabricate());
							break;
						case 26:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.GOLD_SWORD);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 27:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(MeridianScepter.class);
							items.add(factory.fabricate());
							break;
						case 28:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.GOLD_SWORD);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 29:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_AXE);
							factory.setSuffix(HasteAttribute.class);
							items.add(factory.fabricate());
							break;
						case 30:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(WindBlade.class);
							items.add(factory.fabricate());
							break;
						case 31:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setSuperPrefix(ScorchingAttribute.class);
							factory.setSuffix(SlayingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 32:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 33:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(HyperAxe.class);
							items.add(factory.fabricate());
							break;
						case 34:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_SWORD);
							factory.setSuperPrefix(FlamingAttribute.class);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 35:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_AXE);
							factory.setSuperPrefix(FrostedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 36:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(GiantsBroadsword.class);
							items.add(factory.fabricate());
							break;
						case 37:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setPrefix(InverseAttribute.class);
							items.add(factory.fabricate());
							break;
						case 38:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.DIAMOND_CHESTPLATE);
							factory.setPrefix(ReinforcedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 39:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(WindBlade.class);
							items.add(factory.fabricate());
							break;
						case 40:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.LEATHER_BOOTS);
							factory.setPrefix(SlantedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 41:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_SWORD);
							factory.setSuperPrefix(FrostedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 42:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(GiantsBroadsword.class);
							items.add(factory.fabricate());
							break;
						case 43:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.IRON_LEGGINGS);
							factory.setPrefix(PaddedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 44:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.GOLD_SWORD);
							factory.setPrefix(SharpAttribute.class);
							items.add(factory.fabricate());
							break;
						case 45:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(MeridianScepter.class);
							items.add(factory.fabricate());
							break;
						case 46:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.CHAINMAIL_CHESTPLATE);
							factory.setPrefix(SlantedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 47:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_SWORD);
							factory.setSuffix(ConqueringAttribute.class);
							items.add(factory.fabricate());
							break;
						case 48:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(MagneticMaul.class);
							factory.setPrefix(SharpAttribute.class);
							items.add(factory.fabricate());
							break;
						case 49:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.IRON_HELMET);
							factory.setSuffix(ConqueringArmorAttribute.class);
							items.add(factory.fabricate());
							break;
						case 50:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setSuffix(HasteAttribute.class);
							items.add(factory.fabricate());
							break;
						case 51:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(AlligatorsTooth.class);
							factory.setSuperPrefix(FrostedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 52:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.LEATHER_BOOTS);
							factory.setPrefix(ReinforcedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 53:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.IRON_LEGGINGS);
							factory.setPrefix(SlantedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 54:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(HyperAxe.class);
							items.add(factory.fabricate());
							break;
						case 55:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setPrefix(RecursiveAttribute.class);
							items.add(factory.fabricate());
							break;
						case 56:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.CHAINMAIL_BOOTS);
							factory.setPrefix(PaddedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 57:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(GiantsBroadsword.class);
							factory.setSuffix(ConqueringAttribute.class);
							items.add(factory.fabricate());
							break;
						case 58:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.IRON_HELMET);
							factory.setPrefix(SlantedAttribute.class);
							factory.setSuffix(ConqueringArmorAttribute.class);
							items.add(factory.fabricate());
							break;
						case 59:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setPrefix(InverseAttribute.class);
							items.add(factory.fabricate());
							break;
						case 60:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(WindBlade.class);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 61:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_AXE);
							factory.setSuperPrefix(FrostedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 62:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.LEATHER_BOOTS);
							factory.setPrefix(PaddedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 63:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(AlligatorsTooth.class);
							items.add(factory.fabricate());
							break;
						case 64:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.GOLD_AXE);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 65:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.DIAMOND_SWORD);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 66:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(MeridianScepter.class);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 67:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_AXE);
							factory.setSuperPrefix(FrostedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 68:
							factory = new RareItemFactory(ItemType.BOW);
							factory.setType(Material.BOW);
							factory.setPrefix(HuntingAttribute.class);
							items.add(factory.fabricate());
							break;
						case 69:
							factory = new RareItemFactory(ItemType.LEGENDARY);
							factory.setLegendary(HyperAxe.class);
							factory.setPrefix(JaggedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 70:
							factory = new RareItemFactory(ItemType.ARMOR);
							factory.setType(Material.DIAMOND_CHESTPLATE);
							factory.setPrefix(ReinforcedAttribute.class);
							items.add(factory.fabricate());
							break;
						case 71:
							factory = new RareItemFactory(ItemType.WEAPON);
							factory.setType(Material.IRON_AXE);
							factory.setSuperPrefix(FlamingAttribute.class);
							items.add(factory.fabricate());
							break;
						}
					}
					for (int slot = 0; slot < one.getBlockInventory().getSize(); slot++)
					{
						if (items.isEmpty())
						{
							break;
						}
						one.getBlockInventory().setItem(slot, items.remove(0));
					}
					for (int slot = 0; slot < two.getBlockInventory().getSize(); slot++)
					{
						if (items.isEmpty())
						{
							break;
						}
						two.getBlockInventory().setItem(slot, items.remove(0));
					}
				}
			}
		}
	}
	
	private void loadUUIDs(Callback<List<UUID>> uuidCallback)
	{
		ClansManager.getInstance().runAsync(() ->
		{
			List<UUID> ret = new ArrayList<>();
			try
			{
				FileInputStream fstream = new FileInputStream(new File(".").getCanonicalPath() + File.separator + "compensating.dat");
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String line = null;
				while ((line = br.readLine()) != null)
				{
					UUID uuid = UUID.fromString(line);
					ret.add(uuid);
				}
				br.close();
				in.close();
				fstream.close();
				
				uuidCallback.run(ret);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				uuidCallback.run(ret);
			}
		});
	}
	
	private void saveUUIDs(boolean async)
	{
		Runnable r = () ->
		{
			try
			{
				File storage = new File(new File(".").getCanonicalPath() + File.separator + "compensating.dat");
				if (storage.exists())
				{
					FileUtils.deleteQuietly(storage);
				}
				
				if (!_compensating.isEmpty())
				{
					storage.createNewFile();
					
					FileWriter fstream = new FileWriter(storage);
					BufferedWriter out = new BufferedWriter(fstream);
					
					out.write(_compensating.get(0).toString());
					
					for (int i = 1; i < _compensating.size(); i++)
					{
						UUID comp = _compensating.get(i);
						out.write("\n");
						out.write(comp.toString());
					}
					out.close();
					fstream.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		};
		
		if (async)
		{
			ClansManager.getInstance().runAsync(r);
		}
		else
		{
			r.run();
		}
	}
}