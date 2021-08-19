<?php class Report
{
    /** @var Int */
    private $id;

    /** @var User|Null */
    private $handler;

    /** @var User */
    private $suspect;

    /** @var UserReport[] */
    private $reporters;

    /** @var Int */
    private $category;

    /**
     * Report constructor.
     * @param Int $id
     * @param User|Null $handler
     * @param User $suspect
     * @param UserReport[] $reporters
     * @param Int $category
     */
    function __construct($id, $handler, $suspect, $reporters, $category)
    {
        $this->id = $id;
        $this->handler = $handler;
        $this->suspect = $suspect;
        $this->reporters = $reporters;
        $this->category = $category;
    }

    /**
     * @return DateTime
     */
    public function getTimeCreated()
    {
        return $this->getOldestReport()->getTime();
    }

    /**
     * @return UserReport
     */
    private function getOldestReport()
    {
        /** @var UserReport $oldestReason */
        $oldestReason = null;
        
        foreach ($this->reporters as $reason)
        {
            if ($oldestReason == null || $oldestReason->getTime()->diff($reason->getTime())->invert)
            {
                $oldestReason = $reason;
            }
        }
        
        return $oldestReason;
    }

    /**
     * @return Int
     */
    public function getId()
    {
        return $this->id;
    }

    /**
     * @return User|Null
     */
    public function getHandler()
    {
        return $this->handler;
    }

    /**
     * @return User
     */
    public function getSuspect()
    {
        return $this->suspect;
    }

    /**
     * @return UserReport[]
     */
    public function getReporters()
    {
        return $this->reporters;
    }

    /**
     * @return Int
     */
    public function getCategory()
    {
        return $this->category;
    }
}