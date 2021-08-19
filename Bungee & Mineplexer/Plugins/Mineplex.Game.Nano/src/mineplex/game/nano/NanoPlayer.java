package mineplex.game.nano;

import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.disguise.disguises.DisguiseBase;

public class NanoPlayer
{

	public static void clear(NanoManager manager, Player player)
	{
		// Game Mode
		player.setGameMode(GameMode.ADVENTURE);

		// Inventory
		UtilPlayer.clearInventory(player);
		UtilPlayer.closeInventoryIfOpen(player);

		// Potion Effects
		manager.getConditionManager().EndCondition(player, null, null);
		UtilPlayer.clearPotionEffects(player);

		// Falling
		player.setFallDistance(0);

		// Vehicles
		player.eject();
		player.leaveVehicle();

		// Level
		player.setLevel(0);
		player.setExp(0);

		// Heath
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());

		// Food
		player.setFoodLevel(20);
		player.setExhaustion(0);

		// Movement
		player.setSprinting(false);
		player.setSneaking(false);

		// Client Side
		player.resetPlayerTime();
		player.resetPlayerWeather();

		// Remove Arrows
		((CraftPlayer) player).getHandle().o(0);

		// Flight
		player.setFlySpeed(0.1F);
		player.setFlying(false);
		player.setAllowFlight(false);

		// Things that could be affected by the current tick
		manager.runSyncLater(() ->
		{
			player.setFireTicks(0);
			UtilAction.zeroVelocity(player);
		}, 0);

		// Disguise
		DisguiseBase disguise = manager.getDisguiseManager().getActiveDisguise(player);

		if (disguise != null)
		{
			manager.getDisguiseManager().undisguise(disguise);
		}
	}

	public static void setSpectating(Player player, boolean spectating)
	{
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		entityPlayer.spectating = spectating;
		entityPlayer.setGhost(spectating);
		entityPlayer.k = !spectating;
	}

}
