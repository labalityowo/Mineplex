package nautilus.game.pvp.modules;

import me.chiss.Core.Module.AModule;
import me.chiss.Core.Plugin.IPlugin;
import me.chiss.Core.PvpShop.PvpShopFactory;
import me.chiss.Core.Shop.PvpBuildShop;
import me.chiss.Core.Shop.PvpDonatorShop;
import me.chiss.Core.Shop.PvpItemShop;
import mineplex.core.CurrencyType;
import mineplex.core.MiniPlugin;
import mineplex.core.server.RemoteRepository;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilEvent.ActionType;
import nautilus.game.pvp.modules.Benefit.BenefitManager;
import nautilus.game.pvp.modules.Benefit.BenefitShop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopManager extends MiniPlugin
{
	private PvpDonatorShop _donatorShop;
	private PvpBuildShop _buildShop;
	private PvpItemShop _combatShop;
	private PvpItemShop _blockShop;
	private PvpItemShop _itemShop;
	
	public ShopManager(JavaPlugin plugin, RemoteRepository _repository, BenefitManager benefitManager) 
	{
		super("Shop Manager", plugin);

		_donatorShop = new PvpDonatorShop((IPlugin)plugin, _repository, Clients(), Classes(), Skills());
		_buildShop = new PvpBuildShop((IPlugin)plugin, _repository, Clients(), Classes(), Skills());
		
		_combatShop = new PvpItemShop((IPlugin)plugin, _repository, Clients(), new PvpShopFactory(plugin, _repository, "pvp_shop_combat.dat"), "§4Blacksmith");
		_blockShop = new PvpItemShop((IPlugin)plugin, _repository, Clients(), new PvpShopFactory(plugin, _repository, "pvp_shop_block.dat"), "§4Miner");
		_itemShop = new PvpItemShop((IPlugin)plugin, _repository, Clients(), new PvpShopFactory(plugin, _repository, "pvp_shop_item.dat"), "§4Merchant");
		new BenefitShop(benefitManager, "§4Benefactor", CurrencyType.Gems);
	}

	@Override
	public void enable() 
	{

	}

	@Override
	public void disable() 
	{

	}

	@Override
	public void config()
	{

	}

	@Override
	public void commands() 
	{
		addCommand("bank");
		addCommand("coins");
		addCommand("balance");
	}

	@Override
	public void command(Player caller, String cmd, String[] args)
	{
		if (cmd.equals("bank") || cmd.equals("coins") || cmd.equals("balance"))
		{
			UtilPlayer.message(caller, F.main("Economy", "You have " + F.elem(Clients().Get(caller).Game().GetEconomyBalance() + " Coins") + "."));
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!Util().Event().isAction(event, ActionType.R_BLOCK))
			return;

		if (_donatorShop.ShouldOpenShop(event.getClickedBlock()))
		{
			_donatorShop.OpenShopForPlayer(player);
			event.setCancelled(true);
		}
		
		if (Clans().CUtil().isSafe(event.getPlayer()))
		{
			if (_itemShop.ShouldOpenShop(event.getClickedBlock()))
			{
				_itemShop.OpenShopForPlayer(player);
				event.setCancelled(true);
			}
			
			if (_combatShop.ShouldOpenShop(event.getClickedBlock()))
			{
				_combatShop.OpenShopForPlayer(player);
				event.setCancelled(true);
			}
			
			if (_blockShop.ShouldOpenShop(event.getClickedBlock()))
			{
				_blockShop.OpenShopForPlayer(player);
				event.setCancelled(true);
			}
		}
		
		if (_buildShop.ShouldOpenShop(event.getClickedBlock()))
		{
			if (Clients().Get(player).Class().GetGameClass() == null)
			{
				UtilPlayer.message(player, F.main("Class Setup", "You do not have a Class."));
				return;
			}
			
			_buildShop.OpenShopForPlayer(player);
			event.setCancelled(true);
		}
	}
}
