package nautilus.game.arcade.game.games.wizards.spells;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.metadata.FixedMetadataValue;

public class SpellSpectralArrow extends Spell implements SpellClick
{
	private HashMap<Arrow, Location[]> _spectralArrows = new HashMap<Arrow, Location[]>();

	@Override
	public void castSpell(Player player)
	{
		Arrow arrow = player.launchProjectile(Arrow.class);

		arrow.setVelocity(arrow.getVelocity().multiply(2.3));

		arrow.setMetadata("SpellLevel", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), getSpellLevel(player)));

		((CraftArrow) arrow).getHandle().fromPlayer = 0;

		_spectralArrows.put(arrow, new Location[]
			{
					player.getLocation(), player.getLocation()
			});

		charge(player);
	}

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		Iterator<Entry<Arrow, Location[]>> itel = _spectralArrows.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<Arrow, Location[]> entry = itel.next();

			for (Location loc : UtilShapes.getLinesDistancedPoints(entry.getValue()[1], entry.getKey().getLocation(), 0.7D))
			{
				UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc, 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
			}

			entry.getValue()[1] = entry.getKey().getLocation();
		}
	}

	@EventHandler
	public void onSecond(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<Arrow> itel = _spectralArrows.keySet().iterator();

		while (itel.hasNext())
		{
			Arrow entity = itel.next();

			if (entity.isOnGround() || !entity.isValid())
			{
				itel.remove();
			}
		}
	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		Location[] loc = _spectralArrows.remove(event.GetProjectile());

		if (loc != null)
		{
			int spellLevel = event.GetProjectile().getMetadata("SpellLevel").get(0).asInt();

			event.AddMod("Negate Damage", "Negate Damage", -event.GetDamage(), false);
			event.AddMod("Spectral Arrow", "Spectral Arrow", 6 + loc[0].distance(event.GetDamageeEntity().getLocation())
					/ (7D - spellLevel), true);
		}
	}
}
