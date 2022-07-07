package nautilus.game.arcade.game.games.quiver.ultimates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.Game;

public class UltimateNecromancer extends UltimatePerk
{

	private static final float CHARGE_PASSIVE = 0.4F;
	private static final float CHARGE_PAYLOAD = 0.4F;
	private static final float CHARGE_KILL = 5F;
	private static final float CHARGE_ASSIST = 2F;
	
	private int _skeletons;

	private Map<UUID, Set<LivingEntity>> _entities = new HashMap<>();

	public UltimateNecromancer(long length, int skeletons)
	{
		super("Summon Undead", new String[] {}, length, CHARGE_PASSIVE, CHARGE_PAYLOAD, CHARGE_KILL, CHARGE_ASSIST);

		_skeletons = skeletons;
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		for (Set<LivingEntity> entityList : _entities.values())
		{
			for (LivingEntity livingEntity : entityList)
			{
				if (event.getEntity().equals(livingEntity))
				{
					event.setCancelled(true);
					event.getEntity().setFireTicks(0);
				}
			}
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event)
	{
		for (Set<LivingEntity> livingEntities : _entities.values())
		{
			if (event.getTarget() instanceof Player && livingEntities.contains(event.getEntity()))
			{
				Game game = Manager.GetGame();
				Player source = getPlayerFromEntity(event.getEntity());
				Player targetPlayer = (Player) event.getTarget();

				if (game.GetTeam(targetPlayer).equals(game.GetTeam(source)))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (event.GetProjectile() != null)
		{
			if (event.GetProjectile() instanceof Arrow)
			{
				LivingEntity livingEntity = event.GetDamagerEntity(true);

				for (UUID uuid : _entities.keySet())
				{
					Set<LivingEntity> entities = _entities.get(uuid);
					Player player = UtilPlayer.searchExact(uuid);
					
					for (LivingEntity livingEntity2 : entities)
					{
						if (livingEntity.equals(livingEntity2))
						{
							event.SetDamager(player);
						}
					}
				}
			}
		}
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		Set<LivingEntity> entities = new HashSet<>();

		Manager.GetGame().CreatureAllowOverride = true;

		for (int i = 0; i < _skeletons; i++)
		{
			LivingEntity livingEntity = player.getWorld().spawn(player.getLocation().add((Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3), Skeleton.class);

			livingEntity.getEquipment().setItemInHand(new ItemStack(Material.BOW));
			entities.add(livingEntity);
			UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, livingEntity.getEyeLocation(), 1F, 1F, 1F, 0.1F, 20, ViewDist.NORMAL);

			ArrayList<Player> players = Manager.GetGame().GetTeam(player).GetPlayers(true);
			if (Manager.GetGame().GetTeamList().size() == 1)
			{
				players.clear();
				players.add(player);
			}
			((Creature) livingEntity).setTarget(UtilPlayer.getClosest(livingEntity.getLocation(), players));
		}

		Manager.GetGame().CreatureAllowOverride = false;

		_entities.put(player.getUniqueId(), entities);
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		for (LivingEntity entity : _entities.get(player.getUniqueId()))
		{
			entity.getEquipment().clear();
			entity.remove();
		}

		_entities.remove(player.getUniqueId());
	}

	public Player getPlayerFromEntity(Entity entity)
	{
		for (UUID uuid : _entities.keySet())
		{
			Set<LivingEntity> livingEntities = _entities.get(uuid);

			for (LivingEntity livingEntity : livingEntities)
			{
				if (livingEntity.equals(livingEntity))
				{
					return UtilPlayer.searchExact(uuid);
				}
			}
		}

		return null;
	}

}
