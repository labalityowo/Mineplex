package mineplex.core.gadget.gadgets.particle.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.RGBData;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.particleeffects.ColoredCircleEffect;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleSpringHalo extends ParticleGadget
{

	private static final long BLOCK_TIME = TimeUnit.SECONDS.toMillis(8);

	private final Map<Player, ColoredCircleEffect> _effects = new HashMap<>();

	public ParticleSpringHalo(GadgetManager manager)
	{
		super(manager, "Spring Halo", UtilText.splitLinesToArray(new String[] {C.cGray + "Spring is everywhere, if you look hard enough."}, LineFormat.LORE),
				-19, Material.YELLOW_FLOWER, (byte) 0);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		super.enableCustom(player, message);
		Manager.removeGadgetType(player, GadgetType.MORPH, this);
		Manager.removeOutfit(player, OutfitGadget.ArmorSlot.HELMET);
		ColoredCircleEffect circleEffect = new ColoredCircleEffect(player, 0.7d, false);
		RGBData colorA = UtilColor.hexToRgb(0x5a92ed);
		RGBData colorB = UtilColor.hexToRgb(0xdb5aed);
		RGBData colorC = UtilColor.hexToRgb(0xd2cdf2);
		RGBData colorD = UtilColor.hexToRgb(0x7c6df2);
		RGBData colorE = UtilColor.hexToRgb(0xedeb97);
		RGBData colorF = UtilColor.hexToRgb(0xeac07c);
		circleEffect.addColors(colorA, colorB, colorC, colorD, colorE, colorF);
		circleEffect.setYOffset(2.3d);
		circleEffect.start();
		_effects.put(player, circleEffect);
		player.getEquipment().setHelmet(new ItemStack(Material.RED_ROSE, 1, (byte) 8));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);
		if (_effects.containsKey(player))
		{
			ColoredCircleEffect circleEffect = _effects.get(player);
			if (circleEffect != null)
			{
				circleEffect.stop();
			}
		}
		_effects.remove(player);
		player.getInventory().setHelmet(null);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
	}

	@EventHandler
	public void spawnFlowers(PlayerMoveEvent event)
	{
		if (!isActive(event.getPlayer()) || Manager.isGameLive())
		{
			return;
		}

		Block block = event.getFrom().getBlock();

		if (block.getType() != Material.AIR || Manager.getBlockRestore().contains(block))
		{
			return;
		}

		if (block.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.GRASS && block.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.DIRT)
		{
			return;
		}

		Manager.getBlockRestore().add(block, Material.RED_ROSE.getId(), (byte) UtilMath.random.nextInt(8), BLOCK_TIME);
	}
}
