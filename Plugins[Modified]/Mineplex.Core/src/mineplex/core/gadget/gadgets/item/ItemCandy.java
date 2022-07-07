package mineplex.core.gadget.gadgets.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;

public class ItemCandy extends ItemGadget
{

	private static final PotionEffectType[] POTION_EFFECTS = {
			PotionEffectType.SPEED,
			PotionEffectType.JUMP,
	};

	public ItemCandy(GadgetManager manager)
	{
		super(manager, "Halloween Candy",
				UtilText.splitLineToArray(C.cWhite + "Get some serious sugar rush", LineFormat.LORE),
				CostConstants.FOUND_IN_TRICK_OR_TREAT, Material.COOKIE, (byte) 0, 2000, new Ammo("Halloween Candy", "1 Halloween Candy", Material.COOKIE, (byte) 0, new String[]
						{
						}
						, CostConstants.FOUND_IN_TRICK_OR_TREAT, 1));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		player.sendMessage(F.main(Manager.getName(), "Sugar Rush!!!"));
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

		PotionEffectType effectType = UtilMath.randomElement(POTION_EFFECTS);

		if (effectType == null)
		{
			return;
		}

		player.removePotionEffect(effectType);
		player.addPotionEffect(new PotionEffect(effectType, 10 * 20, 5 + UtilMath.r(5)));
	}
}
