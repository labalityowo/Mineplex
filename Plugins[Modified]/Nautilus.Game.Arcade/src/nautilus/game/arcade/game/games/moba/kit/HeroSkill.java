package nautilus.game.arcade.game.games.moba.kit;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeroSkill extends Perk
{

	private ItemStack _item;
	private ItemStack _cooldownItem;
	private final int _slot;
	private final ActionType _actionType;
	private boolean _sneakActivate;
	private boolean _dropItemActivate;

	protected HeroKit _kit;
	private long _cooldown;

	private final Map<UUID, Long> _lastSkill = new HashMap<>();

	public HeroSkill(String name, String[] perkDesc)
	{
		this(name, perkDesc, null, -1, null);
	}

	public HeroSkill(String name, String[] perkDesc, ItemStack itemStack, int slot, ActionType actionType)
	{
		super(name, perkDesc);

		_item = itemStack;
		_slot = slot;
		_actionType = actionType;
	}

	public void setSneakActivate(boolean activate)
	{
		_sneakActivate = activate;
	}

	public void setDropItemActivate(boolean activate)
	{
		_dropItemActivate = activate;
	}

	protected void setCooldown(long cooldown)
	{
		_cooldown = cooldown;
	}

	private void prettifyItems()
	{
		String action = null;

		if (_actionType == ActionType.ANY)
		{
			action = "Click";
		}
		if (_actionType == ActionType.L)
		{
			action = "Left Click";
		}
		else if (_actionType == ActionType.R)
		{
			action = "Right Click";
		}

		if (_sneakActivate)
		{
			action += "/Sneak";
		}

		if (_dropItemActivate)
		{
			action += "/Drop Item";
		}

		_item = new ItemBuilder(_item)
				.setTitle((action != null ? C.cYellowB + action + C.cGray + " - " : "") + C.cGreenB + GetName())
				.setLore()
				.addLore(GetDesc())
				.setUnbreakable(true)
				.build();

		_cooldownItem = new ItemBuilder(Material.INK_SACK, (byte) 8)
				.setTitle(C.cRed + GetName())
				.setLore()
				.addLore(GetDesc())
				.setUnbreakable(true)
				.build();
	}

	@Override
	public void SetHost(Kit kit)
	{
		super.SetHost(kit);

		_kit = (HeroKit) kit;
		prettifyItems();
	}

	@EventHandler
	public void giveItem(PlayerKitGiveEvent event)
	{
		Player player = event.getPlayer();

		if (!hasPerk(player))
		{
			return;
		}

		if (isOnCooldown(player))
		{
			player.getInventory().setItem(_slot, _cooldownItem);
		}
		else
		{
			giveItem(player);
		}
	}

	@EventHandler
	public void clearCooldowns(PlayerQuitEvent event)
	{
		UUID key = event.getPlayer().getUniqueId();

		_lastSkill.remove(key);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
	}

	protected boolean isSkillItem(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return false;
		}

		if (!UtilEvent.isAction(event, _actionType))
		{
			if (_actionType != null || event.getAction() == Action.PHYSICAL)
			{
				return false;
			}
		}

		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem();

		if (!hasPerk(player) || UtilPlayer.isSpectator(player) || itemStack == null)
		{
			return false;
		}

		if (itemStack.isSimilar(_item))
		{
			if (_dropItemActivate && !Recharge.Instance.use(player, "Ultimate", _cooldown - 250, false, false))
			{
				return false;
			}

			return !callEvent(player);
		}

		return false;
	}

	protected boolean isSkillSneak(PlayerToggleSneakEvent event)
	{
		if (event.isCancelled() || !event.isSneaking())
		{
			return false;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getInventory().getItem(_slot);

		if (!hasPerk(player) || UtilPlayer.isSpectator(player) || itemStack == null || !itemStack.isSimilar(_item))
		{
			return false;
		}

		return !callEvent(player);
	}

	private boolean callEvent(Player player)
	{
		HeroSkillUseEvent event = new HeroSkillUseEvent(player, this);
		UtilServer.CallEvent(event);

		return event.isCancelled();
	}

	@EventHandler
	public void dropTrigger(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (!_dropItemActivate || !hasPerk(player))
		{
			return;
		}

		// Call interact with a fake PlayerInteractEvent
		PlayerInteractEvent interactEvent = new PlayerInteractEvent(event.getPlayer(), Action.RIGHT_CLICK_AIR, player.getInventory().getItem(_slot), null, null);

		// You actually need to setCancelled false here otherwise it remains cancelled by default when the clicked block is null, thanks Bukkit
		interactEvent.setCancelled(false);
		interact(interactEvent);
	}


	public void useSkill(Player player)
	{
		_lastSkill.put(player.getUniqueId(), System.currentTimeMillis());
		if (_cooldown > 0 && !UtilPlayer.isSpectator(player))
		{
			player.getInventory().setItem(_slot, _cooldownItem);
		}
	}

	protected void broadcast(Player player)
	{
		Moba game = (Moba) Manager.GetGame();
		GameTeam team = game.GetTeam(player);
		HeroKit kit = game.getMobaData(player).getKit();

		if (team == null || kit == null)
		{
			return;
		}

		game.Announce(team.GetColor() + C.Bold + player.getName() + " " + kit.getRole().getChatColor() + kit.GetName() + C.cWhiteB + " activated their " + team.GetColor() + C.Bold + GetName() + C.cWhiteB + ".", false);
		player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0.5F);
	}

	@EventHandler
	public void updateCooldowns(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || _item == null)
		{
			return;
		}

		long current = System.currentTimeMillis();

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player) || UtilPlayer.isSpectator(player) || !_lastSkill.containsKey(player.getUniqueId()))
			{
				continue;
			}

			ItemStack itemStack = player.getInventory().getItem(_slot);
			long start = _lastSkill.get(player.getUniqueId());
			long cooldown = _cooldown;

			// Modify the cooldown with respect to the upgrade items purchased from the shop
			CooldownCalculateEvent cooldownEvent = new CooldownCalculateEvent(player, GetName(), cooldown);
			UtilServer.CallEvent(cooldownEvent);

			cooldown = cooldownEvent.getCooldown();

			boolean done = UtilTime.elapsed(start, cooldown);

			if (done)
			{
				_lastSkill.remove(player.getUniqueId());
				giveItem(player);
			}
			else
			{
				long timeDiff = current - start;
				// Work out the itemstack amount based on the cooldowns.
				// Adding 1 as due to the nature of cooldowns it seems to take much longer to go
				// from 2 -> 1 -> 0 as the itemstack doesn't change
				double amount = (cooldown / 1000) - Math.ceil((double) timeDiff / 1000) + 1;

				if (itemStack == null)
				{
					itemStack = _cooldownItem;
				}

				itemStack.setAmount((int) amount);
			}
		}
	}

	public void giveItem(Player player)
	{
		player.getInventory().setItem(_slot, _item);
	}

	public void useActiveSkill(Player player, long time)
	{
		useActiveSkill(null, player, time);
	}

	public void useActiveSkill(Runnable complete, Player player, long time)
	{
		long now = System.currentTimeMillis();
		long ticks = (long) (time / 1000D);
		ItemStack itemStack = player.getInventory().getItem(getSlot());
		itemStack.setAmount((int) (ticks / 20D));
		UtilInv.addDullEnchantment(itemStack);

		Manager.runSyncTimer(new BukkitRunnable()
		{

			@Override
			public void run()
			{
				long timeLeft = now + time - System.currentTimeMillis();
				double percentage = (double) timeLeft / (double) time;

				if (percentage <= 0)
				{
					UtilTextBottom.display(C.cRedB + GetName(), player);
					cancel();
					return;
				}

				UtilTextBottom.displayProgress(C.cWhiteB + GetName(), percentage, UtilTime.MakeStr(timeLeft), player);
			}
		}, 0, 1);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int iterations = 0;

			@Override
			public void run()
			{
				if (iterations++ > ticks)
				{
					if (complete != null)
					{
						complete.run();
					}
					useSkill(player);
					Kit.GiveItems(player);
					cancel();
					return;
				}

				itemStack.setAmount(itemStack.getAmount() - 1);
			}
		}, 0, 20);
	}

	public void resetCooldown(Player player)
	{
		_lastSkill.put(player.getUniqueId(), 0L);
	}

	protected boolean isTeamDamage(LivingEntity damagee, LivingEntity damager)
	{
		if (damagee.equals(damager))
		{
			return true;
		}

		if (!(damager instanceof Player) || Manager.GetGame().DamageTeamSelf)
		{
			return false;
		}

		GameTeam team = Manager.GetGame().GetTeam((Player) damager);

		return team != null && MobaUtil.isTeamEntity(damagee, team);
	}

	public int getSlot()
	{
		return _slot;
	}

	public ItemStack getCooldownItem()
	{
		return _cooldownItem;
	}

	public boolean isOnCooldown(Player player)
	{
		return _lastSkill.containsKey(player.getUniqueId());
	}

	public boolean isSneakActivate()
	{
		return _sneakActivate;
	}
}
