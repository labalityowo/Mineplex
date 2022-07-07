package nautilus.game.arcade.game.games.gladiators.hotbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.modules.Module;

/**
 * Created by William (WilliamTiger).
 * 18/12/15
 */
public class HotbarEditor extends Module
{
	public static final String HOTBAR_DATA_KEY = GameDisplay.Gladiators.getCustomDataKeyName() + "hotbar";

	private final ItemStack _item;
	private final Listener _pageListener;

	public HotbarEditor()
	{
		_item = new ItemBuilder(Material.NAME_TAG).setTitle(C.cGold + "Hotbar Editor")
				.addLore(C.cGray + "Right click to edit your Gladiators hotbar").build();

		_pageListener = new HotbarPageListener(this);
		UtilServer.RegisterEvents(_pageListener);
	}

	@Override
	public void cleanup()
	{
		UtilServer.Unregister(_pageListener);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (getGame().Manager.GetGame() != getGame())
			return;

		if (getGame().GetState() == Game.GameState.Recruit || getGame().GetState() == Game.GameState.Live)
		{
			event.getPlayer().getInventory().setItem(0, _item);
		}
	}

	@EventHandler
	public void onDeath(final PlayerDeathEvent event)
	{
		getGame().getArcadeManager().runSyncLater(() ->
		{
			if (getGame().IsLive())
			{
				event.getEntity().getInventory().setItem(0, _item);
			}
		}, 1);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Observer(PlayerCommandPreprocessEvent event)
	{
		if (getGame() == null || getGame().InProgress())
		{
			return;
		}

		if (event.getMessage().equalsIgnoreCase("/spec"))
		{
			if (!getGame().IsAlive(event.getPlayer())
					&& !UtilInv.contains(event.getPlayer(), _item.getType(), (byte) 0, 1))
			{
				event.getPlayer().getInventory().setItem(0, _item);
			}
		}
	}

	@EventHandler
	public void onJoin(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.Recruit)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				player.getInventory().setItem(0, _item);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.PHYSICAL && event.getAction().name().contains("RIGHT"))
		{
			ItemStack item = event.getItem();

			if (UtilItem.isSimilar(item, _item, UtilItem.ItemAttribute.NAME))
			{
				event.setCancelled(true);
				HotbarInventory.open(event.getPlayer(), this);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event)
	{
		ItemStack item = event.getPlayer().getItemInHand();

		if (UtilItem.isSimilar(item, _item, UtilItem.ItemAttribute.NAME))
		{
			event.setCancelled(true);
			HotbarInventory.open(event.getPlayer(), this);
		}
	}

	public HotbarLayout getLayout(Player player)
	{
		int data = getGame().Manager.getCustomDataManager().getData(player, HOTBAR_DATA_KEY);
		data = (data == -1 ? 1239 : data);

		List<Integer> ints = new ArrayList<>();
		for (int i : UtilMath.digits(data))
			ints.add(i - 1);
		ints = Lists.reverse(ints);

		return new HotbarLayout(ints.get(0), ints.get(1), ints.get(2), ints.get(3));
	}

	public void saveLayout(Player player, Inventory inv)
	{
		List<ItemStack> items = Arrays.asList(inv.getContents());
		ItemStack sword = null, rod = null, bow = null, arrows = null;
		for (ItemStack i : items)
		{
			if (i == null || i.getType() == null)
				continue;

			if (i.getType().equals(Material.DIAMOND_SWORD))
				sword = i;
			else if (i.getType().equals(Material.FISHING_ROD))
				rod = i;
			else if (i.getType().equals(Material.BOW))
				bow = i;
			else if (i.getType().equals(Material.ARROW))
				arrows = i;
		}

		HotbarLayout save = new HotbarLayout(
				items.indexOf(sword) - 9,
				items.indexOf(rod) - 9,
				items.indexOf(bow) - 9,
				items.indexOf(arrows) - 9
		);

		if (save.getArrows() > 8 || save.getArrows() < 0)
		{
			save.setArrows(save.getEmpty());
		}
		if (save.getBow() > 8 || save.getBow() < 0)
		{
			save.setBow(save.getEmpty());
		}
		if (save.getSword() > 8 || save.getSword() < 0)
		{
			save.setSword(save.getEmpty());
		}
		if (save.getRod() > 8 || save.getRod() < 0)
		{
			save.setRod(save.getEmpty());
		}

		getGame().Manager.getCustomDataManager().Get(player).put(HOTBAR_DATA_KEY, save.toDataSaveNumber());
		getGame().Manager.getCustomDataManager().saveData(player);
		player.sendMessage(F.main("Game", "Saved new hotbar layout!"));
	}
}
