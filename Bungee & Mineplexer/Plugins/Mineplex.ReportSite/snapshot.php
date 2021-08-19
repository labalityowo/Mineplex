<?php class Snapshot
{

    /** @var String */
    private $identifier;

    /** @var Message[] */
    private $messages;

    /** @var User[] */
    private $players; // String UUID as Key

    /**
     * Snapshot constructor.
     * @param String $identifier
     * @param Message[] $messages
     * @param User[] $players
     */
    function __construct($identifier, $messages, $players)
    {
        $this->identifier = $identifier;
        $this->messages = $messages;
        $this->players = $players;
    }

    /**
     * @return String
     */
    public function getIdentifier()
    {
        return $this->identifier;
    }

    /**
     * @return Message[]
     */
    public function getMessages()
    {
        return $this->messages;
    }

    /**
     * @return User[]
     */
    public function getPlayers()
    {
        return $this->players;
    }
}