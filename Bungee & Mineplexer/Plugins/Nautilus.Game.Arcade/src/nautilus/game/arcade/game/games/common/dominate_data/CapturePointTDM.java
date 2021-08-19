package nautilus.game.arcade.game.games.common.dominate_data;

import java.util.ArrayList;
import java.util.Collection;

import mineplex.core.common.util.UtilFirework;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.common.TeamDeathmatch;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CapturePointTDM 
{
	private TeamDeathmatch Host;

	private String _name;

	//Locations
	private ArrayList<Block> _floor = new ArrayList<Block>();
	private ArrayList<Block> _indicators = new ArrayList<Block>();
	private Location _loc;


	private int _indicatorTick = 0;

	private boolean _enabled = false;

	private GameTeam _captureTeam = null;
	private int _captureAmount = 0;
	private int _captureMax = 60;


	public CapturePointTDM(TeamDeathmatch host, String name, Location loc)
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

					Block floor = loc.getBlock().getRelative(x, -2, z);
					floor.setType(Material.WOOL);
					_floor.add(floor);

				}

				//Glass
				if (Math.abs(x) <= 2 && Math.abs(z) <= 2)
				{
					Block block = loc.getBlock().getRelative(x, -1, z);
					block.setType(Material.GLASS);
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
		if (_enabled)
			CaptureUpdate();
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

		//Capture
		if (teamB == null && teamA != null)
			Capture(teamA, playersA.size(), playersA);

		else if (teamA == null && teamB != null)
			Capture(teamB, playersB.size(), playersB);

		else if (playersA.size() > playersB.size())
			Capture(teamA, playersA.size()-playersB.size(), playersA);

		else if (playersB.size() > playersA.size())
			Capture(teamB, playersB.size()-playersA.size(), playersB);
	}

	public void Capture(GameTeam team, int count, Collection<Player> capturers)
	{
		if (team == null)
			return;

		if (_captureTeam == null)
			SetTeam(team);

		if (_captureTeam.equals(team))
		{
			_captureAmount = Math.min(_captureMax, (_captureAmount + 1));
		}
		else
		{
			_captureAmount = Math.max(0, (_captureAmount - 1));

			if (_captureAmount == 0)
			{
				SetTeam(team);
			}
		}
		
		//Score Add
		Host.EndCheckScore();

		//Color
		Color color = Color.BLUE;
		if (team.GetColor() == ChatColor.RED)
			color = Color.RED;

		Indicate(color);
	}

	public void SetTeam(GameTeam team)
	{
		_captureTeam = team;

		for (Block block : _floor)
		{
			if (team.GetColor() == ChatColor.RED)
				block.setData((byte) 14);
			else
				block.setData((byte) 11);
		}

		for (Block block : _indicators)
		{
			if (team.GetColor() == ChatColor.RED)
				block.setData((byte) 14);
			else
				block.setData((byte) 11);
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

	public void Enable()
	{
		Block block = _loc.getBlock().getRelative(0, -2, 0);
		block.setType(Material.BEACON);

		_enabled = true;
	}

	public String GetName() 
	{
		return _name;
	}

	public String GetOwnership() 
	{
		if (_captureTeam != null)
			return _captureTeam.GetColor() + "" + _captureAmount + "/" + _captureMax;

		return "0/" + _captureMax;
	}

	public GameTeam GetWinner() 
	{
		if (_captureTeam == null)
			return null;

		if (_captureAmount >= _captureMax)
			return _captureTeam;

		return null;
	}
}
