package nautilus.game.arcade.game.games.draw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilBlockText;
import mineplex.core.common.util.UtilBlockText.TextAlign;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.GameScore;
import nautilus.game.arcade.game.games.draw.kits.KitArtist;
import nautilus.game.arcade.game.games.draw.tools.Tool;
import nautilus.game.arcade.game.games.draw.tools.ToolCircle;
import nautilus.game.arcade.game.games.draw.tools.ToolLine;
import nautilus.game.arcade.game.games.draw.tools.ToolSquare;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.DrawGuessStatTracker;
import nautilus.game.arcade.stats.KeenEyeStatTracker;
import nautilus.game.arcade.stats.MrSquiggleStatTracker;
import nautilus.game.arcade.stats.PureLuckStatTracker;

public class Draw extends SoloGame
{
	private ArrayList<GameScore> _ranks = new ArrayList<GameScore>();

	private GameTeam _drawers = null;
	private GameTeam _guessers = null;

	//Brush
	private byte _brushColor = 15;
	private Material _brushMaterial = Material.WOOL;
	private Location _brushPrevious = null;
	
	private boolean _lockDrawer = true;

	//Round
	private int _roundCount = 0;
	private int _roundMax = 0;
	private ArrayList<Player> _roundPlayer = new ArrayList<Player>();
	private long _roundTime = 0;
	private DrawRound _round = null;

	private ArrayList<Block> _canvas = new ArrayList<Block>();
	private Location _drawerLocation = null;
	private Location _textLocation = null;
	
	private Collection<Block> _textBlocks = null;

	private HashSet<Tool> _tools;
	private String[] _words;
	private String[] _christmasWords;
	private String[] _halloweenWords;
	private boolean _useChristmasWords = false;
	private boolean _useHalloweenWords = false;
	private HashSet<String> _usedWords = new HashSet<String>();

