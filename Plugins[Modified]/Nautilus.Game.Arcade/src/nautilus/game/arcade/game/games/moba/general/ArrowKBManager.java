package nautilus.game.arcade.game.games.moba.general;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

public class ArrowKBManager implements Listener
{

	private static final String ENTITY_METADATA = "KB";
	private final Moba _host;

	public ArrowKBManager(Moba host)
	{
		_host = host;
	}

	@EventHandler
	public void arrowDamage(CustomDamageEvent event)
	{
		Projectile projectile = event.GetProjectile();

		if (projectile == null || projectile.getType() != EntityType.ARROW || projectile.hasMetadata(ENTITY_METADATA))
		{
			return;
		}

		event.SetKnockback(false);
	}

	public void allowKnockback(Entity projectile)
	{
		projectile.setMetadata(ENTITY_METADATA, new FixedMetadataValue(_host.getArcadeManager().getPlugin(), true));
	}

}
