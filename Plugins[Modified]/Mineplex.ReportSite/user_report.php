<?php class UserReport
{
    private $user;
    private $time;
    private $reason;

    /**
     * @param User $user
     * @param DateTime $time
     * @param String $reason
     */
    function __construct($user, $time, $reason)
    {
        $this->user = $user;
        $this->time = $time;
        $this->reason = $reason;
    }

    /**
     * @return User
     */
    public function getUser()
    {
        return $this->user;
    }

    /**
     * @return DateTime
     */
    public function getTime()
    {
        return $this->time;
    }

    /**
     * @return String
     */
    public function getReason()
    {
        return $this->reason;
    }
}