	public Draw(ArcadeManager manager) 
	{
		super(manager, GameType.Draw,

				new Kit[]
						{
				//new KitSlowAndSteady(manager),
				//new KitSelector(manager),
				//new KitTools(manager),
				new KitArtist(manager)
						},

						new String[]
								{
				"Take turns to draw something",
				"Right-Click with items to draw",
				"Hints are given at the bottom of screen",
								});
		
		AnticheatDisabled = true;
		StrictAntiHack = true;
		Damage = false;
		HungerSet = 20;
		WorldTimeSet = 8000;
		AllowParticles = false;
		
		_words = new String[]
				{
						"Bird", "Volcano", "Sloth", "Love", "Dance", "Hair", "Glasses", "Domino", "Dice", "Computer", "Top Hat",
						"Beard", "Wind", "Rain", "Minecraft", "Push", "Fighting", "Juggle", "Clown", "Miner", "Creeper",
						"Ghast", "Spider", "Punch", "Roll", "River", "Desert", "Cold", "Photo", "Quick", "Mario",
						"Luigi", "Bridge", "Turtle", "Door Knob", "Mineplex", "Binoculars", "Telescope", "Planet",
						"Mountain Bike", "Moon", "Comet", "Flower", "Squirrel", "Horse Riding", "Chef", "Elephant", "Yoshi",
						"Shotgun", "Pistol", "James Bond", "Money", "Salt and Pepper", "Truck", "Helicopter", "Hot Air Balloon",
						"Sprout", "Yelling", "Muscles", "Skinny", "Zombie", "Lava", "Snake", "Motorbike", "Whale", "Boat",
						"Letterbox", "Window", "Lollipop", "Handcuffs", "Police", "Uppercut", "Windmill", "Eyepatch", "Campfire",
						"Rainbow", "Storm", "Pikachu",  "Charmander", "Tornado", "Crying", "King", "Hobo", "Worm", "Snail",
						"XBox", "Playstation", "Nintendo", "Duck", "Pull", "Dinosaur", "Alligator", "Ankle", "Angel", "Acorn",
						"Bread", "Booty", "Bacon", "Crown", "Donut", "Drill", "Leash", "Magic", "Wizard", "Igloo", "Plant",
						"Screw", "Rifle", "Puppy", "Stool", "Stamp", "Letter", "Witch", "Zebra", "Wagon", "Compass", "Watch",
						"Clock", "Time", "Cyclops", "Coconut", "Hang",  "Penguin", "Confused", "Bucket", "Lion", "Rubbish",
						"Spaceship", "Bowl", "Shark", "Pizza", "Pyramid", "Dress", "Pants", "Shorts", "Boots", "Boy",  "Girl",
						"Math", "Sunglasses", "Frog", "Chair", "Cake", "Grapes", "Kiss", "Snorlax", "Earth", "Spaghetti",
						"Couch", "Family", "Milk", "Pig", "Giraffe", "Mouse", "Couch", "Fat", "Chocolate", "Camel",
						"Cheese", "Beans", "Water", "Chicken", "Zipper",  "Book", "Swimming", "Horse", "Paper", "Toaster",
						"Television", "Hammer", "Piano", "Sleeping", "Yawn", "Sheep", "Night", "Chest", "Lamp", "Redstone",
						"Grass", "Plane", "Ocean", "Lake", "Melon", "Pumpkin", "Gift", "Fishing", "Pirate",
						"Lightning", "Stomach", "Belly Button", "Fishing Rod",  "Iron Ore", "Diamonds", "Emeralds",
						"Nether Portal", "Ender Dragon", "Rabbit", "Harry Potter", "Torch", "Light", "Battery", "Zombie Pigman",
						"Telephone", "Tent", "Hand", "Traffic Lights", "Anvil", "Tail", "Umbrella", "Piston", "Skeleton",
						"Spikes", "Bridge", "Bomb", "Spoon", "Rainbow", "Staircase", "Poop", "Dragon", "Fire", "Apple", "Shoe",
						"Squid", "Cookie", "Tooth", "Camera", "Sock", "Monkey",  "Unicorn", "Smile", "Pool", "Rabbit",
						"Cupcake", "Pancake", "Princess", "Castle", "Flag", "Planet", "Stars", "Camp Fire", "Rose",  "Spray",
						"Pencil", "Ice Cream", "Toilet", "Moose", "Bear", "Batman", "Eggs", "Teapot",  "Golf Club",
						"Tennis Racket", "Shield", "Crab", "Pot of Gold", "Cactus", "Television", "Pumpkin Pie", "Chimney",
						"Stable", "Nether", "Wither",  "Beach", "Stop Sign", "Chestplate", "Pokeball", "Christmas Tree",
						"Present", "Snowflake", "Laptop", "Superman", "Football", "Basketball", "Creeper",  "Tetris", "Jump",
						"Ninja", "Baby", "Troll Face", "Grim Reaper", "Temple", "Explosion", "Vomit", "Ants", "Barn", "Burn",
						"Baggage", "Frisbee", "Iceberg", "Sleeping", "Dream", "Snorlax", "Balloons", "Elevator", "Alligator",
						"Bikini", "Butterfly", "Bumblebee", "Pizza", "Jellyfish", "Sideburns", "Speedboat", "Treehouse",
						"Water Gun", "Drink", "Hook", "Dance", "Fall", "Summer", "Autumn", "Spring", "Winter", "Night Time",
						"Galaxy", "Sunrise", "Sunset", "Picnic", "Snowflake", "Holding Hands", "America", "Laptop", "Anvil",
						"Bagel", "Bench", "Darts", "Muffin", "Queen", "Wheat", "Dolphin", "Scarf", "Swing", "Thumb",
						"Tomato", "Armor", "Alien", "Beans", "Cheek", "Phone", "Keyboard", "Orange", "Calculator",
						"Paper", "Desk", "Disco", "Elbow", "Drool", "Giant", "Golem", "Grave", "Llama", "Moose", "Party",
						"Panda", "Plumber", "Salsa", "Salad", "Skunk", "Skull", "Stump", "Sugar", "Ruler", "Bookcase",
						"Hamster", "Soup", "Teapot", "Towel", "Waist", "Archer", "Anchor", "Bamboo", "Branch", "Booger",
						"Carrot", "Cereal", "Coffee", "Wolf", "Crayon", "Finger", "Forest", "Hotdog", "Burger", "Obsidian",
						"Pillow", "Swing", "YouTube", "Farm", "Rain", "Cloud", "Frozen", "Garbage", "Music", "Twitter",
						"Facebook", "Santa Hat", "Rope", "Neck", "Sponge", "Sushi", "Noodles", "Soup", "Tower", "Berry",
						"Capture", "Prison", "Robot", "Trash", "School", "Skype", "Snowman", "Crowd", "Bank", "Mudkip",
						"Joker", "Lizard", "Tiger", "Royal", "Erupt", "Wizard", "Stain", "Cinema", "Notebook", "Blanket",
						"Paint", "Guard", "Astronaut" , "Slime" , "Mansion" , "Radar" , "Thorn" , "Tears" , "Tiny" , "Candy" ,
						"Pepsi" , "Flint" , "Draw My Thing" , "Rice" , "Shout" , "Prize" , "Skirt" , "Thief" , "Syrup" ,
						"Kirby" , "Brush" , "Violin", "Car", "Sun", "Eye", "Bow", "Axe", "Face", "Mushroom", "Guitar",
						"Pickle", "Banana", "Crab", "Sugar", "Soda", "Cookie", "Burger", "Fries", "Speaker",
						"Pillow", "Rug", "Purse", "Monitor", "Bow", "Pen", "Cat", "Kitten", "Puppy", "Bed", "Button",
						"Computer", "Key", "Spoon", "Lamp", "Bottle", "Card", "Newspaper", "Glasses", "Mountain", "Minecraft",
						"Shirt", "Truck", "Car", "Phone", "Cork", "iPod", "Paper", "Bag", "USB", "CD", "Wallet", "Cow", "Pig",
						"Sheep", "Tomato", "Painting", "Chair", "Keyboard", "Chocolate", "Duck", "Clock", "Balloon", "Remote",
						"Bread", "Ring", "Necklace", "Hippo", "Flag", "Window", "Door", "Radio", "Television", "Boat",
						"Fridge", "House", "Piano", "Guitar", "Trumpet", "Drums", "Speaker", "Helmet", "Tree", "Slippers",
						"Table", "Doll", "Headphones", "Box", "Flower", "Book", "Carrot", "Egg", "Sun", "Hill", "Candle",
						"Food", "Mouse", "Money", "Emerald", "Magnet", "Camera", "Movie", "Video Game", "Teddy", "Lake",
						"Violin", "Cheese", "Burger", "Peasant", "King", "Queen", "Prince", "Princess", "Mother", "Father", "Taco",
						"Racecar", "Car", "Truck", "Tree", "Elephant", "Lion", "Pig", "Cow", "Chicken", "Dog", "Cat", "Moon", "Stars",
						"Sun", "Diamond", "Gold", "Redstone", "Skateboard", "Bike", "Swimming Pool", "Cookie", "Computer", "Laptop",
						"Piano", "Guitar", "Trumpet", "Drums", "Flute", "Helicopter", "Plane", "Football", "Tennis", "Hockey",
						"Water", "Ocean", "Microsoft", "Twitter", "Godzilla", "Building", "House", "Rainbow", "Barbie", "Girl", "Boy",
						"Children", "Bomb", "Explosion", "Gun", "Tank", "Penguin", "Eagle", "America", "Kangaroo", "Sea", "Raspberry",
						"Strawberry", "Jam", "Sandwich", "Owl", "Watermelon", "Australia", "Canada", "United States", "Diary",
						"Airplane", "Alarm clock", "Alien", "Alligator", "Ant", "Apple", "Arm", "Autumn", "Baby", "Ball",
						"Balloon", "Banana", "Barn", "Base", "Baseball", "Basketball", "Bat", "Bathroom", "Battery", "Beach",
						"Bear", "Beaver", "Bed", "Beehive", "Bell", "Bicycle", "Bike", "Bird", "Birthday cake", "Blocks",
						"Boat", "Bone", "Book", "Boot", "Bottle", "Bowtie", "Boy", "Bracelet", "Brain", "Branch",
						"Bread", "Bridge", "Bubble", "Bug", "Bunny", "Bus", "Cage", "Cake", "Camera", "Cape",
						"Carrot", "Castle", "Cat", "Cave", "Chair", "Chalk", "Cheek", "Cheese", "Cheeseburger", "Cherry",
						"Chess", "Chicken", "Chin", "Christmas", "Circle", "Circus", "Clock", "Cloud", "Coal", "Coat",
						"Coconut", "Computer", "Cone", "Cookie", "Corn", "Cow", "Crab", "Crib", "Cup", "Cupcake",
						"Desert", "Desk", "Dinosaur", "Dog", "Doll", "Dominoes", "Door", "Doormat", "Drum", "Duck",
						"Ear", "Ears", "Egg", "Electricity", "Elephant", "Eraser", "Eyes", "Face", "Farm", "Fishing pole",
						"Fist", "Flamingo", "Flashlight", "Flower", "Flute", "Fly", "Football", "Forest", "Fountain", "Frenchfries",
						"Frog", "Garbage", "Garden", "Gate", "Ghost", "Gingerbread man", "Giraffe", "Girl", "Glasses", "Grapes",
						"Grass", "Graveyard", "Hair dryer", "Halloween", "Hat", "Head", "Heart", "Hippo", "Hockey", "Hook",
						"Hopscotch", "Horse", "Hospital", "House", "Hula hoop", "Ice", "Icecream", "Jacket", "Jar", "Jellyfish",
						"Jungle", "Kangaroo", "Key", "Kitchen", "Kite", "Knot", "Lamp", "Lawnmower", "Leaf", "Light",
						"Lightbulb", "Lighthouse", "Lightsaber", "Lips", "Lipstick", "Lobster", "Lollipop", "Mail", "Mailman", "Mattress",
						"Milk", "Money", "Monkey", "Moon", "Mosquito", "Mouse", "Mouth", "Muffin", "Mushroom", "Music",
						"Nail", "Newspaper", "Nightmare", "Nose", "Ocean", "Orange", "Owl", "Pajamas", "Palace", "Park",
						"Party", "Peach", "Peanut", "Pen", "Pencil", "Penguin", "Person", "Photograph", "Piano", "Pie",
						"Pig", "Pillow", "Pineapple", "Ping pong", "Pinwheel", "Pirate", "Pizza", "Plate", "Pool Party", "Popcorn",
						"Popsicle", "Potato", "Pretzel", "Prison", "Puppet", "Purse", "Queen", "Rain", "Rainbow", "Restaurant",
						"Rhinoceros", "Ring", "River", "Road", "Robot", "Rocket", "Rocking chair", "Roof", "Round", "Rug",
						"Ruins", "Saddle", "Sailboat", "Salt and pepper", "Scale", "School", "Scissors", "Seahorse", "Seashell", "Seesaw",
						"Shark", "Sheep", "Shirt", "Shoe", "Shopping cart", "Shovel", "Skate", "Skateboard", "Ski", "Skirt",
						"Slide", "Smile", "Snail", "Snake", "Snowball", "Snowflake", "Snowman", "Soap", "Socks", "Soda",
						"Song", "Spaceship", "Spider", "Spider web", "Spoon", "Spring", "Stage", "Stairs", "Star", "State",
						"Statue", "Stingray", "Stoplight", "Storm", "Suitcase", "Summer", "Sun", "Sunflower", "Swimming pool", "Swing",
						"Swordfish", "Tail", "Taxi", "Teapot", "Telephone", "Thief", "Toast", "Toothbrush", "Torch", "Treasure",
						"Tree", "Truck", "Trumpet", "Turtle", "TV", "Vest", "Violin", "Volcano", "Washing machine", "Water",
						"Waterfall", "Watering can", "Whale", "Whisk", "Whistle", "Windmill", "Winter", "Worm", "Wrench", "Yo-yo",
						"Zoo"
				};

		_christmasWords = new String[]
				{
						"Santa", "Reindeer", "Ornament", "Elf", "North Pole", "Candy Cane", "Christmas Tree",
						"Fireplace", "Hot Chocolate", "Snowflake", "Snowman", "Sleigh", "Toys", "Milk", "Eggnog", "Coal",
						"Cookies", "Mistletoe", "Icicle", "Gingerbread", "Stocking", "Jingle Bells", "Family", "Mittens",
						"Snowball Fight", "Decorations", "Snow Fort", "Chimney", "Scrooge", "Sweater", "Ice Skating",
						"Pinecone", "Cabin", "Bells", "Cold", "Nutcracker", "Sled", "Grinch", "Igloo",
						"Boots", "Gingerbread Man", "Glacier", "Ice Hockey", "Scarf", "Snowboard"
				};

		_halloweenWords = new String[]
				{
						"Bat", "Cauldron", "Broomstick", "Witch", "Witch Hat", "Haunted House", "Ghost", "Spider",
						"Werewolf", "Full Moon", "Vampire", "Dracula", "Zombie", "Grim Reaper", "Graveyard",
						"Gravestone", "Pumpkin", "Pumpkin Patch", "Jack-O-Lantern", "Scarecrow", "Haunted",
						"Monster", "Halloween", "Skeleton", "Skull", "Coffin", "Tomb", "Cobweb", "Spider Web",
						"Costume", "Frankenstein", "Black Cat", "Bone", "Candy", "Trick or Treat", "Eyeball", "Fangs",
						"Goblin", "Potion", "Treat", "Trick"
				};

		_tools = new HashSet<>();
		_tools.add(new ToolLine(this));
		_tools.add(new ToolSquare(this));
		_tools.add(new ToolCircle(this));
	
		registerStatTrackers(
				new MrSquiggleStatTracker(this),
				new KeenEyeStatTracker(this),
				new PureLuckStatTracker(this),
				new DrawGuessStatTracker(this)
		);

		registerChatStats(
				new ChatStatData("TotalGuess", "Total Guesses", true),
				new ChatStatData("PureLuck", "Lucky Guesses", true)
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	@Override
	public void ParseData()
	{
		int count = 0;
		for (Block b : UtilBlock.getInBoundingBox(WorldData.GetDataLocs("PINK").get(0), WorldData.GetDataLocs("PINK").get(1), false))
		{
			if (b.getType() != Material.AIR)
			{
				_canvas.add(b);
				count++;
			}
		}

		System.out.println("===");
		System.out.println("Draw loc: " + WorldData.GetDataLocs("RED").size());
		System.out.println("Canvas Count: " + count);
		System.out.println("===");

		_drawerLocation = WorldData.GetDataLocs("RED").get(0);
		_textLocation = WorldData.GetDataLocs("YELLOW").get(0);
	}

	@EventHandler
	public void clearBoardStart(GameStateChangeEvent e)
	{
		if (e.GetState() != GameState.Live)
			return;

		Reset();

		for (Player player : GetPlayers(true))
			player.setGameMode(GameMode.ADVENTURE);
	}

	@EventHandler
	public void playerFallCloudy(PlayerMoveEvent e)
	{
		if (!GetPlayers(true).contains(e.getPlayer()))
			return;

		if (!WorldData.MapName.equalsIgnoreCase("Cloudy"))
			return;

		if (e.getTo().getBlockY() <= 130)
		{
			GetTeam(e.getPlayer()).SpawnTeleport(e.getPlayer());
		}
	}

	@Override
	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Recruit)
			return;

		_guessers = this.GetTeamList().get(0);
		_guessers.SetName("Guessers");
		_guessers.SetColor(ChatColor.GREEN);
	}
	
