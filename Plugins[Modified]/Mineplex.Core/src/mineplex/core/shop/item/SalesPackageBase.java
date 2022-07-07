package mineplex.core.shop.item;


import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.donation.repository.GameSalesPackageToken;
import mineplex.core.itemstack.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class SalesPackageBase implements ICurrencyPackage, IDisplayPackage
{
	private Material _displayMaterial;
	private byte _displayData;
	
	protected String Name;
	protected String DisplayName;
	protected String[] Description;
	protected int Quantity;
	
	protected int SalesPackageId;
	protected boolean Free;
	protected NautHashMap<GlobalCurrency, Integer> CurrencyCostMap;
	protected boolean KnownPackage = true;
	protected boolean OneTimePurchaseOnly = true;
	
	public SalesPackageBase(String name, Material material, String...description)
	{
		this(name, material, (byte)0, description);
	}
	
	public SalesPackageBase(String name, Material material, byte displayData, String[] description)
	{
		this(name, material, displayData, description, 0);
	}
	
	public SalesPackageBase(String name, Material material, byte displayData, String[] description, int coins)
	{
		this(name, material, displayData, description, coins, 1);
	}

	public SalesPackageBase(String name, Material material, byte displayData, String[] description, int coins, int quantity)
	{
		CurrencyCostMap = new NautHashMap<>();
		
		Name = name;
		DisplayName = name;
		Description = description;
		_displayMaterial = material;
		_displayData = displayData;
		
		CurrencyCostMap.put(GlobalCurrency.TREASURE_SHARD, coins);
		Quantity = quantity;
	}
	
	@Override
	public String getName()
	{
		return Name;
	}
	
	@Override
	public String[] getDescription()
	{
		return Description;
	}
	
	@Override
	public int getCost(GlobalCurrency currencyType)
	{		
		return CurrencyCostMap.containsKey(currencyType) ? CurrencyCostMap.get(currencyType) : 0;
	}

	@Override
	public int getSalesPackageId()
	{
		return SalesPackageId;
	}

	@Override
	public boolean isFree()
	{
		return Free;
	}
	
	@Override
	public Material getDisplayMaterial()
	{
		return _displayMaterial;
	}
	
	@Override
	public byte getDisplayData()
	{
		return _displayData;
	}
	
	@Override
	public void update(GameSalesPackageToken token)
	{
		SalesPackageId = token.GameSalesPackageId;
		Free = token.Free;
		
		if (token.Gems > 0)
		{
			CurrencyCostMap.put(GlobalCurrency.GEM, token.Gems);
		}
	}
	
	public int getQuantity()
	{
		return Quantity;
	}

	public boolean isKnown()
	{
		return KnownPackage;
	}

	public boolean oneTimePurchase()
	{
		return OneTimePurchaseOnly;
	}

	public String getDisplayName()
	{
		return DisplayName;
	}
	
	public void setDisplayName(String name)
	{
		DisplayName = name;
	}

	public ItemStack buildIcon()
	{
		return new ItemBuilder(_displayMaterial).setData(_displayData).setTitle(DisplayName).addLore(Description).build();
	}
}
