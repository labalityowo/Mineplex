package mineplex.core.pet;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.NavigationAbstract;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPigZombie;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.google.gson.Gson;

import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.ClientWebResponseEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.pet.event.PetSpawnEvent;
import mineplex.core.pet.repository.PetRepository;
import mineplex.core.pet.repository.token.ClientPetTokenWrapper;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class PetManager extends MiniClientPlugin<PetClient>
{
	public enum Perm implements Permission
	{
		WIDDER_PET,
		GUARDIAN_PET,
	}

	private static Object _petOwnerSynch = new Object();
	private static Object _petRenameSynch = new Object();
	
	private DisguiseManager _disguiseManager;
	private mineplex.core.creature.Creature _creatureModule;
	private PetRepository _repository;
	private BlockRestore _blockRestore;

	private Map<String, PetType> _activePetOwnerTypes = new HashMap<>();
	private Map<String, Entity> _activePetOwners;
	private Map<String, Integer> _failedAttempts;
	
	private Map<String, PetType> _petOwnerQueue = new HashMap<>();
	private Map<String, String> _petRenameQueue = new HashMap<>();
	private DonationManager _donationManager;
	private CoreClientManager _clientManager;
	private InventoryManager _inventoryManager;

	private Map<Entity, FlyingPetManager> _flyingPets = new HashMap<>();
	private Map<Entity, TrueLoveData> _trueLovePets = new HashMap<>();

	private ShapeWings _grimReaperWings = new ShapeWings(ParticleType.RED_DUST.particleName, new org.bukkit.util.Vector(0.2, 0.2, 0.2), 1, 0, false, ShapeWings.DEFAULT_ROTATION, ShapeWings.SMALL_ANGEL_WING_PATTERN);
	private ShapeWings _grimReaperWingsEdge = new ShapeWings(ParticleType.RED_DUST.particleName, new org.bukkit.util.Vector(0.1, 0.1, 0.1), 1, 0, true, ShapeWings.DEFAULT_ROTATION, ShapeWings.SMALL_ANGEL_WING_PATTERN);

	private ShapeWings _cupidWings = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, false, ShapeWings.DEFAULT_ROTATION, ShapeWings.SMALL_HEART_WING_PATTERN);
	private ShapeWings _cupidWingsWhite = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, '%', ShapeWings.DEFAULT_ROTATION, ShapeWings.SMALL_HEART_WING_PATTERN);
	private ShapeWings _cupidWingsEdge = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(0, 0, 0), 1, 0, true, ShapeWings.DEFAULT_ROTATION, ShapeWings.SMALL_HEART_WING_PATTERN);
	
	public PetManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
			InventoryManager inventoryManager, DisguiseManager disguiseManager, mineplex.core.creature.Creature creatureModule, BlockRestore restore)
	{
		super("Pet Manager", plugin);
		
		_creatureModule = creatureModule;		
		_disguiseManager = disguiseManager;
		_repository = new PetRepository();
		_blockRestore = restore;
        _donationManager = donationManager;
        _clientManager = clientManager;
        _inventoryManager = inventoryManager;
		
		_activePetOwners = new HashMap<>();
		_failedAttempts = new HashMap<>();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.LEGEND.setPermission(Perm.WIDDER_PET, true, true);
		PermissionGroup.TITAN.setPermission(Perm.GUARDIAN_PET, true, true);
	}
	
	public void addPetOwnerToQueue(String playerName, PetType petType)
	{
		synchronized (_petOwnerSynch)
		{
			_petOwnerQueue.put(playerName, petType);
		}
	}
	
	public void addRenamePetToQueue(String playerName, String petName) 
	{
		synchronized (_petRenameSynch)
		{
			_petRenameQueue.put(playerName, petName);
		}
	}
	
	@EventHandler
	public void processQueues(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		synchronized (_petOwnerSynch)
		{
			for (String playerName : _petOwnerQueue.keySet())
			{
				Player player = Bukkit.getPlayerExact(playerName);
				
				if (player != null && player.isOnline())
				{
					addPetOwner(player, _petOwnerQueue.get(playerName), player.getLocation());
				}
			}
			
			_petOwnerQueue.clear();
		}
		
		synchronized (_petRenameQueue)
		{
			for (String playerName : _petRenameQueue.keySet())
			{
				Player player = Bukkit.getPlayerExact(playerName);
				
				if (player != null && player.isOnline())
				{
					Entity activePet = getActivePet(playerName);
					if (activePet != null)
					{
						activePet.setCustomNameVisible(true);
						activePet.setCustomName(_petRenameQueue.get(playerName));
					}
				}
			}
			
			_petRenameQueue.clear();
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
	    Player p = event.getPlayer();

		if (_clientManager.Get(p).hasPermission(Perm.WIDDER_PET))
		{
			if (!Get(p).getPets().containsKey(PetType.WITHER))
			{
				Get(p).getPets().put(PetType.WITHER, "Widder");
			}
		}

		if (_clientManager.Get(p).hasPermission(Perm.GUARDIAN_PET))
		{
			if (!Get(p).getPets().containsKey(PetType.SKELETON))
			{
				Get(p).getPets().put(PetType.SKELETON, "Guardian");
			}
		}
	}
	
	public void addPetOwner(Player player, PetType petType, Location location)
	{
		if (_activePetOwnerTypes.containsKey(player.getName()))
		{
			if (_activePetOwnerTypes.get(player.getName()) != petType)
			{
				removePet(player, true);
			}
			else
			{
				return;
			}
		}

		PetSpawnEvent petSpawnEvent = new PetSpawnEvent(player, petType.getEntityType(), location);
		Bukkit.getPluginManager().callEvent(petSpawnEvent);

		if (petSpawnEvent.isCancelled())
			return;

		Entity pet;
		EntityType entityType = petType.getEntityType();

		//Wither Spawn
		if (entityType == EntityType.WITHER)
		{ 
			_creatureModule.SetForce(true);

			pet = location.getWorld().spawnEntity(location, EntityType.SILVERFISH);
			UtilEnt.silence(pet, true);

			DisguiseWither witherDisguise = new DisguiseWither(pet);

			witherDisguise.setInvulTime(530);

			Creature silverfish = (Creature) _creatureModule.SpawnEntity(location, EntityType.SILVERFISH);
            UtilEnt.vegetate(silverfish, true);
			UtilEnt.silence(silverfish, true);
			silverfish.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
			pet.setPassenger(silverfish);

			_disguiseManager.disguise(witherDisguise);

			_creatureModule.SetForce(false);
		}
		// Baby zombie
		else if (entityType.equals(EntityType.RABBIT))
		{
			for (int x = -5; x < 5; x++)
			{
				for (int y = 0; y < 255; y++)
				{
					for (int z = -5; z < 5; z++)
					{
						Location spawnLoc = location.clone().add(x, 0, z);
						spawnLoc.setY(y);
						Block block = spawnLoc.getBlock();
						if (block.getType().equals(Material.WATER) || block.getType().equals(Material.STATIONARY_WATER))
						{
							UtilPlayer.message(player, F.main("Pets", "You cannot spawn that pet there!"));
							return;
						}
					}
				}
			}
			pet = _creatureModule.SpawnEntity(location, petType.getEntityType());
		}
		//Default Spawn
		else
		{
		    pet = _creatureModule.SpawnEntity(location, petType.getEntityType());
		}
		
		//Named Pet
		if (Get(player).getPets().get(petType) != null && Get(player).getPets().get(petType).length() > 0)
		{
			pet.setCustomNameVisible(true);
			pet.setCustomName(Get(player).getPets().get(petType));
		}

		if (petType.equals(PetType.ZOMBIE))
		{
			Zombie zombie = (Zombie) pet;
			zombie.setBaby(true);
			zombie.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
			zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 0));
			UtilEnt.silence(pet, true);
		}
		if (pet instanceof PigZombie)
		{
			PigZombie pigZombie = (PigZombie) pet;
			pigZombie.setBaby(true);
			pigZombie.getEquipment().setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte) 1));
			pigZombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 0));
		}
		else if (pet instanceof Villager)
		{
			((Villager) pet).setBaby();
			((Villager) pet).setAgeLock(true);
		}
		else if (pet instanceof Skeleton)
		{
			Skeleton skeleton = (Skeleton) pet;
			skeleton.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));	//stop burning
			UtilEnt.silence(pet, true);
			
			DisguiseGuardian disguise = new DisguiseGuardian(skeleton);

			if (Get(player).getPets().get(petType) != null && Get(player).getPets().get(petType).length() > 0)
			{
				disguise.setName(Get(player).getPets().get(petType));
				disguise.setCustomNameVisible(true);
			}
			
			_disguiseManager.disguise(disguise);
		}
		else if (petType.equals(PetType.RABBIT))
		{
			UtilEnt.silence(pet, true);
			DisguiseChicken disguise = new DisguiseChicken(pet);
			_disguiseManager.disguise(disguise);

			Zombie zombie = pet.getWorld().spawn(pet.getLocation(), Zombie.class);
			zombie.setBaby(true);
			zombie.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
			if (Get(player).getPets().get(petType) != null && Get(player).getPets().get(petType).length() > 0)
			{
				zombie.setCustomName(Get(player).getPets().get(petType));
				zombie.setCustomNameVisible(true);
			}
			disguise.getEntity().getBukkitEntity().setPassenger(zombie);
		}
		else if (pet instanceof Blaze)
		{
			DisguiseZombie disguiseZombie = new DisguiseZombie(pet);
			disguiseZombie.setBaby(true);
			disguiseZombie.setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.WITHER.ordinal()));
			disguiseZombie.setChestplate(new ItemStack(Material.BANNER));
			disguiseZombie.setHeldItem(new ItemStack(Material.WOOD_HOE));

			if (Get(player).getPets().get(petType) != null && Get(player).getPets().get(petType).length() > 0)
			{
				disguiseZombie.setName(Get(player).getPets().get(petType));
				disguiseZombie.setCustomNameVisible(true);
			}

			_disguiseManager.disguise(disguiseZombie);
			FlyingPetManager flyingPetManager = new FlyingPetManager(player, pet);
			_flyingPets.put(pet, flyingPetManager);
			UtilEnt.silence(pet, true);
		}
		else if (petType.equals(PetType.GINGERBREAD_MAN))
		{
			Zombie zombie = (Zombie) pet;
			zombie.setBaby(true);
			zombie.getEquipment().setHelmet(SkinData.GINGERBREAD.getSkull());
			zombie.getEquipment().setChestplate(ItemStackFactory.Instance.createColoredLeatherArmor(1, org.bukkit.Color.fromRGB(203, 122, 56)));
			zombie.getEquipment().setLeggings(ItemStackFactory.Instance.createColoredLeatherArmor(2, org.bukkit.Color.fromRGB(203, 122, 56)));
			zombie.getEquipment().setBoots(ItemStackFactory.Instance.createColoredLeatherArmor(3, org.bukkit.Color.fromRGB(203, 122, 56)));

			UtilEnt.silence(zombie, true);

			if (Get(player).getPets().get(petType) != null && Get(player).getPets().get(petType).length() > 0)
			{
				zombie.setCustomName(Get(player).getPets().get(petType));
				zombie.setCustomNameVisible(true);
			}
		}
		else if (petType.equals(PetType.CUPID_PET))
		{
			Zombie zombie = (Zombie) pet;
			UtilEnt.silence(zombie, true);

			DisguiseVillager disguiseVillager = new DisguiseVillager(zombie);
			disguiseVillager.setBaby();
			disguiseVillager.setHeldItem(new ItemStack(Material.BOW));

			if (Get(player).getPets().get(petType) != null && Get(player).getPets().get(petType).length() > 0)
			{
				disguiseVillager.setName(Get(player).getPets().get(petType));
				disguiseVillager.setCustomNameVisible(true);
			}

			_disguiseManager.disguise(disguiseVillager);
			FlyingPetManager flyingPetManager = new FlyingPetManager(player, pet);
			_flyingPets.put(pet, flyingPetManager);
		}
		else if (petType.equals(PetType.TRUE_LOVE_PET))
		{
			Zombie zombie = (Zombie) pet;
			zombie.setBaby(true);

			UtilEnt.silence(zombie, true);

			if (Get(player).getPets().get(petType) != null && Get(player).getPets().get(petType).length() > 0)
			{
				zombie.setCustomName(Get(player).getPets().get(petType));
				zombie.setCustomNameVisible(true);
			}

			// Spawns villager
			Villager villager = _creatureModule.SpawnEntity(zombie.getLocation(), Villager.class);
			villager.setBaby();
			villager.setAgeLock(true);
			UtilEnt.silence(villager, true);
			_trueLovePets.put(zombie, new TrueLoveData(player, zombie, villager));
		}
		else if (petType.equals(PetType.LEPRECHAUN))
		{
			Zombie zombie = (Zombie) pet;
			zombie.setBaby(true);

			UtilEnt.silence(zombie, true);

			zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));

			zombie.getEquipment().setHelmet(SkinData.LEPRECHAUN.getSkull());
			zombie.getEquipment().setChestplate(ItemStackFactory.Instance.createColoredLeatherArmor(1, org.bukkit.Color.fromRGB(0, 153, 0)));
			zombie.getEquipment().setLeggings(ItemStackFactory.Instance.createColoredLeatherArmor(2, org.bukkit.Color.fromRGB(0, 153, 0)));
			zombie.getEquipment().setBoots(ItemStackFactory.Instance.createColoredLeatherArmor(3, org.bukkit.Color.fromRGB(0, 153, 0)));
		}
		else if (petType.equals(PetType.KILLER_BUNNY))
		{
			Rabbit rabbit = (Rabbit) pet;
			rabbit.setAdult();
			rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
		}

		_activePetOwnerTypes.put(player.getName(), petType);
		_activePetOwners.put(player.getName(), pet);
		_failedAttempts.put(player.getName(), 0);
		
		if (pet instanceof Ageable)
		{
			((Ageable)pet).setBaby();
			((Ageable)pet).setAgeLock(true);
		}
		
		UtilEnt.vegetate(pet);
	}
	
	public Entity getPet(Player player)
	{
		return _activePetOwners.get(player.getName());
	}
	
	public void removePet(final Player player, boolean removeOwner)
	{
		if (_activePetOwners.containsKey(player.getName()))
		{
			final Entity pet = _activePetOwners.get(player.getName());
			
			//Wither Silverfish
			if (pet.getPassenger() != null)
				pet.getPassenger().remove();

			if (pet instanceof Blaze)
			{
				_flyingPets.remove(pet);
			}

			if (_trueLovePets.containsKey(pet))
			{
				_trueLovePets.get(pet).remove();
				_trueLovePets.remove(pet);
			}

			pet.remove();

			if (removeOwner)
			{
				_activePetOwnerTypes.remove(player.getName());
				_activePetOwners.remove(player.getName());
			}
		}
	}

	@EventHandler
	public void preventWolfBone(PlayerInteractEntityEvent event)
	{
		if (event.getPlayer().getItemInHand().getType() == Material.BONE)
		{
			event.setCancelled(true);
			event.getPlayer().updateInventory();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		removePet(event.getPlayer(), true);
	}
	
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) 
    {
    	if (event.getEntity() instanceof Creature && _activePetOwners.containsValue((Creature)event.getEntity()))
    	{
    		event.setCancelled(true);
    	}
    }
	
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
    	if (event.getEntity() instanceof Creature && _activePetOwners.containsValue((Creature)event.getEntity()))
    	{
    		if (event.getCause() == DamageCause.VOID)
    		{
    			String playerName = null;
    			
    			for (Entry<String, Entity> entry : _activePetOwners.entrySet())
    			{
    				if (entry.getValue() == event.getEntity())
    					playerName = entry.getKey();
    			}
    			
    			if (playerName != null)
    			{
    				Player player = Bukkit.getPlayerExact(playerName);
    				
    				if (player != null && player.isOnline())
    				{
    					removePet(player, true);
    				}
    			}
    		}
    		event.setCancelled(true);
    	}
    }
    
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{

		for (Entry<String, Entity> entry : _activePetOwners.entrySet())
		{
			String playerName = entry.getKey();
			Entity entity = entry.getValue();

			if (event.getType() == UpdateType.TICK)
			{
				if (entity instanceof PigZombie)
				{
					UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, entity.getLocation(), 0.2f, 0.0f, 0.2f, 0.0f, 4, ViewDist.NORMAL);
					if(event.getTick() % 3 == 0) entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_BREATH, 0.03f, 0f);
					if(!((CraftPigZombie) entity).getHandle().isSilent())
					{
						((CraftPigZombie) entity).getHandle().setSilent(true);
					}
				}
			}
			else if (event.getType() == UpdateType.FAST)
			{
				if (entity instanceof Blaze)
				{
					Location loc = entity.getLocation().clone().add(0, .5, 0).add(entity.getLocation().getDirection().multiply(-0.2));
					_grimReaperWings.display(loc);
					_grimReaperWingsEdge.display(loc);
				}
				else
				{
					PetType petType = getActivePetType(playerName);
					if (petType == PetType.CUPID_PET)
					{
						Location loc = entity.getLocation().clone().add(0, .5, 0).add(entity.getLocation().getDirection().multiply(-0.2));

						_cupidWings.displayColored(loc, Color.PINK);
						_cupidWingsWhite.displayColored(loc, Color.WHITE);
						_cupidWingsEdge.displayColored(loc, Color.BLACK);
					}
				}
			}
			else if (event.getType() == UpdateType.SEC)
			{
				PetType petType = getActivePetType(playerName);
				if (petType == PetType.CUPID_PET)
				{
					Location loc = entity.getLocation().clone().add(0, .5, 0);
					UtilParticle.PlayParticle(ParticleType.HEART, loc, 0.25f, 0.25f, 0.25f, 0.25f, 3, ViewDist.NORMAL);
				}
			}
		}

		if (event.getType() != UpdateType.FAST)
			return;
		
		int xDiff;
		int yDiff;
		int zDiff;
		
		Iterator<String> ownerIterator = _activePetOwners.keySet().iterator(); 
		
		while (ownerIterator.hasNext())
		{
			String playerName = ownerIterator.next();
			Player owner = Bukkit.getPlayer(playerName);
			
			Entity pet = _activePetOwners.get(playerName);
			Location petSpot = pet.getLocation();
			Location ownerSpot = owner.getLocation();
			xDiff = Math.abs(petSpot.getBlockX() - ownerSpot.getBlockX());
			yDiff = Math.abs(petSpot.getBlockY() - ownerSpot.getBlockY());
			zDiff = Math.abs(petSpot.getBlockZ() - ownerSpot.getBlockZ());
			
			//Guardian
			if (pet instanceof Skeleton && Math.random() > 0.66 && UtilEnt.isGrounded(pet))
			{
				UtilAction.velocity(pet, UtilAlg.getTrajectory(pet, owner), Math.random() * 0.3 + 0.3, false, 0, 0.3, 1, true);
			}
		
			if ((xDiff + yDiff + zDiff) > 4)
			{
				EntityCreature ec = ((CraftCreature) pet).getHandle();
	            NavigationAbstract nav = ec.getNavigation();
	            
	            int xIndex = -1;
	            int zIndex = -1;
	            Block targetBlock = ownerSpot.getBlock().getRelative(xIndex, -1, zIndex);
	            while (targetBlock.isEmpty() || targetBlock.isLiquid())
	            {
	            	if (xIndex < 2)
	            		xIndex++;
	            	else if (zIndex < 2)
	            	{
	            		xIndex = -1;
	            		zIndex++;
	            	}
	            	else
	            		return;
	            	
	            	targetBlock = ownerSpot.getBlock().getRelative(xIndex, -1, zIndex);
	            }
	            
	            float speed = 0.9f;
	            if (pet instanceof Villager)
	            	speed = 0.6f;
	            
	            if (_failedAttempts.get(playerName) > 4)
	            {
	            	pet.teleport(owner);
	            	_failedAttempts.put(playerName, 0);
	            }
	            else if (!nav.a(targetBlock.getX(), targetBlock.getY() + 1, targetBlock.getZ(), speed))
	            {
	            	if (pet.getFallDistance() == 0)
	            	{
	            		_failedAttempts.put(playerName, _failedAttempts.get(playerName) + 1);
	            	}
	            }
	            else
	            {
	            	_failedAttempts.put(playerName, 0);
	            }
			}
		}
	}

	@EventHandler
	public void onClientWebResponse(ClientWebResponseEvent event)
	{		
		ClientPetTokenWrapper token = new Gson().fromJson(event.GetResponse(), ClientPetTokenWrapper.class);
	
		Get(event.getUniqueId()).load(token.DonorToken);
	}

	/**
	 * Makes the Flying pet fly around the player
	 * Copied from {@link mineplex.core.gadget.gadgets.particle.ParticleFairyData}
	 * @param event
	 */
	@EventHandler
	public void grimReaperFly(UpdateEvent event)
	{
		for (Entry<Entity, FlyingPetManager> entry : _flyingPets.entrySet())
		{
			FlyingPetManager flyingPetManager = entry.getValue();
			flyingPetManager.update();
		}
	}

	@EventHandler
	public void trueLovePetWalk(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		Iterator<Entry<Entity, TrueLoveData>> iterator = _trueLovePets.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<Entity, TrueLoveData> entry = iterator.next();
			Entity zombie = entry.getKey();
			UtilParticle.PlayParticle(ParticleType.HEART, zombie.getLocation().add(0, 0.25, 0), 0.25f, 0.25f, 0.25f, 0, 1, ViewDist.NORMAL);
			TrueLoveData trueLoveData = entry.getValue();
			trueLoveData.update();
		}
	}

	/**
	 * Blocks zombie pets catching fire
	 * @param event
	 */
	@EventHandler
	public void noFire(EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Zombie)
		{
			if (_activePetOwners.containsValue(event.getEntity()))
			{
				event.setCancelled(true);
			}
		}
	}

	@Override
	protected PetClient addPlayer(UUID uuid)
	{
		return new PetClient();
	}

	public PetRepository getRepository()
	{
		return _repository;
	}

	public boolean hasActivePet(String name)
	{
		return _activePetOwnerTypes.containsKey(name);
	}

	public PetType getActivePetType(String name)
	{
		return _activePetOwnerTypes.get(name);
	}

	public Entity getActivePet(String name)
	{
		return _activePetOwners.get(name);
	}
	
	public void disableAll()
	{
		for (Player player : UtilServer.getPlayers())
			removePet(player, true);
	}
	
	public void disableAll(Player player)
	{
		removePet(player, true);
	}

    public Collection<Entity> getPets()
    {
        return _activePetOwners.values();
    }

    public mineplex.core.creature.Creature getCreatureModule()
	{
		return _creatureModule;
	}

	public DisguiseManager getDisguiseManager()
	{
		return _disguiseManager;
	}

}
