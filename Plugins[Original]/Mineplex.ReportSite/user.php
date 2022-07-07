<?php class User
{
    /** @var Int */
    private $id;
    
    /** @var String */
    private $uuid;

    /** @var String */
    private $username;

    /** @var String */
    private $rank;

    /**
     * Player constructor.
     * @param Int $id
     * @param String $uuid
     * @param String $username
     * @param String $rank
     */
    function __construct($id, $uuid, $username, $rank)
    {
        $this->id = $id;
        $this->uuid = $uuid;
        $this->username = $username;
        $this->rank = $rank;
    }

    /**
     * @return Int
     */
    public function getId()
    {
        return $this->id;
    }
    
    /**
     * @return String
     */
    public function getUUID()
    {
        return $this->uuid;
    }

    /**
     * @return String
     */
    public function getUsername()
    {
        return $this->username;
    }

    /**
     * @return String
     */
    public function getRank()
    {
        return $this->rank;
    }
}