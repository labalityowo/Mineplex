package nautilus.game.arcade.game.games.quiver.ultimates;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class UltimateNinja extends UltimatePerk
{

	private static final float CHARGE_PASSIVE = 0.4F;
	private static final float CHARGE_PAYLOAD = 0.4F;
	private static final float CHARGE_KILL = 5F;
	private static final float CHARGE_ASSIST = 2F;
	
	private static final int SPEED_AMPLIFIER = 1; 
	
	private long _length;
	
	private Map<UUID, Integer> _kills = new HashMap<>();
	
	public UltimateNinja(long length)
	{
		super("Ancient Blade", new String[] {}, length, CHARGE_PASSIVE, CHARGE_PAYLOAD, CHARGE_KILL, CHARGE_ASSIST);
	}
	
	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}
		
		if (isUsingUltimate(event.GetDamagerPlayer(false)) && event.GetDamagerPlayer(false).getItemInHand().getType() == Material.DIAMOND_SWORD)
		{
			event.AddMod(event.GetDamagerPlayer(false).getName(), GetName(), 9001, true);
		}
	}
	
	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) ((_length / 1000) * 20), SPEED_AMPLIFIER));
		
		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, player.getEyeLocation(), 0, 0, 0, 1F, 100, ViewDist.NORMAL);
	}
	
	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		if (_kills.containsKey(player.getUniqueId()))
		{
			int kills = _kills.get(player.getUniqueId());
			
			if (kills >= 5)
			{
				Manager.GetGame().AddStat(player, "Assassin", 1, true, false);
			}
		}
		
		_kills.remove(player.getUniqueId());
		player.getInventory().setItem(0, new ItemStack(Material.GOLD_SWORD));
	}

	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{		
		if (event.GetEvent().getEntity() == null || event.GetLog().GetKiller() == null)
		{
			return;
		}

		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

		if (player == null)
		{
			return;
		}
		
		if (isUsingUltimate(player))
		{
			UUID uuid = player.getUniqueId();
			
			 _kills.putIfAbsent(uuid, 0);
			_kills.put(uuid, _kills.get(uuid) + 1);
		}
	}
}
