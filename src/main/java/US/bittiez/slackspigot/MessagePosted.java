package US.bittiez.slackspigot;

public class MessagePosted {
    public String user;
    public UserProfile user_profile;
    public File file;


    public class File {
        public String name;
        public String permalink;
    }
    public class UserProfile {
        public String display_name;
    }
}
