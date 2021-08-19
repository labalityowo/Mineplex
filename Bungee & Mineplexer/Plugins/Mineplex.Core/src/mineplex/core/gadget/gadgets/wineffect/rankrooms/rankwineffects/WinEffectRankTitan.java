package mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Giant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.MaterialData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.wineffect.rankrooms.WinEffectRankBased;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectRankTitan extends WinEffectRankBased
{
	private DisguisePlayer _npc;
	private Giant _giant = null;
	private int _step = 0;

	private static final int RADIUS = 5;

	public WinEffectRankTitan(GadgetManager manager)
	{
		super(manager, "Titan Win Effect",
				UtilText.splitLinesToArray(new String[]{C.cGray + "Legend has it that the Titans were so powerful they towered over even the gods."}, LineFormat.LORE),
				Material.ROTTEN_FLESH, (byte) 0, CostConstants.UNLOCKED_WITH_TITAN, PermissionGroup.TITAN);
	}

	@Override
	public void play()
	{
		Location loc = getBaseLocation();

		loc.setDirection(_player.getLocation().subtract(loc).toVector().multiply(-1));

		_npc = getNPC(getPlayer(), loc);
	}

	@Override
	public void finish()
	{
		if (_giant != null)
		{
			_giant.remove();
			_giant = null;
		}
		_step = 0;
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isRunning())
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		if (_giant == null && _step >= 5)
		{
			Location loc = getBaseLocation();

			Manager.getPetManager().getCreatureModule().SetForce(true);

			_giant = loc.getWorld().spawn(loc.add(0, 20, 0), Giant.class);

			Manager.getPetManager().getCreatureModule().SetForce(false);
		}
		else
		{
			_step++;
		}
	}

	@EventHandler
	public void onGiantFall(EntityDamageEvent event)
	{
		if (_giant == null)
			return;

		if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL))
		{
			if (event.getEntity().equals(_giant))
			{
				Set<Block> blocks = UtilBlock.getBlocksInRadius(_npc.getEntity().getBukkitEntity().getLocation(), RADIUS, RADIUS);
				for (Block block : blocks)
				{
					MaterialData materialData = MaterialData.of(block.getType(), block.getData());
					block.setType(Material.AIR);
					FallingBlock fallingBlock = block.getLocation().getWorld().spawnFallingBlock(block.getLocation(), materialData.getMaterial(), materialData.getData());
					fallingBlock.setDropItem(false);
					UtilAction.velocity(fallingBlock, UtilAlg.getTrajectory(fallingBlock.getLocation(), _npc.getEntity().getBukkitEntity().getLocation()).multiply(-1), .75, true, 0.8, 0, 1.0, true);
				}
				ArmorStand armorStand = (ArmorStand) _npc.getEntity().getBukkitEntity();
				armorStand.setHealth(0);
			}
		}
	}
}