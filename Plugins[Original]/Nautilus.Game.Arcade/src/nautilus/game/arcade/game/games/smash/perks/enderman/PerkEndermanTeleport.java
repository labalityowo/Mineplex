package nautilus.game.arcade.game.games.smash.perks.enderman;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkEndermanTeleport extends SmashPerk
{

	private int _cooldown = 5000;
	private float _chargeTick = 0.015F;

	private Map<UUID, Block> _target = new HashMap<>();
	private Map<UUID, Float> _charge = new HashMap<>();

	public PerkEndermanTeleport()
	{
		super("Teleport", new String[] {C.cYellow + "Hold Sneak" + C.cGray + " to " + C.cGreen + "Teleport"});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_chargeTick = getPerkFloat("Charge Per Tick");
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			if (!hasPerk(player) || UtilPlayer.isSpectator(player))
			{
				continue;
			}

			UUID key = player.getUniqueId();

			if (!player.isSneaking() || !Recharge.Instance.usable(player, GetName()))
			{
				_target.remove(key);
				_charge.remove(key);
				continue;
			}

			Block block = UtilPlayer.getTarget(player, UtilBlock.blockPassSet, 100);

			if (!_target.containsKey(key) || !_charge.containsKey(key))
			{
				if (block == null || block.getType() == Material.AIR)
				{
					continue;
				}

				_target.put(key, block);
				_charge.put(key, 0f);
			}

			// Invalid Block - End
			if (block == null || block.getType() == Material.AIR)
			{
				_target.remove(key);
				_charge.remove(key);
			}

			// Same Block - Increase Charge
			else if (block.equals(_target.get(key)))
			{
				_charge.put(key, _charge.get(key) + _chargeTick);

				UtilTextMiddle.display(null, UtilTextMiddle.progress(_charge.get(key)), 0, 10, 10, player);

				if (_charge.get(key) >= 1)
				{
					UtilTextMiddle.display(null, C.cGreen + "Teleported", 0, 10, 10, player);
					Recharge.Instance.useForce(player, GetName(), _cooldown);

					while (block.getRelative(BlockFace.UP).getType() != Material.AIR)
					{
						block = block.getRelative(BlockFace.UP);
					}

					player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 0.5f);
					player.teleport(block.getLocation().add(0.5, 1, 0.5).setDirection(player.getLocation().getDirection()));
					player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 0.5f);

					UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, player.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0.1f, 100, ViewDist.LONG);
				}
				else
				{
					player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f + _charge.get(key));
					UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, player.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0.05f, 10, ViewDist.LONG);
				}
			}
			// New Block - Reset
			else
			{
				_target.put(key, block);
				_charge.put(key, 0f);
			}
		}
	}

	@Override
	public void unregisteredEvents()
	{
		_target.clear();
		_charge.clear();
	}

	@EventHandler
	public void clean(PlayerQuitEvent event)
	{
		UUID key = event.getPlayer().getUniqueId();

		_target.remove(key);
		_charge.remove(key);
	}
}
