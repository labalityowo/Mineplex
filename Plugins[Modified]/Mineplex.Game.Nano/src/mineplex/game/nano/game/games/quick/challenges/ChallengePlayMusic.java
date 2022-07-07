package mineplex.game.nano.game.games.quick.challenges;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.noteblock.NBSReader;
import mineplex.core.noteblock.Note;
import mineplex.core.noteblock.NoteSong;
import mineplex.core.noteblock.UtilNote;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengePlayMusic extends Challenge
{

	private final NoteSong _song;
	private final Map<Player, Integer> _songTick;

	public ChallengePlayMusic(Quick game)
	{
		super(game, ChallengeType.PLAY_MUSIC);

		NoteSong song;

		try
		{
			song = NBSReader.loadSong(".." + File.separator + ".." + File.separator + "update" + File.separator + "songs" + File.separator + "bebop.nbs");
		}
		catch (FileNotFoundException e)
		{
			song = null;
			e.printStackTrace();
		}

		_song = song;
		_songTick = new HashMap<>();

		_timeout = TimeUnit.SECONDS.toMillis(5);
	}

	@Override
	public void challengeSelect()
	{
		_game.getGreenPoints().forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.NOTE_BLOCK));
	}

	@Override
	public void disable()
	{
		_songTick.clear();
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block.getType() != Material.NOTE_BLOCK)
		{
			return;
		}

		event.setCancelled(true);

		Location location = block.getLocation().add(0.5, 1.2, 0.5);

		UtilParticle.PlayParticle(ParticleType.NOTE, location, null, 0.1F, 1, ViewDist.NORMAL, player);

		int tick = _songTick.getOrDefault(player, 0);
		_songTick.put(player, tick + 1);

		_song.getLayerMap().values().forEach(layer ->
		{
			Note note = layer.getNote(tick);

			if (note != null)
			{
				Sound sound = UtilNote.getInstrumentSound(note.getInstrument());
				float volume = layer.getVolume() / 100F;
				float pitch = (float) UtilNote.getPitch(note.getNote() - 33);

				player.playSound(location, sound, volume, pitch);
			}
		});

		if (tick == 10)
		{
			completePlayer(player, false);
		}
	}
}
