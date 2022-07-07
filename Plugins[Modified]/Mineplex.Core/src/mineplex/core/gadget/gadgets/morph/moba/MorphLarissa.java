package mineplex.core.gadget.gadgets.morph.moba;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.utils.UtilGameProfile;

public class MorphLarissa extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.DIAMOND_HOE)
			.setTitle(C.cGreenB + "Larissa's Wand")
			.addLore("Click to fire out a beam of water that knocks back", "players that it hits.")
			.build();
	private static final int ACTIVE_SLOT = 2;

	public MorphLarissa(GadgetManager manager)
	{
		super(manager, "Larissa Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "Become a water mage and splash your enemies away!",
				"",
				C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Wand" + C.cWhite + " to fire",
				C.cWhite + "a beam of " + C.cWhite + "Water" + C.cWhite + " that",
				C.cYellow + "Knocks Back" + C.cWhite + " players it hits."
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.LARISSA.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.LARISSA.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		player.getInventory().setItem(ACTIVE_SLOT, ACTIVE_ITEM);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.getInventory().setItem(ACTIVE_SLOT, null);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem();

		if (!isActive(player) || itemStack == null || !itemStack.equals(ACTIVE_ITEM))
		{
			return;
		}

		event.setCancelled(true);

		Location location = player.getEyeLocation();

		if (!Manager.selectLocation(this, location))
		{
			Manager.informNoUse(player);
			return;
		}

		if (!Recharge.Instance.use(player, "Aqua Cannon", 4000, true, true, "Cosmetics"))
		{
			return;
		}

		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), location.getDirection(), 0.2, 10, ParticleType.DRIP_WATER, UtilServer.getPlayers());

		while (!lineParticle.update())
		{
			for (Player nearby : UtilPlayer.getNearby(lineParticle.getLastLocation(), 2))
			{
				if (player.equals(nearby))
				{
					continue;
				}

				UtilAction.velocity(nearby, UtilAlg.getTrajectory(player.getLocation(), nearby.getLocation()).setY(1));
				break;
			}
		}

		player.getWorld().playSound(lineParticle.getLastLocation(), Sound.BLAZE_HIT, 1, 1.4F);
		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, lineParticle.getLastLocation(), 0, 0, 0, 0.2F, 10, ViewDist.NORMAL);
	}

}
