<!DOCTYPE html>
<html>
    <head>
        <title>The Fall Invitational Leaderboard</title>
        <meta http-equiv="refresh" content="30">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
        <style type="text/css">
            .table td.center, .table th.center {
                text-align: center;
            }
            
            .panel-heading {
                text-align: center;
                font-size: 18pt;
                font-weight: bold;
                color: orange!important;
                background-color: #333!important;
                border-color: #333!important;
            }
            
            .panel-primary {
                border-color: #333!important;
            }
        </style>
    </head>
    <body>
        <div class="jumbotron" style="text-align: center">
            <div class="container" style="text-align: center">
                <img src="mineplex.png" alt="Mineplex"/>
                <h1>The Tournament</h1>
                <h2>Leaderboard</h2>
            </div>
        </div>

        <hr/>

        <div class="container-fluid">
            <div class="row">

<?php

$tournamentTypes = array('Tournament');

$con = mysqli_connect('db.mineplex.com', 'root', 'tAbechAk3wR7tuTh', 'Account');
//$con = mysqli_connect('localhost', 'root', 'ZXquwQyEdKMB', 'Development');

for ($i = 0; $i < count($tournamentTypes); $i++)
{
    ?>
    <div class="col-md-4">
        <div class="panel panel-primary">
            <div class="panel-heading"><?php echo $tournamentTypes[$i] ?></div>
            <div class="table-responsive">
                <table class="table">
                    <col style="width: 15%">
                    <col style="width: 40%">
                    <col style="width: 15%">
                    <col style="width: 15%">
                    <col style="width: 15%">
                    <tr>
                        <th class="center">Rank</th>
                        <th>Player</th>
                        <th class="center">Wins</th>
                    </tr>
    <?php

	$query = <<<QUERY
        SELECT LB.rank, accounts.name, LB.value AS 'wins'
        FROM TournamentLB LB
		INNER JOIN accounts ON accounts.id = accountId
        ORDER BY rank ASC
        LIMIT 30;
QUERY;

    $result = mysqli_query($con, $query);

    $index = 1;
    
	while($row = mysqli_fetch_array($result))
    {        
        ?>
        <tr>
            <td class="center"><?php echo $row['rank'] ?></td>
            <td><?php echo $row['name'] ?></td>
            <td class="center"><?php echo $row['wins'] ?></td>
        </tr>
        <?php
        
        $index++;
    }
    
    ?>
                </table>
            </div>
        </div>
    </div>
    <?php
}

mysqli_close($con);

?>
            </div>
        </div>
        <p style="text-align: center; font-style: italic">*Score = 100 &times; Wins &times; (Wins / Total)<sup>3</sup></p>
        <hr/>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
    </body>
</html>