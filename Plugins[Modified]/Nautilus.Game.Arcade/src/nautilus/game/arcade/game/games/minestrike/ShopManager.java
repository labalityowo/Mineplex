package nautilus.game.arcade.game.games.minestrike;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItem;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItemType;
import nautilus.game.arcade.game.games.minestrike.items.equipment.DefusalKit;
import nautilus.game.arcade.game.games.minestrike.items.equipment.armor.Armor;
import nautilus.game.arcade.game.games.minestrike.items.equipment.armor.Helmet;
import nautilus.game.arcade.game.games.minestrike.items.equipment.armor.Kevlar;
import nautilus.game.arcade.game.games.minestrike.items.grenades.FlashBang;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Grenade;
import nautilus.game.arcade.game.games.minestrike.items.grenades.HighExplosive;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Incendiary;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Molotov;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Smoke;
import nautilus.game.arcade.game.games.minestrike.items.guns.Gun;
import nautilus.game.arcade.game.games.minestrike.items.guns.GunStats;
import nautilus.game.arcade.game.games.minestrike.items.guns.Shotgun;

public class ShopManager
{
	
	private static final int MAX_GRENADES_PER_ROUND = 4;
	
	private Minestrike Host;

	private HashMap<Player, HashMap<Integer, StrikeItem>> _shop = new HashMap<Player, HashMap<Integer, StrikeItem>>();
	private HashMap<Player, Integer> _money = new HashMap<Player, Integer>();
	private HashSet<Player> _inShop = new HashSet<Player>();
	
	private Map<UUID, Integer> _boughtGrenades = new HashMap<>();
	
	private boolean _disabled;

	public ShopManager(Minestrike minestrike)
	{
		Host = minestrike;
	}

	public void enterShop(Player player)
	{		
		if (_disabled)
			return;
		
		GameTeam team = Host.GetTeam(player);
		if (team == null)
			return;

		clearShopInventory(player);

		_shop.put(player, new HashMap<Integer, StrikeItem>());

		int slot;

		//Pistols
		slot = 9;
		addItem(team.GetColor() == ChatColor.RED ? new Gun(GunStats.GLOCK_18, Host.getGunModule()) : new Gun(GunStats.P2000, Host.getGunModule()), player, slot++);
		addItem(new Gun(GunStats.P250, Host.getGunModule()), player, slot++);
		addItem(new Gun(GunStats.CZ75, Host.getGunModule()), player, slot++);
		addItem(new Gun(GunStats.DEAGLE, Host.getGunModule()), player, slot++);

		//Shotgun
		slot = 18;
		addItem(new Shotgun(GunStats.NOVA, Host.getGunModule()), player, slot++);
		addItem(new Shotgun(GunStats.XM1014, Host.getGunModule()), player, slot++);
		
		//SMG
		addItem(new Gun(GunStats.PPBIZON, Host.getGunModule()), player, slot++);
		addItem(new Gun(GunStats.P90, Host.getGunModule()), player, slot++);

		//Rifles
		slot = 27;
		addItem(team.GetColor() == ChatColor.RED ? new Gun(GunStats.GALIL, Host.getGunModule()) : new Gun(GunStats.FAMAS, Host.getGunModule()), player, slot++);
		addItem(team.GetColor() == ChatColor.RED ? new Gun(GunStats.AK47, Host.getGunModule()) : new Gun(GunStats.M4A4, Host.getGunModule()), player, slot++);
		addItem(team.GetColor() == ChatColor.RED ? new Gun(GunStats.SG553, Host.getGunModule()) : new Gun(GunStats.AUG, Host.getGunModule()), player, slot++);
		addItem(new Gun(GunStats.SSG08, Host.getGunModule()), player, slot++);	
		addItem(new Gun(GunStats.AWP, Host.getGunModule()), player, slot++);

		//Grenades
		addItem(new FlashBang(), player, 14);
		addItem(new HighExplosive(), player, 15);
		addItem(new Smoke(), player, 16);
		addItem(team.GetColor() == ChatColor.RED ? new Molotov() : new Incendiary(), player, 17);

		//Gear
		if (team.GetColor() == ChatColor.AQUA)
			addItem(new DefusalKit(), player, 26);

		//Equipment
		addItem(new Helmet(), player, 34);
		addItem(new Kevlar(), player, 35);

		_inShop.add(player);
	}

	public void addItem(StrikeItem item, Player player, int slot)
	{
		player.getInventory().setItem(slot, item.getShopItem(getMoney(player), hasItem(player, item)));
		_shop.get(player).put(slot, item);
	}

	public boolean hasItem(Player player, StrikeItem item)
	{
		int count = 0;

		for (int i=0 ; i<9 ; i++)
		{
			if (UtilGear.isMatAndData(player.getInventory().getItem(i), item.getSkinMaterial(), item.getSkinData()))
				count++;
			
			if (UtilGear.isMatAndData(player.getInventory().getHelmet(), item.getSkinMaterial(), item.getSkinData()))
				count++;
			
			if (UtilGear.isMatAndData(player.getInventory().getChestplate(), item.getSkinMaterial(), item.getSkinData()))
				count++;
		}
		
		if (count > 0)
		{
			if (item.getType() == StrikeItemType.PRIMARY_WEAPON ||
				item.getType() == StrikeItemType.SECONDARY_WEAPON ||
				item.getType() == StrikeItemType.EQUIPMENT)
					return true;
			
			if (item instanceof Grenade)
			{
				Grenade grenade = (Grenade)item;
				
				if (!grenade.canGiveToPlayer(player))
					return true;
			}
		}
		
		if (item instanceof Kevlar)
		{
			if (Armor.isArmor(player.getInventory().getChestplate()))
			{
				return true;
			}			
		}
		
		if (item instanceof Helmet)
		{
			if (Armor.isArmor(player.getInventory().getHelmet()))
				return true;
		}
		
		return false;
	}
	
