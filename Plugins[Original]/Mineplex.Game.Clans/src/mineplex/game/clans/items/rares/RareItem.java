package mineplex.game.clans.items.rares;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.game.clans.items.CustomItem;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class RareItem extends CustomItem
{
	public final long BLOCK_COOLDOWN = 200l;	// Right clicking activates right click for 200ms
	
	protected long _lastBlock;	// Timestamp of last block from wielder
	public long timeSinceLastBlock() { return System.currentTimeMillis() - _lastBlock; }
	
	public RareItem(String name, String[] description,  Material material)
	{
		super(name, description, material);
		
		_lastBlock = 0l;
	}
	
	public void update(Player wielder)
	{
		//	Leave implementation to potential subtypes
	}
	
	public void preUpdate(Player wielder)
	{
	}
	
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		//	Leave implementation to potential subtypes
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{
		if (event.GetDamagerPlayer(true) != null)
		{
			onAttack(event, event.GetDamagerPlayer(true));
		}
		
		super.onAttack(event);
	}
	
	public void onInteract(PlayerInteractEvent event)
	{
		Action action = event.getAction();
		
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
		{
			_lastBlock = System.currentTimeMillis();
		}
		
		super.onInteract(event);
	}
	
	public boolean isHoldingRightClick()
	{
		return timeSinceLastBlock() <= BLOCK_COOLDOWN;
	}
	
	protected void log(String message)
	{
		System.out.println("[Custom Item - " + _displayName + "] " + message);
	}
	
	/**
	 * @param minValue - the minimum value for attribute value range
	 * @param maxValue - the maximum value for attribute value range
	 * @return newly instantiated {@link ValueDistribution} for attribute values in range [{@code minValue}. {@code maxValue}].
	 */
	public static ValueDistribution generateDistribution(double minValue, double maxValue)
	{
		return new ValueDistribution(minValue, maxValue);
	}

	/**
	 * Add a {@link PotionEffect} to {@code player} with specified {@code type}, {@code amplifier} (power) and
	 * {@code tickDuration} of the effect.
	 * @param player - the player to receive the potion effect
	 * @param type - the type of potion to apply
	 * @param tickDuration - the duration (in ticks) to apply the potion for
	 * @param amplifier - the amplifier (level/power, zero-based) of the potion effect
	 */
	public static void grantPotionEffect(Player player, PotionEffectType type, int tickDuration, int amplifier)
	{
		player.removePotionEffect(type);
		player.addPotionEffect(new PotionEffect(type, amplifier, tickDuration));
	}
}
