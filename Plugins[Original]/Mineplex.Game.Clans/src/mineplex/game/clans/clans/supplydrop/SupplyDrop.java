package mineplex.game.clans.clans.supplydrop;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.supplydrop.SupplyDropManager.SupplyDropType;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntityBeacon;

public class SupplyDrop
{
	private static Field BEACON_LEVEL;
	private static Field BEACON_ENABLED;
	
	static
	{
		try
		{
			BEACON_LEVEL = TileEntityBeacon.class.getDeclaredField("j");
			BEACON_LEVEL.setAccessible(true);
			BEACON_ENABLED = TileEntityBeacon.class.getDeclaredField("i");
			BEACON_ENABLED.setAccessible(true);
		}
		catch (NoSuchFieldException | SecurityException e)
		{
			e.printStackTrace();
		}
	}
	
	private static final int DROP_TICKS = 20 * 60 * 3; //3 Minutes
	private static final int REMOVE_TICKS = DROP_TICKS + (20 * 120); // 2 Minutes
	public static final Material SUPPLY_DROP_MATERIAL = Material.BEACON;
	public static final String SUPPLY_DROP_FILLED_METADATA = "SUPPLY_DROP_FILLED";
	
	private final SupplyDropType _type;
	private final mineplex.game.clans.clans.supplydrop.SupplyDropManager.BlockPosition _position;
	private final Block _block;
	private final Block[] _below = new Block[9];
	@SuppressWarnings("unchecked")
	private final Pair<Material, Byte>[] _oldBelow = new Pair[9];
	private int _ticks;
	private boolean _ended;
	private final Hologram _hologram;

	@SuppressWarnings("deprecation")
	protected SupplyDrop(SupplyDropType type, Block block, HologramManager hologramManager)
	{
		_type = type;
		_position = new mineplex.game.clans.clans.supplydrop.SupplyDropManager.BlockPosition(block);
		_block = block;
		_ticks = 0;
		_ended = false;
		_hologram = new Hologram(hologramManager, _block.getLocation().add(0.5, 1.5, 0.5));
		_hologram.setInteraction((player, clickType) ->
		{
			UtilServer.CallEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), _block, _block.getFace(player.getLocation().getBlock())));
		});
		_hologram.start();
		
		block.setType(SUPPLY_DROP_MATERIAL);
		
		int index = 0;
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				Block b = block.getRelative(x, -1, z);
				_below[index] = b;
				_oldBelow[index] = Pair.create(b.getType(), b.getData());
				b.setType(Material.DIAMOND_BLOCK);
				index++;
			}
		}
	}
	
	private String getChatColor(long millis)
	{
		if (millis > 30000)
		{
			return C.cAqua;
		}
		else if (millis > 10000)
		{
			return C.cYellow;
		}
		else
		{
			return C.cRed;
		}
	}
	
	private void placeChest()
	{
		_block.setType(Material.CHEST);
		ClansManager.getInstance().runSyncLater(() ->
		{
			Chest chest = (Chest) _block.getState();

			Inventory inventory = chest.getBlockInventory();
			
			int i = 0;
			for (ItemStack item : _type.generateLootItems())
			{
				inventory.setItem(i++, item);
			}
			chest.update(true);
			chest.setMetadata(SUPPLY_DROP_FILLED_METADATA, new FixedMetadataValue(UtilServer.getPlugin(), true));
		}, 5);
	}
	
	public boolean isDropping()
	{
		return _ticks < DROP_TICKS;
	}
	
	public boolean isActive()
	{
		return _ticks < REMOVE_TICKS && !_ended;
	}
	
	public Chunk getChunk()
	{
		return _block.getChunk();
	}
	
	public mineplex.game.clans.clans.supplydrop.SupplyDropManager.BlockPosition getPosition()
	{
		return _position;
	}

	public int getTicks()
	{
		return _ticks;
	}

	public void tick()
	{
		if (_ended)
		{
			return;
		}
		if (getTicks() < DROP_TICKS)
		{
			if (getTicks() == 10)
			{
				try
				{
					TileEntity tileEntity = ((CraftWorld) _block.getWorld()).getHandle().getTileEntity(new BlockPosition(_block.getX(), _block.getY(), _block.getZ()));

					if (tileEntity instanceof TileEntityBeacon)
					{
						BEACON_ENABLED.set(tileEntity, true);
						BEACON_LEVEL.set(tileEntity, 3);
						tileEntity.update();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			if (getTicks() > 15 && getTicks() % 10 == 0)
			{
				FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.AQUA, Color.WHITE, Color.GRAY).withFade(Color.BLACK).withFlicker().build();
				UtilFirework.playFirework(_block.getLocation().add(0.5, 0.5, 0.5), effect);
			}

			if (getTicks() % 20 == 0)
			{
				long millis = (DROP_TICKS - getTicks()) * 50; // Multiply by 50 to convert ticks to ms
				_hologram.setText(getChatColor(millis) + UtilTime.MakeStr(millis) + " Until Drop");
			}
		}
		else
		{
			if (getTicks() == DROP_TICKS)
			{
				Bukkit.broadcastMessage(F.main("Supply Drop", "A supply drop has landed at " + F.elem(UtilWorld.locToStrClean(_block.getLocation()))));
				placeChest();
			}

			// Drop supply drop
			if (getTicks() % 20 == 0)
			{
				long millis = (REMOVE_TICKS - getTicks()) * 50; // Multiply by 50 to convert ticks to ms
				_hologram.setText(getChatColor(millis) + UtilTime.MakeStr(millis) + " Remaining");
			}

			if (getTicks() >= REMOVE_TICKS)
			{
				finish(false);
			}
		}

		_ticks++;
	}

	@SuppressWarnings("deprecation")
	public void finish(boolean onDisable)
	{
		_ended = true;
		_hologram.stop();
		for (int i = 0; i < 9; i++)
		{
			_below[i].setTypeIdAndData(_oldBelow[i].getLeft().getId(), _oldBelow[i].getRight(), false);
		}
		_block.removeMetadata(SUPPLY_DROP_FILLED_METADATA, UtilServer.getPlugin());
		if (onDisable)
		{
			_block.setType(Material.AIR);
		}
		else
		{
			_block.breakNaturally();
		}
	}
}