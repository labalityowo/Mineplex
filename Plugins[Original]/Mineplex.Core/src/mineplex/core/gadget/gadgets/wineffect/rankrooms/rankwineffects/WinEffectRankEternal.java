package mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

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

public class WinEffectRankEternal extends WinEffectRankBased
{
	private DisguisePlayer _npc;
	private Guardian _guardian = null;
	private int _step = 0;

	private static final int POINTS = 100;
	private static final float RADIUS = 15F;
	private static final int BLOCK_RADIUS = 5;

	public WinEffectRankEternal(GadgetManager manager)
	{
		super(manager, "Eternal Win Effect",
				UtilText.splitLinesToArray(new String[]{C.cGray + "GWEN is ALWAYS watching."}, LineFormat.LORE),
				Material.PRISMARINE_SHARD, (byte) 0, CostConstants.UNLOCKED_WITH_ETERNAL, PermissionGroup.ETERNAL);
	}

	@Override
	public void play()
	{
		Location loc = getBaseLocation();

		loc.setDirection(_player.getLocation().subtract(loc).toVector().multiply(-1));

		_npc = getNPC(getPlayer(), loc, true);
	}

	@Override
	public void finish()
	{
		if (_guardian != null)
		{
			_guardian.remove();
			_guardian = null;
		}
		_step = 0;
	}

	@EventHandler
	public void spawnGuardian(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_guardian != null)
		{
			return;
		}

		if (!isRunning())
			return;

		_step++;

		if (_step == 5)
		{
			Location npcLocation = _npc.getEntity().getBukkitEntity().getLocation();
			Manager.getPetManager().getCreatureModule().SetForce(true);
			_guardian = npcLocation.getWorld().spawn(npcLocation, Guardian.class);
			Manager.getPetManager().getCreatureModule().SetForce(false);
			_step = 0;
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isRunning())
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		if (_guardian == null)
		{
			return;
		}

		Location npcLocation = _npc.getEntity().getBukkitEntity().getLocation();

		_step++;

		double increment = (2 * Math.PI) / POINTS;
		double angle = _step * increment;
		Vector vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
		_guardian.setVelocity(new Vector(0,0,0));
		_guardian.teleport(npcLocation.clone().subtract(vector));
		Vector direction = npcLocation.toVector().subtract(_guardian.getEyeLocation().toVector());
		Location enderLocation = _guardian.getLocation().setDirection(direction);
		_guardian.teleport(enderLocation);

		if (_step % 2 == 0 && _step < 45)
			zap();

		if (_step == 45)
		{
			breakBlocks();
		}
	}

	private void zap()
	{
		if (_guardian != null)
		{
			_guardian.setTarget((LivingEntity) _npc.getEntity().getBukkitEntity());
		}
	}

	private void breakBlocks()
	{
		Set<Block> blocks = UtilBlock.getBlocksInRadius(_npc.getEntity().getBukkitEntity().getLocation(), BLOCK_RADIUS, BLOCK_RADIUS);
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