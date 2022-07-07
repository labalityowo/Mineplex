package nautilus.game.arcade.game.games.typewars;

import java.util.ArrayList;
import java.util.Collections;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.hologram.Hologram;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.Game.GameState;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class Minion
{

	private static String[] NAMES = new String[]{"Fishing", "Cookie", "Sleeping", "Diamond", "Banana", "Tree", "Egg", "Cat",
		"Quadrilateral", "Rollercoaster", "Hallucinating", "Advertisement", "Entertainment", "Administrator", "Intergalactic", "International", "Understanding", "Investigation",
		"Veterinarian", "Photographer", "Cheeseburger", "Civilization", "Tranquilizer", "Conversation", "EnderDragon", "Engineering", "Philippines", "Countryside",
		"Electricity", "Caterpillar", "Keyboarding", "Agriculture", "Mathematics", "Millimeters", "Centimeters", "Screwdriver", "Achievement", "Necromancer",
		"Grasshopper", "Quadrillion", "Horseradish", "Aboveground", "Belowground", "Mississippi", "Computerize", "Hibernation", "Radioactive", "Unfortunate",
		"Demonstrate", "Gymnastics", "Toothpaste", "Paraphrase", "Limitless", "Breakfast", "Graveyard", "Philippines", "Countryside",
		"Competition", "Management", "Peppermint", "Pyromaniac", "Sandstone", "Vengeance", "Passwords", "Chew", "Philippines", "Countryside",
		"Competitive", "Accounting", "Generation", "Mechanized", "Minecraft", "Sprinting", "Beautiful", "Container", "Mayonaise", "Generator",
		"Bombardment", "Laboratory", "BlackBerry", "Calculator", "Mushrooms", "Heartbeat", "Authority", "Apartment", "Deception", "Recommend",
		"Highlighter", "Incomplete", "Javascript", "Compressor", "Dentistry", "Rectangle", "Exhausted", "Slimeball", "Commander", "Associate",
		"Complicated", "Government", "Ceptillion", "Deflection", "Cosmetics", "Trapezoid", "Hamburger", "Raspberry", "Developer", "Accompany",
		"Basketball", "Milkshakes", "Antibiotic", "Vocabulary", "Australia", "Dodecagon", "Miniature", "Blueberry", "Historian", "Machinery",
		"Volleyball", "Earthquake", "Girlfriend", "Definition", "Christmas", "Cardboard", "Dimension", "Overreact", "Character",
		"Television", "Motorcycle", "Despicable", "Contradict", "Chocolate", "Screaming", "Microsoft", "Barbarian", "Backspace", "Knowledge",
		"Microphone", "Buccaneers", "Affordable", "Attendance", "Halloween", "Demanding", "Wrestling", "Lightbulb", "Wisconsin", "Secondary",
		"Rhinoceros", "Applesauce", "Disconnect", "Protection", "Vacations", "Hopscotch", "Moderator", "Invisible", "Tennessee", "Adjective",
		"Chestpiece", "Headphones", "Watermelon", "Reasonless", "Traveling", "Spectator", "Paintball", "Carnivore", "Awareness", "Direction",
		"Complicated", "Controller", "Chimpanzee", "Deportment", "Saxophone", "Quadruple", "Champions", "Herbivore", "Unexcused", "Different",
		"Antarctica", "Paintbrush", "Newsletter", "Appearance", "Hurricane", "Autopilot", "Architect", "Automatic", "Diplomacy", "Construct",
		"Snowflakes", "Typewriter", "Sunglasses", "Occupation", "Piercings", "Principle", "Sharpness", "Performer", "Valentine", "Alternate",
		"Strawberry", "Smartwatch", "Horrendous", "Antarctica", "Necklaces", "September", "Trademark", "Miniscule", "Copyright", "Opposable",
		"Blackholes", "Minestrike", "California", "Wristwatch", "Evolution", "Microwave", "Dangerous", "Humongous", "Practical", "Imaginary",
		"Rocketship", "Deathmatch", "Transplant", "Confusion", "Spaceship", "Eyeshadow", "Afternoon", "Judgement", "Imperfect", "Bonemeal",
		"Aquamarine", "Playground", "Inevitable", "Surprised", "Lightning", "Butterfly", "Beekeeper", "Gladiator", "Excessive", "Courages",
		"Levitation", "Resistance", "Inflatable", "Newspaper", "Sketching", "Centipede", "Parachute", "Treachery", "Crocodile", "Baseball",
		"Vegetables", "Lighthouse", "Relentless", "Dinosaurs", "Teenagers", "Cartwheel", "Barricade", "Blowtorch", "Alligator", "Presents",
		"Whispering", "Helicopter", "Mistakable", "Tarantula", "Grassland", "President", "Raincloud", "Incentive", "Balloons",
		"Announcing", "Mechanical", "Expectance", "Mountains", "Fingertip", "Millenium", "Structure", "Keyboard",
		"Meditation", "Toothbrush", "Tumbleweed", "Sandstone", "Dumplings", "Scientist", "Pineapple", "Boyfriend", "Spotlight", "Computer",
		"Clothing", "Elephant", "Reptiles", "Scorpion", "Redstone", "Diamonds", "Porkchop", "Endermen", "Obsidian", "Planting",
		"Potatoes", "Vampires", "Bracelet", "Coloring", "Thousand", "Hologram", "Lipstick", "Cruising", "Delivery", "Dreaming",
		"Minecart", "Werewolf", "Highways", "Painting", "Infinity", "Ancestor", "Eyeliner", "Complete", "Packages", "Thinking",
		"Unicorns", "Pumpkins", "Internet", "Toddlers", "Swimming", "Wreckage", "Siblings", "Branches", "Criminal", "Engineer",
		"Military", "Costumes", "Earrings", "Children", "Triangle", "Defender", "Baguette", "Politics", "Handsome", "Reindeer",
		"Portland", "Chipotle", "Dolphins", "Pre-teen", "Pentagon", "Homework", "Princess", "Citizens", "Gorgeous", "Necklace",
		"Penguins", "Sapphire", "Galaxies", "Campfire", "Heptagon", "February", "Alphabet", "Username", "Panthers", "Mineplex",
		"Barbecue", "Amethyst", "Cartoons", "Tropical", "Lollipop", "November", "Scissors", "Medicine", "Warriors", "Pallette",
		"Mermaids", "Clarinet", "Basement", "Broccoli", "Shouting", "December", "Eternity", "Behavior", "Chatting", "Dominate",
		"Assassin", "Elevator", "Weakness", "Blizzard", "Entrance", "Universe", "Teleport", "Director", "Stuffing", "Eruption",
		"Godzilla", "Electron", "Strength", "Powerful", "Dynamite", "Backyard", "Gradient", "Producer", "Festival", "Mattress",
		"Empoleon", "Building", "Dinosaur", "Illusion", "Mustache", "Ceremony", "Shipment", "Cosmetic", "Applause", "Research",
		"Chimchar", "Aquarium", "Sidewalk", "Calendar", "Treasure", "Airplane", "Envelope", "Kangaroo", "Goldfish", "Starfish",
		"Nickname", "Slowness", "Official", "Accident", "Cinnamon", "Collapse", "Geometry", "Barnacle", "Football", "Creative",
		"Hypnotic", "Antidote", "Emulator", "Foothold", "Friction", "Tungsten", "Tablets", "Torches", "Fairies", "Windows",
		"Conquest", "Province", "Overflow", "Graceful", "Negative", "Doctrine", "Charger", "Carrots", "Spirits", "Robbers",
		"Karambit", "Solution", "Sandwich", "Catapult", "Positive", "Firework", "Ukulele", "Dragons", "Cobwebs", "Drawing",
		"Internal", "Japanese", "Atronomy", "Villager", "Tranquil", "Compress", "Glasses", "Nursing", "College", "Magenta",
		"Trillion", "Standard", "Astrology", "Infringe", "Fortress", "Prisoner", "Daisies", "Soldier", "Courses", "Serpent",
		"Carnival", "Parasite", "Porridge", "Variable", "Charcoal", "Decision", "Hazards", "Jupiter", "Buttons", "Camping",
		"Concrete", "Carriage", "Pressure", "Practice", "Commerce", "Windmill", "Cheetah", "Mercury", "Octopus", "Canyons",
		"Pavement", "Auxilary", "Demolish", "Maintain", "Barbeque", "Parmesan", "Vulture", "America", "Printer", "Seventy",
		"Joystick", "Marshall", "Franklin", "Umbrella", "Contract", "Warthog", "Turtles", "Ireland", "Titanic", "Hundred",
		"Speaker", "Suitcase", "Michigan", "Darkness", "Separate", "Puzzled", "Ocelots", "Germany", "Vanilla", "Million",
		"Figurine", "Mandarin", "Arkansas", "Ethernet", "Eligible", "Shocked", "Creeper", "Chillie", "Tornado", "Billion",
		"Boundary", "Anteater", "Colorado", "Everyday", "Fraction", "Figures", "Zombies", "Jamaica", "Seaweed", "Twitter",
		"Birthday", "Sunshine", "Virginia", "Surprise", "Compound", "Pillows", "Leather", "Bermuda", "Craters", "Waiting",
		"Hogwarts", "Particle", "American", "Together", "Precious", "Erasers", "Chicken", "Bahamas", "Meteors", "Passion",
		"Walking", "Decagon", "Spatula", "Science", "Bicycle", "Animate", "Cereal", "Graphic", "Message", "Episode",
		"Running", "Talking", "Cooking", "Biology", "Sweater", "Cabinet", "Pokemon", "Kingdom", "Funeral", "Destroy",
		"Jogging", "Yelling", "Fashion", "Pajamas", "Lettuce", "Furnace", "Chariot", "Package", "Grinder", "Defrost",
		"Breathe", "Ladybug", "Brother", "Reflect", "Cheddar", "Bridges", "Spawner", "Exhibit", "Nuclear", "Avocado",
		"Muscles", "Invader", "Grandpa", "Confirm", "Speaker", "Wizards", "Stacker", "Feather", "Channel", "Thunder",
		"Marbles", "Contest", "Grandma", "History", "Minigun", "Skywars", "Turtwig", "Morning", "Explode", "Factory",
		"Polygon", "Teacher", "Royalty", "Balcony", "Android", "Monster", "Emerald", "Primate", "Village", "Company",
		"Degrees", "Glacier", "Cricket", "Partner", "Medieval", "Gravity", "Surgeon", "Volcano", "Forward", "Console",
		"Hexagon", "Cyclops", "Kung-fu", "Bonjour", "Painter", "Snowman", "Caramel", "Lullaby", "Sparrow", "Blowgun",
		"Octagon", "January", "Century", "Bowling", "Plumber", "Explore", "Healing", "Circuit", "Vampire", "Distort",
		"Nonagon", "October", "Lockers", "Justice", "England", "Pancake", "Whisper", "Voltage", "Ceramic", "Avenger",
		"Bazooka", "Actress", "Highway", "Fighter", "Notepad", "Knuckle", "YouTube", "Fishing", "Florida", "Capsule",
		"Missile", "Haircut", "Apricot", "Deathly", "Cracker", "Western", "Colonel", "Balance", "Georgia", "Boolean",
		"Pyramid", "Stomach", "Dracula", "Fractal", "Network", "Eastern", "Creator", "Monitor", "Glowing", "Integer",
		"Mailbox", "Phantom", "Harpoon", "Endless", "Ketchup", "English", "Sunrise", "Examine", "Blowing", "Perfect",
		"Algebra", "Pattern", "Cottage", "Crystal", "Mustard", "Spanish", "Unlucky", "Tragedy", "Deviate", "Builder",
		"Penguin", "Emperor", "Amplify", "Hamster", "Paprika", "Chinese", "Shackle", "Kitchen", "Liberty", "Cupcake",
		"Robotic", "Fortune", "Gazelle", "Scratch", "Revenge", "Honesty", "Hideout", "Compass", "Italian", "Demoman",
		"Machine", "Gymnast", "Balloon", "Country", "Poision", "Brendan", "Connect", "Fireman", "Mexican", "Neptune",
		"Aquatic", "Hostage", "Program", "Witness", "Villain", "Virtual", "Supreme", "Platter", "Ukraine", "Profile",
		"Hatchet", "Hangers", "Bayonet", "Gamepad", "Bandage", "Blister", "Archive", "Implode", "Hilbert", "Offline",
		"Shelter", "Primary", "Organic", "Healthy", "Makeup", "Blazes", "Brazil", "Horror", "Subway", "Babies",
		"Capture", "Various", "Gradual", "Rapture", "Pollen", "String", "Warren", "Moving", "Shorts", "Elders",
		"Elegant", "Violate", "Heroic", "Violent", "Leaves", "Soccer", "Europe", "School", "Scarves", "Orange",
		"Dentist", "Neglect", "Strong", "Solvent", "Monkey", "Closet", "Africa", "Hotels", "Sharks", "Yellow",
		"Combine", "Fulfill", "Barbie", "Engrave", "Rabbit", "Carpet", "Winter", "Zipper", "Whales", "Purple",
		"Surface", "Sailing", "Pencil", "Passage", "Kitten", "Saturn", "Spring", "Acorns", "Comets",
		"Gelatin", "Klarin", "Phones", "Quality", "Ingots", "Uranus", "Summer", "Pariot", "Comedy", "Poison",
		"Similar", "Flutter", "Shield", "Psychic", "Spider", "Mexico", "Autumn", "Cruise", "Sports", "Forest",
		"Oxidize", "Disease", "Guitar", "Opossum", "Ghasts", "France", "Ghosts", "Lucius", "Cement", "Desert",
		"Purpose", "Symptom", "Sticks", "Measure", "Slimes", "Greece", "Spooky", "Coffee", "Aliens", "Cities",
		"Bikini", "Mortal", "Serena", "Future", "Bottle", "Helmet", "Crunch", "Afraid", "Threat", "Static",
		"Happy", "Knife", "Scary", "Lapis", "Skirt", "Waves", "Calem", "Clock", "Taste", "Lucas",
		"Anger", "Spork", "Make", "Candy", "Shirt", "Tides", "Ocean", "Crawl", "Smell", "React",
		"Dolls", "Roses", "Trips", "Flute", "Pants", "Brick", "Three", "Ethan", "Uncle", "Lunch",
		"Legos", "Tulip", "Beach", "Wipes", "Heels", "Straw", "Seven", "Hands", "Queen", "Books",
		"Couch", "Grass", "Clans", "Frame", "Nails", "Cream", "Eight", "Belly", "Crown", "Polls",
		"Vases", "Tiger", "Wagon", "Sleet", "Rings", "Attic", "Forty", "Chest", "Staff", "Hello",
		"Sword", "Panda", "Sleep", "Roads", "Money", "Green", "Fifty", "Brush", "Tools", "Howdy",
		"Banjo", "Sloth", "X-ray", "Truck", "Coral", "Speed", "Sixty", "Peace", "Music", "Court",
		"Drums", "Snake", "Socks", "Plane", "Reefs", "Hilda", "Brown", "Heart", "Lucia", "Raven",
		"Spoon", "Boots", "Pearl", "Train", "Horse", "Woods", "Silly", "Lotta", "Month", "Games",
		"Love", "Cats", "Lava", "Ship", "Moon", "Five", "Head", "July", "Mask", "Hola",
		"Rosa", "Wolf", "Soda", "Ruby", "News", "Nine", "Hair", "Feel", "Jazz", "Soft",
		"Toys", "Duck", "Mars", "Mint", "Ufos", "Grey", "Ears", "Hear", "Hour", "Hard",
		"Soap", "Ores", "Cuba", "Snow", "Cops", "Derp", "Eyes", "Oven", "Week", "Clay",
		"Wigs", "Gold", "Asia", "Rain", "Lime", "Time", "Star", "King", "Year", "Gold",
		"Fork", "Iron", "Elfs", "Suit", "Blue", "Tony", "Salt", "Ants", "Nate", "Mind",
		"Weed", "Pigs", "Bricks", "Blue", "Pink", "Hide", "Kris", "File", "Yard", "Comb",
		"Wood", "Lyra", "Frog", "Hats", "Heal", "Feet", "Yoga", "Edit", "Mile", "Paws",
		"Bird", "Wool", "Fish", "Eels", "Jump", "Arms", "Boom", "View", "Girl", "Tree",
		"Lion", "Dirt", "Yarn", "Dawn", "Four", "Neck", "June", "Help", "Mail", "Lamp",
		"Sad", "Sun", "Pan", "Yes", "Dad", "Bat", "Wig", "KFC", "War", "Fan",
		"Red", "Jam", "Ivy", "Map", "Fur", "Yen", "Hum", "May", "Dog",
		"One", "Day", "Sky", "Add", "Orb", "Hip", "Sew", "Act", "Ice", 
		"Two", "Gum", "Cow", "Moo", "Bee", "Ape", "Zoo", "Pit", "Hat",
		"Six", "Gym", "Rat", "Mow", "Pot", "Dot", "Paw", "Hen", "Bed",
		"Ten", "Art", "Bag", "Mob", "End", "Egg", "Saw", "Law", "Fog",
		"Fly", "Boy", "Rag", "New", "Jet", "Pet", "Tin", "Pen", "Car",
		"Old", "Age", "TNT", "Leg", "Axe", "UFO", "Rap", "Wet", "Tie",
		"May", "Gas", "Hue", "Wax", "Toy", "Lay", "Pop", "Dry", "Sea",
		"See", "Ash", "Mom", "Box", "Key", "Fat", "Spy"};
	
	private ArcadeManager _manager;
	
	private MinionType _type;
	private Entity _entity;
	private String _name;
	private Location _location;
	private Location _target;
	private GameTeam _team;
	private Player _player;
	private Player _killer;
	private Location _lastNameChanged;
	private Hologram _hologram;
	private int _money;
	private float _walkSpeed;
	private boolean _spawned;
	private double _tagHight;
	private boolean _moving;
	
	private int _size;
	
	private int _spawnID;
	
	private boolean _die;
	private boolean _killed;
	private int _frame;
	private int _lives;
	
	public Minion(ArcadeManager manager, Location location, Location target, GameTeam team, int spawnID)
	{
		this(manager, location, target, team, null, true, null, spawnID);
	}
	
	public Minion(ArcadeManager manager, Location location, Location target, GameTeam team, Player player, boolean spawn, MinionType type, int spawnID)
	{
		_manager = manager;
		
		_type = type;
		_moving = true;
		if(_type == null)
			_type = randomType();
		
		_walkSpeed = _type.getWalkSpeed();
		_tagHight = _type.getTagHight();
		_die = false;
		_killed = false;
		_frame = 0;
		_entity = null;
		_team = team;
		_player = player;
		_killer = null;
		_money = _type.getMoney();
		_spawnID = spawnID;
		_size = 10;
		if(_type == MinionType.WITHER)
			_size = 0;
			
		changeRandomName(_type.getMinName(), _type.getMaxName(), true);
		
		_location = location;
		_target = target;
		_spawned = false;
		_lives = _type.getSize().getLives();
		if(spawn)
			spawnMinion();
	}
	
	public void spawnMinion()
	{	
		_entity = _location.getWorld().spawn(_location, Zombie.class);
		Zombie zombie = (Zombie) _entity;
		zombie.setRemoveWhenFarAway(false);
		zombie.setMaxHealth(200);
		zombie.setHealth(200);
		zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
		disguiseCreeper();
		path();
		_spawned = true;
	}
	
	private Material[] _items = new Material[]{Material.DIAMOND_AXE, Material.IRON_SWORD, Material.CARROT, Material.STONE_SPADE, Material.GOLD_PICKAXE, Material.AIR};
	
	private void disguiseCreeper()
	{
		if (_hologram != null)
		{
			_hologram.setText(_team.GetColor() + _name);
		}
		else
		{
			if(_manager.GetGame().GetState() == GameState.Live)
			{
				_hologram = new Hologram(_manager.getHologramManager(), _entity.getLocation().add(0, 2.3, 0), _team.GetColor() + _name);
			}
			else
			{
				_hologram = new Hologram(_manager.getHologramManager(), _entity.getLocation().add(0, 2.3, 0), ChatColor.WHITE + _name);
			}
			_hologram.setHologramTarget(Hologram.HologramTarget.WHITELIST);
			//_hologram.setFollowEntity(_entity);

			for (Player player : _manager.GetGame().GetPlayers(false))
			{
				if (_manager.GetGame().GetTeam(player) == _team && _manager.GetGame().GetState() != GameState.Prepare)
				{
					continue;
				}

				_hologram.addPlayer(player);
			}

			_hologram.start();
		}

		try
		{
			int i = 0;
			for(Class clazz : _type.getDisguiseClasses())
			{
				Object disguise = null;
				Entity ent = null;		
				if(i == 0)
				{
					disguise = clazz.getConstructors()[0].newInstance(_entity);
				}
				else
				{
					ent = _location.getWorld().spawn(_location, Creeper.class);
					disguise = clazz.getConstructors()[0].newInstance(ent);
				}
				try
				{
					clazz.getMethod("setHelmet", ItemStack.class).invoke(disguise, new ItemStack(Material.LEATHER_HELMET));
					clazz.getMethod("setHeldItem", ItemStack.class).invoke(disguise, new ItemStack(_items[UtilMath.r(_items.length)]));
				}
				catch (Exception e) {}
				if(disguise instanceof DisguiseZombie && i > 0)
				{
					DisguiseZombie zombie = (DisguiseZombie) disguise;
					zombie.setBaby(true);
				}
				if(disguise instanceof DisguiseSlime)
				{
					DisguiseSlime slime = (DisguiseSlime) disguise;
					slime.SetSize(_size);
				}
				if(disguise instanceof DisguiseWither)
				{
					DisguiseWither wither = (DisguiseWither) disguise;
					wither.setInvulTime(_size);
				}
				_entity.setPassenger(ent);
				_manager.GetDisguise().disguise((DisguiseBase)disguise);
				_manager.GetDisguise().updateDisguise((DisguiseBase)disguise);
				i++;
			}
		}
		catch (Exception e) {}	
		_hologram.start();
	}
	
	private void path()
	{
		UtilEnt.vegetate(_entity);
		UtilEnt.silence(_entity, true);
		UtilEnt.ghost(_entity, true, false);
	}
	
	private MinionType randomType()
	{
		if(System.currentTimeMillis() - _manager.GetGame().GetStateTime() <= 30000)
		{
			return MinionSize.EASY.getRandomType();
		}
		if(System.currentTimeMillis() - _manager.GetGame().GetStateTime() <= 60000)
		{
			int rdm = UtilMath.r(2);
			if(rdm == 0)
				return MinionSize.MEDIUM.getRandomType();
			else
				return MinionSize.EASY.getRandomType();
		}
		int rdm = UtilMath.r(MinionType.values().length);
		int freak = UtilMath.r(1000);
		if(freak <= 10)
		{
			ArrayList<MinionType> minions = new ArrayList<>();
			for(MinionType type : MinionType.values())
			{
				if(type.getSize() == MinionSize.FREAK)
					minions.add(type);
			}
			return minions.get(UtilMath.r(minions.size()));
		}
		for(MinionType type : MinionType.values())
		{
			if(type.ordinal() == rdm)
			{
				if(type.getSize() != MinionSize.FREAK && type.getSize() != MinionSize.BOSS)
				{
					return type;
				}
				else
				{
					return randomType();
				}
			}
		}
		return null;
	}
	
	public boolean hasLives()
	{
		return _lives > 1;
	}
	
	public void despawn(Player player, boolean killed, boolean clean)
	{
		_money = _money + 1;
		if(_lives > 1)
		{
			_lives = _lives - 1;
			changeRandomName(_name.length()+1, _name.length()+1, false);
			if(_type == MinionType.WITHER)
			{
				_size = _size + 100;
				_tagHight = _tagHight - 0.15;
			}
			else 
			{
				_size = _size -1;
				_tagHight = _tagHight - 0.1;
			}
			return;
		}
		_killed = killed;
		_killer = player;
		_die = true;
		_hologram.stop();
		try
		{
			if(_entity.getPassenger() != null)
			{
				if(!clean)
					((Zombie) _entity.getPassenger()).damage(10000);
				else
					_entity.getPassenger().remove();
			}
			if(!clean)
				((Zombie) _entity).damage(10000);
			else
				_entity.remove();
			
			if(!_entity.isDead())
				_entity.remove();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void despawn(Player player, boolean killed)
	{
		despawn(player, killed, false);
	}
	
	public void animation()
	{
		if(!_die)
			return;
		
		if(_killed)
		{
			if(_frame <= 30)
			{
				if(_team.GetColor() == ChatColor.RED)
				{
					double radius = _frame / 20D;
					int particleAmount = _frame / 2;
					for (int e = 0; e < particleAmount; e++)
					{
						double xDiff = Math.sin(e/(double)particleAmount * 2 * Math.PI) * radius;
						double zDiff = Math.cos(e/(double)particleAmount * 2 * Math.PI) * radius;

						Location location = _entity.getLocation().clone().add(0.5, 0, 0.5).add(xDiff, particleAmount/10, zDiff);
						try
						{
							UtilParticle.PlayParticle(UtilParticle.ParticleType.RED_DUST, location, 0, 0, 0, 0, 1, ViewDist.NORMAL, _player);
						}
						catch (Exception ex)
						{
							
						}

					}
				}
				else
				{
					double radius = _frame / 20D;
					int particleAmount = _frame / 2;
					for (int e = 0; e < particleAmount; e++)
					{
						double xDiff = Math.sin(e/(double)particleAmount * 2 * Math.PI) * radius;
						double zDiff = Math.cos(e/(double)particleAmount * 2 * Math.PI) * radius;

						Location location = _entity.getLocation().clone().add(0.5, 0, 0.5).add(xDiff, particleAmount/10, zDiff);
						try
						{
							UtilParticle.PlayParticle(ParticleType.RED_DUST, location, -1, 1, 1, 1, 0,ViewDist.NORMAL, _player);
						}
						catch (Exception ex)
						{
							
						}

					}
			
				}
			}
		}
		else
		{
			if(_frame <= 1)
			{
				UtilFirework.playFirework(_entity.getLocation().add(0.5, 0.5, 0.5), Type.BALL_LARGE, Color.GREEN, true, true);
			}
		}
		
		if(_frame == 31)
		{
			_die = false;
		}
		
		_frame++;
	}
	
	public void changeName(String name)
	{
		_name = name;
		Location loc = _entity.getLocation();
		_lastNameChanged = loc;
		disguiseCreeper();
	}
	
	public void changeRandomName(int min, int max, boolean spawned)
	{
		ArrayList<String> tempList = new ArrayList<>();
		for(String names : NAMES)
			tempList.add(names);
		
		Collections.shuffle(tempList);
		for(String str : tempList)
		{
			if(str.length() >= min && str.length() <= max)
			{
				if(!spawned)
					changeName(str);
				else
					_name = str;
				
				return;
			}
		}
	}
	
	public boolean isNameChangeable()
	{
		if(_lastNameChanged == null)
			return true;
		
		if(_entity.getLocation().getBlockX() != _lastNameChanged.getBlockX())
			return true;
		
		if(_entity.getLocation().getBlockZ() != _lastNameChanged.getBlockZ())
			return true;
		
		return false;
	}
	
	public void setWalkSpeed(float speed)
	{
		_walkSpeed = speed;
	}
	
	public void increaseWalkSpeed(float speed)
	{
		float oldSpeed = _walkSpeed;
		_walkSpeed = oldSpeed + speed;
		if(_walkSpeed <= 0.5)
			_walkSpeed = 0.6F;
	}
	
	public Location getTarget()
	{
		return _target;
	}

	public boolean isSpawned()
	{
		return _spawned;
	}
	
	public Entity getEntity()
	{
		return _entity;
	}
	
	public MinionType getType()
	{
		return _type;
	}
	
	public int getMoney()
	{
		return _money;
	}
	
	public Player getKiller()
	{
		return _killer;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public GameTeam getTeam()
	{
		return _team;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public float getWalkSpeed()
	{
		return _walkSpeed;
	}
	
	public Location getLastNameChanged()
	{
		return _lastNameChanged;
	}
	
	public Hologram getHologram()
	{
		return _hologram;
	}
	
	public int getSpawnID()
	{
		return _spawnID;
	}
	
	public double getTagHight()
	{
		return _tagHight;
	}

	public void setTarget(Location location)
	{
		_target = location;
	}
	
	public void setMoving(boolean moving)
	{
		_moving = moving;
	}
	
	public boolean isMoving()
	{
		return _moving;
	}
	
}
