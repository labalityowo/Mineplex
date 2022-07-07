package nautilus.game.arcade.game.games.smash.perks.magmacube;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseMagmaCube;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkMagmaBoost extends Perk
{

	private static final ItemStack BOOTS = new ItemStack(Material.IRON_BOOTS);
	private static final ItemStack CHESTPLATE = new ItemStack(Material.IRON_CHESTPLATE);
	private static final ItemStack HELMET = new ItemStack(Material.DIAMOND_HELMET);

	private int _maxStacks;

	private Map<UUID, Integer> _kills = new HashMap<>();

	public PerkMagmaBoost()
	{
		super("Fuel the Fire", new String[0]);
	}

	@Override
	public void setupValues()
	{
		_maxStacks = getPerkInt("Max Stacks");

		setDesc(C.cGray + "Kills give +1 Damage, -15% Knockback Taken and +1 Size.", C.cGray + "Kill bonuses can stack " + _maxStacks + " times, and reset on death.");
	}

	@Override
	public void unregisteredEvents()
	{
		_kills.clear();
	}

	@EventHandler
	public void kill(CombatDeathEvent event)
	{
		Player killed = event.GetEvent().getEntity();

		_kills.remove(killed.getUniqueId());

		if (event.GetLog().GetKiller() == null)
		{
			return;
		}

		Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

		if (killer == null || killer.equals(killed) || !hasPerk(killer))
		{
			return;
		}

		DisguiseMagmaCube slime = (DisguiseMagmaCube) Manager.GetDisguise().getActiveDisguise(killer);
		UUID key = killer.getUniqueId();

		if (slime == null)
		{
			return;
		}

		int size = 1;

		if (_kills.containsKey(key))
		{
			size += _kills.get(key);
		}

		size = Math.min(_maxStacks, size);

		// Adjust armour
		if (size == 1)
		{
			killer.getInventory().setBoots(BOOTS);
		}
		else if (size == 2)
		{
			killer.getInventory().setChestplate(CHESTPLATE);
		}
		else if (size == 3)
		{
			killer.getInventory().setHelmet(HELMET);
		}

		_kills.put(key, size);

		slime.SetSize(size + 1);
		Manager.GetDisguise().updateDisguise(slime);

		killer.setExp(0.99F * (size / (float) _maxStacks));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void sizeDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(false);

		if (damager == null)
		{
			return;
		}

		if (!hasPerk(damager))
		{
			return;
		}

		UUID key = damager.getUniqueId();

		if (!_kills.containsKey(key))
		{
			return;
		}

		event.AddMod(damager.getName(), GetName(), _kills.get(key), false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void sizeKnockback(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();

		if (damagee == null)
		{
			return;
		}
		if (!hasPerk(damagee))
		{
			return;
		}

		if (!_kills.containsKey(damagee.getUniqueId()))
		{
			return;
		}

		int bonus = _kills.get(damagee.getUniqueId());

		event.AddKnockback(GetName(), bonus * 0.15d);
	}

	@EventHandler
	public void energyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			if (!hasPerk(player))
			{
				continue;
			}

			float size = 0;

			if (_kills.containsKey(player.getUniqueId()))
			{
				size += _kills.get(player.getUniqueId());
			}

			playParticles(player, size);
		}
	}

	private void playParticles(Player player, float size)
	{
		UtilParticle.PlayParticleToAll(ParticleType.LAVA, player.getLocation().add(0, 0.4, 0), 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0.15f + 0.15f * size, 0, 1, ViewDist.LONG);
	}
}
