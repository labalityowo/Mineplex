package mineplex.game.clans.clans.supplydrop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.weight.Weight;
import mineplex.core.common.weight.WeightSet;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.loot.GoldTokenLoot;
import mineplex.game.clans.clans.loot.MountLoot;
import mineplex.game.clans.clans.loot.RuneLoot;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.ItemType;
import mineplex.game.clans.items.RareItemFactory;
import mineplex.game.clans.items.attributes.AttributeContainer;
import mineplex.game.clans.items.legendaries.LegendaryItem;
import mineplex.game.clans.items.legendaries.MeridianScepter;
import mineplex.game.clans.items.rares.RunedPickaxe;

public class SupplyDropManager extends MiniPlugin
{
	private SupplyDrop _active = null;
	private final Block _dropBlock = Bukkit.getWorld("world").getBlockAt(-31, 55, -7);
	private final SupplyDropShop _shop;

	public SupplyDropManager(JavaPlugin plugin)
	{
		super("Supply Drop", plugin);
		
		_shop = new SupplyDropShop(this);
	}
	
	@Override
	public void disable()
	{
		if (_active != null)
		{
			_active.finish(true);
		}
	}
	
	public SupplyDropShop getShop()
	{
		return _shop;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (_active != null)
		{
			if (_active.isActive())
			{
				_active.tick();
			}
			else
			{
				_active = null;
			}
		}
	}

	@EventHandler
	public void cancelInteract(PlayerInteractEvent event)
	{
		if (!event.hasBlock())
		{
			return;
		}
		if (!UtilEvent.isAction(event, ActionType.ANY))
		{
			return;
		}
		if (_active == null)
		{
			return;
		}
		
		if (new BlockPosition(event.getClickedBlock()).equals(_active.getPosition()))
		{
			event.setCancelled(true);
			if (!_active.isActive() || _active.isDropping())
			{
				return;
			}
			if (!event.getClickedBlock().hasMetadata(SupplyDrop.SUPPLY_DROP_FILLED_METADATA))
			{
				return;
			}
			if (UtilPlayer.isSpectator(event.getPlayer()) || event.getPlayer().getGameMode() != GameMode.SURVIVAL)
			{
				return;
			}
			_active.finish(false);
			_active = null;
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		if (_active != null && _active.getChunk().getX() == event.getChunk().getX() && _active.getChunk().getZ() == event.getChunk().getZ())
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (_active != null)
		{
			boolean landed = !_active.isDropping();
			runSyncLater(() ->
			{
				if (_active != null)
				{
					if (_active.isDropping())
					{
						UtilPlayer.message(event.getPlayer(), F.main(getName(), "A supply drop is landing at " + F.elem("(" + _active.getPosition()._x + ", " + _active.getPosition()._z + ")") + "!"));
					}
					else if (landed)
					{
						UtilPlayer.message(event.getPlayer(), F.main(getName(), "A supply drop has landed at " + F.elem("(" + _active.getPosition()._x + ", " + _active.getPosition()._z + ")") + "!"));
					}
				}
			}, 40L);
		}
	}
	
	/**
	 * Checks whether there is a supply drop active on this server
	 * @return Whether there is a supply drop active on this server
	 */
	public boolean hasActiveSupplyDrop()
	{
		return _active != null;
	}
	
	/**
	 * Checks how many of a certain supply drop type a player owns
	 * @param player The player to check
	 * @param type The type of supply drop to check for
	 * @return The amount of supply drops of that type owned
	 */
	public int getAmountOwned(Player player, SupplyDropType type)
	{
		return ClansManager.getInstance().getInventoryManager().Get(player).getItemCount(type.getItemName());
	}
	
	/**
	 * Makes a player use a supply drop
	 * @param user The player to use the supply drop
	 * @param type The type of supply drop to use
	 */
	public void useSupplyDrop(Player user, SupplyDropType type)
	{
		if (getAmountOwned(user, type) < 1)
		{
			return;
		}
		if (hasActiveSupplyDrop())
		{
			return;
		}
		_dropBlock.getChunk().load();
		_active = new SupplyDrop(type, _dropBlock, ClansManager.getInstance().getHologramManager());
		ClansManager.getInstance().getInventoryManager().addItemToInventory(user, type.getItemName(), -1);
		UtilTextMiddle.display(C.cRed + "Supply Drop", "(" + _active.getPosition()._x + ", " + _active.getPosition()._z + ")");
		Bukkit.broadcastMessage(F.main(getName(), "A supply drop has been summoned by " + F.elem(user.getName()) + " at " + F.elem("(" + _active.getPosition()._x + ", " + _active.getPosition()._z + ")") + "!"));
	}
	
	protected static class BlockPosition
	{
		private final int _x, _y, _z;
		
		public BlockPosition(Block block)
		{
			_x = block.getX();
			_y = block.getY();
			_z = block.getZ();
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(_x, _y, _z);
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o instanceof BlockPosition)
			{
				BlockPosition pos = (BlockPosition) o;
				return pos._x == _x && pos._y == _y && pos._z == _z;
			}
			
			return false;
		}
	}
	
