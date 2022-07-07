package mineplex.core.gadget.gadgets.morph;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.disguise.disguises.DisguiseCat;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.gadget.util.CostConstants;

public class MorphOcelot extends MorphGadget
{

	private static final PotionEffect POTION_EFFECT = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false);

	public MorphOcelot(GadgetManager manager)
	{
		super(manager, "Ocelot Morph", new String[]
				{
						C.cGray + "Meeeeeeeeeeoooooooowwwwww",
						C.cGreen + "Crouch" + C.cWhite + " to meow."
				}, CostConstants.LEVEL_REWARDS, Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.OCELOT));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		DisguiseCat disguise = new DisguiseCat(player);
		UtilMorph.disguise(player, disguise, Manager);

		player.addPotionEffect(POTION_EFFECT);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());
		player.removePotionEffect(PotionEffectType.SPEED);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerToggleSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if (!event.isSneaking() || !isActive(player))
		{
			return;
		}

		player.getWorld().playSound(player.getLocation(), Sound.CAT_MEOW, 1, 1);
	}
}
