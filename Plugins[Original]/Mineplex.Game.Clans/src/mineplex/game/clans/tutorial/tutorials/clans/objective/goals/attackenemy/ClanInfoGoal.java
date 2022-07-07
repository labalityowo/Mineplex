package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy;


import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansPlayer;
import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.core.repository.tokens.ClanToken;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.AttackEnemyObjective;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.sql.Timestamp;
import java.util.UUID;


public class ClanInfoGoal extends ObjectiveGoal<AttackEnemyObjective>
{
    public ClanInfoGoal(AttackEnemyObjective objective)
    {
        super(
                objective,
                "Lookup Enemy Details",
                "View info about the enemy clan by typing '/c EnemyClan'",
                "You can lookup details about your enemy before going for an " +
                        "attack! This can give you a crucial advantage before " +
                        "you fight.",
                null
        );
    }

    @Override
    protected void customStart(Player player)
    {
    }

    @Override
    protected void customFinish(Player player)
    {
    }

    @EventHandler
    public void onClanInfo(ClansCommandPreExecutedEvent event)
    {
        if (contains(event.getPlayer()))
        {
            if(event.getArguments().length  < 1) return;

            event.setCancelled(true);

            if (!event.getArguments()[0].equalsIgnoreCase("EnemyClan"))
            {
                event.getPlayer().sendMessage(F.main("Clans", "That clan does not exist."));
                return;
            }

            ClanToken token = new ClanToken();
            token.Name = "EnemyClan";
            token.Description = "Chiss";
            token.Home = "";
            token.Admin = false;
            token.Energy = 4320;
            token.Id = -1;
            token.Kills =  UtilMath.random.nextInt(100);
            token.Murder =  UtilMath.random.nextInt(100);
            token.Deaths =  UtilMath.random.nextInt(100);
            token.WarWins =  UtilMath.random.nextInt(100);
            token.WarLosses =  UtilMath.random.nextInt(100);
            token.DateCreated = new Timestamp(System.currentTimeMillis() - (UtilTime.TimeUnit.DAYS.getMilliseconds() * 10));
            token.LastOnline = new Timestamp(System.currentTimeMillis() - (UtilTime.TimeUnit.DAYS.getMilliseconds() * 1));

            ClanInfo clan = new ClanInfo(ClansManager.getInstance(), token);

            ClansPlayer chiss = new ClansPlayer("Chiss", UUID.fromString("1d2bfe61-7ebd-445d-ba7d-8354a0ffd1ea"), ClanRole.LEADER);
            ClansPlayer jon = new ClansPlayer("defek7", UUID.fromString("89d463f7-23ec-470a-8244-457f0c8d861c"), ClanRole.MEMBER);
            chiss.setOnline(true);
            jon.setOnline(true);

            clan.getMembers().put(chiss.getUuid(), chiss);
            clan.getMembers().put(jon.getUuid(), jon);

            ClansManager.getInstance().getClanShop().openClanWho(event.getPlayer(), clan);
            finish(event.getPlayer());

        }
    }
}