	public static enum SupplyDropType
	{
		NORMAL("Clans Supply Drop", "Supply Drop", 5, 7, () ->
		{
			GearManager gear = ClansManager.getInstance().getGearManager();
			ItemType[] itemTypes = {ItemType.ARMOR, ItemType.BOW, ItemType.WEAPON};
			Material[] weaponTypes = {Material.IRON_AXE, Material.IRON_SWORD};
			Material[] armorTypes = Stream.of(Material.values()).filter(UtilItem::isArmor).toArray(size -> new Material[size]);
			GoldTokenLoot goldLoot = new GoldTokenLoot(30000, 70000);
			MountLoot mountLoot = new MountLoot(1, 2);
			
			return (amount) ->
			{
				WeightSet<Supplier<ItemStack>> set = new WeightSet<>();
				set.add(1, () ->
				{
					return RareItemFactory.begin(ItemType.RARE).setRare(RunedPickaxe.class).fabricate();
				});
				set.add(25, () ->
				{
					ItemType type = UtilMath.randomElement(itemTypes);
					Material mat = UtilMath.randomElement((type == ItemType.ARMOR) ? armorTypes : (type == ItemType.BOW ? new Material[] {Material.BOW} : weaponTypes));
					AttributeContainer container = new AttributeContainer();

					gear.generateAttributes(container, type, ThreadLocalRandom.current().nextInt(1, 3));
					return RareItemFactory.begin(type).setType(mat).setSuperPrefix(container.getSuperPrefix()).setPrefix(container.getPrefix()).setSuffix(container.getSuffix()).fabricate();
				});
				Weight<Supplier<ItemStack>> gold = set.add(4, () ->
				{
					return goldLoot.getItemStack();
				});
				Weight<Supplier<ItemStack>> mount = set.add(4, () ->
				{
					return mountLoot.getItemStack();
				});
				
				List<ItemStack> items = new ArrayList<>();
				for (int i = 0; i < amount; i++)
				{
					ItemStack item = set.generateRandom().get();
					items.add(set.generateRandom().get());
					if (item.getType() == Material.RABBIT_FOOT)
					{
						set.remove(gold);
					}
					if (item.getType() == Material.IRON_BARDING || item.getType() == Material.GOLD_BARDING)
					{
						set.remove(mount);
					}
				}
				
				return items;
			};
		}),
		GILDED("Clans Gilded Supply Drop", "Gilded Supply Drop", 8, 10, () ->
		{
			GearManager gear = ClansManager.getInstance().getGearManager();
			List<Class<? extends LegendaryItem>> legendaryTypes = Lists.newArrayList(gear.getFindableLegendaries());
			List<Class<? extends LegendaryItem>> runeableLegendaryTypes = Lists.newArrayList(gear.getFindableLegendaries());
			runeableLegendaryTypes.remove(MeridianScepter.class);
			ItemType[] rareTypes = {ItemType.RARE, ItemType.LEGENDARY};
			ItemType[] itemTypes = {ItemType.ARMOR, ItemType.BOW, ItemType.WEAPON};
			Material[] weaponTypes = {Material.IRON_AXE, Material.IRON_SWORD};
			Material[] armorTypes = Stream.of(Material.values()).filter(UtilItem::isArmor).toArray(size -> new Material[size]);
			GoldTokenLoot goldLoot = new GoldTokenLoot(30000, 70000);
			MountLoot mountLoot = new MountLoot(1, 3);
			RuneLoot runeLoot = new RuneLoot();
			
			return (amount) ->
			{
				WeightSet<Supplier<ItemStack>> set = new WeightSet<>();
				set.add(1, () ->
				{
					ItemType type = UtilMath.randomElement(rareTypes);
					RareItemFactory factory = RareItemFactory.begin(type);
					if (type == ItemType.RARE)
					{
						return factory.setRare(RunedPickaxe.class).fabricate();
					}
					boolean runed = false;
					if (ThreadLocalRandom.current().nextDouble() < 0.02)
					{
						runed = true;
						AttributeContainer container = new AttributeContainer();
						gear.generateAttributes(container, type, ThreadLocalRandom.current().nextInt(1, 4));
						factory.setSuperPrefix(container.getSuperPrefix()).setPrefix(container.getPrefix()).setSuffix(container.getSuffix());
					}
					return factory.setLegendary(UtilMath.randomElement(runed ? runeableLegendaryTypes : legendaryTypes)).fabricate();
				});
				set.add(25, () ->
				{
					ItemType type = UtilMath.randomElement(itemTypes);
					Material mat = UtilMath.randomElement((type == ItemType.ARMOR) ? armorTypes : (type == ItemType.BOW ? new Material[] {Material.BOW} : weaponTypes));
					AttributeContainer container = new AttributeContainer();

					gear.generateAttributes(container, type, ThreadLocalRandom.current().nextInt(1, 4));
					return RareItemFactory.begin(type).setType(mat).setSuperPrefix(container.getSuperPrefix()).setPrefix(container.getPrefix()).setSuffix(container.getSuffix()).fabricate();
				});
				set.add(3, () ->
				{
					return runeLoot.getItemStack();
				});
				Weight<Supplier<ItemStack>> gold = set.add(4, () ->
				{
					return goldLoot.getItemStack();
				});
				Weight<Supplier<ItemStack>> mount = set.add(4, () ->
				{
					return mountLoot.getItemStack();
				});
				
				List<ItemStack> items = new ArrayList<>();
				int golds = 0;
				int mounts = 0;
				for (int i = 0; i < amount; i++)
				{
					ItemStack item = set.generateRandom().get();
					items.add(set.generateRandom().get());
					if (item.getType() == Material.RABBIT_FOOT)
					{
						if (++golds > 1)
						{
							set.remove(gold);
						}
					}
					if (item.getType() == Material.IRON_BARDING || item.getType() == Material.GOLD_BARDING)
					{
						if (++mounts > 2)
						{
							set.remove(mount);
						}
					}
				}
				
				return items;
			};
		});
		
