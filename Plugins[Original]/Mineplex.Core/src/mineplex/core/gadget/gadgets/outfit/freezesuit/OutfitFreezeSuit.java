package mineplex.core.gadget.gadgets.outfit.freezesuit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class OutfitFreezeSuit extends OutfitGadget
{

	private final Set<FreezeSuitPathData> _data = new HashSet<>();

	private static final int DURATION = 2000;
	private static final int RANGE = 3;
	private static final int MELT_TIME = 6000;
	private static final int COOLDOWN = 15000;

	public OutfitFreezeSuit(GadgetManager manager, String name, int cost, ArmorSlot slot, Material mat, byte data)
	{
		super(manager, name,
				UtilText.splitLineToArray(C.cGray + "Stolen directly from the Winter Lord's closet, this coat is designed to " +
						"survive the coldest of weather! Press sneak to generate your ice bridge.", LineFormat.LORE),
				cost, slot, mat, data);
		setColor(Color.fromRGB(129, 212, 250));
		// Sets the display item
		if (slot.equals(ArmorSlot.HELMET))
		{
			setDisplayItem(new ItemStack(mat, 1, data));
		}
		else
		{
			ItemStack displayItem = new ItemStack(mat, 1, data);
			if (displayItem.getItemMeta() instanceof LeatherArmorMeta)
			{
				LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) displayItem.getItemMeta();
				leatherArmorMeta.setColor(Color.fromRGB(129, 212, 250));
				displayItem.setItemMeta(leatherArmorMeta);
			}
			setDisplayItem(displayItem);
		}
	}

	@EventHandler
	public void activateBridge(PlayerToggleSneakEvent event)
	{
		// Prevents running event 4 times
		if (getSlot() != ArmorSlot.HELMET)
			return;

		if (!setActive(event.getPlayer()))
			return;

		if (!event.isSneaking())
			return;

		if (!Recharge.Instance.use(event.getPlayer(), "Ice Path", COOLDOWN, true, false, "Cosmetics"))
			return;

		Player player = event.getPlayer();

		player.teleport(player.getLocation().add(0, 1, 0));
		UtilAction.velocity(player, new Vector(0, 0.5, 0));

		_data.add(new FreezeSuitPathData(player));
	}

	@EventHandler
	public void snowAura(UpdateEvent event)
	{
		// Prevents running event 4 times
		if (getSlot() != ArmorSlot.HELMET)
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		for(Player player : UtilServer.getPlayers())
		{
			if (!setActive(player))
			{
				continue;
			}

			UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.SNOW_SHOVEL, player.getLocation().add(0, 1, 0),
					0.25f, 0.25f, 0.25f, 0.1f, 1, UtilParticle.ViewDist.NORMAL);

			Map<Block, Double> blocks = UtilBlock.getInRadius(player.getLocation(), RANGE);

			Manager.selectBlocks(this, blocks.keySet());

			for (Block block : blocks.keySet())
			{
				Manager.getBlockRestore().snow(block, (byte) 1, (byte) 1, (int) (DURATION * (1 + blocks.get(block))), 250, 0);
			}
		}
	}

	@EventHandler
	public void icePath(UpdateEvent event)
	{
		// Prevents running event 4 times
		if (getSlot() != ArmorSlot.HELMET)
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<FreezeSuitPathData> dataIterator = _data.iterator();

		while (dataIterator.hasNext())
		{
			FreezeSuitPathData data = dataIterator.next();

			Block block = data.getNextBlock();

			if (block == null)
			{
				dataIterator.remove();
			}
			else if (Manager.selectBlocks(this, block))
			{
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 79);
				Manager.getBlockRestore().add(block, 79, (byte) 0, MELT_TIME);
			}
		}
	}

	private boolean setActive(Player player)
	{
		return getSet() != null && getSet().isActive(player);
	}

}
