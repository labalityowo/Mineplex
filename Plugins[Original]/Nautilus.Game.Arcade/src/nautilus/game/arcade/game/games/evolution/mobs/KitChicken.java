package nautilus.game.arcade.game.games.evolution.mobs;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.game.games.evolution.mobs.perks.PerkDoubleJumpEVO;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkConstructor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class KitChicken extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
	public KitChicken(ArcadeManager manager)
	{
		super(manager, "Chicken", new String[]
		{
				C.cYellow + "Ability: " + C.cWhite + "Egg Throw", C.Line,
				C.cGreen + "No Fall Damage", C.cGreen + "Double Jump"
		}, 16, 3,
				new Perk[]
				{
					new PerkConstructor("Egg Pouch", 2.0, 3, Material.EGG,
							"Egg Launcher Ammo", false), new PerkDoubleJumpEVO("Double Jump", 0.6, 0.9, false)
				}, EntityType.CHICKEN);
	}
	
	@Override
	protected void giveItems(Player player)
	{
		player.getInventory().setItem(0, new ItemBuilder(Material.FEATHER).setTitle(C.cYellow + C.Bold + "Right Click" + C.cWhite + " - " + C.cGreen + C.Bold + "Egg Launch").build());

		//Disguise
		DisguiseChicken disguise = new DisguiseChicken(player);
		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		
		Manager.GetDisguise().undisguise(player);
		Manager.GetDisguise().disguise(disguise);
		
		player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_IDLE, 4f, 1f);
	}
	
	@EventHandler
	public void onLaunch(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!HasKit(event.getPlayer()))
			return;
		
		if (!UtilGear.isMat(event.getItem(), Material.FEATHER))
			return;
		
		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(event.getPlayer(), "Egg", 0);
		Bukkit.getServer().getPluginManager().callEvent(useEvent);
		
		if (useEvent.isCancelled())
		{
			event.setCancelled(true);
			return;
		}
		
		if (!UtilInv.contains(event.getPlayer(), Material.EGG, (byte) 0, 1))
			return;
		
		UtilInv.remove(event.getPlayer(), Material.EGG, (byte) 0, 1);
		event.getPlayer().launchProjectile(Egg.class);
	}
	
	@EventHandler
	public void onEggLaunch(final PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!HasKit(event.getPlayer()))
			return;
		
		if (!UtilGear.isMat(event.getItem(), Material.EGG))
			return;
		
		event.setCancelled(true);
		Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				UtilInv.Update(event.getPlayer());
			}
		}, 2);
	}

	@EventHandler
	public void EggHit(CustomDamageEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (event.GetDamagerPlayer(true) != null && event.GetDamageePlayer() != null && event.GetDamagerPlayer(true) == event.GetDamageePlayer())
		{
			event.SetCancelled("Self damage");
			return;
		}
		
		if (!(event.GetDamagerEntity(true) instanceof Player))
			return;
			
		Player player = event.GetDamagerPlayer(true);
		
		if (!Manager.IsAlive(player))
			return;
		
		if (!HasKit(player))
			return;
		
		if (event.GetProjectile() == null)
			return;

		if (!(event.GetProjectile() instanceof Egg))
			return;
		
		event.AddMod("Chicken Kit", "Egg", 2, true);
	}
}
