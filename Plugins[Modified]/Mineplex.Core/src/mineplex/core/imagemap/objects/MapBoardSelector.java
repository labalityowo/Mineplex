package mineplex.core.imagemap.objects;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MapBoardSelector implements Listener
{

	private final PlayerMapBoard _board;
	private ArmorStand _goNext;
	private Slime _goNextBox;
	private ArmorStand _goBack;
	private Slime _goBackBox;

	public MapBoardSelector(PlayerMapBoard board)
	{
		_board = board;

		UtilServer.RegisterEvents(this);
	}

	public void createHolograms(Location goNext, Location goBack)
	{
		_goNext = createArmourStand(goNext);
		_goNext.setCustomName(C.cGreen + "Next Page");

		_goNextBox = createSlimeBox(goNext);

		_goBack = createArmourStand(goBack);
		_goBack.setCustomName(C.cGreen + "Previous Page");

		_goBackBox = createSlimeBox(goBack);
	}

	private ArmorStand createArmourStand(Location location)
	{
		ArmorStand stand = location.getWorld().spawn(location.clone().subtract(0, 0.5, 0), ArmorStand.class);

		UtilEnt.vegetate(stand);
		UtilEnt.ghost(stand, true, false);

		stand.setCustomNameVisible(true);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setRemoveWhenFarAway(false);

		return stand;
	}

	private Slime createSlimeBox(Location location)
	{
		Slime slime = location.getWorld().spawn(location.clone().add(0, 1, 0), Slime.class);

		UtilEnt.vegetate(slime);
		UtilEnt.ghost(slime, true, false);
		UtilEnt.setFakeHead(slime, true);
		UtilEnt.silence(slime, true);

		slime.setSize(5);
		slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
		slime.setRemoveWhenFarAway(false);

		return slime;
	}

	public void cleanup()
	{
		_goNext.remove();
		_goNextBox.remove();
		_goBack.remove();
		_goBackBox.remove();
		UtilServer.Unregister(this);
	}

	@EventHandler
	public void onClick(PlayerInteractAtEntityEvent event)
	{
		if (onClick(event.getPlayer(), event.getRightClicked()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onClick(EntityDamageByEntityEvent event)
	{
		if (onClick(event.getDamager(), event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	private boolean onClick(Entity clicker, Entity clicked)
	{
		if (!(clicker instanceof Player))
		{
			return false;
		}

		Player player = (Player) clicker;
		boolean action = false;

		if (!Recharge.Instance.use(player, "Change Page", 500, false, false))
		{
			return true;
		}

		if (_goNextBox != null && _goNextBox.equals(clicked))
		{
			_board.goTo(player, true);
			action = true;
		}
		else if (_goBackBox != null && _goBackBox.equals(clicked))
		{
			_board.goTo(player, false);
			action = true;
		}

		if (action)
		{
			UtilParticle.PlayParticle(ParticleType.CRIT, clicked.getLocation().add(0, 1.5, 0), 0.25F, 0.25F, 0.25F, 0.2F, 15, ViewDist.SHORT, player);
			player.playSound(clicked.getLocation(), Sound.WOOD_CLICK, 1, 1);
		}

		return action;
	}

}
