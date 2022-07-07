package mineplex.game.clans.fields;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FieldBlockLootData 
{
	protected int id;
	protected byte data;
	protected int base;
	protected int bonus;
	protected int chance;
	
	public FieldBlockLootData(int idIn, byte dataIn, int baseIn, int bonusIn, int chanceIn)
	{
		id = idIn;
		data = dataIn;
		base = baseIn;
		bonus = bonusIn;
		chance = chanceIn;
		
		if (chance <= 0)	 chance = 1;
		if (chance > 100)	 chance = 100;
		
		if (base <= 0)			base = 1;
		if (bonus < 0)			bonus = 0;
	}
	
	public boolean drop()
	{
		return (UtilMath.r(100) <= chance);
	}
	
	public void showInfo(Player player) 
	{
		String name = Material.getMaterial(id).toString();
		String amount = base + "";
		if (bonus > 0)
			amount = base + " to " + (base+bonus);
	
		name = ItemStackFactory.Instance.GetName(id, data, false);
		
		UtilPlayer.message(player, F.desc(name, amount + " at " + chance + "%"));
	}
}
