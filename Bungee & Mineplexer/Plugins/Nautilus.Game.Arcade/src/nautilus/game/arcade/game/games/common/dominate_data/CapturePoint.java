package nautilus.game.arcade.game.games.common.dominate_data;

import java.util.ArrayList;
import java.util.Collection;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTextMiddle;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.champions.events.CaptureEvent;
import nautilus.game.arcade.game.games.common.Domination;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class CapturePoint 
{
	private Domination Host;

	private String _name;

	//Locations
	private ArrayList<Block> _floor = new ArrayList<Block>();
	private ArrayList<Block> _indicators = new ArrayList<Block>();
	private Location _loc;

	//Capture
	private double _captureMax = 24;
	private double _captureRate = 1;
	private double _captureAmount = 0;
	private GameTeam _owner = null;
	private boolean _captured = false;
	private ArrayList<Block> _captureFloor = new ArrayList<Block>();
	private long _decayDelay = 0;

	private int _indicatorTick = 0;

	private ChatColor _scoreboardColor = ChatColor.WHITE;
	private int _scoreboardTick = 0;

	public CapturePoint(Domination host, String name, Location loc)
	{
		Host = host;

		_name = name;

		for (int x=-3 ; x<= 3 ; x++)
		{
			for (int z=-3 ; z<= 3 ; z++)
			{
				//Indicators
				if (Math.abs(x) == 3 && Math.abs(z) == 3)
				{
					Block ind = loc.getBlock().getRelative(x, 3, z);
					ind.setType(Material.WOOL);
					_indicators.add(ind);
				}

				//Floors
				if (Math.abs(x) <= 2 && Math.abs(z) <= 2)
				{
					if (x != 0 || z != 0)
					{
						Block floor = loc.getBlock().getRelative(x, -2, z);
						floor.setType(Material.WOOL);
						_floor.add(floor);
					}
					else
					{
						Block block = loc.getBlock().getRelative(x, -2, z);
						block.setType(Material.BEACON);
					}
				}

				//Glass
				if (Math.abs(x) <= 2 && Math.abs(z) <= 2)
				{
					Block block = loc.getBlock().getRelative(x, -1, z);
					block.setType(Material.STAINED_GLASS);
				}

				//Iron
				if (Math.abs(x) <= 1 && Math.abs(z) <= 1)
				{
					Block block = loc.getBlock().getRelative(x, -3, z);
					block.setType(Material.IRON_BLOCK);
				}
			}
		}

		//Firework
		_loc = loc;
	}

	public void Update()
	{
		CaptureUpdate();
		Points();
	}

	private void Points()
	{
		if (!_captured)
			return;

		Host.AddScore(_owner, 4);
	}

	private void CaptureUpdate() 
	{
		//Who's on the CP?
		GameTeam teamA = null;
		ArrayList<Player> playersA = new ArrayList<Player>();

		GameTeam teamB = null;
		ArrayList<Player> playersB = new ArrayList<Player>();

		for (GameTeam team : Host.GetTeamList())
		{
			for (Player player : team.GetPlayers(true))
			{
				if (Host.Manager.isSpectator(player))
					continue;

				if (Math.abs(_loc.getX() - player.getLocation().getX()) > 2.5)
					continue;

				if (Math.abs(_loc.getY() - player.getLocation().getY()) > 2.5)
					continue;

				if (Math.abs(_loc.getZ() - player.getLocation().getZ()) > 2.5)
					continue;

				if (teamA == null || teamA.equals(team))
				{
					teamA = team;
					playersA.add(player);
				}
				else
				{
					teamB = team;
					playersB.add(player);
				}
			}
		}

		//No one around (DEGENERATE CAPTURE)
		if (teamA == null)
		{
			if (_captureAmount > 0)
				RegenDegen();

			return;
		}

		//Capture
		if (teamB == null)
			Capture(teamA, playersA.size(), playersA);

		else if (playersA.size() > playersB.size())
			Capture(teamA, playersA.size()-playersB.size(), playersA);

		else if (playersB.size() > playersA.size())
			Capture(teamB, playersB.size()-playersA.size(), playersB);
	}

	private void RegenDegen() 
	{
		if (!UtilTime.elapsed(_decayDelay, 2000))
			return;

		//Degen
		if (!_captured)
		{
			_captureAmount = Math.max(0, (_captureAmount - (_captureRate*1)));

			//Floor Color
			while ((double)_captureFloor.size()/((double)_captureFloor.size() + (double)_floor.size()) > _captureAmount/_captureMax)
			{
				Block block = _captureFloor.remove(UtilMath.r(_captureFloor.size()));

				_floor.add(block);

				setWoolColor(block, null, false);
			}

			//Set Uncaptured
			if (_captureAmount == 0)
			{
				_owner = null;

				//Indicators
				for (Block block : _indicators)
				{
					block.setData((byte)0);
				}
			}

			//Effect
			for (Block block : _indicators)
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 35);
		}
		//Regen
		else if (_captureAmount < _captureMax)
		{
			_captureAmount = Math.min(_captureMax, (_captureAmount + (_captureRate*1)));

			//Floor Color
			while ((double)_captureFloor.size()/((double)_captureFloor.size() + (double)_floor.size()) < _captureAmount/_captureMax)
			{
				Block block = _floor.remove(UtilMath.r(_floor.size()));

				_captureFloor.add(block);

				setWoolColor(block, _owner.GetColor(), false);
			}

			//Effect
			for (Block block : _indicators)
			{
				if (_owner.GetColor() == ChatColor.RED)
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 152);
				else
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 22);
			}

		}	
	}

	public void Capture(GameTeam team, int count, Collection<Player> capturers)
	{
		_scoreboardColor = team.GetColor();

		//Decay Delay
		_decayDelay = System.currentTimeMillis();

		//Defend Score
		//if (_captured)
		//	Host.AddScore(_owner, count);

		//Color
		Color color = Color.RED;
		if (team.GetColor() == ChatColor.BLUE)
			color = Color.BLUE;

		//Count Up
		if (_owner != null && _owner.equals(team))
		{
			//Given if the other team hadnt actually captured it fully
			int bonus = 0;
			if (_captured)
				bonus = 1;

			_captureAmount = Math.min(_captureMax, (_captureAmount + ((_captureRate*count)+bonus)));

			//Floor Color
			while ((double)_captureFloor.size()/((double)_captureFloor.size() + (double)_floor.size()) < _captureAmount/_captureMax)
			{
				Block block = _floor.remove(UtilMath.r(_floor.size()));

				_captureFloor.add(block);

				setWoolColor(block, team.GetColor(), false);
			}

			//Set Fully Captured
			if (_captureAmount == _captureMax && !_captured)
			{
				_captured = true;

				//Firework
				Firework(_loc, color, true);

				//Indicators
				for (Block block : _indicators)
				{
					if (team.GetColor() == ChatColor.RED)	block.setData((byte)14);
					else									block.setData((byte)11);
				}

				//Center
				setWoolColor(_loc.getBlock().getRelative(0, -2, 0), _owner.GetColor(), true);

				//Reward Gems
				if (capturers != null)
				{
					for (Player player : capturers)
					{
						RewardCapture(player, 30);
					}
				}
				
				UtilTextMiddle.display(null, _owner.GetColor() + _owner.GetName() + " captured " + _name, 5, 40, 5);
				
				UtilServer.CallEvent(new CaptureEvent(capturers));
			}
		}
		//Count Down
		else
		{
			//Given if the other team hadnt actually captured it fully
			int bonus = 0;
			if (!_captured)
				bonus = 1;

			_captureAmount = Math.max(0, (_captureAmount - ((_captureRate*count)+bonus)));

			//Announce
			if (_owner != null && _captureFloor.size() >= 24)
			{
				for (Player player : _owner.GetPlayers(false))
				{
					UtilPlayer.message(player, C.Bold + _name + " is being captured...");
					player.playSound(player.getLocation(), Sound.GHAST_SCREAM2, 0.6f, 0.6f);
				}
			}

			//Floor Color
			while ((double)_captureFloor.size()/((double)_captureFloor.size() + (double)_floor.size()) > _captureAmount/_captureMax)
			{
				Block block = _captureFloor.remove(UtilMath.r(_captureFloor.size()));

				_floor.add(block);

				setWoolColor(block, null, false);
			}

			//Set Uncaptured
			if (_captureAmount == 0)
			{
				_captured = false;
				_owner = team;
				
				//Center
				setWoolColor(_loc.getBlock().getRelative(0, -2, 0), null, true);

				//Indicators
				for (Block block : _indicators)
				{
					block.setData((byte)0);
				}
			}
		}

		if (_captureAmount != _captureMax)
		{
			//Reward 
			if (capturers != null)
			{
				for (Player player : capturers)
				{
					RewardCapture(player, 1);
				}
			}

			Indicate(color);
		}

	}

	private void setWoolColor(Block block, ChatColor color, boolean glassOnly)
	{
		if (color == null)				
		{
			if (!glassOnly)
				block.setData((byte)0);
			block.getRelative(BlockFace.UP).setTypeIdAndData(95, (byte)0, true);
		}
		else if (color == ChatColor.RED)		
		{
			if (!glassOnly)
				block.setData((byte)14);
			block.getRelative(BlockFace.UP).setTypeIdAndData(95, (byte)14, true);
		}
		else								
		{
			if (!glassOnly)
				block.setData((byte)11);
			block.getRelative(BlockFace.UP).setTypeIdAndData(95, (byte)11, true);
		}
	}

	public void Firework(Location loc, Color color, boolean major)
	{
		if (!major)
			UtilFirework.playFirework(loc, FireworkEffect.builder().flicker(false).withColor(color).with(Type.BURST).trail(false).build());
		else
			UtilFirework.playFirework(loc, FireworkEffect.builder().flicker(true).withColor(color).with(Type.BALL_LARGE).trail(true).build());
	}

	public void Indicate(Color color)
	{
		//Effect
		for (Block block : _indicators)
			if (color == Color.RED)
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 152);
			else
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 22);

		//for (Block block : _indicators)
		//Firework(_indicators.get(_indicatorTick).getLocation().add(0.5, 0.5, 0.5), color, false);

		_indicatorTick = (_indicatorTick + 1)%_indicators.size();
	}

	public String GetScoreboardName()
	{		
		_scoreboardTick = (_scoreboardTick + 1)%2;

		String out = "";

		if (_scoreboardColor != null && _scoreboardTick == 0)
		{
			if (_scoreboardColor == ChatColor.BLUE)
				_scoreboardColor = ChatColor.AQUA;

			out = _scoreboardColor + C.Bold + _name;
			_scoreboardColor = null;
		}
		else
		{
			if (_captured)
			{
				if (_owner.GetColor() == ChatColor.BLUE)
					out = ChatColor.AQUA + _name;
				else
					out = _owner.GetColor() + _name;
			}

			else
				out = _name;
		}

		if (out.length() > 16)
			out = out.substring(0, 16);

		return out;
	}

	public void RewardCapture(Player player, int amount)
	{
		Host.AddGems(player, amount / 25d, "Control Point Score", true, true);
		Host.GetStats(player).CaptureScore += amount;
	}

	public Location getLocation() 
	{
		return _loc.clone();
	}

	public String getRadarTag()
	{
		String out = "";
		
		if (_owner != null)
		{
			if (_owner.GetColor() == ChatColor.BLUE)
				out += ChatColor.AQUA;
			else
				out += _owner.GetColor();
		}
			
		
		return out + _name.substring(0, Math.min(1, _name.length()));
	}
}
