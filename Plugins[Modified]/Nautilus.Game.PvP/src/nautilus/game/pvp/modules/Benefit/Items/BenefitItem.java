package nautilus.game.pvp.modules.Benefit.Items;

import me.chiss.Core.Shopv2.Item.SalesPackageBase;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import nautilus.game.pvp.modules.Benefit.BenefitManager;

public abstract class BenefitItem extends SalesPackageBase implements Listener
{
	protected BenefitManager Plugin;
	
	public BenefitItem(BenefitManager plugin, String name, Material material, String...description)
	{
		super(name, material, description);
		
		Plugin = plugin;
		
		plugin.RegisterEvents(this);
	}
}
