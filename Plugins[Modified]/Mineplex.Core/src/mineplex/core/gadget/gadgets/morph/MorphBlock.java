package mineplex.core.gadget.gadgets.morph;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.utils.UtilScheduler;

public class MorphBlock extends MorphGadget implements IPacketHandler
{
	public static final String FLAG_BLOCK_MORPH_COMPONENT = "block-morph-component";

	private Map<Player, BlockForm> _active = new HashMap<>();

	public MorphBlock(GadgetManager manager)
	{
		super(manager, "Block Morph", UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "The blockiest block that ever blocked.",
								C.blankLine,
								"#" + C.cWhite + "Left Click to use Change Block",
								"#" + C.cWhite + "Stay Still to use Solidify",
						}, LineFormat.LORE),
				30000,
				Material.EMERALD_BLOCK, (byte) 0);

		UtilScheduler.runEvery(UpdateType.TICK, () -> _active.values().forEach(BlockForm::update));

		manager.getPacketManager().addPacketHandler(this, PacketPlayOutBlockChange.class, PacketPlayInBlockDig.class);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		_active.put(player, new BlockForm(this, player, Material.EMERALD_BLOCK, 0));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);

		BlockForm form = _active.remove(player);
		if (form != null)
		{
			form.remove();
		}
	}

	@EventHandler
	public void formChange(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
			return;

		if (!UtilEvent.isAction(event, ActionType.L_BLOCK) && !UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;

		if (!Recharge.Instance.use(event.getPlayer(), getName(), 500, false, false))
			return;

		BlockForm form = _active.get(event.getPlayer());

		if (form == null)
			return;

		form.setType(event.getClickedBlock());
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event)
	{
		if (UtilEnt.hasFlag(event.getEntity(), FLAG_BLOCK_MORPH_COMPONENT))
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayOutBlockChange)
		{
			PacketPlayOutBlockChange packet = (PacketPlayOutBlockChange) packetInfo.getPacket();

			for (BlockForm form : _active.values())
			{
				if (form.getBlock() == null)
					continue;

				Location location = form.getBlock().getLocation();
				if (packetInfo.getPlayer().getWorld() == location.getWorld() && packet.a.getX() == location.getX() && packet.a.getY() == location.getY() && packet.a.getZ() == location.getZ())
				{
					if (packetInfo.getPlayer() == form.getPlayer())
					{
						packet.block = Blocks.AIR.getBlockData();
					}
					else
					{
						packet.block = form.getBlockData();
					}
				}
			}
		}
		else if (packetInfo.getPacket() instanceof PacketPlayInBlockDig)
		{
			PacketPlayInBlockDig packet = (PacketPlayInBlockDig) packetInfo.getPacket();

			if (packet.c != PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK)
				return;

			for (BlockForm form : _active.values())
			{
				if (form.getBlock() == null)
					continue;

				Location location = form.getBlock().getLocation();
				if (packetInfo.getPlayer().getWorld() == location.getWorld() && packet.a.getX() == location.getX() && packet.a.getY() == location.getY() && packet.a.getZ() == location.getZ())
				{
					packetInfo.setCancelled(true);
					packetInfo.getPlayer().sendBlockChange(location, 0, (byte) 0);
				}
			}
		}
	}
}
