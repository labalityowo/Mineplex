package nautilus.game.arcade.game.games.smash.perks.pig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.disguise.disguises.DisguisePigZombie;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkPigZombie extends SmashPerk
{
	
	private int _minHealth;
	
	public Set<UUID> _active = new HashSet<>();

	public PerkPigZombie()
	{
		super("Nether Pig", new String[] { C.cGray + "Become Nether Pig when HP is below 6.", C.cGray + "Return to Pig when HP is 10 or higher." });
	}

	@Override
	public void setupValues()
	{
		_minHealth = getPerkInt("Min Health");
	}

	@EventHandler
	public void Check(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
			{
				continue;
			}
			
			// Active
			if (_active.contains(player.getUniqueId()))
			{
				Manager.GetCondition().Factory().Speed("Pig Zombie", player, player, 0.9, 0, false, false, false);

				if (player.getHealth() < 10 || isSuperActive(player))
				{
					continue;
				}
				
				// Deactivate
				_active.remove(player.getUniqueId());

				// Armor
				player.getInventory().setHelmet(null);
				player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
				player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
				player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

				player.getInventory().remove(Material.IRON_HELMET);
				player.getInventory().remove(Material.IRON_CHESTPLATE);
				player.getInventory().remove(Material.IRON_LEGGINGS);
				player.getInventory().remove(Material.IRON_BOOTS);

				SmashKit kit = (SmashKit) Kit;
				
				kit.disguise(player, DisguisePig.class);
				
				// Sound
				player.getWorld().playSound(player.getLocation(), Sound.PIG_IDLE, 2f, 1f);
				player.getWorld().playSound(player.getLocation(), Sound.PIG_IDLE, 2f, 1f);

				// Inform
				UtilPlayer.message(player, F.main("Skill", "You returned to " + F.skill("Pig Form") + "."));
			}
			// Not Active
			else
			{
				if (player.getHealth() <= 0 || (!isSuperActive(player) && player.getHealth() > _minHealth))
				{
					continue;
				}
				
				// Activate
				_active.add(player.getUniqueId());

				// Armor
				player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
				player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
				player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
				player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

				SmashKit kit = (SmashKit) Kit;
				
				kit.disguise(player, DisguisePigZombie.class);

				// Sound
				player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 2f, 1f);
				player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 2f, 1f);

				// Inform
				UtilPlayer.message(player, F.main("Skill", "You transformed into " + F.skill("Nether Pig Form") + "."));

				player.setExp(0.99f);
			}
		}
	}

	@EventHandler
	public void Clean(PlayerDeathEvent event)
	{
		_active.remove(event.getEntity());
	}
}
