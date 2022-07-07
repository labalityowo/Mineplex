package mineplex.hub.doublejump;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseBat;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.disguise.disguises.DisguiseEnderman;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.gadget.event.GadgetBlockEvent;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.preferences.Preference;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;

public class JumpManager extends MiniPlugin
{

	public final HubManager Manager;
	private final Set<String> _preparedDoubleJump = new HashSet<>();

	public JumpManager(HubManager manager)
	{
		super("Double Jump", manager.getPlugin());

		Manager = manager;
	}

	@EventHandler
	public void toggleFlight(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE || player.isFlying())
		{
			return;
		}

		if (Manager.getPreferences().get(player).isActive(Preference.INVISIBILITY) && Manager.GetClients().Get(event.getPlayer()).hasPermission(Preference.INVISIBILITY))
		{
			return;
		}

		//Chicken Cancel
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);
		if (disguise != null &&
				((disguise instanceof DisguiseChicken && !((DisguiseChicken) disguise).isBaby()) || disguise instanceof DisguiseBat || disguise instanceof DisguiseEnderman || disguise instanceof DisguiseWither))
			return;

		event.setCancelled(true);

		//Disable Flight
		player.setFlying(false);
		player.setAllowFlight(false);

		Vector vec = player.getLocation().getDirection();
		vec.setY(Math.abs(vec.getY()));

		//Velocity
		_preparedDoubleJump.add(player.getName());
		UtilAction.velocity(player, vec, 1.4, false, 0, 0.2, 1, true);

		//Sound
		player.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);

		Recharge.Instance.useForce(player, "Double Jump", 250);
	}

	@EventHandler
	public void updateFlight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight())
			{
				continue;
			}

			// TODO put this somewhere better
			if (Manager.getPreferences().get(player).isActive(Preference.INVISIBILITY) && Manager.GetClients().Get(player).hasPermission(Preference.INVISIBILITY))
			{
				player.setAllowFlight(true);
				continue;
			}

			DoubleJumpPrepareEvent jumpEvent = new DoubleJumpPrepareEvent(player);
			UtilServer.CallEvent(jumpEvent);

			if (jumpEvent.isCancelled())
			{
				player.setFlying(false);
				player.setAllowFlight(false);
				continue;
			}

			//if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
			if (UtilEnt.onBlock(player) && !player.getAllowFlight() && Recharge.Instance.usable(player, "Double Jump"))
			{
				player.setAllowFlight(true);
				player.setFlying(false);
				_preparedDoubleJump.remove(player.getName());
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		_preparedDoubleJump.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void jumpPad(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getTo().getBlock();

		if (block.getType() == Material.GOLD_PLATE && Recharge.Instance.use(player, "Jump Pad", 500, false, false))
		{
			Location location = player.getLocation();
			Vector direction = location.getDirection().multiply(4);
			direction.setY(1.2);
			player.getWorld().playSound(location, Sound.CHICKEN_EGG_POP, 2, 0.5F);
			UtilAction.velocity(player, direction);
			Manager.getMissionManager().incrementProgress(player, 1, MissionTrackerType.LOBBY_JUMP_PAD, null, null);
		}
	}

	@EventHandler
	public void gadgetBlock(GadgetBlockEvent event)
	{
		event.getBlocks().removeIf(block -> block.getRelative(BlockFace.DOWN).getType() == Material.GOLD_PLATE);
	}

	public boolean hasDoubleJumped(Player player)
	{
		return _preparedDoubleJump.contains(player.getName());
	}

	public boolean isDoubleJumping(Player player)
	{
		return !Recharge.Instance.usable(player, "Double Jump");
	}
}