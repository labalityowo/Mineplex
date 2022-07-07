package nautilus.game.arcade.game.games.sheep;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.GameTeam;

public class SheepData
{
	public SheepGame Host;

	public Sheep Sheep;

	public long LastMoved = 0;

	public Player Holder = null;
	public GameTeam HolderTeam = null;

	public GameTeam Owner = null;
	public ArrayList<Block> OwnerPen = null;
	public Location OwnerPenMiddle = null;

	public Location StuckLocation = null;
	public long StuckTime = System.currentTimeMillis();

	public Location Target = null;

	public SheepData(SheepGame host, Sheep sheep)
	{
		Host = host;
		Sheep = sheep;

		LastMoved = System.currentTimeMillis();

		StuckLocation = Sheep.getLocation();
		StuckTime = System.currentTimeMillis();

		UtilEnt.vegetate(Sheep);
		UtilEnt.ghost(Sheep, true, false);
	}

	public void SetHolder(Player player)
	{
		Holder = player;
		HolderTeam = Host.GetTeam(player);

		if (Holder != null)
			Bukkit.getPluginManager().callEvent(new SheepGame.SheepStolenEvent(Holder));
	}

	public void SetOwner(GameTeam team, ArrayList<Block> locs)
	{
		//Holding sheep in an enemies base.
		if (Sheep.getVehicle() != null)
			if (HolderTeam != null && !HolderTeam.equals(team))
				return;

		Sheep.leaveVehicle();

		Target = null;

		//Already owned, dont do all the stuff
		if (Owner != null && Owner.equals(team))
			return;

		Owner = team;
		OwnerPen = locs;

		//Middle
		Vector vec = new Vector(0, 0, 0);
		for (Block block : locs)
			vec.add(block.getLocation().toVector());
		vec.multiply(1d / (double) locs.size());
		OwnerPenMiddle = vec.toLocation(OwnerPen.get(0).getWorld());
		OwnerPenMiddle.add(0.5, 0, 0.5);

		Sheep.setColor(DyeColor.getByWoolData(team.GetColorData()));

		StuckLocation = Sheep.getLocation();
		StuckTime = System.currentTimeMillis();

		//Effect
		Sheep.getWorld().playSound(Sheep.getLocation(), Sound.SHEEP_IDLE, 2f, 1.5f);
		UtilFirework.playFirework(Sheep.getLocation().add(0, 0.5, 0), FireworkEffect.builder().flicker(false).withColor(team.GetColorBase()).with(Type.BALL).trail(false).build());

		//Reward
		if (Holder != null && HolderTeam != null)
		{
			Host.AddGems(Holder, 3, "Sheep Captured", true, true);

			UtilPlayer.message(Holder, F.main("Game", "You captured a Sheep!"));
			
			UtilTextTop.display(C.cGreen + C.Bold + "You captured a Sheep!", Holder);
		}

		Holder = null;
		HolderTeam = null;
	}

	public boolean Update()
	{
		if (!Sheep.isValid())
			return true;

		Host.GetTeamPen(this);

		if (Sheep.getVehicle() != null)
			return false;

		//No Team - Not picked up for a long time
		if (Owner == null || OwnerPen == null)
		{
			if (UtilMath.offset(Sheep.getLocation(), Host.GetSheepSpawn()) > 14)
			{
				UtilEnt.CreatureMoveFast(Sheep, Host.GetSheepSpawn(), 1.2f);

				if (UtilMath.offset(Sheep.getLocation(), StuckLocation) > 1)
				{
					StuckLocation = Sheep.getLocation();
					StuckTime = System.currentTimeMillis();
				}
				else
				{
					if (UtilTime.elapsed(StuckTime, 1000))
					{
						UtilAction.velocity(Sheep, 0.3, 0.3, 0.7, true);
					}
				}
			}
		}
		else
		{
			if (IsInsideOwnPen())
			{
				LastMoved = System.currentTimeMillis();

				//Roam Around in Pen
				if (Target == null)
					Target = UtilAlg.Random(OwnerPen).getLocation().add(0.5, 0, 0.5);

				if (UtilMath.offset(Sheep.getLocation(), Target) < 1)
					Target = UtilAlg.Random(OwnerPen).getLocation().add(0.5, 0, 0.5);

				UtilEnt.CreatureMoveFast(Sheep, Target, 0.8f);
			}
			else
			{
				UtilEnt.CreatureMoveFast(Sheep, OwnerPenMiddle, 1.2f);

				if (UtilMath.offset(Sheep.getLocation(), StuckLocation) > 1)
				{
					StuckLocation = Sheep.getLocation();
					StuckTime = System.currentTimeMillis();
				}
				else
				{
					if (UtilTime.elapsed(StuckTime, 1000))
					{
						UtilAction.velocity(Sheep, 0.3, 0.3, 0.7, true);
					}
				}
			}
		}

		return false;
	}

	public Block SheepBlock()
	{
		Entity ent = Sheep;

		while (ent.getVehicle() != null)
		{
			ent = ent.getVehicle();
		}

		return ent.getLocation().getBlock();
	}

	public boolean IsInsideOwnPen()
	{
		if (OwnerPen == null)
			return false;

		return OwnerPen.contains(Sheep.getLocation().getBlock());
	}
}
