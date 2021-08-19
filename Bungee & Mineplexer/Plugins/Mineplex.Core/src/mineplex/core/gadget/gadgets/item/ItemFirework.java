package mineplex.core.gadget.gadgets.item;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;

public class ItemFirework extends ItemGadget
{
	public ItemFirework(GadgetManager manager)
	{
		super(manager, "Fireworks", 
				UtilText.splitLineToArray(C.cWhite + "Need to celebrate? These fireworks should do the trick!", LineFormat.LORE),
				-1,  
				Material.FIREWORK, (byte)0, 
				500, new Ammo("Fireworks", "50 Fireworks", Material.FIREWORK, (byte)0, new String[] { C.cWhite + "50 Fireworks for you to launch!" }, 500, 50));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Location loc = player.getEyeLocation().add(player.getLocation().getDirection());
		
		//Portal Disallow
		for (Block block : UtilBlock.getSurrounding(loc.getBlock(), true))
		{
			if (block.getTypeId() == 90)
			{
				UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(getName()) + " near Portals."));
				return;
			}
		}
		
		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(getName()) + "."));
		
		double r = Math.random();
		
		Color color = Color.FUCHSIA;
		if (r > 0.9)		color = Color.RED;
		else if (r > 0.8)	color = Color.YELLOW;
		else if (r > 0.7)	color = Color.GREEN;
		else if (r > 0.6)	color = Color.BLUE;
		else if (r > 0.5)	color = Color.AQUA;
		else if (r > 0.4)	color = Color.LIME;
		else if (r > 0.3)	color = Color.ORANGE;
		else if (r > 0.2)	color = Color.TEAL;
		else if (r > 0.1)	color = Color.WHITE;
		
		r = Math.random();
		
		Type type = Type.BURST;
		if (r > 0.66)		type = Type.BALL;
		else if (r > 0.33)	type = Type.BALL_LARGE;
		
		UtilFirework.launchFirework(loc, 
				FireworkEffect.builder().flicker(Math.random() > 0.5).withColor(color).with(type).trail(Math.random() > 0.5).build(), 
				new Vector(0,0,0), UtilMath.r(3) + 1);
	}
}
