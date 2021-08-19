package mineplex.core.gadget.gadgets.morph;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.particleeffects.LoveDoctorEffect;
import mineplex.core.recharge.Recharge;
import mineplex.core.utils.UtilGameProfile;

public class MorphLoveDoctor extends MorphGadget
{

	public MorphLoveDoctor(GadgetManager manager)
	{
		super(manager, "Love Doctor", UtilText.splitLinesToArray(new String[]{C.cGray + "The Doctor is in! Sneak to diagnose players with cooties!"}, LineFormat.LORE),
				-17, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.LOVE_DOCTOR.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.LOVE_DOCTOR.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
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

		Player player = event.getPlayer();

		if (!Recharge.Instance.use(player, "Love Doctor Laser", 5000, true, false, "Cosmetics"))
			return;

		HashSet<Material> ignore = new HashSet<Material>();
		ignore.add(Material.AIR);
		Location loc = player.getTargetBlock(ignore, 64).getLocation().add(0.5, 0.5, 0.5);

		if (!Manager.selectLocation(this, loc))
		{
			Manager.informNoUse(player);
			return;
		}

		LoveDoctorEffect loveDoctorEffect = new LoveDoctorEffect(player, loc, this);
		loveDoctorEffect.start();
	}

}
