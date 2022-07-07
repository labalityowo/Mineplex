package nautilus.game.minekart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nautilus.game.minekart.kart.KartType;
import nautilus.game.minekart.repository.KartItemToken;
import nautilus.game.minekart.repository.KartRepository;
import nautilus.game.minekart.shop.KartItem;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.donation.repository.GameSalesPackageToken;

public class KartFactory extends MiniPlugin
{
	private KartRepository _repository;
	private NautHashMap<String, KartItem> _karts;
	private List<KartItem> _sortedKarts;
	
	public KartFactory(JavaPlugin plugin, KartRepository repository)
	{
		super("Kart Factory", plugin);
		
		_repository = repository;
		_karts = new NautHashMap<String, KartItem>();
		
		PopulateKarts();
	}
	
	public Collection<KartItem> GetKarts()
	{
		return _sortedKarts;
	}
	
	private void PopulateKarts()
	{
		_karts.put("Chicken", new KartItem(Material.FEATHER, KartType.Chicken));
		_karts.put("Sheep", new KartItem(Material.WHEAT, KartType.Sheep));
		_karts.put("Cow", new KartItem(Material.MILK_BUCKET, KartType.Cow));
		_karts.put("Pig", new KartItem(Material.GRILLED_PORK, KartType.Pig));
		_karts.put("Spider", new KartItem(Material.STRING, KartType.Spider));
		_karts.put("Wolf", new KartItem(Material.SUGAR, KartType.Wolf));
		_karts.put("Enderman", new KartItem(Material.FIREBALL, KartType.Enderman));
		_karts.put("Blaze", new KartItem(Material.BLAZE_ROD, KartType.Blaze));
		_karts.put("Golem", new KartItem(Material.IRON_INGOT, KartType.Golem));
		
		List<KartItemToken> itemTokens = new ArrayList<KartItemToken>();
		
		for (KartItem item : _karts.values())
		{
			KartItemToken itemToken = new KartItemToken();
			itemToken.Name = item.GetName();
			itemToken.Material = item.GetDisplayMaterial().toString();
			itemToken.Data = item.GetDisplayData() + "";
			itemToken.SalesPackage = new GameSalesPackageToken();
			
			itemTokens.add(itemToken);
		}

		for (KartItemToken itemToken : _repository.GetKartItems(itemTokens))
		{
			if (_karts.containsKey(itemToken.Name))
			{
				_karts.get(itemToken.Name).Update(itemToken.SalesPackage);
			}
		}
		
		_sortedKarts = new ArrayList<KartItem>(_karts.values());
		
		Collections.sort(_sortedKarts, new Comparator<KartItem>()
		{
			@Override
			public int compare(KartItem kartItem1, KartItem kartItem2)
			{
				if (kartItem1.GetKartType().GetStability() < kartItem2.GetKartType().GetStability())
					return -1;
				
				if (kartItem1.GetKartType().GetStability() == kartItem2.GetKartType().GetStability())
					return 0;
				
				return 1;
			}
		});
	}
}
