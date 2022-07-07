package mineplex.core.gadget.gadgets.morph.moba;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle.ParticleType;
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

public class MorphRowena extends MorphGadget
{

	private static final ItemStack BOW = new ItemBuilder(Material.BOW)
			.setTitle(C.cGreenB + "Rowena's Bow")
			.addEnchantment(Enchantment.ARROW_INFINITE, 1)
			.addLore("Firing this bow causes arrows to be imbued with " + F.elem("Light") + ".")
			.setUnbreakable(true)
			.build();
	private static final int BOW_SLOT = 2;
	private static final ItemStack ARROW = new ItemStack(Material.ARROW);
	private static final int ARROW_SLOT = 9;

	public MorphRowena(GadgetManager manager)
	{
		super(manager, "Rowena Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "Make the hub a little brighter with Rowena's Light Arrows.",
				"",
				C.cGreen + "Shoot" + C.cWhite + " your " + C.cYellow + "Bow" + C.cWhite + " to fire",
				C.cWhite + "arrows to be imbued with " + C.cYellow + "Light" + C.cWhite + ".",
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.ROWENA.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.ROWENA.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		player.getInventory().setItem(BOW_SLOT, BOW);
		player.getInventory().setItem(ARROW_SLOT, ARROW);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.getInventory().setItem(BOW_SLOT, null);
		player.getInventory().setItem(ARROW_SLOT, null);
	}

	@EventHandler
	public void entityShootBow(EntityShootBowEvent event)
	{
		LivingEntity entity = event.getEntity();

		if (!(entity instanceof Player))
		{
			return;
		}

		Player player = (Player) entity;

		if (!isActive(player))
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

		if (!Recharge.Instance.use(player, "Light Arrows", 500, false, true, "Cosmetics"))
		{
			return;
		}

		LineParticle lineParticle = new LineParticle(location, location.getDirection(), 0.4, 40, ParticleType.FIREWORKS_SPARK, UtilServer.getPlayers());
		lineParticle.setIgnoreAllBlocks(true);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < 6; i++)
				{
					if (lineParticle.update())
					{
						cancel();
						return;
					}
				}
			}
		}, 0, 1);
	}
}
