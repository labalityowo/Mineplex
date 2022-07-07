package nautilus.game.arcade.game.games.skywars.kits.perks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class PerkVoidSaver extends SkywarsPerk
{

	private final Map<Player, Location> _safeLocations = new HashMap<>();

	public PerkVoidSaver(ItemStack itemStack)
	{
		super("Void Saver", itemStack);
	}

	@Override
	public void onUseItem(Player player)
	{
		Location location = _safeLocations.get(player);

		if (location == null)
		{
			return;
		}

		player.setItemInHand(null);
		player.teleport(location.add(0, 2, 0));
		player.setFallDistance(0);
		player.getInventory().remove(Material.EYE_OF_ENDER);
		player.sendMessage(F.main("Game", "You used your safe teleport."));
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0);
		UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, player.getEyeLocation(), 0.5F, 0.5F, 0.5F, 0.5F, 10, ViewDist.NORMAL);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player) || !UtilEnt.isGrounded(player))
			{
				continue;
			}

			_safeLocations.put(player, player.getLocation());
		}
	}
}
