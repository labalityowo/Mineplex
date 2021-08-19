package mineplex.minecraft.game.core.combat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.minecraft.server.v1_8_R3.ItemStack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.incognito.events.IncognitoStatusChangeEvent;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.combat.event.CombatQuitEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

@ReflectivelyCreateMiniPlugin
public class CombatManager extends MiniClientPlugin<ClientCombat>
{

	public enum AttackReason
	{
		Attack,
		CustomWeaponName,
		DefaultWeaponName
	}

	private static final long EXPIRE_TIME = TimeUnit.SECONDS.toMillis(15);

	private final Map<UUID, CombatLog> _active = new HashMap<>();
	private final Set<UUID> _removeList = new HashSet<>();

	private AttackReason _attackReason;

	public CombatManager()
	{
		super("Death");

		_attackReason = AttackReason.CustomWeaponName;
	}

	@Override
	protected ClientCombat addPlayer(UUID uuid)
	{
		return new ClientCombat();
	}

	//This is a backup, for when CustomDamageEvent is disabled (manually)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void AddAttack(EntityDamageEvent event)
	{
		if (event.getEntity() == null || !(event.getEntity() instanceof Player))
		{
			return;
		}

		Player damagee = (Player) event.getEntity();

		LivingEntity damagerEnt = UtilEvent.GetDamagerEntity(event, true);

		//Attacked by Entity
		if (damagerEnt != null)
		{
			if (damagerEnt instanceof Player)
			{
				getLog((Player) damagerEnt).SetLastCombat(System.currentTimeMillis());
			}

			String cause;
			switch (event.getCause())
			{
				case ENTITY_ATTACK:
					cause = "Attack";
					break;
				case ENTITY_EXPLOSION:
					cause = "Explosion";
					break;
				case MAGIC:
					cause = "Thrown Potion";
					break;
				case PROJECTILE:
					cause = "Ranged Weapon";
					break;
				case THORNS:
					cause = "Thorns Enchantment";
					break;
				default:
					cause = event.getCause() + "";
					break;
			}

			if (UtilEvent.isBowDamage(event))
			{
				cause = "Bow";
			}

			if (damagerEnt instanceof Player)
			{
				if (event.getCause() == DamageCause.ENTITY_ATTACK)
				{
					Player player = (Player) damagerEnt;

					if (player.getItemInHand() == null)
					{
						cause = "Fists";
					}
					else if (player.getItemInHand().getType() == Material.AIR)
					{
						cause = "Fists";
					}
					else
					{
						cause = ItemStackFactory.Instance.GetName(player.getItemInHand(), false);
					}
				}
			}

			getLog(damagee).Attacked(UtilEnt.getName(damagerEnt), event.getDamage(), damagerEnt, cause, null);
		}
		// Damager is WORLD
		else
		{
			DamageCause cause = event.getCause();
			Pair<String, String> source = getSourceAndReason(cause);

			getLog(damagee).Attacked(source.getLeft(), event.getDamage(), null, source.getRight(), null);
		}
	}

	public void AddAttack(CustomDamageEvent event)
	{
		// Not Player > No Log
		if (event.GetDamageePlayer() == null)
		{
			return;
		}

		// Damager is ENTITY
		if (event.GetDamagerEntity(true) != null)
		{
			String reason = event.GetReason();

			if (reason == null)
			{
				if (event.GetDamagerPlayer(false) != null)
				{
					Player damager = event.GetDamagerPlayer(false);

					reason = "Attack";

					if (_attackReason == AttackReason.DefaultWeaponName)
					{
						reason = "Fists";

						if (damager.getItemInHand() != null)
						{
							byte data = 0;

							if (damager.getItemInHand().getData() != null)
							{
								data = damager.getItemInHand().getData().getData();
							}

							reason = ItemStackFactory.Instance.GetName(damager.getItemInHand().getType(), data, false);
						}
					}
					else if (_attackReason == AttackReason.CustomWeaponName)
					{
						reason = "Fists";

						if (damager.getItemInHand() != null)
						{
							ItemStack itemStack = CraftItemStack.asNMSCopy(damager
									.getItemInHand());

							if (itemStack != null)
							{
								reason = CraftItemStack.asNMSCopy(
										damager.getItemInHand()).getName();
							}
						}
					}
				}
				else if (event.GetProjectile() != null)
				{
					if (event.GetProjectile() instanceof Arrow)
					{
						reason = "Archery";
					}
					else if (event.GetProjectile() instanceof Fireball)
					{
						reason = "Fireball";
					}
				}
			}

			if (event.GetDamagerEntity(true) instanceof Player)
			{
				getLog(event.GetDamagerPlayer(true)).SetLastCombat(System.currentTimeMillis());
				getLog(event.GetDamagerPlayer(true)).SetLastCombatEngaged(System.currentTimeMillis());
				getLog(event.GetDamageePlayer()).SetLastCombatEngaged(System.currentTimeMillis());
			}

			getLog(event.GetDamageePlayer()).Attacked(UtilEnt.getName(event.GetDamagerEntity(true)), (int) event.GetDamage(), event.GetDamagerEntity(true), reason, event.GetDamageMod(), event.getMetadata());
		}
		// Damager is WORLD
		else
		{
			DamageCause cause = event.GetCause();
			Pair<String, String> source = getSourceAndReason(cause);

			if (event.GetReason() != null)
			{
				source.setRight(event.GetReason());
			}

			getLog(event.GetDamageePlayer()).Attacked(source.getLeft(), (int) event.GetDamage(), null, source.getRight(), event.GetDamageMod(), event.getMetadata());
		}
	}

