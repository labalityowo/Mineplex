package mineplex.core.gadget.gadgets.wineffect;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class WinEffectLogo extends WinEffectGadget
{

	private DisguisePlayer _npc;
	private SchematicData _data;
	private Set<Block> _logoBlocks;

	public WinEffectLogo(GadgetManager manager)
	{
		super(manager, "Mineplex Logo", new String[]
				{
						C.cGray + "Awaken this ancient Mineplex Logo.",
						C.blankLine,
						C.cBlue + "Unlocked by linking your Mineplex",
						C.cBlue + "forum account in-game",
				}, CostConstants.NO_LORE, Material.WOOL, (byte) 1);

		_schematicName = "LogoEffect";
	}

	@Override
	public void play()
	{
		Location location = getBaseLocation();
		location.setYaw(180);
		BlockRestore restore = Manager.getBlockRestore();

		Manager.runSyncLater(() ->
		{
			_npc = getNPC(getPlayer(), location, true);
			_logoBlocks.forEach(restore::restore);
			location.getWorld().playSound(location, Sound.EXPLODE, 2, 0.6F);

			List<Location> corners = _data.getDataLocationMap().getIronLocations(DyeColor.YELLOW);
			Location start = corners.get(0), end = corners.get(1);
			Vector direction = location.getDirection();

			for (int i = 0; i < 14; i++)
			{
				Location fallingLocation = start.clone();
				fallingLocation.setX(UtilMath.rRange(end.getBlockX(), start.getBlockX()));
				fallingLocation.setY(UtilMath.rRange(start.getBlockY() + 2, end.getBlockY()));

				FallingBlock block = start.getWorld().spawnFallingBlock(fallingLocation, Material.STONE, (byte) 0);
				block.setDropItem(false);
				block.setHurtEntities(false);
				block.setVelocity(direction);
			}

			UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, getBaseLocation().add(3, 8, 0), 4, 4, 0, 0, 10, ViewDist.NORMAL);

			Manager.runSyncTimer(new BukkitRunnable()
			{
				int x = start.getBlockX();
				int i = 0;

				@Override
				public void run()
				{
					if (!isRunning())
					{
						cancel();
						return;
					}

					if (i % 3 == 0)
					{
						UtilFirework.playFirework(UtilAlg.getRandomLocation(getBaseLocation().add(0, 13, 0), 10, 3, 10), FireworkEffect.builder()
								.with(Type.BALL)
								.withColor(Color.ORANGE)
								.withFade(Color.BLACK)
								.build());

						Entity entity = _npc.getEntity().getBukkitEntity();
						_npc.sendHit();

						if (UtilEnt.isGrounded(entity))
						{
							entity.setVelocity(new Vector(0, 0.42, 0));
						}
					}

					for (Block block : _logoBlocks)
					{
						if (block.getType() == Material.WOOL || block.getLocation().getBlockX() != x)
						{
							continue;
						}

						restore.add(block, block.getTypeId(), (byte) 0, 240);
					}

					if (--x < end.getBlockX())
					{
						x = start.getBlockX();
					}

					if (i % 10 == 0)
					{
						UtilTextBottom.display(C.cGray + "Unlock " + C.cYellowB + "THIS" + C.cGray + " Win Effect by signing up on " + C.cGoldB + "MINEPLEX.COM", UtilServer.getPlayers());
					}

					i++;
				}
			}, 10, 3);
		}, 60);
	}

	@Override
	public void buildWinnerRoom()
	{
		_data = pasteSchematic(_schematicName);

		List<Location> corners = _data.getDataLocationMap().getIronLocations(DyeColor.YELLOW);
		corners.sort((o1, o2) -> o2.getBlockX() - o1.getBlockX());

		BlockRestore restore = Manager.getBlockRestore();
		_logoBlocks = new HashSet<>();

		for (Block block : UtilBlock.getInBoundingBox(corners.get(0), corners.get(1)))
		{
			Material type = block.getType();

			if (type == Material.WOOL || type == Material.STAINED_CLAY || type == Material.STAINED_GLASS)
			{
				restore.add(block, Material.STONE.getId(), (byte) 0, Integer.MAX_VALUE);
				_logoBlocks.add(block);
			}
		}
	}

	@Override
	public void teleport()
	{
		teleport(_data.getDataLocationMap().getGoldLocations(DyeColor.GREEN).get(0));
	}

	@Override
	public void finish()
	{
		Manager.getDisguiseManager().undisguise(_npc);
		_npc = null;
		_data = null;
		_logoBlocks.clear();
		_logoBlocks = null;
	}

}