	@EventHandler
	public void CreateDrawers(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Prepare)
			return;
		
		//Undead Team
		_drawers = new GameTeam(this, "Drawer", ChatColor.RED, WorldData.GetDataLocs("RED"));	
		GetTeamList().add(_drawers);
	}
	
	@EventHandler
	public void AddDrawers(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Prepare)
			return;
		
		ArrayList<Player> players = GetPlayers(true);
		
		for (int i=0 ; i<2 ; i++)
			for (Player player : players)
			{
				_roundPlayer.add(player);
				_roundMax++;
			}
	}
	
	@EventHandler
	public void RemoveDrawer(PlayerQuitEvent event)
	{
		while (_roundPlayer.contains(event.getPlayer()))
		{
			_roundPlayer.remove(event.getPlayer());
			_roundMax--;
		}
			
		
		Iterator<GameScore> scoreIterator = _ranks.iterator();
		
		while (scoreIterator.hasNext())
		{
			GameScore score = scoreIterator.next();
			
			if (score.Player.equals(event.getPlayer()))
				scoreIterator.remove();
		}
	}
	
	@EventHandler
	public void RoundUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FAST)
			return;
		
		//Word Selection
		if (_round != null && !_round.ChooseWordUpdate())
			return;

		//End Round
		if (_round != null && (_round.IsDone() || _drawers.GetPlayers(true).isEmpty() || _round.AllGuessed(_guessers.GetPlayers(true))))
		{
			Bukkit.getPluginManager().callEvent(new DrawRoundEndEvent(_round));

			Announce(C.cGold + C.Bold + "Round " + (_roundCount + 1) + " Ended: " + C.cYellow + C.Bold + "The word was " + _round.Word + "!");
			_textBlocks = UtilBlockText.MakeText(_round.Word, _textLocation, BlockFace.WEST, 159, (byte)15, TextAlign.CENTER);
			
			_roundTime = System.currentTimeMillis();
			_round = null;

			//Remove Old Drawers
			for (Player player : _drawers.GetPlayers(false))
			{
				_drawers.RemovePlayer(player);
				_guessers.AddPlayer(player, true);
				UtilInv.Clear(player);

				player.setAllowFlight(false);
				player.setFlying(false);
				player.setFlySpeed(0.1f);

				player.teleport(_guessers.GetSpawn());
			}

			_roundCount++;

			EndCheck();	
		}

		//Reset Round
		if ((_round == null && UtilTime.elapsed(_roundTime, 5000) && !_roundPlayer.isEmpty()))
		{
			Reset();
			
			//Select New Drawer
			Player drawer = _roundPlayer.remove(0);
			_guessers.RemovePlayer(drawer);
			_drawers.AddPlayer(drawer, true);
			
			//Create Round
			_round = new DrawRound(this, drawer);

			//Prep Drawer
			drawer.teleport(_drawerLocation);

			drawer.setAllowFlight(true);
			drawer.setFlying(true);
			drawer.setFlySpeed(0.4f);
			
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.WOOD_SWORD, (byte) 0, 1, "Pencil"));
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1, "Paint Brush"));
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.STONE_SWORD, (byte) 0, 1, "Line Tool"));
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.GOLD_SWORD, (byte)0, 1, "Square Tool"));
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_SWORD, (byte)0, 1, "Circle Tool"));
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BOW, (byte)0, 1, "Spray Can"));
			
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_HOE, (byte)0, 1, "Paint Bucket"));
			drawer.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.GOLD_HOE, (byte)0, 1, "Clear Canvas"));

			drawer.getInventory().setItem(10, ItemStackFactory.Instance.CreateStack(Material.ARROW, (byte)0, 1, "Paint"));

			Announce(C.cGold + C.Bold + "Round " + (_roundCount + 1) + ": " + C.cYellow + C.Bold + drawer.getName() + " is drawing!");
		}
	}
	
	public String GetWord()
	{
		//Get Word
		String word = getRandomWord();
		while (!_usedWords.add(word))
			word = getRandomWord();
		
		return word;
	}

	private String getRandomWord()
	{
		// TODO: These should be even no matter what word packs are enabled
		if (_useHalloweenWords && Math.random() <= 0.3)
		{
			return _halloweenWords[UtilMath.r(_halloweenWords.length)];
		}
		else if (_useChristmasWords && Math.random() <= 0.3)
		{
			return _christmasWords[UtilMath.r(_christmasWords.length)];
		}
		else
		{
			return _words[UtilMath.r(_words.length)];
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void Guess(AsyncPlayerChatEvent event)
	{
		if (!IsLive())
			return;

		if (_round == null)
			return;
		
		if (!_round.IsReady())
			return;
		
		Player player = event.getPlayer();

		if (_guessers.HasPlayer(player))
		{
			int wordsInMessage = 0;
			String message = event.getMessage().toLowerCase().replace(_round.Word.toLowerCase(), "");;

			for (String word : _words)
			{
				if (message.contains(word.toLowerCase()))
				{
					message = message.replace(word.toLowerCase(), "");

					if (++wordsInMessage >= 3)
					{
						UtilPlayer.message(player, F.main("Game", "Multiple guesses are not allowed!"));

						event.setCancelled(true);
						return;
					}
				}
			}

			if (event.getMessage().toLowerCase().contains(_round.Word.toLowerCase()))
			{
				// First Guess
				int score = 1;
				if (_round.Guessed.isEmpty())
				{
					score = 3;

					// Points for Drawer
					AddScore(_round.Drawer, 2);

					this.AddGems(_round.Drawer, 2, "Drawings Guessed", true, true);
				}

				if (_round.Guessed(player))
				{
					AddScore(player, score);
					Announce(C.cYellow + C.Bold + "+" + score + " " + C.cGreen + C.Bold + player.getName()
							+ " has guessed the word!");

					long since = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - _round.Time);
					getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.DMT_GUESS, GetType().getDisplay(), null, (int) since);

					if (score == 1)
						this.AddGems(player, 1, "Words Guessed", true, true);
					else
						this.AddGems(player, 4, "Words Guessed First", true, true);
				}
				else
				{
					UtilPlayer.message(player, F.main("Game", "You have already guessed the word!"));
				}

				event.setCancelled(true);
				return;
			}
		}
		else
		{
			if (event.getMessage().toLowerCase().contains(_round.Word.toLowerCase()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void TextUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		if (_round == null)
			return;
		
		if (!_round.IsReady())
		{
			UtilTextBottom.displayProgress(C.Bold + "Word Selection", _round.GetTimePercent(), _round.GetTimeString(), UtilServer.getPlayers());
			return;
		}
		
		for (Player player : UtilServer.getPlayers())
		{
			if (_drawers.HasPlayer(player))
			{
				UtilTextBottom.displayProgress(C.cYellow + C.Bold + "Draw: " + ChatColor.RESET + _round.Word, _round.GetTimePercent(), _round.GetTimeString(), player);
			}	
			else
			{
				UtilTextBottom.display(_round.GetRevealedWord() + C.cYellow + C.Bold + "   HINT   " +  ChatColor.RESET + _round.GetTimeString(), player);
			}
		}
	}	
	
	@EventHandler
	public void DrawerMove(PlayerMoveEvent event)
	{
		if (!IsLive())
			return;
		
		if (!_lockDrawer)
			return;
		
		if (!_drawers.HasPlayer(event.getPlayer()))
			return;
		
		if (UtilMath.offset(_drawerLocation, event.getTo()) > 1)
		{
			event.setTo(_drawerLocation);
			
			Player player = event.getPlayer();
			
			if (Recharge.Instance.use(player, "Instruct", 1000, false, false))
			{
				player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 0.5f);
				UtilPlayer.message(player, C.cRed + C.Bold + "Hold Block with a Sword to Draw!");
			}
		}	
	}
	
	@EventHandler
	public void toolStart(PlayerInteractEvent event)
	{		
		if (!IsLive())
			return;
		
		for (Tool tool : _tools)
			tool.start(event);
	}
	
	@EventHandler
	public void toolUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Tool tool : _tools)
			tool.update();
	}

	@EventHandler
	public void sprayCan(UpdateEvent e)
	{
		if (e.getType() != UpdateType.TICK)
			return;

		if (!IsLive())
			return;

		for (Player p : _drawers.GetPlayers(true))
		{
			if (!UtilGear.isMat(p.getItemInHand(), Material.BOW))
				continue;

			if (!UtilPlayer.isChargingBow(p))
			{
				_brushPrevious = null;
				continue;
			}

			Block block = UtilPlayer.getTarget(p, UtilBlock.blockPassSet, 400);
			if (block == null || !_canvas.contains(block))
				continue;

			// Spray
			block.setType(_brushMaterial);
			block.setData(_brushColor);

			for (Block surround : UtilBlock.getSurrounding(block, true))
			{
				if (!_canvas.contains(surround))
					continue;

				if (Math.random() > 0.5)
				{
					surround.setType(_brushMaterial);
					surround.setData(_brushColor);
				}
			}

			for (Player other : UtilServer.getPlayers())
				other.playSound(other.getLocation(), Sound.FIZZ, 0.2f, 2f);

			_lockDrawer = false;

			_brushPrevious = block.getLocation().add(0.5, 0.5, 0.5);
		}
	}

	@EventHandler
	public void sprayCanArrowCancel(EntityShootBowEvent e)
	{
		if (e.getEntity() instanceof Player)
		{
			e.setCancelled(true);
			((Player)e.getEntity()).updateInventory();
		}
	}

	@EventHandler
	public void Paint(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : _drawers.GetPlayers(true))
		{
			if (!UtilGear.isMat(player.getItemInHand(), Material.WOOD_SWORD) && !UtilGear.isMat(player.getItemInHand(), Material.IRON_SWORD))
				continue;

			if (!player.isBlocking())
			{
				_brushPrevious = null;
				continue;
			}

			Block block = UtilPlayer.getTarget(player, UtilBlock.blockPassSet, 400);
			if (block == null || !_canvas.contains(block))
				continue;

			if (block.getData() == _brushColor && block.getType() == _brushMaterial)
				continue;

			//Color
			block.setType(_brushMaterial);
			block.setData(_brushColor);

			//Thick Brush
			if (UtilGear.isMat(player.getItemInHand(), Material.IRON_SWORD))
			{
				for (Block other : UtilBlock.getSurrounding(block, false))
				{
					if (!_canvas.contains(other))
						continue;

					block.setType(_brushMaterial);
					other.setData(_brushColor);
				}
			}

			//Join Dots
			if (_brushPrevious != null)
			{
				while (UtilMath.offset(_brushPrevious, block.getLocation().add(0.5, 0.5, 0.5)) > 0.5)
				{
					_brushPrevious.add(UtilAlg.getTrajectory(_brushPrevious, block.getLocation().add(0.5, 0.5, 0.5)).multiply(0.5));

					Block fixBlock = _brushPrevious.getBlock();

					if (!_canvas.contains(fixBlock))
						continue;

					fixBlock.setType(_brushMaterial);
					fixBlock.setData(_brushColor);

					//Thick Brush
					if (UtilGear.isMat(player.getItemInHand(), Material.IRON_SWORD))
					{
						for (Block other : UtilBlock.getSurrounding(fixBlock, false))
						{
							if (!_canvas.contains(other))
								continue;

							other.setType(_brushMaterial);
							other.setData(_brushColor);
						}
					}
				}
			}

			for (Player other : UtilServer.getPlayers())
				other.playSound(other.getLocation(), Sound.FIZZ, 0.2f, 2f);

			_lockDrawer = false;

			_brushPrevious = block.getLocation().add(0.5, 0.5, 0.5);
		}
	}
	
	@EventHandler
	public void PaintReset(PlayerInteractEvent event)
	{		
		if (!IsLive())
			return;
		
		Player player = event.getPlayer();

		if (!UtilGear.isMat(player.getItemInHand(), Material.GOLD_HOE))
			return;

		if (!_drawers.HasPlayer(player))
			return;
		
		if (!Recharge.Instance.use(player, "Clear Canvas", 5000, true, false))
			return;
		
		byte color = _brushColor;
		
		Reset();
		
		//Restore
		_brushColor = color;
		_brushMaterial = Material.WOOL;
		_lockDrawer = false;
		
		for (Player other : UtilServer.getPlayers())
			other.playSound(other.getLocation(), Sound.EXPLODE, 0.5f, 1.5f);
	}

	@EventHandler
	public void paintFill(PlayerInteractEvent e)
	{
		if (!IsLive())
			return;

		Player p = e.getPlayer();

		if (!UtilGear.isMat(p.getItemInHand(), Material.IRON_HOE))
		{
			// Not the correct tool (iron hoe = paint fill).
			return;
		}

		if (!_drawers.HasPlayer(p))
		{
			// Not drawing.
			return;
		}

		// Get the target block that the player clicks on.
		Block target = UtilPlayer.getTarget(p, UtilBlock.blockPassSet, 400);

		if (target == null || !_canvas.contains(target))
		{
			// Target block is non-existent or not in the canvas.
			return;
		}

		Material material = target.getType();
		byte data = target.getData();

		if (data == _brushColor && material == _brushMaterial)
			return;

		fillRecursive(target, material, data);

		for (Player other : UtilServer.getPlayers())
			other.playSound(other.getLocation(), Sound.SPLASH, 0.4f, 1.5f);
	}

	private void fillRecursive(Block block, final Material fillMaterial, final byte fillData)
	{
		if (!_canvas.contains(block) || block.getType() != fillMaterial || block.getData() != fillData)
		{
			return;
		}

		block.setTypeIdAndData(_brushMaterial.getId(), _brushColor, false);

		List<Block> around = UtilBlock.getSurrounding(block, false);

		for (Block next : around)
		{
			fillRecursive(next, fillMaterial, fillData);
		}
	}

	@EventHandler
	public void ColorSelect(PlayerInteractEvent event)
	{		
		if (!IsLive())
			return;
		
		Player player = event.getPlayer();

		if (!_drawers.HasPlayer(player))
			return;

		Block target = UtilPlayer.getTarget(player, UtilBlock.blockPassSet, 400);

		if (target == null)
			return;

		Location loc = target.getLocation();

		List<Block> possibleBlocks = UtilBlock.getInBoundingBox(WorldData.GetDataLocs("GREEN").get(0),
				WorldData.GetDataLocs("GREEN").get(1));

		Block block = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

		if (block == null)
		{
			return;
		}

		for (Block other : possibleBlocks)
		{
			if (
					other.getX() == block.getX()
					&& other.getY() == block.getY()
					&& other.getZ() == block.getZ()
					)
			{
				block = other;
			}
		}

		if (block == null || !possibleBlocks.contains(block))
			return;

		_brushColor = block.getData();
		_brushMaterial = block.getType();

		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2f, 1f);
	}

	private void Reset() 
	{
		for (Block block : _canvas)
		{
//			if (block.getTypeId() != 35 || block.getData() != 0)
//				block.setTypeIdAndData(35, (byte)0, false);
			block.setTypeIdAndData(35, (byte) 0, false);
		}
		
		_brushColor = 15;
		_brushMaterial = Material.WOOL;
		
		if (_textBlocks != null)
		{
			for (Block block : _textBlocks)
				block.setType(Material.AIR);
			
			_textBlocks.clear();
			_textBlocks = null;
		}
		
		_lockDrawer = true;
	}

	public void AddScore(Player player, double amount)
	{
		for (GameScore score : _ranks)
		{
			if (score.Player.equals(player))
			{
				score.Score += amount;
				EndCheck();
				return;
			}
		}

		_ranks.add(new GameScore(player, amount));
	}

	private void SortScores()
	{
		Collections.sort(_ranks, GameScore.SCORE_DESC);
	}
	
	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			SortScores();
			
			//Set Places
			ArrayList<Player> places = new ArrayList<Player>();
			for (int i=0 ; i<_ranks.size() ; i++)
				places.add(i, _ranks.get(i).Player);
			
			if (places.isEmpty() || !places.get(0).isOnline())
				return Arrays.asList();
			else
				return Arrays.asList(places.get(0));
		}
		else
			return null;
	}
	
	@Override
	public List<Player> getLosers()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			SortScores();

			//Set Places
			ArrayList<Player> places = new ArrayList<Player>();
			for (int i = 0; i < _ranks.size(); i++)
				places.add(i, _ranks.get(i).Player);

			if (places.size() > 0)
				places.remove(0);

			return places;
		}
		else
			return null;
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if ((_roundCount == _roundMax && UtilTime.elapsed(_roundTime, 5000)) || GetPlayers(true).size() <= 1)
		{
			SortScores();

			//Set Places
			ArrayList<Player> places = new ArrayList<Player>();
			for (int i=0 ; i<_ranks.size() ; i++)
				places.add(i, _ranks.get(i).Player);

			//Award Gems
			if (_ranks.size() >= 1)
				AddGems(_ranks.get(0).Player, 20, "1st Place", false, false);

			if (_ranks.size() >= 2)
				AddGems(_ranks.get(1).Player, 15, "2nd Place", false, false);

			if (_ranks.size() >= 3)
				AddGems(_ranks.get(2).Player, 10, "3rd Place", false, false);

			//Participation
			for (Player player : GetPlayers(false))
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);

			SetState(GameState.End);
			AnnounceEnd(places);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		SortScores();
		
		//Wipe Last
		Scoreboard.reset();
		
		//Rounds
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cWhite + "Round:");
		Scoreboard.write(C.cYellow + Math.min(_roundCount+1, _roundMax) + " of " + _roundMax);
		
		//Drawer
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cWhite + "Drawer:");
		if (_round == null)
			Scoreboard.write(C.cYellow + "None");
		else
			Scoreboard.write(C.cYellow + _round.Drawer.getName());
		
		//Scores
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cWhite + "Scores:");


		Scoreboard.writeGroup(_ranks, score -> Pair.create(C.cYellow + score.Player.getName(), (int) score.Score), true);
		
		Scoreboard.draw();
	}
	
	@EventHandler
	public void selectionInput(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().startsWith("/selectword "))
			return;
		
		event.setCancelled(true);
		
		if (_round == null)
			return;
		
		if (!_round.Drawer.equals(event.getPlayer()))
			return;
		
		if (event.getMessage().length() <= 12)
			return;
		
		_round.WordClicked(event.getMessage().substring(12));
	}

	public ArrayList<Block> getCanvas()
	{
		return _canvas;
	}

	public boolean isDrawer(Player player)
	{
		return _drawers.HasPlayer(player);
	}

	public byte getColor()
	{
		return _brushColor;
	}

	public Material getBrushMaterial()
	{
		return _brushMaterial;
	}

	public void setLock(boolean b)
	{
		_lockDrawer = b;
	}
}
