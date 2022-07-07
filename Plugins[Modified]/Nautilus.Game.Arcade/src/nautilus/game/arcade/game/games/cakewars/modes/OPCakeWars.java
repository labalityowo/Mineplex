package nautilus.game.arcade.game.games.cakewars.modes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.event.CakeRotEvent;
import nautilus.game.arcade.game.games.cakewars.general.CakePlayerModule;
import nautilus.game.arcade.game.games.cakewars.island.CakeIslandModule;
import nautilus.game.arcade.game.games.cakewars.item.CakeSpecialItem;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeDeployPlatform;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeIceBridge;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeSafeTeleport;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeSheep;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeWall;
import nautilus.game.arcade.game.games.cakewars.shop.CakeItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeResource;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopItemType;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopModule;
import nautilus.game.arcade.game.games.cakewars.shop.trap.CakeBearTrap;
import nautilus.game.arcade.game.games.cakewars.shop.trap.CakeTNTTrap;
import nautilus.game.arcade.game.games.cakewars.ui.CakeResourcePage;
import nautilus.game.arcade.game.modules.chest.ChestLootPool;

public class OPCakeWars extends CakeWars
{

	private final PotionEffect SUGAR_BUFF = new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false);

	public OPCakeWars(ArcadeManager manager)
	{
		super(manager);

		AllowParticles = false;
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		getCakeTeamModule().getCakeTeams().values().forEach(cakeTeam ->
		{
			for (Block block : UtilBlock.getSurrounding(cakeTeam.getCake().getBlock(), false))
			{
				if (block.getType() == Material.AIR)
				{
					getCakePlayerModule().getPlacedBlocks().add(block);
					block.setType(Material.OBSIDIAN);
				}
			}

			cakeTeam.getUpgrades().entrySet().forEach(entry -> entry.setValue(entry.getKey().getLevels().length));
		});

		for (GameTeam team : GetTeamList())
		{
			for (Player player : team.GetPlayers(false))
			{
				UtilTextMiddle.display(team.GetColor() + C.Bold + "Sugar Rush", team.GetColor() + "Hyper" + C.Reset + " items available!", 0, 50, 40, player);
				player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
			}
		}
	}

	@EventHandler
	public void updateSugarRush(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		GetTeamList().forEach(team ->
		{
			if (getCakePointModule().ownedNetherStarPoints(team) > 0)
			{
				for (Player player : team.GetPlayers(true))
				{
					if (UtilPlayer.isSpectator(player))
					{
						continue;
					}

					player.addPotionEffect(SUGAR_BUFF, true);
					UtilParticle.PlayParticleToAll(ParticleType.FLAME, player.getLocation().add(0, 1, 0), null, 0.1F, 3, ViewDist.LONG);
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK || !hasSugarBuff(event.GetDamagerPlayer(false)))
		{
			return;
		}

		event.AddMod(GetName(), event.GetDamage() * 0.25);
		event.AddKnockback(GetName(), 1.25);
	}

	@EventHandler
	public void cakeRot(CakeRotEvent event)
	{
		WorldBorder border = WorldData.World.getWorldBorder();
		border.setCenter(GetSpectatorLocation());
		border.setSize(Math.max(WorldData.MaxX, WorldData.MaxZ) * 2);
		border.setSize(40, TimeUnit.MINUTES.toSeconds(15));
		border.setDamageAmount(0);

		UtilTextMiddle.display(C.cRedB + "Sugar Crash", "The border is closing in.", 0, 50, 0, UtilServer.getPlayers());

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!IsLive())
				{
					cancel();
					return;
				}

				int size = (int) border.getSize() / 2;

				WorldData.MinX = WorldData.MinZ = -size;
				WorldData.MaxX = WorldData.MaxZ = size;
			}
		}, 0, 20);
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		WorldBorder border = WorldData.World.getWorldBorder();
		border.setSize(Integer.MAX_VALUE);
	}

	@Override
	public int getGeneratorRate(CakeResource resource, int current)
	{
		if (resource == CakeResource.STAR)
		{
			return -1;
		}

		// 2 seems like a low number to increase by, but all upgrades, including Resource Generator are maxed out by default.
		return current * 2;
	}

	@Override
	public List<CakeItem> generateItems(CakeResource resource)
	{
		switch (resource)
		{
			case BRICK:
				return Arrays.asList
						(
								// Diamond Set
								new CakeShopItem(CakeShopItemType.HELMET, new ItemStack(Material.DIAMOND_HELMET), 5),
								new CakeShopItem(CakeShopItemType.CHESTPLATE, new ItemStack(Material.DIAMOND_CHESTPLATE), 8),
								new CakeShopItem(CakeShopItemType.LEGGINGS, new ItemStack(Material.DIAMOND_LEGGINGS), 6),
								new CakeShopItem(CakeShopItemType.BOOTS, new ItemStack(Material.DIAMOND_BOOTS), 5),

								// Sword
								new CakeShopItem(CakeShopItemType.SWORD, new ItemStack(Material.DIAMOND_SWORD), 5),

								// Bow
								new CakeShopItem(CakeShopItemType.BOW, new ItemStack(Material.BOW), 12),

								// Pickaxe
								new CakeShopItem(CakeShopItemType.PICKAXE, new ItemStack(Material.DIAMOND_PICKAXE), 8),

								// Axe
								new CakeShopItem(CakeShopItemType.AXE, new ItemStack(Material.DIAMOND_AXE), 3),

								// Arrow
								new CakeShopItem(CakeShopItemType.OTHER, new ItemStack(Material.ARROW, 3), 12),

								// Blocks
								// Wool
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.WOOL, 16), 3),

								// Coloured Clay
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.STAINED_CLAY, 8), 8),

								// Wood
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.WOOD, 8), 8),

								// End Stone
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.ENDER_STONE, 8), 12),

								// Deploy Platform
								new CakeShopItem(CakeShopItemType.OTHER, CakeDeployPlatform.ITEM_STACK, 5),

								// Walls
								new CakeShopItem(CakeShopItemType.OTHER, CakeWall.ITEM_STACK, 5),

								// Emerald
								new CakeShopItem(CakeShopItemType.OTHER, new ItemStack(Material.EMERALD), 20)
						);
			case EMERALD:
				return Arrays.asList
						(
								// Obsidian
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.OBSIDIAN), 8),

								// Shears
								new CakeShopItem(CakeShopItemType.SHEARS, new ItemStack(Material.SHEARS), 5),

								// Golden Apple
								new CakeShopItem(CakeShopItemType.OTHER, new ItemStack(Material.GOLDEN_APPLE), 8),

								// Ender pearl
								new CakeShopItem(CakeShopItemType.OTHER, CakeShopModule.ENDER_PEARL, 7),

								// Rune of Holding
								new CakeShopItem(CakeShopItemType.OTHER, CakePlayerModule.RUNE_OF_HOLDING, 20),

								// Special
								new CakeShopItem(CakeShopItemType.OTHER, CakeSheep.ITEM_STACK, 8),
								new CakeShopItem(CakeShopItemType.OTHER, CakeIceBridge.ITEM_STACK, 10),
								new CakeShopItem(CakeShopItemType.OTHER, CakeSafeTeleport.ITEM_STACK, 10),

								// Traps
								new CakeTNTTrap(8),
								new CakeBearTrap(8)
						);
			default:
				return super.generateItems(resource);
		}
	}

	@Override
	public List<CakeSpecialItem> generateSpecialItems()
	{
		return Arrays.asList
				(
						new CakeDeployPlatform(this),
						new CakeWall(this),
						new CakeSheep(this),
						new CakeIceBridge(this),
						new CakeSafeTeleport(this)
				);
	}

	@Override
	public void generateChests()
	{
		_chestLootModule.registerChestType(CakeIslandModule.CHEST_TYPE, new ArrayList<>(),

				new ChestLootPool()
						.addItem(new ItemBuilder(Material.DIAMOND_SWORD)
								.addEnchantment(Enchantment.KNOCKBACK, 1)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.BOW)
								.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1)
								.addEnchantment(Enchantment.ARROW_INFINITE, 1)
								.build())
						.addItem(new ItemBuilder(Material.GOLD_PICKAXE)
								.setTitle(C.cGoldB + "The Golden Pickaxe")
								.setUnbreakable(true)
								.build())
						.addItem(CakeShopModule.ENDER_PEARL, 10, 20)
						.addItem(CakeWall.ITEM_STACK, 10, 20)
						.addItem(CakeIceBridge.ITEM_STACK, 2, 4)
						.addItem(CakeSafeTeleport.ITEM_STACK, 2, 4)
						.addItem(CakeSheep.ITEM_STACK, 2, 4)
						.addItem(new ItemStack(Material.GOLDEN_APPLE), 5, 10)

		).destroyAfterOpened(30);
	}

	@Override
	public CakeResourcePage getShopPage(CakeResource resource, Player player)
	{
		if (resource == CakeResource.STAR)
		{
			return new CakeResourcePage(getArcadeManager(), getCakeShopModule().getShop(), player, 27, CakeResource.STAR, Collections.emptyList())
			{
				@Override
				protected void buildPage()
				{
					super.buildPage();

					addButtonNoAction(13, new ItemBuilder(Material.SUGAR)
							.setTitle(C.cRedB + "Sugar Buff")
							.addLore(
									"",
									"Capture the center beacon to earn",
									"your team the following buffs:",
									"",
									" - " + C.cGreen + "Speed I" + C.cGray + " Potion Buff",
									" - +" + C.cGreen + "25%" + C.cGray + " Damage",
									" - +" + C.cGreen + "25%" + C.cGray + " Knockback"
									)
							.build());
				}

				@Override
				protected void buildMulti()
				{
				}
			};
		}

		return super.getShopPage(resource, player);
	}

	@Override
	public boolean isAllowingGameStats()
	{
		return false;
	}

	@Override
	public String GetMode()
	{
		return "Sugar Rush";
	}

	private boolean hasSugarBuff(Player player)
	{
		return player != null && player.hasPotionEffect(PotionEffectType.SPEED);
	}
}
