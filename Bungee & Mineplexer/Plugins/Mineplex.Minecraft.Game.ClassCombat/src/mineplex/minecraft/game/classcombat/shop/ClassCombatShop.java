package mineplex.minecraft.game.classcombat.shop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.shop.event.OpenClassShopEvent;
import mineplex.minecraft.game.classcombat.shop.page.CustomBuildPage;

public class ClassCombatShop extends ShopBase<ClassShopManager>
{

	private final NautHashMap<String, ItemStack[]> _playerInventoryMap = new NautHashMap<>();
	private final NautHashMap<String, ItemStack[]> _playerArmorMap = new NautHashMap<>();
	private final GameKit _gameKit;
	private IPvpClass _gameClass;
	private boolean _takeAwayStuff;
	private boolean _skillsOnly;
	private boolean _hub;

	public ClassCombatShop(ClassShopManager plugin, CoreClientManager clientManager, DonationManager donationManager, boolean skillsOnly, String name)
	{
		super(plugin, clientManager, donationManager, name);

		_gameKit = null;
		_skillsOnly = skillsOnly;
	}

	public ClassCombatShop(ClassShopManager plugin, CoreClientManager clientManager, DonationManager donationManager, boolean skillsOnly, String name, GameKit gameKit, IPvpClass iPvpClass, boolean hub)
	{
		super(plugin, clientManager, donationManager, name);
		_gameKit = gameKit;
		_gameClass = iPvpClass;
		_takeAwayStuff = true;
		_skillsOnly = skillsOnly;
		_hub = hub;
	}

	protected void openShopForPlayer(Player player)
	{
		OpenClassShopEvent event = new OpenClassShopEvent(player);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (_gameClass != null)
			getPlugin().GetClassManager().Get(player).SetGameClass(_gameClass);

		if (_takeAwayStuff)
		{
			_playerInventoryMap.put(player.getName(), player.getInventory().getContents());
			_playerArmorMap.put(player.getName(), player.getInventory().getArmorContents());
		}
	}

	public boolean attemptShopOpen(Player player)
	{
		if (!getOpenedShop().contains(player.getUniqueId()))
		{
			if (!canOpenShop(player))
				return false;

			ShopPageBase<ClassShopManager, ClassCombatShop> page;

			if (!_hub)
			{
				if (getPlugin().GetClassManager().Get(player).GetGameClass() == null)
				{
					// page = new ArmorPage(_plugin, this, _clientManager, _donationManager, player);
					UtilPlayer.message(player, F.main(getPlugin().getName(), ChatColor.RED + "You need to have an armor set on to modify class builds."));
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
					return false;
				}
				else
					page = new CustomBuildPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
			}
			else
			{
				page = new CustomBuildPage(getPlugin(), this, getClientManager(), getDonationManager(), player, _gameClass);
			}

			getOpenedShop().add(player.getUniqueId());
			openShopForPlayer(player);

			if (!getPlayerPageMap().containsKey(player.getUniqueId()))
			{
				getPlayerPageMap().put(player.getUniqueId(), page);
			}

			openPageForPlayer(player, page);

			return true;
		}

		return false;
	}

	@Override
	protected void closeShopForPlayer(Player player)
	{
		ClientClass clientClass = getPlugin().GetClassManager().Get(player);

		if (clientClass != null && clientClass.IsSavingCustomBuild())
		{
			CustomBuildToken customBuild = clientClass.GetSavingCustomBuild();
			clientClass.SaveActiveCustomBuild();
			clientClass.SetActiveCustomBuild(clientClass.GetGameClass(), customBuild);
			clientClass.EquipCustomBuild(customBuild, false, _skillsOnly);
		}

		if (_takeAwayStuff)
		{
			player.getInventory().clear();

			player.getInventory().setContents(_playerInventoryMap.remove(player.getName()));
			player.getInventory().setArmorContents(_playerArmorMap.remove(player.getName()));

			((CraftPlayer) player).getHandle().updateInventory(((CraftPlayer) player).getHandle().defaultContainer);
		}
	}

	@Override
	protected ShopPageBase<ClassShopManager, ? extends ShopBase<ClassShopManager>> buildPagesFor(Player player)
	{
		return new CustomBuildPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	@EventHandler
	public void clearPlayerFromMaps(PlayerQuitEvent event)
	{
		_playerInventoryMap.remove(event.getPlayer().getName());
		_playerArmorMap.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void npcInteract(NPCInteractEvent event)
	{
		if (_gameKit == null || !event.getNpc().getMetadata().equals("KIT_" + _gameKit.getId()))
		{
			return;
		}

		attemptShopOpen(event.getPlayer());
	}

	public boolean skillOnly()
	{
		return _skillsOnly;
	}
}
