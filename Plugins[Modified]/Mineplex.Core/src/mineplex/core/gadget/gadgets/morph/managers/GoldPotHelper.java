package mineplex.core.gadget.gadgets.morph.managers;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.disguise.disguises.DisguiseCat;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.recharge.Recharge;

public class GoldPotHelper
{

	private static final float EXP_INCREMENT = 0.2f;
	private static final long COOLDOWN = 300000;
	private static final int SHARDS = 250;
	private static final int GEMS = 60;

	private final Player _player;
	private final GadgetManager _manager;
	private final Gadget _gadget;
	private GoldPotStands _goldPotStands;
	private Block _block;
	private boolean _solid = false;
	private boolean _nuggets = false;

	private HashSet<Item> _items = new HashSet<>();

	public GoldPotHelper(Player player, GadgetManager manager, Gadget gadget)
	{
		_player = player;
		_manager = manager;
		_gadget = gadget;
		_goldPotStands = new GoldPotStands();
	}

	public void solififyPlayer()
	{
		if (_solid)
			return;

		Block block = _player.getLocation().getBlock();

		if (!_manager.selectBlocks(_gadget, block) || block.getType() != Material.AIR)
		{
			_manager.informNoUse(_player);
			return;
		}

		if (!Recharge.Instance.usable(_player, _gadget.getName(), true, "Your pot will be refilled with gold in %t"))
		{
			return;
		}

		UtilMorph.undisguise(_player, _manager.getDisguiseManager());
		DisguiseChicken disguiseChicken = new DisguiseChicken(_player);
		disguiseChicken.setSoundDisguise(new DisguiseCat(_player));
		disguiseChicken.setInvisible(true);
		UtilMorph.disguise(_player, disguiseChicken, _manager);

		block.setType(Material.CAULDRON);
		_block = block;
		_goldPotStands.setBlock(_block);
		_goldPotStands.createStands();

		_solid = true;

		UtilPlayer.message(_player, F.main("Gold Pot", "You're now filled with gold!"));
	}

	public void unsolidifyPlayer()
	{
		if (!_solid)
			return;

		_goldPotStands.removeStands();
		UtilMorph.undisguise(_player, _manager.getDisguiseManager());
		DisguiseBlock disguiseBlock = new DisguiseBlock(_player, Material.CAULDRON, (byte) 0);
		UtilMorph.disguise(_player, disguiseBlock, _manager);

		if (_block != null)
		{
			_block.setType(Material.AIR);
			_block = null;
		}

		_solid = false;

		UtilPlayer.message(_player, F.main("Gold Pot", "You're no longer filled with gold!"));
	}

	public boolean updatePlayer(boolean second, boolean tick)
	{
		boolean solidify = false;
		if (second)
		{
			if (!_solid)
			{
				// Updates EXP Bar
				_player.setExp(_player.getExp() + EXP_INCREMENT);

				if (_player.getExp() == 1)
				{
					// Solidifies (or tries to)
					solidify = true;
					_player.setExp(0f);
				}
				if (_manager.isMoving(_player))
				{
					_player.setExp(0f);
					solidify = false;
				}
			}
			else
			{
				// Throws items in the air
				for (int i = 1; i < 5; i++)
				{
					ItemStack itemStack = new ItemStack((_nuggets) ? Material.GOLD_NUGGET : Material.GOLD_INGOT);
					ItemMeta itemMeta = itemStack.getItemMeta();
					itemMeta.setDisplayName("DROPPED" + System.currentTimeMillis() + i);
					itemStack.setItemMeta(itemMeta);
					Item gold = _block.getWorld().dropItem(_block.getLocation().add(0.5, 1.5, 0.5), itemStack);
					_items.add(gold);

					gold.setVelocity(new Vector((Math.random()-0.5)*0.3, Math.random()-0.4, (Math.random()-0.5)*0.3));
				}
				_nuggets = !_nuggets;
			}
		}
		if (tick)
		{
			UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.GOLD_BLOCK,
					(byte) 0), _player.getLocation().add(0, 0.5, 0), 0.1f, 0.1f, 0.1f, 0.3f, 1, UtilParticle.ViewDist.LONG);
			cleanItems(false);
		}
		return solidify;
	}

	public void performRightClick(Player clicked, Block block)
	{
		if (_block == null)
			return;

		if (!block.equals(_block))
			return;

		if (clicked.equals(_player))
			return;

		unsolidifyPlayer();

		Recharge.Instance.use(_player, _gadget.getName(), COOLDOWN, false, false, "Cosmetics");

		boolean shards = UtilMath.random.nextBoolean();
		if (shards)
		{
			_manager.getDonationManager().rewardCurrency(GlobalCurrency.TREASURE_SHARD, clicked, _gadget.getName() + " Gold Pot Pickup Shards", SHARDS);
			Bukkit.broadcastMessage(F.main("Gold Pot", F.name(clicked.getName()) + " found a gold pot worth " + F.currency(GlobalCurrency.TREASURE_SHARD, SHARDS) + "!"));
		} else
		{
			_manager.getDonationManager().rewardCurrency(GlobalCurrency.GEM, clicked, _gadget.getName() + " Gold Pot Pickup Gems", GEMS);
			Bukkit.broadcastMessage(F.main("Gold Pot", F.name(clicked.getName()) + " found a gold pot worth " + F.currency(GlobalCurrency.GEM, GEMS) + "!"));
		}
	}

	public HashSet<Item> getItems()
	{
		return _items;
	}

	public void cleanItems(boolean force)
	{
		Iterator<Item> it = _items.iterator();
		while (it.hasNext())
		{
			Item item = it.next();
			if (item.getTicksLived() >= 20 || force)
			{
				item.remove();
				it.remove();
			}
		}
	}

	public boolean isSolid()
	{
		return _solid;
	}

}