package mineplex.game.clans.clans.siege.weapon.projectile;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.google.common.collect.Lists;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.siege.weapon.SiegeWeapon;
import mineplex.game.clans.clans.siege.weapon.projectile.event.CraterExplodeEvent;
import mineplex.game.clans.core.repository.ClanTerritory;

public class Crater
{
	private final SiegeWeapon _weapon;
	private final Location _origin;
	private final Player _cause;
	
	public Crater(SiegeWeapon weapon, WeaponProjectile projectile, Location origin)
	{
		_weapon = weapon;
		_origin = origin;
		_cause = projectile.getShooter();
		createExplosion();
	}
	
	@SuppressWarnings("deprecation")
	private void createExplosion()
	{
		List<Block> blocks = Lists.newArrayList();
		List<String> noRepeats = Lists.newArrayList();
		for (Block block : UtilBlock.getInRadius(_origin, 3).keySet())
		{
			String locID = block.getLocation().getX() + " " + block.getLocation().getY() + " " + block.getLocation().getZ();
			if (noRepeats.contains(locID))
			{
				continue;
			}
			else
			{
				noRepeats.add(locID);
			}
			if (block.getType() == Material.AIR || block.isLiquid() || block.getType() == Material.BEDROCK)
			{
				continue;
			}
			ClanTerritory terr = _weapon.getClans().getClanUtility().getClaim(block.getLocation());
			
			if (terr != null)
			{
				if (!ClansManager.getInstance().getBlacklist().allowed(terr.Owner))
				{
					continue;
				}
				ClanInfo clan = ClansManager.getInstance().getClanUtility().getOwner(terr);
				if (clan != null && !ClansManager.getInstance().getWarManager().isBeingBesiegedBy(clan, _weapon.getOwner()))
				{
					continue;
				}
			}
			blocks.add(block);
		}
		
		ClansManager.getInstance().runSyncLater(() ->
		{
			CraterExplodeEvent event = UtilServer.CallEvent(new CraterExplodeEvent(_weapon, _cause, _origin, blocks));
			UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, _origin, null, 0, 1, ViewDist.NORMAL);
			for (Block block : event.getBlocks())
			{
				if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE || block.getType() == Material.BED_BLOCK)
				{
					block.breakNaturally();
				}
				else if (block.getType() == Material.SMOOTH_BRICK)
				{
					if (block.getData() != 2)
					{
						block.setTypeIdAndData(98, (byte)2, true);
					}
					else
					{
						block.breakNaturally();
					}
				}
				else
				{
					if (Math.random() <= .3)
					{
						block.breakNaturally();
					}
					else
					{
						block.setType(Material.AIR);
					}
				}
			}
		}, 1L);
		
		Map<LivingEntity, Double> hitMap = UtilEnt.getInRadius(_origin, 3.5);
		for (LivingEntity hit : hitMap.keySet())
		{
			ClansManager.getInstance().getDamageManager().NewDamageEvent(hit, _cause, null, DamageCause.ENTITY_EXPLOSION, 7 / hitMap.get(hit), true, true, false, _cause.getName(), "Siege Cannon");
		}
	}
}