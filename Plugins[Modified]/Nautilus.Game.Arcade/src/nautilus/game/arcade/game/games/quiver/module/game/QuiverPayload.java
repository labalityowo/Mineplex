package nautilus.game.arcade.game.games.quiver.module.game;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase.GemAwardReason;
import nautilus.game.arcade.game.games.quiver.module.ModulePayload;
import nautilus.game.arcade.game.games.quiver.module.ModulePayload.PayloadState;
import nautilus.game.arcade.game.games.quiver.module.QuiverTeamModule;
import nautilus.game.arcade.scoreboard.GameScoreboard;

public class QuiverPayload extends QuiverTeamModule implements Listener
{

	private static final long GAME_TIMEOUT = 600000;

	private static final int END_EFFECT_DELAY = 100;
	private static final int END_EFFECT_TNT_AMOUNT = 8;
	private static final int END_EFFECT_EXPLOSION_RADIUS = 5;
	private static final int MAX_SCORE = 5;
	
	private static final int PAYLOAD_RESPAWN_DELAY_TICKS = 7 * 20;
	
	private ModulePayload _payload;

	private Set<Block> _lastOvertimeTrack = new HashSet<>();
	private Map<GameTeam, Integer> _teamScore = new HashMap<>();

	private boolean _isEnding;
	private boolean _isOvertime;

	public QuiverPayload(QuiverTeamBase base)
	{
		super(base);
		
		base.DeathSpectateSecs = 5;
		
		_payload = getBase().getQuiverTeamModule(ModulePayload.class);
	}
	
	@Override
	public void updateScoreboard()
	{
		GameScoreboard scoreboard = getBase().GetScoreboard();
		
		scoreboard.reset();
		scoreboard.writeNewLine();

		scoreboard.write(C.cGoldB + "Payload");

		GameTeam teamDirection = _payload.getTeamDirection();
		PayloadState payloadState = _payload.getState();
		
		if (payloadState.equals(PayloadState.CONTESTED))
		{
			scoreboard.write(C.cDPurpleB + "Contested");
		}
		else if (payloadState.equals(PayloadState.RESTARTING))
		{
			scoreboard.write("Respawning...");
		}
		else if (payloadState.equals(PayloadState.NONE))
		{
			scoreboard.write("None");
		}
		else if (teamDirection != null)
		{
			String distance = new DecimalFormat("0.0").format(_payload.getTrackDistanceToMarker(_payload.getDestination(teamDirection)));
			scoreboard.write(teamDirection.GetFormattedName() + ChatColor.RESET + teamDirection.GetColor() + " (" + teamDirection.GetColor() + distance + "m)");
		}
		else
		{
			scoreboard.write("Loading...");
		}

		if (getBase().IsLive())
		{
			scoreboard.writeNewLine();

			if (_isOvertime)
			{
				for (GameTeam gameTeam : _teamScore.keySet())
				{
					scoreboard.write(gameTeam.GetColor() + C.Bold + "Team " + gameTeam.getDisplayName());

					int alivePlayers = 0;

					for (Player player : gameTeam.GetPlayers(true))
					{
						if (UtilPlayer.isSpectator(player))
						{
							continue;
						}

						alivePlayers++;
					}

					scoreboard.write(alivePlayers + "/" + gameTeam.GetPlayers(true).size() + " Alive");
					scoreboard.writeNewLine();
				}
			}
			else
			{
				for (GameTeam gameTeam : _teamScore.keySet())
				{
					int score = Math.min(_teamScore.get(gameTeam), MAX_SCORE);

					scoreboard.write(gameTeam.GetColor() + C.Bold + "Team " + gameTeam.getDisplayName());
					scoreboard.write(score + " Point" + (score == 1 ? "" : "s") + " (" + (MAX_SCORE - score) + ")");
					scoreboard.writeNewLine();
				}
			}

			scoreboard.write(C.cRedB + "Game End");

			if (_isOvertime)
			{
				scoreboard.write(QuiverTeamBase.OVERTIME);
			}
			else
			{
				scoreboard.write(UtilTime.MakeStr(GAME_TIMEOUT - (System.currentTimeMillis() - getBase().GetStateTime())));
			}
		}
		else if (getBase().WinnerTeam != null)
		{
			scoreboard.writeNewLine();
			scoreboard.write(getBase().WinnerTeam.GetFormattedName() + " Won!");
		}

		scoreboard.writeNewLine();
		scoreboard.draw();
	}