		private final String _item, _display;
		private final int _min, _max;
		private final Function<Integer, List<ItemStack>> _loot;
		
		private SupplyDropType(String itemName, String displayName, int minItems, int maxItems, Supplier<Function<Integer, List<ItemStack>>> loot)
		{
			_item = itemName;
			_display = displayName;
			_min = minItems;
			_max = maxItems;
			_loot = loot.get();
		}
		
		/**
		 * Gets the name of this supply drop as recognized by the inventory database
		 * @return The name of this supply drop as recognized by the inventory database
		 */
		public String getItemName()
		{
			return _item;
		}
		
		/**
		 * Gets the display name for this supply drop
		 * @return The display name for this supply drop
		 */
		public String getDisplayName()
		{
			return _display;
		}
		
		/**
		 * Gets the minimum items for this type of supply drop
		 * @return The minimum items for this type of supply drop
		 */
		public int getMinItems()
		{
			return _min;
		}
		
		/**
		 * Gets the maximum items for this type of supply drop
		 * @return The maximum items for this type of supply drop
		 */
		public int getMaxItems()
		{
			return _max;
		}
		
		public List<ItemStack> generateLootItems()
		{
			return _loot.apply(ThreadLocalRandom.current().nextInt(_min, _max + 1));
		}
	}
}