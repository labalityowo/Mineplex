package mineplex.hub.plugin;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ChristmasHubPlugin extends HubPlugin
{

	private static final long RELEASE_DATE;

	static
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("America/New_York")));
		calendar.set(2017, Calendar.DECEMBER, 8, 6, 0);
		RELEASE_DATE = calendar.getTimeInMillis();
	}

	private NPC _christmasNPC;

	public ChristmasHubPlugin()
	{
		super("Christmas");
	}

	@Override
	protected void setupWorld()
	{
		_manager.GetSpawn().getWorld().setTime(18000);
	}

	@EventHandler
	public void updateNPC(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWER)
		{
			return;
		}

		if (_christmasNPC == null)
		{
			List<NPC> npcs = _npcManager.getNPCs("GAME_Christmas");

			if (npcs.isEmpty())
			{
				return;
			}

			NPC npc = npcs.get(0);

			if (npc.getEntity() == null)
			{
				return;
			}

			npc.getNameTag();
			_christmasNPC = npc;
		}

		String formatted;
		long time = getTimeUntilRelease();

		if (time > 0)
		{
			formatted = C.cYellowB + UtilTime.MakeStr(time);
		}
		else
		{
			formatted = C.cAquaB + "NEW GAME";
		}

		_christmasNPC.getNameTag().setText(formatted, _christmasNPC.getEntity().getCustomName());
	}

	@EventHandler
	public void npcInteract(NPCInteractEvent event)
	{
		if (event.getNpc().equals(_christmasNPC) && getTimeUntilRelease() > 0)
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main(_moduleName, "Coming soon."));
		}
	}

	private long getTimeUntilRelease()
	{
		return RELEASE_DATE - Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("America/New_York"))).getTimeInMillis();
	}
}
