package mineplex.core.gadget.gadgets.wineffect.rankrooms.rankwineffects;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.account.permissions.PermissionGroup;
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

public class WinEffectRankHero extends WinEffectRankBased
{
	private DisguisePlayer _npc;
	private EnderDragon _enderDragon = null;
	private int _step = 0;

	private static final int POINTS = 100;
	private static final float RADIUS = 15F;
	private static final int BLOCK_RADIUS = 5;

	public WinEffectRankHero(GadgetManager manager)
	{
		super(manager, "Hero Win Effect",
				UtilText.splitLinesToArray(new String[]{C.cGray + "To become a True Hero you must first defeat the Dragon."}, LineFormat.LORE),
				Material.DRAGON_EGG, (byte) 0, CostConstants.UNLOCKED_WITH_HERO, PermissionGroup.HERO);
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
		if (_enderDragon != null)
		{
			_enderDragon.remove();
			_enderDragon = null;
		}
		_step = 0;
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isRunning())
			return;

		if (_step > 62)
			return;

		if (_step == 62)
		{
			breakBlocks();
		}

		if (event.getType() != UpdateType.FASTEST)
			return;

		Location npcLocation = _npc.getEntity().getBukkitEntity().getLocation();

		_step++;

		if (_enderDragon == null)
		{
			Manager.getPetManager().getCreatureModule().SetForce(true);
			_enderDragon = npcLocation.getWorld().spawn(npcLocation.add(0, 20, 0), EnderDragon.class);
			Manager.getPetManager().getCreatureModule().SetForce(false);
			_step = 0;
		}

		if (_step < 50)
		{
			double increment = (2 * Math.PI) / POINTS;
			double angle = _step * increment;
			Vector vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
			_enderDragon.setVelocity(new Vector(0, 0, 0));
			_enderDragon.teleport(npcLocation.clone().subtract(vector));
			Vector direction = npcLocation.toVector().subtract(_enderDragon.getEyeLocation().toVector()).multiply(-1);
			Location enderLocation = _enderDragon.getLocation().setDirection(direction);
			_enderDragon.teleport(enderLocation);
		}

		if (_step >= 50)
			setTarget();
	}

	private void setTarget()
	{
		((CraftEnderDragon) _enderDragon).getHandle().setTargetBlock(_baseLocation.getBlockX(), _baseLocation.getBlockY(), _baseLocation.getBlockZ());
	}

	private void breakBlocks()
	{
		Set<Block> blocks = UtilBlock.getBlocksInRadius(_baseLocation.add(0, 1, 0), BLOCK_RADIUS, BLOCK_RADIUS);
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