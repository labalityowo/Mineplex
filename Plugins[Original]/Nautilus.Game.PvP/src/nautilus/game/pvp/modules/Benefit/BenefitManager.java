package nautilus.game.pvp.modules.Benefit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nautilus.game.pvp.modules.Benefit.Items.BenefitItem;
import nautilus.game.pvp.modules.Benefit.Items.CoinPack;
import nautilus.game.pvp.modules.Benefit.Items.EnergizedPickaxe;
import nautilus.game.pvp.modules.Benefit.Repository.BenefitItemToken;
import nautilus.game.pvp.modules.Benefit.Repository.BenefitRepository;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.energy.Energy;
import mineplex.core.Rank;
import mineplex.minecraft.account.GetClientEvent;
import mineplex.minecraft.donation.repository.GameSalesPackageToken;

public class BenefitManager extends MiniPlugin
{
	private Energy _energy;
	private NautHashMap<String, BenefitItem> _itemMap;
	private NautHashMap<String, List<Player>> _itemPlayerMap;
	public BenefitRepository Repository;
	
	public BenefitManager(JavaPlugin plugin, String webServerAddress, Energy energy)
	{
		super("Benefit Manager", plugin);
		
		Repository = new BenefitRepository(webServerAddress);
		_energy = energy;
		_itemMap = new NautHashMap<String, BenefitItem>();
		_itemPlayerMap = new NautHashMap<String, List<Player>>();
		
		PopulateFactory();
	}
	
	public boolean PlayerOwnsMe(String name, Player player)
	{
		return _itemPlayerMap.get(name).contains(player);
	}
	
	private void PopulateFactory()
	{
		AddBenefitItem(new EnergizedPickaxe(this, _energy, Material.DIAMOND_PICKAXE), true);
		AddBenefitItem(new CoinPack(this, "50k Coin Pack", Material.GOLD_INGOT, 50000), false);
		AddBenefitItem(new CoinPack(this, "250k Coin Pack", Material.GOLD_BLOCK, 250000), false);
		
		List<BenefitItemToken> itemTokens = new ArrayList<BenefitItemToken>();
		
		for (BenefitItem item : _itemMap.values())
		{
			BenefitItemToken itemToken = new BenefitItemToken();
			itemToken.Name = item.getName();
			itemToken.Material = item.GetDisplayMaterial().toString();
			itemToken.SalesPackage = new GameSalesPackageToken();
			
			itemTokens.add(itemToken);
		}

		for (BenefitItemToken itemToken : Repository.GetBenefitItems(itemTokens))
		{
			if (_itemMap.containsKey(itemToken.Name))
			{
				_itemMap.get(itemToken.Name).Update(itemToken.SalesPackage);
			}
		}
	}
	
    private void AddBenefitItem(BenefitItem benefitItem, boolean addToOwnerList)
	{
    	_itemMap.put(benefitItem.getName(), benefitItem);
    	
    	if (addToOwnerList)
    	{
    		_itemPlayerMap.put(benefitItem.getName(), new ArrayList<Player>());
    	}
    	
	}

	@EventHandler(priority = EventPriority.HIGHEST)
    public void Login(PlayerLoginEvent event)
    {
    	GetClientEvent clientEvent = new GetClientEvent(event.getPlayer());
    	
    	GetPluginManager().callEvent(clientEvent);
    	
    	if (clientEvent.GetClient().Rank().Has(Rank.EMERALD, false))
    	{
    		_itemPlayerMap.get("Energized Pickaxe").add(event.getPlayer());
    	}
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void Quit(PlayerQuitEvent event)
    {
    	_itemPlayerMap.get("Energized Pickaxe").remove(event.getPlayer());
    }
	
	public Collection<BenefitItem> GetBenefitItems()
	{
		return _itemMap.values();
	}
}