	public void clearShopInventory(Player player)
	{
		_shop.remove(player);

		for (int i=9 ; i<36 ; i++)
			player.getInventory().setItem(i, null);
	}

	public int getMoney(Player player)
	{
		if (_disabled)
			return 0;
		
		if (!_money.containsKey(player))
			_money.put(player, 800);

		return _money.get(player);
	}

	public void addMoney(Player player, int amount, String reason)
	{
		if (_disabled)
			return;
		
		_money.put(player, Math.min(16000, getMoney(player) + amount));

		UtilPlayer.message(player, F.main("Game", "Received " + F.elem(C.cDGreen + "$" + amount) + " for " + reason + "."));
	}

	public void inventoryClick(InventoryClickEvent event)
	{
		if (_disabled)
			return;

		if (!(event.getClickedInventory() instanceof PlayerInventory))
			return;

		event.setCancelled(true);

		Player player = UtilPlayer.searchExact(event.getWhoClicked().getName());
		if (player == null)
			return;

		GameTeam team = Host.GetTeam(player);
		if (team == null)
			return;

		if (!_shop.containsKey(player))
			return;

		if (!_shop.get(player).containsKey(event.getSlot()))
			return;

		//Prevent accidentally buying multi
		if (!Recharge.Instance.use(player, "Shop Purchase", 120, false, false))
			return;

		StrikeItem item = _shop.get(player).get(event.getSlot());

		if (item == null)
			return;

		if (hasItem(player, item))
			return;
		

		if (getMoney(player) < item.getCost())
		{
			player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 1f);
			return;
		}

		//Gun
		if (item instanceof Gun)
		{
			Gun gun = (Gun)item;
			Host.getGunModule().dropSlotItem(player, gun.getSlot());
			gun.giveToPlayer(player, true);
			gun.updateWeaponName(player, Host.getGunModule());
			gun.updateSkin(player, Host.getArcadeManager().getCosmeticManager().getGadgetManager());
			Host.getGunModule().registerGun(gun, player);
		}

		//Grenade
		else if (item instanceof Grenade)
		{
			Grenade grenade = (Grenade)item;
			UUID key = player.getUniqueId();
			
			_boughtGrenades.putIfAbsent(key, 0);
			
			if (_boughtGrenades.get(key) > MAX_GRENADES_PER_ROUND)
			{
				player.sendMessage(F.main("Game", "You have purchased the maximum amount of grenades!"));
				return;
			}
			
			if (!grenade.giveToPlayer(player, true))
			{
				player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 1f);
				return;
			}

			_boughtGrenades.put(key, _boughtGrenades.get(key) + 1);
			Host.getGunModule().registerGrenade(grenade, player);
		}

		//Use 250 instead of 255, to show that its kevlar/helmet
		else if (item instanceof Helmet)
		{
			((Helmet)item).giveToPlayer(player, (team.GetColor() == ChatColor.RED) ? Color.fromRGB(250, 75, 75) : Color.fromRGB(125, 200, 250));
		}

		else if (item instanceof Kevlar)
		{
			((Kevlar)item).giveToPlayer(player, (team.GetColor() == ChatColor.RED) ? Color.fromRGB(250, 75, 75) : Color.fromRGB(125, 200, 250));
		}

		else if (item instanceof DefusalKit)
		{
			item.giveToPlayer(player, 8, false);
		}

		_money.put(player, getMoney(player) - item.getCost());

		enterShop(player);
	}

	public void leaveShop(Player player, boolean showShopItem, boolean wipeMoney)
	{
		if (_disabled)
			return;
		
		_shop.remove(player);

		_inShop.remove(player);
		clearShopInventory(player);

		if (wipeMoney)
			_money.remove(player);

		if (showShopItem)
		{
			player.getInventory().setItem(22, 
					ItemStackFactory.Instance.CreateStack(Material.PAPER, (byte)0, 1, C.cRed + "Cannot Purchase Gear", 
							new String[] 
									{
						C.cWhite + "",
						C.cWhite + "You can only purchase gear when",
						C.cWhite + "you are near your spawn point in",
						C.cWhite + "the first 45 seconds of the round!",
									}));
		}
	}

	public boolean isBuyTime()
	{
		return !UtilTime.elapsed(Host.GetStateTime(), 45000) && Host.InProgress();
	}

	public void update()
	{	
		if (_disabled)
			return;
		
		for (Player player : Host.GetPlayers(false))
		{
			GameTeam team = Host.GetTeam(player);

			if (team == null)
			{
				leaveShop(player, false, false);
				continue;
			}

			//Near Shop?
			boolean nearShop = false;
			for (Location loc : team.GetSpawns())
			{
				if (UtilMath.offset(player.getLocation(), loc) < 5)
				{
					nearShop = true;
					break;
				}
			}

			//Leave Shop
			if (_inShop.contains(player) && (!nearShop || !isBuyTime()) || Host.Manager.isSpectator(player) || player.getAllowFlight())
			{	
				leaveShop(player, true, false);
			}
			//Enter Shop
			else if (!_inShop.contains(player) && (nearShop && isBuyTime()) && !Host.Manager.isSpectator(player) && !player.getAllowFlight())
			{
				enterShop(player);
			}
		}
	}
	
	public void setDisabled(boolean disabled)
	{
		_disabled = disabled;
	}
	
	public void resetGrenades()
	{
		_boughtGrenades.clear();
	}

	public boolean isDisabled()
	{
		return _disabled;
	}
}
