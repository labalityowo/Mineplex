package mineplex.game.nano.game.games.wizards;

import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.components.player.DoubleJumpComponent;
import mineplex.game.nano.game.components.player.GiveItemComponent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.game.nano.game.games.wizards.spells.SpellFireball;
import mineplex.game.nano.game.games.wizards.spells.SpellFortify;
import mineplex.game.nano.game.games.wizards.spells.SpellLevitation;
import mineplex.game.nano.game.games.wizards.spells.SpellTNT;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Wizards extends ScoredSoloGame
{

	private final Spell[] _spells;

	public Wizards(NanoManager manager)
	{
		super(manager, GameType.WIZARDS, new String[]
				{
						"Use your " + C.cYellow + "Spells" + C.Reset + " to eliminate other players.",
						"Points are awarded for " + C.cGreen + "Kills" + C.Reset + " and " + C.cGreen + "Assists" + C.Reset + ".",
						"You lose a point for " + C.cRed + "Dying" + C.Reset + "!",
						C.cYellow + "Most points" + C.Reset + " wins!"
				});

		_spells = new Spell[]
				{
						new SpellFireball(this),
						new SpellTNT(this),
						new SpellLevitation(this),
						new SpellFortify(this)
				};

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent.setFall(false);

		_spectatorComponent.setDeathOut(false);

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(2));

		new GiveItemComponent(this)
				.setArmour(new ItemStack[]
						{
								new ItemBuilder(Material.LEATHER_BOOTS)
										.setTitle(C.cGold + "Wizard's Boots")
										.setColor(Color.MAROON)
										.setUnbreakable(true)
										.setGlow(true)
										.build(),
								new ItemBuilder(Material.LEATHER_LEGGINGS)
										.setTitle(C.cGold + "Wizard's Leggings")
										.setColor(Color.MAROON)
										.setUnbreakable(true)
										.setGlow(true)
										.build(),
								new ItemBuilder(Material.LEATHER_CHESTPLATE)
										.setTitle(C.cGold + "Wizard's Cloak")
										.setColor(Color.MAROON)
										.setUnbreakable(true)
										.setGlow(true)
										.build(),
								new ItemBuilder(Material.LEATHER_HELMET)
										.setTitle(C.cGold + "Wizard's Cap")
										.setColor(Color.MAROON)
										.setUnbreakable(true)
										.setGlow(true)
										.build()
						});

		_compassComponent.setGiveToAlive(true);

		new DoubleJumpComponent(this);

		_manager.getAntiHack().addIgnoredCheck(Speed.class);
		_manager.getAntiHack().addIgnoredCheck(Glide.class);
	}

	@Override
	protected void parseData()
	{
		// Legacy support for old wizard maps
		_mineplexWorld.getSpongeLocations(String.valueOf(Material.CHEST.getId())).forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
	}

	@EventHandler
	public void playerRespawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		for (Spell spell : _spells)
		{
			player.getInventory().addItem(spell.getItemStack());
		}

		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.ENTITY_EXPLOSION)
		{
			event.SetCancelled("Entity Explosion");
		}

		if (event.GetDamageeEntity().equals(event.GetDamagerEntity(true)))
		{
			event.AddMod("Self", -event.GetDamage() * 0.75);
		}
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.Simple);

		Player killed = event.GetEvent().getEntity();

		incrementScore(killed, -1);

		CombatComponent killer = event.GetLog().GetKiller();

		if (killer != null)
		{
			Player killerPlayer = UtilPlayer.searchExact(killer.getUniqueIdOfEntity());

			if (!killed.equals(killerPlayer))
			{
				incrementScore(killerPlayer, 3);
			}
		}

		for (CombatComponent attacker : event.GetLog().GetAttackers())
		{
			if (attacker.equals(killer) || !attacker.IsPlayer())
			{
				continue;
			}

			Player attackerPlayer = UtilPlayer.searchExact(attacker.getUniqueIdOfEntity());

			if (attackerPlayer != null)
			{
				incrementScore(attackerPlayer, 1);
			}
		}
	}

	public void createExplosion(Player source, String reason, Location location, int radius, double damage)
	{
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, null, 0.1F, 1, ViewDist.LONG);
		location.getWorld().playSound(location, Sound.EXPLODE, 1, 0.7F);

		blockLoop: for (Block block : UtilBlock.getBlocksInRadius(location, radius))
		{
			Location blockLocation = block.getLocation();

			for (Location spawn : _playersTeam.getSpawns())
			{
				if (UtilMath.offsetSquared(spawn, blockLocation) < 9)
				{
					continue blockLoop;
				}
			}

			MapUtil.QuickChangeBlockAt(blockLocation, Material.AIR);
		}

		UtilPlayer.getInRadius(location, radius + 2).forEach((player, scale) -> _manager.getDamageManager().NewDamageEvent(player, source, null, DamageCause.CUSTOM, damage, true, true, false, source.getName(), reason));
	}
}
