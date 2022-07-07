package mineplex.core.gadget.gadgets.morph.moba;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

public class MorphBiff extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.NETHER_STAR)
			.setTitle(C.cGreenB + "Biff's Cavalry Charge")
			.addLore("Click to fire out a beam of water that knocks back", "players that it hits.")
			.build();
	private static final int ACTIVE_SLOT = 2;
	private static final ItemStack HORSE_ARMOUR = new ItemStack(Material.IRON_BARDING);
	private static final ItemStack SADDLE = new ItemStack(Material.SADDLE);
	private static final int MAX_TICKS = 6 * 20;

	private final Map<Player, Horse> _horses = new HashMap<>();

	public MorphBiff(GadgetManager manager)
	{
		super(manager, "Biff Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "REEEEEEEEEEEEEEEE",
				"",
				C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Nether Star" + C.cWhite + " to",
				C.cWhite + "mount your " + C.cYellow + "Horse" + C.cWhite + " and",
				C.cWhite + "knock back nearby players."
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.BIFF.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.BIFF.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		player.getInventory().setItem(ACTIVE_SLOT, ACTIVE_ITEM);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.getInventory().setItem(ACTIVE_SLOT, null);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem();

		if (!isActive(player) || itemStack == null || !itemStack.equals(ACTIVE_ITEM))
		{
			return;
		}

		event.setCancelled(true);

		if (!Manager.selectLocation(this, player.getLocation()))
		{
			Manager.informNoUse(player);
			return;
		}

		if (!Recharge.Instance.use(player, "Cavalry Charge", 30000,true, true, "Cosmetics"))
		{
			return;
		}

		Manager.getPetManager().getCreatureModule().SetForce(true);

		Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);

		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, horse.getLocation().add(0, 1, 0), 1, 1, 1, 0.1F, 50, ViewDist.NORMAL);
		horse.getWorld().strikeLightningEffect(horse.getLocation());
		horse.getWorld().playSound(horse.getLocation(), Sound.HORSE_DEATH, 1, 1.1F);
		horse.setHealth(20);
		horse.setMaxHealth(horse.getHealth());
		horse.setJumpStrength(1);
		horse.setMaxDomestication(1);
		horse.setDomestication(horse.getMaxDomestication());
		horse.getInventory().setArmor(HORSE_ARMOUR);
		horse.getInventory().setSaddle(SADDLE);
		horse.setOwner(player);
		horse.setPassenger(player);
		_horses.put(player, horse);

		Manager.getPetManager().getCreatureModule().SetForce(false);
	}

	@EventHandler
	public void updateHorses(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_horses.keySet().removeIf(player ->
		{
			Horse horse = _horses.get(player);

			if (horse.getTicksLived() > MAX_TICKS)
			{
				horse.getWorld().playSound(horse.getLocation(), Sound.HORSE_BREATHE, 1, 1.1F);
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, horse.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.1F, 50, ViewDist.LONG);
				horse.remove();
				return true;
			}

			for (Player nearby : UtilPlayer.getNearby(horse.getLocation(), 3))
			{
				if (player.equals(nearby) || !Recharge.Instance.use(player, getName() + player.getUniqueId(), 1500, false, false, "Cosmetics"))
				{
					continue;
				}

				nearby.getWorld().playSound(nearby.getLocation(), Sound.IRONGOLEM_THROW, 1, 0.6F);
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, nearby.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.1F, 10, ViewDist.LONG);
				UtilAction.velocity(nearby, UtilAlg.getTrajectory(horse, nearby).multiply(1.5).setY(0.8));
			}

			return false;
		});
	}

}
