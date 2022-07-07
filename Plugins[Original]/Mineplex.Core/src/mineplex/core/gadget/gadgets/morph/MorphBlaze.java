package mineplex.core.gadget.gadgets.morph;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBlaze;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphBlaze extends MorphGadget
{
	public MorphBlaze(GadgetManager manager)
	{
		super(manager, "Blaze Morph", 
				UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Transform into a fiery Blaze, straight from the Nether!",
				C.blankLine,
				"#" + C.cWhite + "Crouch to use Firefly",
				}, LineFormat.LORE),
				-11,
				Material.BLAZE_POWDER, (byte)0);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseBlaze disguise = new DisguiseBlaze(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}
	
	@EventHandler
	public void Trail(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			for (Player player : getActive())
			{
				if (player.isSneaking())
				{
					player.leaveVehicle();
					player.eject();
					
					player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 0.2f, (float)(Math.random()));
					UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 1, 0), 
							0.25f, 0.25f, 0.25f, 0f, 3,
							ViewDist.NORMAL, UtilServer.getPlayers());
					UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 
							0.1f, 0.1f, 0.1f, 0f, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
					UtilAction.velocity(player, 0.8, 0.1, 1, true);
				}
			}
		}
	}
	
	@EventHandler
	public void HeroOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.HERO_MORPH_BLAZE))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}	
	}
}