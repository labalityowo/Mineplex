package mineplex.core.gadget.gadgets.morph.moba;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.utils.UtilGameProfile;

public class MorphIvy extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.VINE)
			.setTitle(C.cGreenB + "Ivy's Flower Display")
			.addLore("Click to fire out a beam of water that knocks back", "players that it hits.")
			.build();
	private static final int ACTIVE_SLOT = 2;
	private static final int RADIUS = 5;
	private static final long DURATION = TimeUnit.SECONDS.toMillis(8);

	public MorphIvy(GadgetManager manager)
	{
		super(manager, "Ivy Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "Roses are red, violets are violet.",
				"",
				C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Vines" + C.cWhite + " to",
				C.cWhite + "create a " + C.cYellow + "Flower" + C.cWhite + " display.",
		}, LineFormat.LORE), CostConstants.FOUND_IN_MOBA_CHESTS, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.IVY.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.IVY.getProperty());

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

		Location location = player.getLocation();

		if (!Manager.selectLocation(this, location))
		{
			Manager.informNoUse(player);
			return;
		}

		if (!Recharge.Instance.use(player, "Floral Display", 16000, true, true, "Cosmetics"))
		{
			return;
		}

		Collection<Block> blocks = UtilBlock.getInRadius(location, RADIUS).keySet();
		Manager.selectBlocks(this, blocks);

		for (Block block : blocks)
		{
			if (block.getType() != Material.AIR || UtilBlock.airFoliage(block.getRelative(BlockFace.DOWN)))
			{
				continue;
			}

			Manager.getBlockRestore().add(block, Material.RED_ROSE.getId(), (byte) (UtilMath.r(8) + 1), DURATION + (UtilMath.rRange(-500, 500)));

			if (Math.random() > 0.7)
			{
				block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.STEP_SOUND, Material.RED_ROSE);
			}
		}
	}

	@EventHandler
	public void blockPhysics(BlockPhysicsEvent event)
	{
		if (event.getChangedType() == Material.RED_ROSE)
		{
			event.setCancelled(true);
		}
	}
}
