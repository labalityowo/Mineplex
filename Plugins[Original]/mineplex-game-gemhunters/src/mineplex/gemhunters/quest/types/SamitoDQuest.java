package mineplex.gemhunters.quest.types;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.gemhunters.quest.Quest;

public class SamitoDQuest extends Quest
{

	private static final String NPC_NAME = "Hobo";
	private static final String[] REACTIONS = {
		"Well hello there folks and welcome... to... my... youtube channel",
		"WILLIAMMMMMMM",
		"ALEXXXXXXXXXX",
		"CHISS",
		"Rods and Gaps",
		"Hit him with that w-tap",
		"You're the one who wanted to bring out bows young man"
	};
	
	private final Location _pot;
	private final int _gemsToDonate;
	
	public SamitoDQuest(int id, String name, String description, int startCost, int completeReward, int gemsToDonate)
	{
		super(id, name, description, startCost, completeReward);
		
		_pot = _worldData.getCustomLocation("QUEST_SAM").get(0);
		_pot.getBlock().setType(Material.FLOWER_POT);
		
		_gemsToDonate = gemsToDonate;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (event.isCancelled() || !isActive(player))
		{
			return;
		}
		
		Block block = event.getClickedBlock();
		
		if (block == null)
		{
			return;
		}
		
		if (UtilMath.offsetSquared(block.getLocation(), _pot) < 16)
		{
			if (_economy.getGems(player) < _gemsToDonate)
			{
				player.sendMessage(F.main(NPC_NAME, "Awww come on man, even alex has more gems than you."));
				return;
			}
			
			player.sendMessage(F.main(NPC_NAME, REACTIONS[UtilMath.random.nextInt(REACTIONS.length)]));
			_economy.removeFromStore(player, _gemsToDonate);
			UtilFirework.playFirework(_pot, Type.BURST, Color.GREEN, true, false);
			onReward(player);
		}
	}

}
