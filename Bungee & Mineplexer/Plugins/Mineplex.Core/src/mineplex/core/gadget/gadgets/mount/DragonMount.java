package mineplex.core.gadget.gadgets.mount;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.MorphWither;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;

public class DragonMount extends Mount<DragonData>
{

	public DragonMount(GadgetManager manager, String name, String[] desc, int cost, Material material, byte materialData)
	{
		super(manager, name, desc, cost, material, materialData);
	}
	
	@Override
	public void enableCustom(Player player, boolean message)
	{
		Gadget morph = Manager.getActive(player, GadgetType.MORPH);

		if (morph instanceof MorphWither)
		{
			UtilPlayer.message(player, F.main("Gadget", "You cannot enable the " + F.elem(getName()) + " and the " + F.elem(morph.getName()) + " at the same time"));
			morph.disable(player, true);
		}

		super.enableCustom(player, message);
	}

	@Override
	public DragonData spawnMount(Player player)
	{
		DragonData dragonData = new DragonData(this, player);
		//Set max health to 1 so player doesn't see a bunch of mount hearts flashing when NewsManager changes the health
		dragonData.Dragon.setMaxHealth(1.0);
		dragonData.Dragon.setHealth(1.0);

		return dragonData;
	}

}
