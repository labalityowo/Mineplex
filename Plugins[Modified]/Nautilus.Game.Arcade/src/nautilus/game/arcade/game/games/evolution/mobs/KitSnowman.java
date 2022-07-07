package nautilus.game.arcade.game.games.evolution.mobs;


public class KitSnowman 
//extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
//	public KitSnowman(ArcadeManager manager)
//	{
//		super(manager, "Snowman",
//				new String[] 
//						{
//				F.elem("Right-Click with Snowball") + " to use " + F.elem("Throw Snowball")
//						}, 
//						new String[]
//								{
//				F.elem("Recieve ") + C.cWhite + "1 Snowball / 1 Second",
//								},
//						new Perk[] 
//								{
//				new PerkConstructor("Snowballer", 1, 16, Material.SNOW_BALL, "Snowball", false)
//								}, EntityType.SNOWMAN);
//	}
//
//	@Override
//	public void GiveItems(Player player) 
//	{
//		player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).build());
//		player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).build());
//		player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).build());
//		player.getInventory().setBoots(new ItemBuilder(Material.IRON_BOOTS).build());
//		
//		player.getInventory().setItem(0, new ItemBuilder(Material.WOOD_SPADE).build());
//		
//		player.getWorld().playSound(player.getLocation(), Sound.STEP_SNOW, 4f, 1f);
//		
//		//Disguise
//		DisguiseSnowman disguise = new DisguiseSnowman(player);
//		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
//		disguise.setCustomNameVisible(true);
//		
//		Manager.GetDisguise().undisguise(player);
//		Manager.GetDisguise().disguise(disguise);
//		
//		player.getInventory().setItem(8, new ItemBuilder(Material.COMPASS).setTitle(F.item("Tracking Compass")).build());
//	}
//	
//	@EventHandler
//	public void onInteract(final PlayerInteractEvent event)
//	{
//		if (!UtilEvent.isAction(event, ActionType.R))
//			return;
//		
//		if (!Manager.GetGame().IsLive())
//			return;
//		
//		if (!HasKit(event.getPlayer()))
//			return;
//		
//		if (!UtilGear.isMat(event.getItem(), Material.SNOW_BALL))
//			return;
//		
//		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(event.getPlayer());
//		Bukkit.getServer().getPluginManager().callEvent(useEvent);
//		
//		if (useEvent.isCancelled())
//		{
//			event.setCancelled(true);
//			
//			Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					UtilInv.Update(event.getPlayer());
//				}
//			}, 10);
//			return;
//		}
//	}
//	
//	@EventHandler
//	public void SnowballHit(CustomDamageEvent event)
//	{
//		if (!Manager.GetGame().IsLive())
//			return;
//		
//		if (!(event.GetDamagerEntity(true) instanceof Player))
//			return;
//			
//		Player player = event.GetDamagerPlayer(true);
//		
//		if (!Manager.IsAlive(player))
//			return;
//		
//		if (!HasKit(player))
//			return;
//		
//		if (event.GetProjectile() == null)
//			return;
//
//		if (!(event.GetProjectile() instanceof Snowball))
//			return;
//		
//		event.AddMod("Snowman Kit", "Snowball", 3, true);
//	}
}
