package mineplex.minecraft.game.classcombat.item;

import mineplex.core.common.util.UtilGear;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Item implements IItem, Listener
{

	protected final ItemFactory Factory;

	private final Material _type;
	private final String _name;
	private final String[] _desc;
	private final int _amount;
	private boolean _free;
	private final int _gemCost;
	private final boolean _canDamage;
	
	private int _tokenCost;

	public Item(ItemFactory factory, String name, String[] desc, Material type, int amount, boolean canDamage, int gemCost, int tokenCost)
	{
		Factory = factory;
		_name = name;
		_desc = desc;
		_type = type;
		_amount = amount;
		_gemCost = gemCost;
		_tokenCost = tokenCost;
		_canDamage = canDamage;
	}

	@Override
	public Material GetType()
	{
		return _type;
	}

	@Override
	public int GetAmount()
	{
		return _amount;
	}

	@Override
	public int GetGemCost()
	{
		return _gemCost;
	}

	@Override
	public String GetName()
	{
		return _name;
	}
	
	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;
		
		if (!UtilGear.isMat(damager.getItemInHand(), GetType()))
			return;
		
		if (!_canDamage)
			event.SetCancelled("Item Damage Cancel");
	}

	@Override
	public String[] GetDesc() 
	{
		return _desc;
	}
	
	public boolean isFree()
	{
		return _free;
	}
	
	public void setFree(boolean free)
	{
		_free = free;
	}
	
	public int getTokenCost()
	{
		return _tokenCost;
	}
}
