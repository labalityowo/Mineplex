package mineplex.servermonitor;

import java.util.List;

public class MinecraftPingReply {

    private String description;
    private Players players;
    private Version version;
    private String favicon;

    /**
     * @return the MOTD
     */
    public String getDescription() {
        return this.description;
    }

    public Players getPlayers() {
        return this.players;
    }

    /**
     * @return @{link Version}
     */
    public Version getVersion() {
        return this.version;
    }

    /**
     * @return Base64 encoded favicon image
     */
    public String getFavicon() {
        return this.favicon;
    }

    public class Players {
        private int max;
        private int online;
        private List<Player> sample;

        /**
         * @return Maximum player count
         */
        public int getMax() {
            return this.max;
        }

        /**
         * @return Online player count
         */
        public int getOnline() {
            return this.online;
        }

        /**
         * @return List of some players (if any) specified by server
         */
        public List<Player> getSample() {
            return this.sample;
        }
    }

    public class Player {
        private String name;
        private String id;

        /**
         * @return Name of player
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return Unknown
         */
        public String getId() {
            return this.id;
        }

    }

    public class Version {
        private String name;
        private int protocol;

        /**
         * @return Version name (ex: 13w41a)
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return Protocol version
         */
        public int getProtocol() {
            return this.protocol;
        }
    }

}
