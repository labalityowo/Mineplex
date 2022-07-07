package nautilus.game.arcade.game.games.christmasnew.section.three;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;

class CaveIn extends SectionChallenge
{

	private static final Material ALLOW_BREAK = Material.COBBLESTONE;
	private static final int MAX_MOBS = 25;

	private final int _triggerZ;
	private final List<Location> _blocksToExplode;
	private final List<Location> _blocksToFall;
	private final List<Location> _blocksToClear;
	private final List<Location> _mobSpawns;

	private boolean _exploded;

	CaveIn(ChristmasNew host, Location present, Section section)
	{
		super(host, present, section);

		_triggerZ = _worldData.GetDataLocs("ORANGE").get(0).getBlockZ();
		_blocksToExplode = _worldData.GetCustomLocs(String.valueOf(Material.GOLD_ORE.getId()));
		_blocksToFall = _worldData.GetCustomLocs(String.valueOf(Material.MYCEL.getId()));
		_blocksToClear = _worldData.GetCustomLocs(String.valueOf(Material.RED_SANDSTONE.getId()));
		_mobSpawns = _worldData.GetDataLocs("MAGENTA");

		_blocksToExplode.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
		_blocksToFall.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.COBBLESTONE));
		_blocksToClear.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
	}

	@Override
	public void onPresentCollect()
	{

	}

	@Override
	public void onRegister()
	{
		_host.DeathOut = true;
		_host.BlockBreakAllow.add(ALLOW_BREAK.getId());

		for (Player player : _host.GetPlayers(true))
		{
			player.setGameMode(GameMode.SURVIVAL);
		}
	}

	@Override
	public void onUnregister()
	{
		_host.BlockBreakAllow.remove(ALLOW_BREAK.getId());

		for (Player player : _host.GetPlayers(true))
		{
			player.setGameMode(GameMode.ADVENTURE);
		}

		_blocksToClear.forEach(location ->
		{
			if (location.getBlock().getType() != Material.AIR)
			{
				MapUtil.QuickChangeBlockAt(location, Material.AIR);
			}
		});
	}

	public boolean isCleared()
	{
		for (Location location : _blocksToClear)
		{
			if (location.getBlock().getType() != Material.AIR)
			{
				return false;
			}
		}

		return true;
	}

	@EventHandler
	public void updateExplosion(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _exploded)
		{
			return;
		}

		for (Player player : _host.GetPlayers(true))
		{
			if (player.getLocation().getZ() > _triggerZ)
			{
				explodeBlocks();
				return;
			}
		}
	}

	private void explodeBlocks()
	{
		_exploded = true;

		_section.setObjective("Clear a path through the rocks");
		_host.sendSantaMessage("Watch out! It’s a trap!", ChristmasNewAudio.SANTA_ITS_A_TRAP);
		_host.getArcadeManager().runSyncLater(() -> _host.sendSantaMessage("Clear a path through those rocks.", ChristmasNewAudio.SANTA_CLEAR_PATH), 100);

		_blocksToExplode.forEach(location ->
		{
			UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, 0, 0, 0, 0.1F, 1, ViewDist.LONG);
			location.getWorld().playSound(location, Sound.EXPLODE, 3, 0.6F);
		});

		_blocksToFall.forEach(location ->
		{
			MapUtil.QuickChangeBlockAt(location, Material.AIR);

			FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location.add(0.5, 0, 0.5), ALLOW_BREAK, (byte) 0);
			fallingBlock.setHurtEntities(false);
			fallingBlock.setDropItem(false);
		});
	}

	@EventHandler
	public void updateMobs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_exploded || _entities.size() > MAX_MOBS)
		{
			return;
		}

		spawn(UtilAlg.Random(_mobSpawns), Spider.class);

		_entities.forEach(entity ->
		{
			if (entity instanceof Spider)
			{
				Spider spider = (Spider) entity;

				if (spider.getTarget() == null)
				{
					spider.setTarget(UtilPlayer.getClosest(entity.getLocation()));
				}
			}
		});
	}
}