	@Override
	public void setup()
	{
		getBase().Manager.registerEvents(this);
		
		for (GameTeam gameTeam : getBase().GetTeamList())
		{
			_teamScore.put(gameTeam, 0);
		}
	}

	@Override
	public void update(UpdateType updateType)
	{
		if (updateType != UpdateType.SEC)
		{
			return;
		}
		
		for (GameTeam gameTeam : _teamScore.keySet())
		{
			if (gameTeam.GetPlayers(true).isEmpty())
			{
				for (GameTeam otherTeam : _teamScore.keySet())
				{
					if (gameTeam.equals(otherTeam))
					{
						continue;
					}
					
					getBase().WinnerTeam = otherTeam;
					awardWinGems();
					displayEndEffect();
				}
			}
			
			if (_payload.isMinecartNearMarker(_payload.getDestination(gameTeam)) && _payload.getTeamDirection().equals(gameTeam) && _payload.getState() != PayloadState.RESTARTING)
			{
				int score = _teamScore.get(gameTeam);

				getBase().WinnerTeam = gameTeam;
				_teamScore.put(gameTeam, ++score);

				if (score == MAX_SCORE)
				{
					awardWinGems();
					displayEndEffect();
				}
				else
				{
					String message = gameTeam.GetFormattedName() + " scored! Payload Respawning...";
					
					UtilTextMiddle.display("", message, 10, 30, 10);
					UtilServer.broadcast(message);
					
					_payload.resetMinecart();
					displayPointScoreEffect();
					
					new BukkitRunnable()
					{

						Location toTeleport = getBase().WorldData.GetDataLocs(ModulePayload.DATA_POINT_PAYLOAD).get(0);

						@Override
						public void run()
						{
							UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, _payload.getMinecart().getLocation().add(0, 1, 0), 0, 0, 0, 1, 1, ViewDist.LONG);
							UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, toTeleport, 0, 0, 0, 1, 1, ViewDist.LONG);
							_payload.getMinecart().teleport(toTeleport);
							_payload.setState(PayloadState.NONE);
							_payload.setMinecartTeam(_payload.getState());
						}
					}.runTaskLater(getBase().Manager.getPlugin(), PAYLOAD_RESPAWN_DELAY_TICKS);
				}
			}
		}
		
		if (_isOvertime)
		{
			List<Location> pathMarkers = _payload.getPathMarkers();
			
			if (_lastOvertimeTrack.isEmpty())
			{
				_lastOvertimeTrack.add(pathMarkers.get(0).getBlock().getRelative(BlockFace.DOWN));
				_lastOvertimeTrack.add(pathMarkers.get(pathMarkers.size() - 1).getBlock().getRelative(BlockFace.DOWN));
			}

			Set<Block> newTracks = new HashSet<>();
			Minecart _minecart = _payload.getMinecart();

			for (Block block : _lastOvertimeTrack)
			{
				if (_payload.isMinecartNearMarker(block.getLocation()))
				{
					Location locationA = UtilAlg.findClosest(_minecart.getLocation(), getBase().WorldData.GetDataLocs(ModulePayload.DATA_POINT_RED));
					Location locationB = UtilAlg.findClosest(_minecart.getLocation(), getBase().WorldData.GetDataLocs(ModulePayload.DATA_POINT_BLUE));

					if (UtilMath.offset(_minecart.getLocation(), locationA) < UtilMath.offset(_minecart.getLocation(), locationB))
					{
						getBase().WinnerTeam = getBase().GetTeam(ChatColor.AQUA);
					}
					else
					{
						getBase().WinnerTeam = getBase().GetTeam(ChatColor.RED);
					}

					_payload.setState(PayloadState.RESTARTING);
					awardWinGems();
					displayEndEffect();
					return;
				}

				for (Block other : UtilBlock.getInRadius(block, 1.5).keySet())
				{
					if (other.getType() == Material.RAILS)
					{
						block.setType(Material.AIR);
						newTracks.add(other);
						UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, block.getLocation().add(0.5, 0.5, 0.5), 0.5F, 0.5F, 0.5F, 0.05F, 12, ViewDist.NORMAL);
					}
				}
			}

			_lastOvertimeTrack = newTracks;
		}

		if (UtilTime.elapsed(getBase().GetStateTime(), GAME_TIMEOUT) && !_isOvertime)
		{
			/*
			 * If a particular gameTeam has scored the most points they are the
			 * winners otherwise if all gameTeam's scores are the same, the game
			 * goes into overtime and after OVERTIME milliseconds the distance
			 * from the minecart and that gameTeams destination is used to
			 * determine the winner.
			 */

			int lastScore = -1;
			boolean same = false;

			for (GameTeam gameTeam : _teamScore.keySet())
			{
				int score = _teamScore.get(gameTeam);

				if (lastScore == -1)
				{
					lastScore = score;
					getBase().WinnerTeam = gameTeam;
				}
				else if (score > lastScore)
				{
					getBase().WinnerTeam = gameTeam;
				}
				else if (score == lastScore)
				{
					same = true;
				}
			}

			if (same)
			{
				String subTitle = C.cRed + "The track will now shrink over time!";

				UtilTextMiddle.display(QuiverTeamBase.OVERTIME, subTitle, 10, 30, 10);
				UtilServer.broadcast(QuiverTeamBase.OVERTIME + " " + subTitle);

				_isOvertime = true;
				return;
			}

			getBase().AnnounceEnd(getBase().WinnerTeam);
			getBase().SetState(GameState.End);
			return;
		}
	}

	@Override
	public void finish()
	{
		UtilServer.Unregister(this);
	}
	
	public void displayPointScoreEffect()
	{
		if (_isEnding)
		{
			return;
		}
		
		Minecart minecart = _payload.getMinecart();
		Location location = minecart.getLocation().add(0, 1, 0);

		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, 0, 0, 0, 1, 1, ViewDist.LONG);
		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, 0, 0, 0, 1, 200, ViewDist.LONG);

		for (Player player : UtilPlayer.getNearby(minecart.getLocation(), 15))
		{
			if (!getBase().IsAlive(player))
			{
				continue;
			}

			UtilAction.velocity(player, UtilAlg.getTrajectory(location, player.getLocation()).normalize().multiply(UtilMath.offset(location, player.getLocation())).setY(1.5));
		}
	}

	public void displayEndEffect()
	{
		if (_isEnding)
		{
			return;
		}

		Random random = new Random();
		Minecart minecart = _payload.getMinecart();

		_isEnding = true;
		minecart.setDisplayBlock(null);

		for (int i = 0; i < END_EFFECT_TNT_AMOUNT; i++)
		{
			TNTPrimed tntPrimed = minecart.getWorld().spawn(minecart.getLocation().add(0, 1, 0), TNTPrimed.class);

			tntPrimed.setVelocity(new Vector(random.nextDouble() - 0.5, random.nextDouble() / 2, random.nextDouble() - 0.5));
			tntPrimed.setFuseTicks((int) (END_EFFECT_DELAY / 2));
			tntPrimed.setYield(END_EFFECT_EXPLOSION_RADIUS);
		}

		new BukkitRunnable()
		{

			@Override
			public void run()
			{
				getBase().AnnounceEnd(getBase().WinnerTeam);
				getBase().SetState(GameState.End);
			}
		}.runTaskLater(getBase().Manager.getPlugin(), END_EFFECT_DELAY);
	}
	
	private void awardWinGems()
	{
		for (Player player : getBase().GetPlayers(true))
		{
			if (!getBase().GetTeam(player).equals(getBase().WinnerTeam))
			{
				continue;
			}

			getBase().AddGems(player, getGems(GemAwardReason.WIN), "Winning Team", false, false);
		}
	}

	public double getGems(GemAwardReason reason)
	{
		switch (reason)
		{
		case KILL:
			return 1;
		case ASSIST:
			return 0.5;
		case KILLSTEAK:
			return 2;
		case WIN:
			return 10;
		default:
			break;
		}

		return 0;
	}
}
