<?php class Message
{
    public static $TYPE_DISPLAY_NAMES = array("Chat", "PM", "Party");

    const TYPE_CHAT = 0;
    const TYPE_PM = 1;
    const TYPE_PARTY = 2;

    /** @var User */
    private $sender;

    /** @var User[] */
    private $recipients;

    /** @var DateTime */
    private $timestamp;

    /** @var Int */
    private $type;

    /** @var String */
    private $message;

    /** @var String */
    private $server;

    /**
     * Message constructor.
     * @param User $sender
     * @param User[] $recipients
     * @param DateTime $dateTime
     * @param Int $type
     * @param Message $message
     * @param String $server
     */
    function __construct($sender, $recipients, $dateTime, $type, $message, $server)
    {
        $this->sender = $sender;
        $this->recipients = $recipients;
        $this->timestamp = $dateTime;
        $this->type = $type;
        $this->message = $message;
        $this->server = $server;
    }

    /**
     * @return User
     */
    public function getSender()
    {
        return $this->sender;
    }

    /**
     * @return User[]
     */
    public function getRecipients()
    {
        return $this->recipients;
    }

    /**
     * @return DateTime
     */
    public function getTimestamp()
    {
        return $this->timestamp;
    }

    /**
     * @return Int
     */
    public function getType()
    {
        return $this->type;
    }

    /**
     * @return String
     */
    public function getMessage()
    {
        return $this->message;
    }

    /**
     * @return String
     */
    public function getServer()
    {
        return $this->server;
    }
}