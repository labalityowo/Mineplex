package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.utils.UtilGameProfile;

public class MorphTurkey extends MorphGadget
{

	public MorphTurkey(GadgetManager manager)
	{
		super(manager, "Turkey Morph", UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "Gobble, Gobble, please don't stuff me!",
								C.blankLine,
								C.cWhite + "Sneak to gobble."
						},
				LineFormat.LORE), -14, Material.COOKED_CHICKEN, (byte) 0);

		setPPCYearMonth(YearMonth.of(2016, Month.NOVEMBER));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile profile = UtilGameProfile.getGameProfile(player);
		profile.getProperties().clear();
		profile.getProperties().put("textures", SkinData.TURKEY.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, profile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;

		if (!event.isSneaking())
			return;

		Player player = event.getPlayer();
		player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_IDLE, 1f, 1.25f);
	}

}
