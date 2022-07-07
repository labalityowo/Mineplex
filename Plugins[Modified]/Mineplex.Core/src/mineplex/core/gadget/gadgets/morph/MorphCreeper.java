package mineplex.core.gadget.gadgets.morph;

import java.util.HashMap;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseCreeper;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphCreeper extends MorphGadget
{
	private HashMap<Player, Long> _active = new HashMap<>();

	public MorphCreeper(GadgetManager manager)
	{
		super(manager, "Creeper Morph", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Become the creepiest of Creepers that ever creeped!",
				C.blankLine,
				C.cWhite + "Crouch to use Detonate",
				}, LineFormat.LORE),
				-11,
				Material.SKULL_ITEM, (byte)4);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseCreeper disguise = new DisguiseCreeper(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void Trigger(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			for (Player player : getActive())
			{
				if (player.isSneaking())
				{
					player.leaveVehicle();
					player.eject();

					if (!_active.containsKey(player))
						_active.put(player, System.currentTimeMillis());

					double elapsed = (System.currentTimeMillis() - _active.get(player))/1000d;

					player.setExp(Math.min(0.99f, (float)(elapsed/1.5)));
					
					//Sound
					player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, (float)(0.5 + (elapsed/3d)), (float)(0.5 + elapsed));

					IncreaseSize(player);
				}
				else if (_active.containsKey(player))	
				{
					//Unpower
					DecreaseSize(player);
					
					player.setExp(0f);
					
					double elapsed = (System.currentTimeMillis() - _active.remove(player))/1000d;

					if (elapsed < 1.5)
						continue;

					//Explode
					UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 0, 0.5f, 0, 0, 1,
							ViewDist.MAX, UtilServer.getPlayers());
					player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1f, 0.8f);

					player.playEffect(EntityEffect.HURT);
					
					//Knockback
					HashMap<Player, Double> players = UtilPlayer.getInRadius(player.getLocation(), 8);
					for (Player other : players.keySet())
					{	
						if (other.equals(player) || !Manager.selectEntity(this, other))
						{
							continue;
						}
						
						double mult = players.get(other);

						//Knockback
						UtilAction.velocity(other, UtilAlg.getTrajectory(player.getLocation(), other.getLocation()), 1 + 1 * mult, false, 0, 0.6 + 0.6 * mult, 3, true);
					}
				}
			}
		}
	}


	public DisguiseCreeper GetDisguise(Player player)
	{
		DisguiseBase disguise = Manager.getDisguiseManager().getDisguise(player);
		if (disguise == null)
			return null;

		if (!(disguise instanceof DisguiseCreeper))
			return null;

		return (DisguiseCreeper)disguise;
	}

	public int GetSize(Player player)
	{
		DisguiseCreeper creeper = GetDisguise(player);
		if (creeper == null)	return 0;

		return creeper.bV();
	}

	public void DecreaseSize(Player player)
	{
		DisguiseCreeper creeper = GetDisguise(player);
		if (creeper == null)	return;

		creeper.a(-1);

		Manager.getDisguiseManager().updateDisguise(creeper);
	}

	public void IncreaseSize(Player player)
	{
		DisguiseCreeper creeper = GetDisguise(player);
		if (creeper == null)	return;

		creeper.a(1);

		Manager.getDisguiseManager().updateDisguise(creeper);
	}

	@EventHandler
	public void HeroOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.HERO_MORPH_CREEPER))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}	
	}
	
	@EventHandler
	public void Clean(PlayerQuitEvent event)
	{
		_active.remove(event.getPlayer());
	}
}