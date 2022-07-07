package mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
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

public class WinEffectRankLegend extends WinEffectRankBased
{
	private DisguisePlayer _npc;
	private Wither _wither = null;
	private int _step = 0;

	private static final int POINTS = 100;
	private static final float RADIUS = 15F;
	private static final int BLOCK_RADIUS = 5;

	public WinEffectRankLegend(GadgetManager manager)
	{
		super(manager, "Legend Win Effect",
				UtilText.splitLinesToArray(new String[]{C.cGray + "Can you weather this Withering Assault?"}, LineFormat.LORE),
				Material.SKULL_ITEM, (byte) 1, CostConstants.UNLOCKED_WITH_LEGEND, PermissionGroup.LEGEND);
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
		if (_wither != null)
		{
			_wither.remove();
			_wither = null;
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

		Location npcLocation = _npc.getEntity().getBukkitEntity().getLocation();

		_step++;

		if (_wither == null)
		{
			Manager.getPetManager().getCreatureModule().SetForce(true);
			_wither = npcLocation.getWorld().spawn(npcLocation.add(0, 20, 0), Wither.class);
			Manager.getPetManager().getCreatureModule().SetForce(false);
		}

		double increment = (2 * Math.PI) / POINTS;

		double angle = _step * increment;
		Vector vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
		_wither.setVelocity(new Vector(0,0,0));
		_wither.teleport(npcLocation.clone().subtract(vector));
		Vector direction = npcLocation.toVector().subtract(_wither.getEyeLocation().toVector());
		Location enderLocation = _wither.getLocation().setDirection(direction);
		_wither.teleport(enderLocation);

		if (_step % 5 == 0 && _step < 45)
			spawnSkull();

		if (_step == 45)
			breakBlocks();
	}

	private void spawnSkull()
	{
		if (_wither != null)
		{
			_wither.launchProjectile(WitherSkull.class);
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