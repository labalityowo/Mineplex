package mineplex.core.gadget.gadgets.item;

import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.recharge.Recharge;

public class ItemBallCatch extends ItemGadget
{

	private static final double VELOCITY_FACTOR = 0.25;
	private static final double VELOCITY_INCREASE_FACTOR = 1.05;
	private static final double VELOCITY_Y = 0.2;
	private static final double VELOCITY_Y_DECREASE = 0.005;
	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(10);

	private final Map<ArmorStand, BallData> _balls;

	public ItemBallCatch(GadgetManager manager)
	{
		super(manager, "Play Catch", new String[]
				{
						C.cGray + "Play Catch",
						C.cGray + "with other players!",
						C.blankLine,
						C.cWhite + "Left click hit the ball.",
						C.cWhite + "You cannot hit the ball twice",
						C.cWhite + "in a row.",
						C.cWhite + "Try and keep it in the air",
						C.cWhite + "for as long as you can!"
				}, CostConstants.POWERPLAY_BONUS, Material.WOOL, (byte) 14, 500, null);

		_balls = new HashMap<>();

		Free = false;
		setDisplayItem(SkinData.BEACH_BALL.getSkull());
		setPPCYearMonth(YearMonth.of(2018, Month.FEBRUARY));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		_balls.entrySet().removeIf(entry ->
		{
			if (entry.getValue().Shooter.equals(player))
			{
				entry.getKey().remove();
				return true;
			}

			return false;
		});
	}

	@Override
	public boolean activatePreprocess(Player player)
	{
		return _balls.values().stream()
				.noneMatch(data -> data.Shooter.equals(player));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Location location = player.getLocation().add(0, 0.4, 0);
		location.add(location.getDirection());

		ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
		stand.setHelmet(getDisplayItem());
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);

		BallData data = new BallData(player);
		updateHits(stand, data);

		_balls.put(stand, data);
		UtilTextMiddle.display(C.cGreen + "Punch the ball", "Keep it in the air for as long as you can!", 10 ,40, 10, player);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				Vector direction = data.Direction;

				if (data.Last == null || direction == null)
				{
					return;
				}

				Location newLocation = stand.getLocation();

				direction.setY(direction.getY() - VELOCITY_Y_DECREASE);
				stand.teleport(newLocation.add(direction));

				if (!stand.isValid() || UtilBlock.solid(newLocation.add(0, 1.5, 0).getBlock()))
				{
					String message = F.main(Manager.getName(), "You managed to keep the ball in the air for " + F.count(data.Hits) + " passes, " + F.time(UtilTime.MakeStr(System.currentTimeMillis() - data.Start)) + " with " + F.count(data.Players.size()) + " playing the game!");
					data.Players.forEach(player1 ->
					{
						player1.playSound(player1.getLocation(), Sound.NOTE_PLING, 1, 0.3F);
						player1.sendMessage(message);
					});
					_balls.remove(stand);
					stand.remove();
					cancel();
					Recharge.Instance.use(player, getName(), COOLDOWN, true, true);
				}
			}
		}, 10, 1);
	}

	@EventHandler
	public void interactBall(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
		{
			return;
		}

		Entity entity = event.getEntity();
		Player player = (Player) event.getDamager();
		BallData data = _balls.get(entity);

		if (data == null)
		{
			return;
		}

		if (data.Last != null && data.Last.equals(player))
		{
			player.sendMessage(F.main(Manager.getName(), "You cannot hit the ball twice in a row!"));
			return;
		}

		Vector newDirection = player.getLocation().getDirection();
		newDirection.setX(newDirection.getX() * data.VelocityFactor);
		newDirection.setY(VELOCITY_Y);
		newDirection.setZ(newDirection.getZ() * data.VelocityFactor);

		if (data.Start == 0)
		{
			data.Start = System.currentTimeMillis();
		}

		data.Hits++;
		data.Last = player;
		data.Direction = newDirection;
		data.VelocityFactor *= VELOCITY_INCREASE_FACTOR;
		data.Players.add(player);
		player.getWorld().playSound(entity.getLocation().add(0, 1, 0), Sound.ORB_PICKUP, 1, 1.4F);
		updateHits(entity, data);
	}

	@EventHandler
	public void armourStandInteract(PlayerArmorStandManipulateEvent event)
	{
		for (ArmorStand stand : _balls.keySet())
		{
			if (stand.equals(event.getRightClicked()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	private void updateHits(Entity stand, BallData data)
	{
		stand.setCustomName(C.cGreen + data.Hits + " Hit" + (data.Hits == 1 ? "" : "s"));
	}

	private class BallData
	{

		Player Shooter;
		Player Last;
		int Hits;
		long Start;
		double VelocityFactor;
		Vector Direction;
		Set<Player> Players;

		BallData(Player shooter)
		{
			Shooter = shooter;
			VelocityFactor = VELOCITY_FACTOR;
			Players = new HashSet<>();
		}
	}

}
