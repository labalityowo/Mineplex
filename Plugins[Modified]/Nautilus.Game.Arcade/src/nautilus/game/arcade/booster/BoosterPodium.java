package nautilus.game.arcade.booster;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.boosters.Booster;
import mineplex.core.boosters.event.BoosterActivateEvent;
import mineplex.core.boosters.event.BoosterExpireEvent;
import mineplex.core.boosters.tips.BoosterThankManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.SimpleNPC;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class BoosterPodium extends MiniPlugin
{

	private final GameBoosterManager _gameBoosterManager;
	private final HologramManager _hologramManager;
	private final NewNPCManager _npcManager;
	private final Location _podiumLocation;

	private Booster _activeBooster;
	private NPC _npc;
	private Location _npcLocation;
	private ArmorStand _activeArmorStand;
	private Hologram _hologram;

	BoosterPodium(GameBoosterManager gameBoosterManager, HologramManager hologramManager, Location podiumLocation)
	{
		super("Booster Podium");

		_gameBoosterManager = gameBoosterManager;
		_hologramManager = hologramManager;
		_npcManager = require(NewNPCManager.class);
		_podiumLocation = podiumLocation;
		_npcLocation = podiumLocation.clone();

		updateNpcs();
	}

	private void updateNpcs()
	{
		Booster activeBooster = _gameBoosterManager.getActiveBooster();

		if (activeBooster != null)
		{
			if (_npc != null)
			{
				_npcManager.deleteNPC(_npc);
				_npc = null;
			}

			if (_activeArmorStand != null)
			{
				_activeArmorStand.remove();
			}

			if (_hologram == null)
			{
				_hologram = new Hologram(_hologramManager, _npcLocation.clone().add(0, 2.5, 0), true, getHologramText(activeBooster))
						.start();
			}

			ArmorStand armorStand = _podiumLocation.getWorld().spawn(_npcLocation, ArmorStand.class);
			armorStand.setVisible(true);
			armorStand.setCustomNameVisible(false);
			armorStand.setGravity(true);
			armorStand.setArms(true);
			armorStand.setBasePlate(true);
			armorStand.setRemoveWhenFarAway(false);

			armorStand.setHelmet(UtilSkull.getPlayerHead(activeBooster.getPlayerName(), activeBooster.getPlayerName(), null));
			armorStand.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.LIME).build());
			armorStand.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.LIME).build());
			armorStand.setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.LIME).build());

			_activeBooster = activeBooster;
			_activeArmorStand = armorStand;
		}
		else
		{
			// Active booster null!
			if (_activeArmorStand != null)
			{
				_activeArmorStand.remove();
				_activeArmorStand = null;
			}
			if (_hologram != null)
			{
				_hologram.stop();
				_hologram = null;
			}

			if (_npc == null)
			{
				_npc = SimpleNPC.of(_npcLocation, Villager.class, "GAME_BOOSTER");
				_npcManager.addNPC(_npc);

				LivingEntity entity = _npc.getEntity();

				entity.setCustomName(C.cGreen + "Game Amplifiers");
				entity.setCustomNameVisible(true);
			}
		}
	}

	private void updateNpcName()
	{
		if (_activeBooster != null && _hologram != null)
		{
			_hologram.setText(getHologramText(_activeBooster));
		}
	}

	private String[] getHologramText(Booster booster)
	{
		return new String[]
				{
						C.cGreen + "Amplified by " + C.cWhite + booster.getPlayerName(),
						C.cWhite + booster.getTimeRemainingString() + " Remaining",
						C.cAqua + "Click to Thank. You get " + BoosterThankManager.TIP_FOR_TIPPER + " Treasure Shards"
				};
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (_activeArmorStand != null && !_activeArmorStand.isValid())
		{
			updateNpcs();
		}
		else
		{
			updateNpcName();
		}
	}

	@EventHandler
	public void onBoosterEnable(BoosterActivateEvent event)
	{
		updateNpcs();
	}

	@EventHandler
	public void onBoosterDisable(BoosterExpireEvent event)
	{
		updateNpcs();
	}

	@EventHandler
	public void npcInteract(NPCInteractEvent event)
	{
		if (event.getNpc().equals(_npc))
		{
			_gameBoosterManager.getManager().getBoosterManager().openShop(event.getPlayer());
		}
	}

	@EventHandler
	public void one(PlayerInteractAtEntityEvent event)
	{
		if (event.getRightClicked().equals(_activeArmorStand))
		{
			_gameBoosterManager.attemptTip(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player && event.getEntity().equals(_activeArmorStand))
		{
			_gameBoosterManager.attemptTip(((Player) event.getDamager()));
			event.setCancelled(true);
		}
	}
}
