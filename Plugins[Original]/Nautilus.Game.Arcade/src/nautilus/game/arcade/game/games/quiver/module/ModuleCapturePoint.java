package nautilus.game.arcade.game.games.quiver.module;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;

public class ModuleCapturePoint extends QuiverTeamModule
{

	private static final String DATA_POINT_CENTER = "BLACK";
	private static final String DATA_POINT_CICRLE = "GRAY";

	private static final double CIRCLE_INCREMENTATION = Math.PI / 40;

	private static final int TO_CAPTURE = 25;

	private static final float ULTIMATE_PERCENTAGE_CAPTURE = 0.2F;

	private Location _centre;
	private Location _circlePoint;
	private Block _beaconColour;
	private double _radius;

	private int _redPlayers;
	private int _bluePlayers;

	// The amount a team has captured is represented as an integer with -25
	// being Team A and 25 being Team B has captured.
	private int _capture;
	private int _captureToReturn;

	private GameTeam _capturedTeam;

	private CaptureState _captureState;

	public ModuleCapturePoint(QuiverTeamBase base)
	{
		super(base);
	}

	@Override
	public void setup()
	{
		_centre = getBase().WorldData.GetDataLocs(DATA_POINT_CENTER).get(0);
		_circlePoint = getBase().WorldData.GetDataLocs(DATA_POINT_CICRLE).get(0);
		_beaconColour = _centre.getBlock().getRelative(BlockFace.DOWN);

		_beaconColour.setType(Material.STAINED_GLASS);

		_radius = UtilMath.offset2d(_centre, _circlePoint);
		_capture = 0;
		_captureToReturn = 0;
		_captureState = CaptureState.NONE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update(UpdateType updateType)
	{
		if (updateType != UpdateType.FAST)
		{
			return;
		}

		Bukkit.broadcastMessage(_captureState.name() + " " + _capture);

		GameTeam gameTeamA = getBase().GetTeam(ChatColor.RED), gameTeamB = getBase().GetTeam(ChatColor.AQUA);
		int gameTeamACount = 0, gameTeamBCount = 0;

		for (Player player : UtilPlayer.getNearby(_centre, _radius))
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}

			GameTeam gameTeam = getBase().GetTeam(player);

			if (gameTeamA.equals(gameTeam))
			{
				gameTeamACount++;
			}
			else
			{
				gameTeamBCount++;
			}

			getBase().getQuiverTeamModule(ModuleUltimate.class).incrementUltimate(player, ULTIMATE_PERCENTAGE_CAPTURE);
		}

		_redPlayers = gameTeamACount;
		_bluePlayers = gameTeamBCount;

		displayParticles();

		if (gameTeamACount > gameTeamBCount && gameTeamBCount == 0)
		{
			if (!gameTeamA.equals(_capturedTeam))
			{
				_capture += gameTeamACount;
				_captureState = CaptureState.CAPTURING;
			}
			else
			{
				returnTo();
			}
		}
		else if (gameTeamACount < gameTeamBCount && gameTeamACount == 0)
		{
			if (!gameTeamB.equals(_capturedTeam))
			{
				_capture -= gameTeamBCount;
				_captureState = CaptureState.CAPTURING;
			}
			else
			{
				returnTo();
			}
		}
		else
		{
			if (gameTeamACount > 0 && gameTeamBCount > 0)
			{
				_captureState = CaptureState.CONTESTED;
			}
			else
			{
				returnTo();
			}

			displayProgress();
			return;
		}

		displayProgress();

		GameTeam mostPlayers = null;

		if (_capture == TO_CAPTURE)
		{
			mostPlayers = gameTeamA;
			_captureToReturn = TO_CAPTURE;
		}
		else if (_capture == -TO_CAPTURE)
		{
			mostPlayers = gameTeamB;
			_captureToReturn = -TO_CAPTURE;
		}

		if (mostPlayers != null && _captureState != CaptureState.CAPTURED)
		{
			_capturedTeam = mostPlayers;
			_captureState = CaptureState.CAPTURED;
			_beaconColour.setData(_capturedTeam.GetColorData());
			UtilFirework.playFirework(_centre, Type.BALL_LARGE, _capturedTeam.GetColorBase(), false, false);
			UtilServer.broadcast(mostPlayers.GetFormattedName() + " Captured The Point!");
		}
	}

	@Override
	public void finish()
	{
	}

	public static enum CaptureState
	{
		NONE, CONTESTED, CAPTURING, CAPTURED;
	}

	private void returnTo()
	{
		if (_capture == _captureToReturn)
		{
			return;
		}
		if (_capture > _captureToReturn)
		{
			_capture--;
		}
		else
		{
			_capture++;
		}

		_captureState = _capturedTeam == null ? CaptureState.NONE : CaptureState.CAPTURED;
	}

	private void displayParticles()
	{
		for (double t = 0; t < 2 * Math.PI; t += CIRCLE_INCREMENTATION)
		{
			double x = _radius * Math.cos(t);
			double z = _radius * Math.sin(t);

			_centre.add(x, 0.5, z);

			if (UtilBlock.airFoliage(_centre.getBlock()))
			{
				UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, _centre, 0.05F, 0.05F, 0.05F, 0.001F, 1, ViewDist.NORMAL);
			}

			_centre.subtract(x, 0.5, z);
		}
	}

	private void displayProgress()
	{
		String progress;

		if (_capture == 0)
		{
			progress = C.cWhite;
		}
		else if (_capture > 0)
		{
			progress = C.cRed;
		}
		else
		{
			progress = C.cAqua;
		}

		for (int i = 0; i < TO_CAPTURE; i++)
		{
			if (i == Math.abs(_capture))
			{
				progress += _captureState == CaptureState.CONTESTED ? C.cPurple : C.cWhite;
			}

			progress += "▌";
		}

		UtilTextTop.display(C.cRed + _redPlayers + " " + progress + " " + C.cAqua + _bluePlayers, UtilServer.getPlayers());
	}

	public GameTeam getCapturedTeam()
	{
		return _capturedTeam;
	}

	public CaptureState getState()
	{
		return _captureState;
	}
}
