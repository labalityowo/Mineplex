package mineplex.core.gadget.gadgets.item;

import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemOAndX extends GameItemGadget
{

	private static final long COOLDOWN_USE = TimeUnit.SECONDS.toMillis(15);
	private static final int BOARD_SIZE = 9;
	private static final int[][] TILE_LOCATIONS =
			{
					{
							-1, -1,
					},
					{
							-1, 0,
					},
					{
							-1, 1
					},
					{
							0, -1
					},
					{
							0, 0
					},
					{
							0, 1,
					},
					{
							1, -1
					},
					{
							1, 0
					},
					{
							1, 1
					}
			};
	private static final ItemStack FLOOR = new ItemStack(Material.WOOL);
	private static final ItemStack DEFAULT_TILE = new ItemStack(Material.WOOL, 1, (short) 0, (byte) 15);
	private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(2);

	private final Set<GameBoard> _gameBoards;

	public ItemOAndX(GadgetManager manager)
	{
		super(manager, "Tic Tac Toe", new String[]
				{
						C.cGray + "Play Tic Tac Toe",
						C.cGray + "with other players!",
						C.blankLine,
						C.cWhite + "Left click the block",
						C.cWhite + "to claim it.",
						C.cWhite + "First person to get 3",
						C.cWhite + "in a row"
				}, CostConstants.POWERPLAY_BONUS, Material.CARPET, (byte) 14, null);

		_gameBoards = new HashSet<>(3);

		Free = false;
		setPPCYearMonth(YearMonth.of(2017, Month.NOVEMBER));
	}

	@Override
	public boolean activatePreprocess(Player player)
	{
		for (GameBoard board : _gameBoards)
		{
			if (board.PlayerA.equals(player) || board.PlayerB.equals(player))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	protected void startGame(Player invitee, Player inviter)
	{
		Location location = inviter.getLocation();

		if (!UtilEnt.isGrounded(inviter))
		{
			inviter.sendMessage(F.main(Manager.getName(), "You must be on the ground to play."));
			invitee.sendMessage(F.main(Manager.getName(), "Sorry, " + F.name(inviter.getName()) + " needs to be on the ground to play."));
			return;
		}

		for (Block block : UtilBlock.getInBoundingBox(location.clone().add(2, 1, 2), location.clone().subtract(2, 0, 2)))
		{
			if (UtilBlock.airFoliage(block))
			{
				continue;
			}

			inviter.sendMessage(F.main(Manager.getName(), "You cannot play in such a small area."));
			invitee.sendMessage(F.main(Manager.getName(), "Sorry, " + F.name(inviter.getName()) + " needs to find a more open area to play."));
			return;
		}

		invitee.sendMessage(F.main(Manager.getName(), F.name(inviter.getName()) + " challenged you to a game of " + F.name(getName()) + "!"));
		inviter.sendMessage(F.main(Manager.getName(), "You challenged " + F.name(invitee.getName()) + " to a game of " + F.name(getName()) + "!"));

		location.setYaw(0);

		{
			Location teleport = location.clone().add(0, 0, 2);
			teleport.setYaw(180);
			inviter.teleport(teleport);
		}
		{
			Location teleport = location.clone().subtract(0, 0, 2);
			teleport.setYaw(0);
			invitee.teleport(teleport);
		}

		String title = C.cYellowB + getName();
		UtilTextMiddle.display(title, "You are " + C.cGreenB + "Green", 0, 20, 5, invitee);
		UtilTextMiddle.display(title, "You are " + C.cRedB + "Red", 0, 20, 5, inviter);
		location.getWorld().playSound(location, Sound.LEVEL_UP, 1, 1);

		_gameBoards.add(new GameBoard(inviter, invitee, location.subtract(0, 1.4, 0)));
	}

	@EventHandler
	public void armorStandInteract(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
		{
			return;
		}

		Entity entity = event.getEntity();
		Player player = (Player) event.getDamager();

		for (GameBoard board : _gameBoards)
		{
			boolean playerA = board.PlayerA.equals(player);
			boolean playerB = board.PlayerB.equals(player);
			boolean playerATurn = board.Turn % 2 == 0;

			if (!playerA && !playerB)
			{
				continue;
			}
			else if (playerA && !playerATurn || playerB && playerATurn)
			{
				player.sendMessage(F.main(Manager.getName(), "It's not your turn."));
				continue;
			}

			for (int i = 0; i < board.Pieces.length; i++)
			{
				ArmorStand piece = board.Pieces[i];

				if (!piece.equals(entity))
				{
					continue;
				}

				int ownership = board.OwnedPieces[i];

				if (ownership != 0)
				{
					player.sendMessage(F.main(Manager.getName(), "That piece has already been claimed."));
				}
				else
				{
					board.OwnedPieces[i] = playerATurn ? 1 : 2;
					piece.setHelmet(new ItemBuilder(Material.WOOL, (byte) (playerATurn ? 14 : 5)).build());
					piece.getWorld().playSound(piece.getLocation(), Sound.NOTE_STICKS, 1, 1);
					UtilParticle.PlayParticleToAll(ParticleType.CRIT, piece.getLocation().add(0, 1.5, 0), 0.25F, 0.25F, 0.25F, 0.05F, 5, ViewDist.NORMAL);

					board.nextTurn();

					Manager.runSyncTimer(new BukkitRunnable()
					{
						int iterations = 0;

						@Override
						public void run()
						{
							if (++iterations == 10)
							{
								cancel();
								return;
							}

							piece.teleport(piece.getLocation().subtract(0, 0.025, 0));
						}
					}, 1, 1);
				}
			}
		}
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		remove(player);
	}

	private void remove(Player player)
	{
		_gameBoards.removeIf(board ->
		{
			if (board.PlayerA.equals(player) || board.PlayerB.equals(player))
			{
				cleanupBoard(board, false);
				return true;
			}

			return false;
		});

		_invites.remove(player.getName());
	}

	@EventHandler
	public void updateTimeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWER)
		{
			return;
		}

		_gameBoards.removeIf(board ->
		{
			if (UtilTime.elapsed(board.StartTime, TIMEOUT))
			{
				cleanupBoard(board, false);
				return true;
			}

			return false;
		});
	}

	private void cleanupBoard(GameBoard board, boolean remove)
	{
		Recharge.Instance.use(board.PlayerA, getName(), COOLDOWN_USE, true, true);
		board.cleanup();

		if (remove)
		{
			_gameBoards.remove(board);
		}
	}

	private class GameBoard
	{

		final Player PlayerA;
		final Player PlayerB;
		final int[] OwnedPieces;
		final ArmorStand[] Floor;
		final ArmorStand[] Pieces;
		final long StartTime;
		int Turn;
		boolean First = true;

		GameBoard(Player playerA, Player playerB, Location center)
		{
			PlayerA = playerA;
			PlayerB = playerB;

			OwnedPieces = new int[BOARD_SIZE];
			Arrays.fill(OwnedPieces, 0);

			Floor = new ArmorStand[BOARD_SIZE];
			Pieces = new ArmorStand[BOARD_SIZE];

			for (int i = 0; i < Floor.length; i++)
			{
				int[] cords = TILE_LOCATIONS[i];
				Location location = center.clone().add(cords[0], 0, cords[1]);

				{
					ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);

					prepareStand(stand);
					stand.setHelmet(FLOOR);

					Floor[i] = stand;
				}
				{
					ArmorStand stand = location.getWorld().spawn(location.add(0, 1.25, 0), ArmorStand.class);

					prepareStand(stand);
					stand.setSmall(true);
					stand.setHelmet(DEFAULT_TILE);

					Pieces[i] = stand;
				}
			}

			StartTime = System.currentTimeMillis();

			// Starting on turn 1 will make player B go first
			if (Math.random() > 0.5)
			{
				Turn = 1;
			}

			nextTurn();
		}

		void nextTurn()
		{
			boolean playerATurn = Turn % 2 == 0;

			if (hasDrawn())
			{
				drawGame(PlayerA, PlayerB);
				cleanupBoard(this, true);
				return;
			}
			else if (hasWon(playerATurn ? 1 : 2))
			{
				Player winner = playerATurn ? PlayerA : PlayerB;
				Player loser = playerATurn ? PlayerB : PlayerA;

				endGame(winner, loser);
				cleanupBoard(this, true);
				return;
			}
			else
			{
				Turn++;
				playerATurn = !playerATurn;
			}

			if (First)
			{
				boolean finalPlayerATurn = playerATurn;

				Manager.runSyncLater(() -> informTurn(finalPlayerATurn), 20);
			}
			else
			{
				informTurn(playerATurn);
			}
		}

		private void informTurn(boolean playerATurn)
		{
			UtilTextMiddle.display("", C.cYellowB + "Your Turn", 5, 15, 5, playerATurn ? PlayerA : PlayerB);
		}

		void cleanup()
		{
			for (ArmorStand piece : Pieces)
			{
				piece.remove();
			}

			for (ArmorStand floor : Floor)
			{
				floor.remove();
			}
		}

		boolean hasWon(int valueToCheck)
		{
			return (OwnedPieces[0] == valueToCheck && OwnedPieces[1] == valueToCheck && OwnedPieces[2] == valueToCheck) ||
					(OwnedPieces[3] == valueToCheck && OwnedPieces[4] == valueToCheck && OwnedPieces[5] == valueToCheck) ||
					(OwnedPieces[6] == valueToCheck && OwnedPieces[7] == valueToCheck && OwnedPieces[8] == valueToCheck) ||
					(OwnedPieces[0] == valueToCheck && OwnedPieces[4] == valueToCheck && OwnedPieces[8] == valueToCheck) ||
					(OwnedPieces[2] == valueToCheck && OwnedPieces[4] == valueToCheck && OwnedPieces[6] == valueToCheck) ||
					(OwnedPieces[0] == valueToCheck && OwnedPieces[3] == valueToCheck && OwnedPieces[6] == valueToCheck) ||
					(OwnedPieces[1] == valueToCheck && OwnedPieces[4] == valueToCheck && OwnedPieces[7] == valueToCheck) ||
					(OwnedPieces[2] == valueToCheck && OwnedPieces[5] == valueToCheck && OwnedPieces[8] == valueToCheck);
		}

		boolean hasDrawn()
		{
			for (int piece : OwnedPieces)
			{
				if (piece == 0)
				{
					return false;
				}
			}

			return true;
		}

		private void prepareStand(ArmorStand stand)
		{
			stand.setGravity(false);
			stand.setVisible(false);
		}
	}
}
