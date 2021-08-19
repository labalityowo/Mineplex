package nautilus.game.arcade.game.games.moba.boss.pumpkin;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.ai.MobaAI;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaAIMethod;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaDirectAIMethod;
import nautilus.game.arcade.game.games.moba.boss.MobaBoss;
import nautilus.game.arcade.game.games.moba.buff.BuffManager;
import nautilus.game.arcade.game.games.moba.buff.buffs.BuffPumpkinKing;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PumpkinBoss extends MobaBoss
{

	private static final String NAME = "Pumpkin King";
	private static final int SPAWN_TIME = (int) TimeUnit.MINUTES.toMillis(5);
	private static final int RESPAWN_TIME = (int) TimeUnit.MINUTES.toMillis(5);
	private static final MobaAIMethod AI_METHOD = new MobaDirectAIMethod();
	private static final ItemStack HELMET = new ItemStack(Material.PUMPKIN);
	private static final ItemStack IN_HAND = new ItemStack(Material.STONE_SWORD);
	private static final String DAMAGE_REASON = "Pumpkin King";
	private static final int DAMAGE_RADIUS = 2;
	private static final int DAMAGE_RANGE = 2;
	private static final int DAMAGE_DIRECT = 6;
	private static final int DAMAGE_DIRECT_RADIUS_SQUARED = 9;
	private static final int HEALTH = 100;
	private static final int HEALTH_OUT_OF_COMBAT = 2;
	private static final Material[] BLOCKS = {
			Material.OBSIDIAN,
			Material.NETHERRACK,
			Material.NETHER_BRICK
	};

	private MobaAI _ai;
	private boolean _initialSpawn;
	private final Set<Block> _changed;

	public PumpkinBoss(Moba host, Location location)
	{
		super(host, location, RESPAWN_TIME);

		_changed = new HashSet<>();
	}

	@Override
	public void setup()
	{
		// Override this so that the entity isn't spawned as soon as the game starts.
		UtilServer.RegisterEvents(this);
	}

	@Override
	public LivingEntity spawnEntity()
	{
		_host.CreatureAllowOverride = true;

		Skeleton skeleton = UtilVariant.spawnWitherSkeleton(_location);

		skeleton.setCustomName(C.cDRedB + NAME);
		skeleton.setCustomNameVisible(true);
		skeleton.getEquipment().setHelmet(HELMET);
		skeleton.getEquipment().setItemInHand(IN_HAND);
		skeleton.setMaxHealth(HEALTH);
		skeleton.setHealth(HEALTH);

		UtilEnt.vegetate(skeleton);
		UtilEnt.setFakeHead(skeleton, true);

		skeleton.getWorld().strikeLightningEffect(skeleton.getLocation());

		// preDamage uses getAi() which would have been called in a game long before spawnEntity has
		// This is unique to the pumpkin king, so we must manually update the AI's corresponding entity
		getAi().setEntity(skeleton);

		UtilTextMiddle.display(C.cDRedB + "The Pumpkin King", "Has Awoken!", 10, 40, 10);
		_host.Announce(F.main("Game", "The " + F.elem("Pumpkin King") + " has spawned! Killing him will give your team a buff!"), false);

		for (Player player : Bukkit.getOnlinePlayers())
		{
			player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 0.4F);
		}

		for (Entry<Block, Double> entry : UtilBlock.getInRadius(skeleton.getLocation(), 12).entrySet())
		{
			Block block = entry.getKey();
			double setChance = entry.getValue();

			if (!UtilBlock.solid(block) || block.getRelative(BlockFace.UP).getType() != Material.AIR || Math.random() >
					setChance)
			{
				continue;
			}

			_host.getArcadeManager().GetBlockRestore().add(block, BLOCKS[UtilMath.r(BLOCKS.length)].getId(), (byte) 0, Integer.MAX_VALUE);
			_changed.add(block);
		}

		_host.CreatureAllowOverride = false;

		return skeleton;
	}

	@Override
	public MobaAI getAi()
	{
		if (_ai == null)
		{
			_ai = new PumpkinBossAI(_host, _entity, _location, AI_METHOD);
		}

		return _ai;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@EventHandler
	public void updateSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_host.IsLive() || _initialSpawn || !UtilTime.elapsed(_host.GetStateTime(), SPAWN_TIME))
		{
			return;
		}

		_initialSpawn = true;
		_entity = spawnEntity();
	}

	@Override
	public void cleanup()
	{
		super.cleanup();

		for (Block block : _changed)
		{
			_host.getArcadeManager().GetBlockRestore().restore(block);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void preDamage(CustomDamageEvent event)
	{
		if (!event.GetDamageeEntity().equals(_entity) || MobaUtil.isInBoundary(null, _entity, _location, getAi().getBoundaries(), event.GetDamagerPlayer(true)))
		{
			return;
		}

		event.SetCancelled("Outside of area");
	}

	@Override
	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		if (_entity == null || !event.getEntity().equals(_entity))
		{
			return;
		}

		Player player = _entity.getKiller();

		if (player == null)
		{
			return;
		}

		super.entityDeath(event);

		GameTeam team = _host.GetTeam(player);

		if (team == null)
		{
			return;
		}

		_host.Announce(F.main("Game", team.GetFormattedName() + C.mBody + " killed the " + C.cDRedB + DAMAGE_REASON), false);
		UtilTextMiddle.display("", team.GetFormattedName() + C.cWhite + " killed the " + C.cDRedB + DAMAGE_REASON, 10, 40, 10);

		for (Block block : _changed)
		{
			_host.getArcadeManager().GetBlockRestore().restore(block);
		}

		event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.EXPLODE, 1, 0.2F);
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, event.getEntity().getEyeLocation(), 1, 1, 1, 0.1F, 3, ViewDist.LONG);

		// Give the team members the buff
		BuffManager buffManager = _host.getBuffManager();
		for (Player teamMember : team.GetPlayers(true))
		{
			buffManager.apply(new BuffPumpkinKing(_host, teamMember, HELMET));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamage(CustomDamageEvent event)
	{
		if (!event.GetDamageeEntity().equals(_entity))
		{
			return;
		}

		updateDisplay();

		if (event.GetCause() == DamageCause.SUFFOCATION)
		{
			event.SetCancelled("Pumpkin King Suffocation");
		}
	}

	@EventHandler
	public void updateDamage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _entity == null)
		{
			return;
		}

		LivingEntity target = _ai.getTarget();

		if (target != null)
		{
			if (UtilMath.offsetSquared(_entity, target) < DAMAGE_DIRECT_RADIUS_SQUARED)
			{
				_host.getArcadeManager().GetDamage().NewDamageEvent(target, _entity, null, DamageCause.CUSTOM, DAMAGE_DIRECT, true, true, false, DAMAGE_REASON, DAMAGE_REASON);

				// Send a fake hit packet
				// Magic number 0 means swing item/attack
				PacketPlayOutAnimation packet = new PacketPlayOutAnimation(((CraftLivingEntity) _entity).getHandle(), 0);

				for (Player player : Bukkit.getOnlinePlayers())
				{
					UtilPlayer.sendPacket(player, packet);
				}
			}
		}
		else
		{
			MobaUtil.heal(_entity, null, HEALTH_OUT_OF_COMBAT);
			updateDisplay();
		}

		for (LivingEntity entity : UtilEnt.getInRadius(_entity.getLocation(), DAMAGE_RADIUS).keySet())
		{
			if (_entity.equals(entity))
			{
				continue;
			}

			_host.getArcadeManager().GetDamage().NewDamageEvent(entity, _entity, null, DamageCause.CUSTOM, DAMAGE_RANGE, false, true, false, NAME, DAMAGE_REASON);
			UtilAction.velocity(entity, UtilAlg.getTrajectory(_entity, entity).setY(1));
			UtilParticle.PlayParticleToAll(ParticleType.FLAME, entity.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.1F, 5, ViewDist.LONG);
		}
	}

	private void updateDisplay()
	{
		_entity.setCustomName(MobaUtil.getHealthBar(_entity, 20));
	}
}
