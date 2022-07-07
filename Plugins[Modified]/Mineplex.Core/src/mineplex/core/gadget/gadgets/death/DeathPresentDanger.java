package mineplex.core.gadget.gadgets.death;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.ToggleMobsEvent;
import mineplex.core.gadget.types.DeathEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class DeathPresentDanger extends DeathEffectGadget
{

	private List<ArmorStand> _armorStands = new ArrayList<>();

	public DeathPresentDanger(GadgetManager manager)
	{
		super(manager, "Present Danger",
				UtilText.splitLineToArray(C.cGray + "Leave behind a little gift for your enemies.", LineFormat.LORE),
				-16,
				Material.INK_SACK, (byte)1);
		setDisplayItem(SkinData.PRESENT.getSkull());
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);

		Location loc = event.getLocation();
		UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.WOOL, (byte) 14), loc, 0, 0, 0, 0.1f, 15, UtilParticle.ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.WOOL, (byte) 4), loc, 0, 0, 0, 0.1f, 15, UtilParticle.ViewDist.NORMAL);

		Bukkit.getPluginManager().callEvent(new ToggleMobsEvent(true));
		ArmorStand armorStand = loc.getWorld().spawn(loc.clone().subtract(0, 2.3, 0), ArmorStand.class);
		armorStand.setVisible(false);
		armorStand.setGravity(false);
		armorStand.setSmall(true);
		armorStand.setHelmet(SkinData.PRESENT.getSkull());
		_armorStands.add(armorStand);
		Bukkit.getPluginManager().callEvent(new ToggleMobsEvent(false));
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<ArmorStand> iterator = _armorStands.iterator();
		while (iterator.hasNext())
		{
			ArmorStand armorStand = iterator.next();
			if (armorStand.getTicksLived() >= 100 || !armorStand.isValid())
			{
				Location loc = armorStand.getEyeLocation();
				UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.WOOL, (byte) 14), loc, 0, 0, 0, 0.1f, 15, UtilParticle.ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.WOOL, (byte) 4), loc, 0, 0, 0, 0.1f, 15, UtilParticle.ViewDist.NORMAL);
				armorStand.remove();
				iterator.remove();
			}
		}
	}

	@EventHandler
	public void removeArrows(EntityDamageByEntityEvent event)
	{
		if (event.getEntity() instanceof ArmorStand)
		{
			if (_armorStands.contains(event.getEntity()))
			{
				event.setCancelled(true);
			}
		}
	}

}
