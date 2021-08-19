<?php
    require_once('snapshot.php');
    require_once('report.php');
    require_once('message.php');
    require_once('user.php');
    require_once('user_report.php');

    const collapsedMessageCount = 20;

    // In Java this is "DateTimeFormatter.ISO_LOCAL_DATE_TIME"
    const jsonDateTimeFormat = 'Y-m-d\TH:i:s';

    $dateTimeZone = new DateTimeZone('America/Chicago');

    /** @var mysqli[] $connections */
    $connections = array(); // String index = Connection name

    /** @var User[] $users */ // Account id index
    $users = array();

    $categories = array(
        1 => 'Hacking',
        2 => 'Chat Abuse',
        3 => 'Gameplay'
    );

    /** PARSE DB CONNECTIONS */

    $dbConfigFile = new SplFileObject('database-config.dat');

    if ($dbConfigFile->isFile())
    {
        while (!$dbConfigFile->eof())
        {
            $line = trim($dbConfigFile->fgets());

            if ($line) // check not empty line
            {
                $parts = explode(' ', $line);
                $fullUrl = $parts[1];
                $urlParts = explode('/', $fullUrl);
                $host = $urlParts[0];
                $port = 3306;

                // check is port has been declared
                if (strpos($host, ':') !== false)
                {
                    $hostParts = explode(':', $host);
                    $host = $hostParts[0];
                    $port = $hostParts[1];
                }

                $database = $urlParts[1];

                $name = $parts[0];
                $username = $parts[2];
                $password = $parts[3];

                $connection = new mysqli($host, $username, $password, $database, $port);

                if ($connection->connect_error) {
                    die("Connection \"$name\" failed: $connection->connect_error");
                }

                $connections[$name] = $connection;
            }
        }
    }
    else
    {
        die('database-config.dat does not exist or is not a file.');
    }

    /**
     * @param String $name
     * @return mysqli
     */
    function getConnection($name)
    {
        global $connections;
        return $connections[$name];
    }

    /**
     * @param Int $reportId
     * @return Report
     */
    function getReport($reportId)
    {
        $connection = getConnection("ACCOUNT");
        $statement = $connection->prepare('SELECT reports.suspectId, reports.categoryId, reportHandlers.handlerId FROM reports
                                        LEFT JOIN reportHandlers ON reports.id = reportHandlers.reportId AND reportHandlers.aborted IS FALSE
                                        LEFT JOIN reportResults ON reports.id = reportResults.reportId
                                      WHERE reports.id = ?;');

        $statement->bind_param('i', $reportId);
        $statement->execute();
        $statement->store_result();
        $statement->bind_result($suspectId, $categoryId, $handlerId);

        if ($statement->fetch())
        {
            $suspectUser = getUser($suspectId);
            $reportReasons = getReporters($reportId);
            $handlerUser = null;

            if (!is_null($handlerId))
            {
                $handlerUser = getUser($handlerId);
            }

            return new Report($reportId, $handlerUser, $suspectUser, $reportReasons, $categoryId);
        }

        $statement->close();

        return null;
    }

    /**
     * @param string $token
     * @return int|null
     */
    function getSnapshotId($token)
    {
        $connection = getConnection('ACCOUNT');
        $statement = $connection->prepare('SELECT id FROM snapshots WHERE token = ?;');
        $statement->bind_param('s', $token);
        $statement->execute();
        $statement->bind_result($snapshotId);
        $statement->store_result();
        $statement->fetch();
        return $snapshotId;
    }

    /**
     * @param int $snapshotId
     * @return int|null
     */
    function getSnapshotReportId($snapshotId)
    {
        $connection = getConnection('ACCOUNT');
        $statement = $connection->prepare('SELECT id FROM reports WHERE snapshotId = ?;');
        $statement->bind_param('i', $snapshotId);
        $statement->execute();
        $statement->bind_result($reportId);
        $statement->store_result();
        $statement->fetch();
        return $reportId;
    }

    function getSnapshot($snapshotId)
    {
        /** @var $messages Message[] */
        $messages = array();

        $connection = getConnection("ACCOUNT");
        $statement = $connection->prepare("SELECT messageId, senderId, snapshotType, `server`, `time`, message FROM snapshots, snapshotMessages, snapshotMessageMap
WHERE snapshotMessageMap.snapshotId = snapshots.id
  AND snapshotMessages.id = snapshotMessageMap.messageId
  AND snapshots.id = ?;");

        $statement->bind_param('i', $snapshotId);
        $statement->execute();
        $statement->bind_result($snapshotId, $senderId, $snapshotType, $server, $time, $message);
        $statement->store_result();

        while ($statement->fetch())
        {
            $recipients = getUsers(getMessageRecipients($snapshotId));
            $message = new Message(getUser($senderId), $recipients, $time, $snapshotType, $message, $server);
            array_push($messages, $message);
        }

        $statement->close();
        $snapshotUsers = array();

        foreach ($messages as $message)
        {
            $sender = $message->getSender();
            $snapshotUsers[$sender->getId()] = $sender;

            foreach ($message->getRecipients() as $recipient)
            {
                $snapshotUsers[$recipient->getId()] = $recipient;
            }
        }

        return new Snapshot($snapshotId, $messages, $snapshotUsers);
    }

    /**
     * @param $snapshotId
     * @return Integer[] array
     */
    function getMessageRecipients($snapshotId)
    {
        $recipientIds = array();
        $connection = getConnection("ACCOUNT");
        $statement = $connection->prepare("SELECT recipientId FROM snapshotRecipients WHERE messageId = ?");

        $statement->bind_param('i', $snapshotId);
        $statement->execute();
        $statement->bind_result($recipientId);

        while ($statement->fetch())
        {
            array_push($recipientIds, $recipientId);
        }

        $statement->close();

        return $recipientIds;
    }

    /**
     * @param Integer[] $ids
     * @return User[] array
     */
    function getUsers($ids)
    {
        $users = array();

        foreach ($ids as $id)
        {
            array_push($users, getUser($id));
        }

        return $users;
    }

    /**
     * @param $id
     * @return User
     */
    function getUser($id)
    {
        if (isset($users[$id]))
        {
            return $users[$id];
        }
        else
        {
            $connection = getConnection("ACCOUNT");
            $statement = $connection->prepare('SELECT uuid, `name`, rank FROM accounts WHERE id = ?');

            $statement->bind_param('i', $id);
            $statement->execute();
            $statement->bind_result($uuid, $name, $rank);
            $statement->fetch();

            $user = new User($id, $uuid, $name, parseRank($rank));
            $users[$id] = $user;
            $statement->close();

            return $user;
        }
    }

    /**
     * @param int $reportId
     * @return UserReport[]
     */
    function getReporters($reportId)
    {
        global $dateTimeZone;

        $connection = getConnection("ACCOUNT");
        $statement = $connection->prepare("SELECT reporterId, `time`, reason FROM reportReasons WHERE reportId = ?");
        $reportReasons = array();

        $statement->bind_param('i', $reportId);
        $statement->execute();
        $statement->bind_result($reporterId, $time, $reason);
        $statement->store_result(); // prevents issues with other queries running before this statement is closed

        while ($statement->fetch())
        {
            $reportReasons[$reporterId] = new UserReport(getUser($reporterId), new DateTime($time, $dateTimeZone), $reason);
        }

        $statement->close();

        return $reportReasons;
    }

    /**
     * @param Report $report
     * @return User[]
     */
    function getInvolvedUsers($report)
    {
        $involvedUsers[$report->getSuspect()->getId()] = $report->getSuspect();

        foreach ($report->getReporters() as $reporterReason) {
            $reporter = $reporterReason->getUser();
            $involvedUsers[$reporter->getId()] = $reporter;
        }

        return $involvedUsers;
    }

    /**
     * @param string $dbRank
     * @return string
     */
    function parseRank($dbRank)
    {
        $rank = $dbRank;

        if ($dbRank == 'ALL')
        {
            $rank = 'PLAYER';
        }

        return $rank;
    }

    /**
     * @param Message $messageA
     * @param Message $messageB
     * @return int
     */
    function compareMessageTimes($messageA, $messageB)
    {
        return $messageA->getTimestamp()->getTimestamp() - $messageB->getTimestamp()->getTimestamp();
    }

    /**
     * @param String $dateTime
     * @param DateTimeZone $timezone
     * @return DateTime
     */
    function parseDateTime($dateTime, $timezone)
    {
        return DateTime::createFromFormat(jsonDateTimeFormat, $dateTime, $timezone);
    }

    /**
     * Converts an interval to minutes, days or months, depending on the size.
     *
     * @param DateInterval $interval
     * @return string
     */
    function approximateHumanInterval($interval)
    {
        if ($interval->y > 0)
        {
            $humanString = $interval->y . ' year' . ($interval->y != 1 ? 's' : '');
        } else if ($interval->m > 0)
        {
            $humanString = $interval->m . ' month' . ($interval->m != 1 ? 's' : '');
        }
        else if ($interval->d > 0)
        {
            $humanString = $interval->d . ' day' . ($interval->d != 1 ? 's' : '');
        }
        else if ($interval->h > 0)
        {
            $humanString = $interval->h . ' hour' . ($interval->h != 1 ? 's' : '');
        }
        else if ($interval->i > 0)
        {
            $humanString = $interval->i . ' minute' . ($interval->i != 1 ? 's' : '');
        }
        else
        {
            $humanString = $interval->s . ' second' . ($interval->s != 1 ? 's' : '');
        }

        return $humanString;
    }

    function getExpandedURL()
    {
        $vars = $_GET;
        $vars['expanded'] = true;
        return '?' . http_build_query($vars);
    }

    $validToken = isset($_GET['token']);
    $errorMsg = "";

    $title = 'Report & Snapshot System';
    $token = null;
    $expanded = null;
    $report = null;
    $snapshot = null;
    $messages = null;

    if ($validToken)
    {
        $token = $_GET['token'];
        $expanded = isset($_GET['expanded']) && $_GET['expanded'];
        $snapshotId = getSnapshotId($token);

        if ($snapshotId != null)
        {
            $snapshot = getSnapshot($snapshotId);
            $messages = $snapshot->getMessages();
            $reportId = getSnapshotReportId($snapshotId);
            $report = null;

            if ($reportId)
            {
                $report = getReport($reportId);
                $title = "Report #$reportId";
            }
            else
            {
                $title = "Snapshot #$snapshotId";
            }
        }
        else
        {
            $validToken = false;
            $errorMsg = 'Invalid token.';
        }
    }
?>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="css/tiger.css">
    <link href='https://fonts.googleapis.com/css?family=Crete+Round' rel='stylesheet' type='text/css'>
    <link href='https://fonts.googleapis.com/css?family=Oswald' rel='stylesheet' type='text/css'>
    <title>
        <?= $title ?> &middot; Mineplex
    </title>
</head>
<body>
<div id="wrapper">
    <div id="header">
        <img src="img/logo.png" height="70px" width="70px" />
        <h1>Report System</h1>
    </div>
    <div id="search">
        <form id="token-input" name="token-input" action="view.php" method="get">
            <div class="input-group">
                <input name="token" type="text" class="form-control" placeholder="Enter snapshot token...">
                <span class="input-group-btn">
                    <button class="btn btn-secondary" type="submit" form="token-input"><i class="fa fa-search"></i> Search</button>
                </span>
            </div>
        </form>
    </div>

    <?php if ((isset($_GET['token']) && !$validToken) || !empty($errorMsg)): ?>
        <div id="content" class="center-block" style="text-align: center; background-color: rgba(204, 34, 42, 0.52);">
            <p class="error-oh-no" style="font-size: 60px;">What did you do?!?!?</p>
            <img src="img/shaun.gif" />

            <?php if (!empty($errorMsg)): ?>
                <p class="error-oh-no" style="font-size: 40px;">Error: <?= $errorMsg ?></p>
                <br />
            <?php endif; ?>
        </div>
    <?php else: ?>
        <?php if (!isset($_GET['token'])) exit(); ?>

        <div id="content">
            <div>
                <hr>
                <h2 style="font-family: 'Oswald', sans-serif; text-align: center;">
                    <?= $title ?>
                </h2>
                <hr>
            </div>
            <div class="row">
                <div id="chat" class="col-lg-7">
                    <h4><i class="fa fa-comments"></i>&nbsp;&nbsp;&nbsp;Chat Log</h4>
                    <hr>
                    <div id="log">
                        <?php
                            $messageCount = count($messages);
                            $displayAmount = $expanded || $messageCount <= collapsedMessageCount ? $messageCount : collapsedMessageCount;
                            $involvedUsers = null;

                            if ($report != null)
                            {
                                $involvedUsers = getInvolvedUsers($report);
                            }
                            else
                            {
                                $involvedUsers = array();
                            }

                            foreach ($snapshot->getPlayers() as $player)
                            {
                                $involvedUsers[$player->getId()] = $player;
                            }

                            if($displayAmount == 0): ?>
                                <span class="black">No chat log available for this report.</span>
                            <?php else:
                                for($i = 0; $i < $displayAmount; $i++):
                                    $message = $messages[$i];
                                    $typeId = $message->getType();
                                    $typeDisplayName = Message::$TYPE_DISPLAY_NAMES[$typeId];
                                    $isPM = $typeId == Message::TYPE_PM;
                                    ?>

                                    <span class="log-line">
                                        <?php if($isPM): ?>
                                            <span class="label label-primary chat pm"><?= $typeDisplayName ?></span>
                                        <?php elseif($typeId == Message::TYPE_PARTY): ?>
                                            <span class="label label-warning chat"><?= $typeDisplayName ?></span>
                                        <?php else: ?>
                                            <span class="label label-info chat"><?= $typeDisplayName ?></span>
                                        <?php endif; ?>

                                        <span class="remove-whitespace">
                                            <span class="<?= ($report != null && $message->getSender() == $report->getSuspect() ? 'suspect' : 'black') ?>"><?= $message->getSender()->getUsername() ?></span>

                                            <?php if ($isPM): ?>
                                                &nbsp;->&nbsp;<?= $message->getRecipients()[0]->getUsername() ?>
                                            <?php endif; ?>

                                            <span class="message-separator black">: </span>
                                        </span>
                                        <span class="text-muted"><?= $message->getMessage() ?></span>
                                        <?php if ($i < $displayAmount - 1): // Don't break on the last element ?>
                                            <br />
                                        <?php endif; ?>
                                    </span>
                                <?php endfor; ?>
                            <?php endif; ?>
                    </div>

                    <?php if (!$expanded && $displayAmount < $messageCount): ?>
                        <br />
                        <a href="<?= getExpandedURL() ?>">Show All (<?= $messageCount ?> messages)</a>
                    <?php endif; ?>
                </div>
                <div id="users" class="col-lg-5">
                    <?php if ($report != null): ?>
                        <h4><i class="fa fa-info-circle"></i>&nbsp;&nbsp;&nbsp;Information</h4>
                        <hr>
                        <div class="row">
                            <div class="col-lg-12">
                                <?php
                                    // Put all reporter usernames in array for easy access later
                                    $reporterUsernames = array();
                                    foreach ($report->getReporters() as $reporterReason)
                                    {
                                        $reporterUsernames[count($reporterUsernames)] = $reporterReason->getUser()->getUsername();
                                    }

                                    $reportCreationTime = $report->getTimeCreated();
                                    $age = approximateHumanInterval($reportCreationTime->diff(new DateTime('now', $reportCreationTime->getTimezone())));
                                ?>
                                <i class="fa fa-clock-o fa-fw"></i>
                                <span class="label label-pill label-default" title="Last Report: <?= $reportCreationTime->format('Y/m/d H:i:s T') ?>"><?= $age . ' ago' ?></span>
                                <br>

                                <i class="fa fa-sitemap fa-fw"></i>
                                <span class="label label-pill label-primary"><?= $categories[$report->getCategory()] ?></span>
                                <br>

                                <i class="fa fa-user-plus fa-fw"></i>
                                <span class="label label-pill label-success">Reported by <?= implode(", ", $reporterUsernames) ?></span>
                                <br>

                                <i class="fa fa-user-times fa-fw"></i>
                                <span class="label label-pill label-danger">Suspect is <?= $report->getSuspect()->getUsername() ?></span>
                                <br>

                                <i class="fa fa-gavel fa-fw"></i>
                                <span class="label label-pill label-warning">
                                    <?php if ($report->getHandler() != null): ?>
                                        Staff Member assigned is <?= $report->getHandler()->getUsername() ?>
                                    <?php else: ?>
                                        No Staff Member assigned
                                    <?php endif; ?>
                                </span>
                                <br>
                            </div>
                        </div>
                        <br>
                    <?php endif; ?>
                    <h4><i class="fa fa-users"></i>&nbsp;&nbsp;&nbsp;Users</h4>
                    <hr>
                    <?php foreach($involvedUsers as $user): ?>
                        <img src="http://cravatar.eu/avatar/<?= $user->getUUID() ?>/55.png" class="pull-left" />
                        &nbsp;&nbsp;<b class="name"><?= $user->getUsername() ?></b> <span class="label label-staff"><?= ucwords(strtolower($user->getRank())) ?></span><br> <!-- TODO different styling for different ranks -->&nbsp;
                        <code style="font-size: 11px;"><?= $user->getUUID() ?></code>
                        <br><br>
                    <?php endforeach; ?>
                </div>
            </div>
        </div>
    <?php endif; ?>
    <div id="footer">
        <a href="http://www.mineplex.com"><img src="img/logo-full.png" width="225px" /></a>
        <div class="btn-group pull-right indent-link" style="font-family: 'Crete Round', serif; padding-top: 10px;">
            <a href="http://www.mineplex.com" class="btn btn-link btn-small text-muted">Home</a>
            <a href="http://www.mineplex.com/shop/" class="btn btn-link btn-small text-muted">Shop</a>
            <a href="http://www.mineplex.com/forums/" class="btn btn-link btn-small text-muted">Forums</a>
            <a href="http://www.mineplex.com/supporthub/" class="btn btn-link btn-small text-muted">Support</a>
        </div>
    </div>
</div>
</body>

<script src="js/jquery.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/main.js"></script>

</html>
<?php foreach ($connections as $connection) {
    $connection->close();
} ?>
