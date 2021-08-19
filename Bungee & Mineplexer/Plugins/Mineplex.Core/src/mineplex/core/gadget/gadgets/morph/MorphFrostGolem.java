package mineplex.core.gadget.gadgets.morph;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseIronGolem;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphFrostGolem extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.SNOW_BLOCK)
			.setTitle(C.cGreenB + "Snow Day")
			.addLore("Click to create an area of snow.")
			.build();
	private static final ItemStack SKULL_ITEM = new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
			.setPlayerHead("MHF_Golem")
			.build();
	private static final int ACTIVE_SLOT = 2;
	private static final int RADIUS = 3;
	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(30);
	private static final long DURATION = TimeUnit.SECONDS.toMillis(10);
	private static final Vector UP = new Vector(0, 1, 0);

	private final Map<Location, Long> _ability;

	public MorphFrostGolem(GadgetManager manager)
	{
		super(manager, "Frost Golem Morph", UtilText.splitLineToArray(C.cGray + "Snow day? No school!", LineFormat.LORE), CostConstants.FOUND_IN_GINGERBREAD_CHESTS, Material.GLASS, (byte) 0);

		_ability = new HashMap<>();

		setDisplayItem(SKULL_ITEM);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		DisguiseIronGolem disguise = new DisguiseIronGolem(player);
		UtilMorph.disguise(player, disguise, Manager);

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

		if (!Recharge.Instance.use(player, "Snow Day", COOLDOWN, true, true, "Cosmetics"))
		{
			return;
		}

		Location location = player.getLocation();

		player.teleport(location.add(0, 1, 0));
		_ability.put(location.clone().add(0, 3, 0), System.currentTimeMillis());

		Map<Block, Double> blocks = UtilBlock.getInRadius(location, RADIUS);

		Manager.selectBlocks(this, blocks.keySet());

		blocks.forEach((block, scale) ->
		{
			if (block.getType() != Material.AIR || block.getRelative(BlockFace.DOWN).getType() == Material.AIR)
			{
				return;
			}

			byte height = (byte) (UtilMath.r(3) + 1);

			Manager.getBlockRestore().snow(block, height, height, (int) (DURATION * (1 + scale)), 250, 0);

			if (Math.random() < 0.1)
			{
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.SNOW_BLOCK);
			}
		});
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_ability.keySet().removeIf(location ->
		{
			if (UtilTime.elapsed(_ability.get(location), DURATION))
			{
				return true;
			}

			UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, location, RADIUS, 0.5F, RADIUS, 0, 4, ViewDist.NORMAL);
			return false;
		});
	}
}