	private Pair<String, String> getSourceAndReason(DamageCause cause)
	{
		String source = "?";
		String reason = "-";

		switch (cause)
		{
			case BLOCK_EXPLOSION:
				source = "Explosion";
				break;
			case CONTACT:
				source = "Cactus";
				break;
			case CUSTOM:
				source = "Custom";
				break;
			case DROWNING:
				source = "Water";
				break;
			case ENTITY_ATTACK:
				source = "Entity";
				reason = "Attack";
				break;
			case ENTITY_EXPLOSION:
				source = "Explosion";
				break;
			case FALL:
				source = "Fall";
				break;
			case FALLING_BLOCK:
				source = "Falling Block";
				break;
			case FIRE:
				source = "Fire";
				break;
			case FIRE_TICK:
				source = "Fire";
				break;
			case LAVA:
				source = "Lava";
				break;
			case LIGHTNING:
				source = "Lightning";
				break;
			case MAGIC:
				source = "Magic";
				break;
			case MELTING:
				source = "Melting";
				break;
			case POISON:
				source = "Poison";
				break;
			case PROJECTILE:
				source = "Projectile";
				break;
			case STARVATION:
				source = "Starvation";
				break;
			case SUFFOCATION:
				source = "Suffocation";
				break;
			case SUICIDE:
				source = "Suicide";
				break;
			case VOID:
				source = "Void";
				break;
			case WITHER:
				source = "Wither";
				break;
		}

		return Pair.create(source, reason);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerQuit(PlayerQuitEvent event)
	{
		fakeDeath(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void vanishStatus(IncognitoStatusChangeEvent event)
	{
		if (!event.getNewState())
		{
			return;
		}

		fakeDeath(event.getPlayer());
	}

	private void fakeDeath(Player player)
	{
		CombatLog log = _active.get(player.getUniqueId());

		if (log == null)
		{
			return;
		}

		log.ExpireOld();

		if (log.GetAttackers().isEmpty())
		{
			return;
		}

		CombatQuitEvent combatQuitEvent = new CombatQuitEvent(player);
		// Cancelled by default
		combatQuitEvent.setCancelled(true);
		UtilServer.CallEvent(combatQuitEvent);

		if (combatQuitEvent.isCancelled())
		{
			return;
		}

		playerDeath(new PlayerDeathEvent(player, null, 0, null));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerDeath(PlayerDeathEvent event)
	{
		event.setDeathMessage(null);

		CombatLog log = _active.remove(event.getEntity().getUniqueId());

		if (log == null)
		{
			return;
		}

		log.SetDeathTime(System.currentTimeMillis());
		log.ExpireOld();

		// Save Death
		ClientCombat client = Get(event.getEntity());
		client.GetDeaths().addFirst(log);

		// Add Kill/Assist
		int assists = 0;
		for (int i = 0; i < log.GetAttackers().size(); i++)
		{
			CombatComponent attacker = log.GetAttackers().get(i);

			if (!attacker.IsPlayer())
			{
				continue;
			}

			if (log.GetKiller() == null)
			{
				log.SetKiller(attacker);

				ClientCombat killerClient = Get(attacker.getUniqueIdOfEntity());

				if (killerClient != null)
				{
					killerClient.GetKills().addFirst(log);
				}
			}
			else
			{
				assists++;

				ClientCombat assistClient = Get(attacker.getUniqueIdOfEntity());

				if (assistClient != null)
				{
					assistClient.GetAssists().addFirst(log);
				}
			}
		}

		log.SetAssists(assists);

		// Event
		CombatDeathEvent combatEvent = new CombatDeathEvent(event, client, log, "killed");
		UtilServer.CallEvent(combatEvent);

		DeathMessageType messageType = combatEvent.GetBroadcastType();

		//Death Message
		if (messageType == DeathMessageType.Detailed || messageType == DeathMessageType.Absolute)
		{
			// Killed
			String killedColor = log.GetKilledColor();
			String deadPlayer = killedColor + event.getEntity().getName();
			String message;

			// Killer
			if (log.GetKiller() != null)
			{
				String killerColor = log.GetKillerColor();

				String killPlayer = killerColor + log.GetKiller().GetName();

				if (log.GetAssists() > 0)
				{
					killPlayer += " + " + log.GetAssists();
				}

				String weapon = (String) log.GetKiller().GetDamage().getFirst().getMetadata().get("customWeapon");
				weapon = weapon == null ? log.GetKiller().GetLastDamageSource() : weapon;
				message = F.main(getName(), deadPlayer + C.mBody + " " + combatEvent.getKilledWord() + " by " + killPlayer + C.mBody + " with " + F.item(weapon) + ".");
			}
			// No Killer
			else
			{
				if (log.GetAttackers().isEmpty())
				{
					message = F.main(getName(), deadPlayer + C.mBody + " has died.");
				}
				else
				{
					if (log.GetLastDamager() != null && log.GetLastDamager().GetReason() != null && log.GetLastDamager().GetReason().length() > 1)
					{
						message = F.main(getName(), deadPlayer + C.mBody + " " + combatEvent.getKilledWord() + " by " + F.name(log.GetLastDamager().GetReason())) + C.mBody + ".";
					}
					else
					{
						message = F.main(getName(), deadPlayer + C.mBody + " " + combatEvent.getKilledWord() + " by " + F.name(log.GetAttackers().getFirst().GetName())) + C.mBody + ".";
					}
				}
			}

			String finalMessage = message + combatEvent.getSuffix();
			// Tell all players simple info
			combatEvent.getPlayersToInform().forEach(player -> player.sendMessage(finalMessage));

			// Tell the player who died it all
			event.getEntity().sendMessage(messageType == DeathMessageType.Absolute ? log.DisplayAbsolute().toArray(new String[0]) : log.Display().toArray(new String[0]));
		}
		else if (combatEvent.GetBroadcastType() == DeathMessageType.Simple)
		{
			//Simple 
			if (log.GetKiller() != null)
			{
				//Killer
				String killerColor = log.GetKillerColor();
				String killPlayer = killerColor + log.GetKiller().GetName();

				// Killed
				String killedColor = log.GetKilledColor();
				String deadPlayer = killedColor + event.getEntity().getName();

				if (log.GetAssists() > 0)
				{
					killPlayer += " + " + log.GetAssists();
				}

				String weapon = log.GetKiller().GetLastDamageSource();

				Player killer = UtilPlayer.searchExact(log.GetKiller().GetName());

				if (killer != null)
				{
					killer.sendMessage(F.main(getName(), "You " + combatEvent.getKilledWord() + " " + F.elem(deadPlayer) + " with " + F.item(weapon) + "." + combatEvent.getSuffix()));
				}

				event.getEntity().sendMessage(F.main(getName(), killPlayer + C.mBody + " " + combatEvent.getKilledWord() + " you with " + F.item(weapon) + "." + combatEvent.getSuffix()));
			}
			else
			{
				if (log.GetAttackers().isEmpty())
				{
					UtilPlayer.message(event.getEntity(), F.main(getName(), "You have died." + combatEvent.getSuffix()));
				}
				else
				{
					UtilPlayer.message(event.getEntity(), F.main(getName(), "You were " + combatEvent.getKilledWord() + " by " + F.name(log.GetAttackers().getFirst().GetName())) + C.mBody + "." + combatEvent.getSuffix());
				}
			}
		}
	}

	public CombatLog getLog(Player player)
	{
		return _active.computeIfAbsent(player.getUniqueId(), k -> new CombatLog(player, EXPIRE_TIME));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void ClearInactives(UpdateEvent event)
	{
		if (event.getType() == UpdateType.MIN_02)
		{
			// Remove already marked inactives if still offline
			Iterator<UUID> removeIterator = _removeList.iterator();

			while (removeIterator.hasNext())
			{
				UUID uuid = removeIterator.next();
				Player player = Bukkit.getPlayer(uuid);

				if (player == null)
					_active.remove(uuid);

				removeIterator.remove();
			}

			// Mark inactives for cleanup next go around
			for (UUID player : _active.keySet())
			{
				if (Bukkit.getPlayer(player) == null)
					_removeList.add(player);
			}
		}
	}

	public void setUseWeaponName(AttackReason var)
	{
		_attackReason = var;
	}
}
