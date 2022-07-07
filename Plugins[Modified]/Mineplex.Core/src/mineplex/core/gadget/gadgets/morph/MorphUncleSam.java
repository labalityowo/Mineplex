package mineplex.core.gadget.gadgets.morph;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.utils.UtilGameProfile;

public class MorphUncleSam extends MorphGadget
{

	public MorphUncleSam(GadgetManager manager)
	{
		super(manager, "Uncle Sam Morph", UtilText.splitLinesToArray(new String[]
				{
						UtilText.colorWords("Turn into Uncle Sam and bring Justice and Freedom with you!",
								ChatColor.RED, ChatColor.WHITE, ChatColor.BLUE),
						C.blankLine,
						"#" + C.cWhite + "Left-click to use Freedom Fireworks",
						C.blankLine,
						"#" + C.cRed +C.Bold + "WARNING: " + ChatColor.RESET + "FREEDOM FIREWORKS ARE EXTREMELY PATRIOTIC"
				}, LineFormat.LORE),
				-8, Material.FIREWORK, (byte) 0);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		this.applyArmor(player, message);

		GameProfile profile = UtilGameProfile.getGameProfile(player);
		profile.getProperties().clear();
		profile.getProperties().put("textures", SkinData.UNCLE_SAM.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, profile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void firework(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, UtilEvent.ActionType.L))
			return;

		if (!Recharge.Instance.use(player, getName(), 2500, false, false, "Cosmetics"))
			return;

		int r = (int) (Math.random() * 3);
		UtilFirework.playFreedomFirework(player.getLocation().clone().add(0, 2, 0));
	}

}
