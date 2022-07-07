package mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

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

public class WinEffectRankUltra extends WinEffectRankBased
{
	private static final int RADIUS = 5;

	private DisguisePlayer _npc;
	private Creeper _creeper;
	private int _step = 0;

	public WinEffectRankUltra(GadgetManager manager)
	{
		super(manager, "Ultra Win Effect", UtilText.splitLinesToArray(new String[]{C.cGray + "Always check behind you."}, LineFormat.LORE),
				Material.SKULL_ITEM, (byte) 4, CostConstants.UNLOCKED_WITH_ULTRA, PermissionGroup.ULTRA);
	}

	@Override
	public void play()
	{
		Location loc = getBaseLocation();

		loc.setDirection(_player.getLocation().subtract(loc).toVector().multiply(-1));
		loc.setPitch(0);
		loc.setYaw(0);
		_npc = getNPC(getPlayer(), loc, true);
	}

	@Override
	public void finish()
	{
		if (_creeper != null)
		{
			_creeper.remove();
			_creeper = null;
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

		if (_creeper == null && _step >= 5)
		{
			Location loc = getBaseLocation();

			Manager.getPetManager().getCreatureModule().SetForce(true);

			_creeper = loc.getWorld().spawn(loc.add(0, 10, 0), Creeper.class);

			_creeper.teleport(loc.subtract(0, 0, 2));

			_creeper.setTarget((LivingEntity) _npc.getEntity().getBukkitEntity());

			Manager.getPetManager().getCreatureModule().SetForce(false);
		}
		else
		{
			_step++;
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (event.getEntity().equals(_creeper))
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