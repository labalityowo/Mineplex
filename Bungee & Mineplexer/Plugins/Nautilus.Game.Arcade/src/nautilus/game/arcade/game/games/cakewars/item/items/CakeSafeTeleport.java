package nautilus.game.arcade.game.games.cakewars.item.items;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.item.CakeSpecialItem;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeSafeTeleport extends CakeSpecialItem implements Listener
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.EYE_OF_ENDER)
			.setTitle(C.cYellowB + "Safe Teleport")
			.addLore("", "Teleports you have to a safe location", "if you fall into the void.", "Warning! Safe Teleport has a", C.cRed + "20 second" + C.cGray + " cooldown between uses.", "Uses: " + C.cRed + "1")
			.build();

	private final Map<Player, Location> _safeLocations;

	public CakeSafeTeleport(CakeWars game)
	{
		super(game, ITEM_STACK, "Safe Teleport", TimeUnit.SECONDS.toMillis(20));

		_safeLocations = new HashMap<>();
	}

	@Override
	protected void setup()
	{
		UtilServer.RegisterEvents(this);
	}

	@Override
	protected void cleanup()
	{
		_safeLocations.clear();
		UtilServer.Unregister(this);
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, CakeTeam cakeTeam)
	{
		event.setCancelled(true);

		Player player = event.getPlayer();
		Location location = _safeLocations.get(player);

		if (location == null)
		{
			return false;
		}

		player.teleport(location.add(0, 1.5, 0));
		player.setFallDistance(0);
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0);
		UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, player.getEyeLocation(), 0.5F, 0.5F, 0.5F, 0.5F, 10, ViewDist.NORMAL);
		return true;
	}

	@EventHandler
	public void updateSafeLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : _game.GetPlayers(true))
		{
			if (!player.isOnline() || UtilPlayer.isSpectator(player) || !UtilEnt.isGrounded(player))
			{
				continue;
			}

			_safeLocations.put(player, player.getLocation());
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_safeLocations.remove(event.getPlayer());
	}
}
