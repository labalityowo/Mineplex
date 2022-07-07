package mineplex.game.clans.clans.worldevent.capturepoint;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.api.EventState;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;

public class CapturePointEvent extends WorldEvent
{
	private static final long MAX_TICKS = UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.SECONDS) * 20;
	private Player _capturing = null;
	private Player _winner = null;
	private Pair<Material, Byte> _resetData = null;
	private PointBoundary _boundary = null;
	private long _ticks = 0;
	
	public CapturePointEvent(WorldEventManager manager)
	{
		super("Capture Point", CapturePointLocation.getRandomLocation().toLocation(UtilWorld.getWorld("world")), 5, true, manager.getDisguiseManager(), manager.getClans().getProjectile(), manager.getDamage(), manager.getBlockRestore(), manager.getClans().getCondition());
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void customStart()
	{
		Block set = getCenterLocation().getBlock().getRelative(BlockFace.DOWN);
		_resetData = Pair.create(set.getType(), set.getData());
		set.setType(Material.BEACON);
		double minX = (getCenterLocation().getBlockX() + 0.5) - 4.5;
		double maxX = (getCenterLocation().getBlockX() + 0.5) + 4.5;
		double minZ = (getCenterLocation().getBlockZ() + 0.5) - 4.5;
		double maxZ = (getCenterLocation().getBlockZ() + 0.5) + 4.5;
		double minY = getCenterLocation().getBlockY() + 1;
		double maxY = getCenterLocation().getBlockY() + 4;
		_boundary = new PointBoundary(minX, maxX, minZ, maxZ, minY, maxY);
	}
	
	private boolean isEligible(Player player)
	{
		if (ClansManager.getInstance().hasTimer(player))
		{
			if (Recharge.Instance.use(player, "PvP Timer Inform NoCapturePoint", 5000, false, false))
			{
				UtilPlayer.message(player, F.main(getName(), "You cannot participate in the Capture Point whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			}
			return false;
		}
		
		return true;
	}

	@Override
	protected void customTick()
	{
		if (getState() != EventState.LIVE)
		{
			return;
		}
		if (_capturing == null)
		{
			Optional<? extends Player> opt = Bukkit.getOnlinePlayers().stream()
																	.filter(_boundary::isInBoundary)
																	.filter(this::isEligible)
																	.findAny();
			if (opt.isPresent())
			{
				_capturing = opt.get();
				Recharge.Instance.useForce(_capturing, "Capture Point Alert", 30000);
				announceMessage(F.name(_capturing.getName()) + " is capturing the point!");
			}
		}
		else
		{
			if (!_boundary.isInBoundary(_capturing) || _capturing.isDead() || !_capturing.isOnline() || !_capturing.isValid())
			{
				announceMessage(F.name(_capturing.getName()) + " has lost the point!");
				_capturing = null;
				_ticks = 0;
			}
			else
			{
				_ticks++;
				updateLastActive();
				if (_ticks >= MAX_TICKS)
				{
					_winner = _capturing;
					UtilTextTop.display(C.cGoldB + "Capturing Point: " + C.cWhite + "100%", _capturing);
					stop();
					return;
				}
				else if (Recharge.Instance.use(_capturing, "Capture Point Alert", 30000, false, false))
				{
					announceMessage(F.name(_capturing.getName()) + " is still capturing the point!");
				}
				double percentage = ((double)_ticks) / ((double)MAX_TICKS);
				percentage *= 100;
				UtilTextTop.display(C.cGoldB + "Capturing Point: " + C.cWhite + (int)Math.floor(percentage) + "%", _capturing);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void customCleanup(boolean onDisable)
	{
		Block reset = getCenterLocation().getBlock().getRelative(BlockFace.DOWN);
		reset.setTypeIdAndData(_resetData.getLeft().getId(), _resetData.getRight(), false);
	}

	@Override
	protected void customStop()
	{
		if (_winner != null)
		{
			announceMessage(F.name(_winner.getName()) + " has captured the point!");
			for (int i = 0; i < 6; i++)
			{
				ClansManager.getInstance().getLootManager().dropCapturePoint(_winner.getLocation());
			}
		}
		else
		{
			announceMessage("Nobody was able to capture the point this time!");
		}
		_winner = null;
		_capturing = null;
		_resetData = null;
	}
	
	private class PointBoundary
	{
		private final double _minX, _maxX, _minZ, _maxZ, _minY, _maxY;
		
		public PointBoundary(double minX, double maxX, double minZ, double maxZ, double minY, double maxY)
		{
			_minX = minX;
			_maxX = maxX;
			_minZ = minZ;
			_maxZ = maxZ;
			_minY = minY;
			_maxY = maxY;
		}
		
		public boolean isInBoundary(Player player)
		{
			double x = player.getLocation().getX();
			double z = player.getLocation().getZ();
			double y = player.getLocation().getY();
			
			return (x <= _maxX && x >= _minX) && (z <= _maxZ && z >= _minZ) && (y <= _maxY && y >= _minY);
		}
	}
}