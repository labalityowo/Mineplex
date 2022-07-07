package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargedEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.data.WoolBombData;

public class PerkWoolBomb extends Perk implements IThrown
{

	private long _cooldown;
	private long _rate;
	private int _damageRadius;
	private int _damageExplode;
	private int _damageCollide;
	private int _knockbackMagnitude;
	
	private Map<UUID, Item> _thrown = new HashMap<>();
	private Map<UUID, WoolBombData> _active = new HashMap<>();

	public PerkWoolBomb()
	{
		super("Wool Mine", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Wool Mine" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_rate = getPerkInt("Rate (ms)");
		_damageRadius = getPerkInt("Damage Radius");
		_damageExplode = getPerkInt("Damage Explode");
		_damageCollide = getPerkInt("Damage Collide");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.usable(player, GetName() + " Rate"))
		{
			return;
		}

		UUID key = player.getUniqueId();
		
		if (_active.containsKey(key))
		{
			if (detonate(player, true))
			{
				return;
			}
		}

		if (_thrown.containsKey(key))
		{
			if (solidify(player, true))
			{
				return;
			}
		}

		launch(player);

		event.setCancelled(true);
	}

	private void launch(Player player)
	{
		if (!Recharge.Instance.usable(player, GetName(), true))
		{
			return;
		}
		
		Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.WOOL, (byte) 0));

		UtilAction.velocity(ent, player.getLocation().getDirection(), 1, false, 0, 0.2, 10, false);

		Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, true, null, 1f, 1f, null, 1, UpdateType.SLOW, 0.5f);

		_thrown.put(player.getUniqueId(), ent);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You launched " + F.skill(GetName()) + "."));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.SHEEP_IDLE, 2f, 1.5f);

		// Rate
		Recharge.Instance.useForce(player, GetName() + " Rate", _rate);

		// Disguise
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);
		
		if (disguise != null && disguise instanceof DisguiseSheep)
		{
			DisguiseSheep sheep = (DisguiseSheep) disguise;
			sheep.setSheared(true);

			sheep.UpdateDataWatcher();
			Manager.GetDisguise().updateDisguise(disguise);
		}
	}

	@EventHandler
	public void rechargeWool(RechargedEvent event)
	{
		if (event.GetAbility().equals(GetName()))
		{
			DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(event.GetPlayer());
			
			if (disguise != null && disguise instanceof DisguiseSheep)
			{
				DisguiseSheep sheep = (DisguiseSheep) disguise;
				sheep.setSheared(false);

				sheep.UpdateDataWatcher();
				Manager.GetDisguise().updateDisguise(disguise);
			}
		}
	}

	private boolean solidify(LivingEntity ent, boolean inform)
	{
		if (!(ent instanceof Player))
		{
			return false;
		}
		
		Player player = (Player) ent;

		Item thrown = _thrown.remove(player.getUniqueId());
		
		if (thrown == null)
		{
			return false;
		}
		
		// Make Block
		Block block = thrown.getLocation().getBlock();

		Manager.GetBlockRestore().restore(block);

		_active.put(player.getUniqueId(), new WoolBombData(block));

		block.setType(Material.WOOL);
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());

		// Clean
		thrown.remove();

		// Rate
		Recharge.Instance.useForce(player, GetName() + " Rate", _rate);

		// Inform
		if (inform)
		{
			player.getWorld().playSound(player.getLocation(), Sound.SHEEP_IDLE, 2f, 1.5f);

			UtilPlayer.message(player, F.main("Game", "You armed " + F.skill(GetName()) + "."));
		}

		return true;
	}

	private boolean detonate(Player player, boolean inform)
	{
		WoolBombData data = _active.remove(player.getUniqueId());

		if (data == null)
		{
			return false;
		}
		
		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return false;
		}

		// Restore
		data.restore();

		// Explode
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, data.Block.getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
		data.Block.getWorld().playSound(data.Block.getLocation(), Sound.EXPLODE, 3f, 0.8f);

		// Damage
		Map<LivingEntity, Double> targets = UtilEnt.getInRadius(data.Block.getLocation().add(0.5, 0.5, 0.5), _damageRadius);
		
		List<Player> team = TeamSuperSmash.getTeam(Manager, player, false);
		for (LivingEntity cur : targets.keySet())
		{
			if (cur instanceof Player && team.contains(cur))
			{
				continue;
			}
			
			// Damage Event
			Manager.GetDamage().NewDamageEvent(cur, player, null, DamageCause.CUSTOM, _damageExplode * targets.get(cur) + 0.5, false, true, false, player.getName(), GetName());

			// Condition
			Manager.GetCondition().Factory().Falling(GetName(), cur, player, 10, false, true);

			// Knockback
			UtilAction.velocity(cur, UtilAlg.getTrajectory2d(data.Block.getLocation().add(0.5, 0.5, 0.5), cur.getEyeLocation()), 0.5 + 2.5 * targets.get(cur), true, 0.8, 0, 10, true);

			// Inform
			if (cur instanceof Player && !player.equals(cur))
			{
				UtilPlayer.message(cur, F.main("Game", F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));
			}
			
		}

		// Rate
		Recharge.Instance.useForce(player, GetName() + " Rate", _rate);

		// Inform
		if (inform)
		{
			player.getWorld().playSound(player.getLocation(), Sound.SHEEP_IDLE, 2f, 1.5f);

			UtilPlayer.message(player, F.main("Game", "You detonated " + F.skill(GetName()) + "."));
		}

		return true;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDamageSelf(CustomDamageEvent event)
	{
		if(event.GetDamageePlayer() == null || event.GetDamagerPlayer(true) == null)
		{
			return;
		}
		
		if(!event.GetDamageePlayer().equals(event.GetDamagerPlayer(true)))
		{
			return;
		}
		
		if(event.GetCause() != DamageCause.CUSTOM)
		{
			return;
		}
		
		if(!event.IsCancelled())
		{
			return;
		}
		
		event.GetCancellers().remove("Team Damage");
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		solidify(data.getThrower(), false);

		if (target == null)
		{
			return;
		}
		
		// Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.PROJECTILE, _damageCollide, true, true, false, UtilEnt.getName(data.getThrower()), GetName());
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		solidify(data.getThrower(), false);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		solidify(data.getThrower(), false);
	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@EventHandler
	public void colorExpireUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}
		
		Set<Player> detonate = new HashSet<>();
		Iterator<UUID> playerIterator = _active.keySet().iterator();

		while (playerIterator.hasNext())
		{
			UUID key = playerIterator.next();
			
			Player player = UtilPlayer.searchExact(key);
			
			if (player == null)
			{
				playerIterator.remove();
				continue;
			}
			
			WoolBombData data = _active.get(key);

			if (UtilTime.elapsed(data.Time, _cooldown))
			{
				detonate.add(player);
				continue;
			}

			if (Recharge.Instance.usable(player, GetName() + " Rate"))
			{
				if (data.Block.getData() == 14)
				{
					data.Block.setData((byte) 0);
				}
				else
				{
					data.Block.setData((byte) 14);
				}
			}
		}

		for (Player player : detonate)
		{
			detonate(player, false);
		}
	}

	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		clear(event.getEntity());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		clear(event.getPlayer());
	}

	private void clear(Player player)
	{
		WoolBombData data = _active.remove(player.getUniqueId());

		if (data != null)
		{
			data.restore();
		}
	}
}