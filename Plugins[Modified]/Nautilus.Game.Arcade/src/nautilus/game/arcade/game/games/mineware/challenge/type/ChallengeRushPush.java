package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import mineplex.core.itemstack.ItemBuilder;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.TeamChallenge;

/**
 * A team based challenge where every team has to push the opposing team members to the void.
 */
public class ChallengeRushPush extends TeamChallenge
{
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_FIXED_Z = 6;
	private static final int MAP_HEIGHT = 1;

	private static final byte STAINED_CLAY_BLUE_DATA = 11;
	private static final byte STAINED_CLAY_RED_DATA = 14;
	private static final double DAMAGE = 0.001;
	private static final double KNOCKBACK = 5.5;

	public ChallengeRushPush(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Blue",
			"Red",
			true,
			(byte) 11,
			(byte) 14,
			"Rush Push",
			"Attack the enemy team.",
			"Push them off the platform.");

		Settings.setUseMapHeight();
		Settings.setCanCruble();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
		Settings.setTeamBased();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -(size); x <= size; x++)
		{
			for (int z = -MAP_FIXED_Z; z <= MAP_FIXED_Z; z += MAP_FIXED_Z)
			{
				if (z == 0)
					continue;

				spawns.add(getCenter().add(x, MAP_HEIGHT, z));
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -MAP_FIXED_Z; z <= MAP_FIXED_Z; z++)
			{
				Block block = getCenter().getBlock().getRelative(x, 0, z);
				setBlock(block, Material.STAINED_CLAY);

				if (z != 0)
				{
					setData(block, (byte) (z < 0 ? STAINED_CLAY_BLUE_DATA : z > 0 ? STAINED_CLAY_RED_DATA : 0));
				}

				addBlock(block);
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamagePvP = true;

		addDiamondSword();
		equipTeamHelmets();
	}

	@Override
	public void onEnd()
	{
		Host.DamagePvP = false;
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		Player damager = event.GetDamagerPlayer(false);
		Player damagee = event.GetDamageePlayer();

		if (!isPlayerValid(damager) || !isPlayerValid(damagee))
			return;

		if (areOnSameTeam(damager, damagee))
		{
			event.SetCancelled("Friendly Fire");
			return;
		}

		if (event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			event.AddMult("No Damage", null, DAMAGE, false);
			event.AddKnockback("Knockback", KNOCKBACK);
		}
	}

	private void addDiamondSword()
	{
		ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD)
			.setUnbreakable(true)
			.setItemFlags(ItemFlag.HIDE_UNBREAKABLE)
			.build();

		setItem(Settings.getLockedSlot(), sword);
	}

	private void equipTeamHelmets()
	{
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();

		meta.setColor(Color.BLUE);
		helmet.setItemMeta(meta);

		for (Player player : getFirstTeam().getPlayers())
		{
			player.getInventory().setHelmet(helmet);
		}

		meta.setColor(Color.RED);
		helmet.setItemMeta(meta);

		for (Player player : getSecondTeam().getPlayers())
		{
			player.getInventory().setHelmet(helmet);
		}
	}
}
