package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import mineplex.core.common.util.*;
import mineplex.core.recharge.Recharge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.skin.SkinData;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.SantaPresent;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

public class MorphSanta extends MorphGadget
{

	private HashMap<Item, SantaPresent> _items = new HashMap<>();
	// For some reason, present.getTicksLived() is not doing the right job here
  	private HashMap<Item, Long> _spawnTime = new HashMap<>();

	private static final int SHARD_CHARGE = 50;

	public MorphSanta(GadgetManager manager)
	{
		super(manager, "Santa Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "We're all Santa now this Holiday Season!",
				"",
				C.cWhite + "Left click to deliver a random gift for players who have been Naughty or Nice!",
				"",
				C.cRedB + "WARNING: " + ChatColor.RESET + "Delivering a gift uses shards!"
		}, LineFormat.LORE), -14, Material.STAINED_CLAY, (byte) 14);

		setPPCYearMonth(YearMonth.of(2016, Month.DECEMBER));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile profile = UtilGameProfile.getGameProfile(player);
		profile.getProperties().clear();
		profile.getProperties().put("textures", SkinData.SANTA.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, profile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void throwPresent(PlayerInteractEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;

		if (!UtilEvent.isAction(event, UtilEvent.ActionType.L))
			return;

		Player player = event.getPlayer();

		int type = 0;

		if (UtilMath.random(0.1, 1.1) > 0.76)
		{
			type = 1;
		}

		if (player.getItemInHand().getType() != Material.AIR)
			return;

		if (type == 0)
		{
			int shards = UtilMath.rRange(250, 500);

			if (Manager.getDonationManager().Get(player).getBalance(GlobalCurrency.TREASURE_SHARD) < shards + SHARD_CHARGE)
			{
				UtilPlayer.message(player, F.main("Gadget", "You do not have enough Shards."));
				return;
			}

			if (!Recharge.Instance.use(player, "Hide Gift", 30000, true, false, "Cosmetics"))
				return;

			Item present = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
					SkinData.PRESENT.getSkull("Hidden Present " + System.currentTimeMillis(), new ArrayList<>()));
			UtilAction.velocity(present, player.getLocation().getDirection(), 0.2, false, 0, 0.2, 1, false);

			Manager.getDonationManager().rewardCurrency(GlobalCurrency.TREASURE_SHARD, player, this.getName() + " Present Hide", -(shards + SHARD_CHARGE));

			present.setPickupDelay(40);

			_items.put(present, new SantaPresent(player.getName(), SantaPresent.PresentType.PRESENT, shards));
			_spawnTime.put(present, System.currentTimeMillis());

			//Announce
			Bukkit.broadcastMessage(C.cYellow + C.Bold + player.getName() +
					ChatColor.RESET + C.Bold + " hid a " +
					C.cRed + C.Bold + "Christmas Present" +
					ChatColor.RESET + C.Bold + " worth " +
					C.cRed + C.Bold + shards + " Shards");

			for (Player other : UtilServer.getPlayers())
				other.playSound(other.getLocation(), Sound.BLAZE_HIT, 1.5f, 1.5f);
		}
		else
		{
			if (!Recharge.Instance.use(player, "Hide Gift", 30000, true, false, "Cosmetics"))
				return;

			ItemStack coalStack = ItemStackFactory.Instance.CreateStack(Material.COAL, (byte)0, 1, "Hidden Coal" + System.currentTimeMillis());
			Item coal = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), coalStack);
			UtilAction.velocity(coal, player.getLocation().getDirection(), 0.2, false, 0, 0.2, 1, false);

			int coals = UtilMath.rRange(1, 3);

			coal.setPickupDelay(40);

			_items.put(coal, new SantaPresent(player.getName(), SantaPresent.PresentType.COAL, coals));
			_spawnTime.put(coal, System.currentTimeMillis());

			//Announce
			Bukkit.broadcastMessage(C.cYellow + C.Bold + player.getName() +
					ChatColor.RESET + C.Bold + " hid a " +
					C.cRed + C.Bold + "Christmas Coal" +
					ChatColor.RESET + C.Bold + " worth " +
					C.cRed + C.Bold + coals + " Coal Ammo");

			for (Player other : UtilServer.getPlayers())
				other.playSound(other.getLocation(), Sound.DIG_SNOW, 1.5f, 1.5f);
		}
	}

	@EventHandler
	public void presentPickup(PlayerPickupItemEvent event)
	{
		if (_items.containsKey(event.getItem()) && !_items.get(event.getItem()).getThrower().equals(event.getPlayer().getName()))
		{
			SantaPresent santaPresent = _items.get(event.getItem());

			_items.remove(event.getItem());
			_spawnTime.remove(event.getItem());

			event.setCancelled(true);
			event.getItem().remove();

			int presentsLeft = 0, coalsLeft = 0;
			for (SantaPresent present : _items.values())
			{
				if (present.getPresentType().equals(SantaPresent.PresentType.PRESENT))
				{
					presentsLeft++;
				}
				else if (present.getPresentType().equals(SantaPresent.PresentType.COAL))
				{
					coalsLeft++;
				}
			}

			if (santaPresent.getPresentType().equals(SantaPresent.PresentType.PRESENT))
			{
				Manager.getDonationManager().rewardCurrency(GlobalCurrency.TREASURE_SHARD, event.getPlayer(), getName() + " Present Pickup", santaPresent.getAmmo());

				//Announce
				Bukkit.broadcastMessage(C.cGold + C.Bold + event.getPlayer().getName() +
						ChatColor.RESET + C.Bold + " found a " +
						C.cGold + C.Bold + "Christmas Present" +
						ChatColor.RESET + C.Bold + "! " + presentsLeft + " Presents left!");
			}
			else if (santaPresent.getPresentType().equals(SantaPresent.PresentType.COAL))
			{
				// Gives coals
				Manager.getInventoryManager().addItemToInventory(event.getPlayer(), "Coal", santaPresent.getAmmo());

				//Announce
				Bukkit.broadcastMessage(C.cGold + C.Bold + event.getPlayer().getName() +
						ChatColor.RESET + C.Bold + " found a " +
						C.cGold + C.Bold + "Christmas Coal" +
						ChatColor.RESET + C.Bold + "! " + coalsLeft + " Coals left!");
			}

			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ORB_PICKUP, 1.5f, 0.75f);
			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ORB_PICKUP, 1.5f, 1.25f);

			UtilFirework.playFirework(event.getItem().getLocation(), FireworkEffect.Type.BURST, Color.RED, true, true);
		}
	}

	@EventHandler
	public void presentClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<Item> presentIter = _items.keySet().iterator();

		while (presentIter.hasNext())
		{
			Item presentItem = presentIter.next();

			if (!presentItem.isValid() || UtilTime.elapsed(_spawnTime.get(presentItem), 60 * 10000))
			{
				SantaPresent santaPresent = _items.get(presentItem);

				presentItem.remove();
				presentIter.remove();

				//Announce
				if (santaPresent.getPresentType().equals(SantaPresent.PresentType.PRESENT))
				{
					int presentsLeft = 0;
					for (SantaPresent present : _items.values())
					{
						if (present.getPresentType().equals(SantaPresent.PresentType.PRESENT))
						{
							presentsLeft++;
						}
					}
					Bukkit.broadcastMessage(
							ChatColor.RESET + C.Bold + "No one found a " +
									C.cGold + C.Bold + "Christmas Present" +
									ChatColor.RESET + C.Bold + "! " + presentsLeft + " Presents left!");
				}
				else if (santaPresent.getPresentType().equals(SantaPresent.PresentType.COAL))
				{
					int coalsLeft = 0;
					for (SantaPresent present : _items.values())
					{
						if (present.getPresentType().equals(SantaPresent.PresentType.COAL))
						{
							coalsLeft++;
						}
					}
					Bukkit.broadcastMessage(
							ChatColor.RESET + C.Bold + "No one found a " +
									C.cGold + C.Bold + "Christmas Coal" +
									ChatColor.RESET + C.Bold + "! " + coalsLeft + " Coals left!");
				}
			}
			else
			{
				UtilParticle.PlayParticle(UtilParticle.ParticleType.SNOW_SHOVEL, presentItem.getLocation().add(0, 0.1, 0), 0.1f, 0.1f, 0.1f, 0, 1,
						UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}

	@EventHandler
	public void cancelDespawn(ItemDespawnEvent event)
	{
		if (_items.containsKey(event.getEntity()))
			event.setCancelled(true);
	}
}
