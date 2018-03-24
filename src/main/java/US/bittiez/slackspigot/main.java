package US.bittiez.slackspigot;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class main extends JavaPlugin {
    private FileConfiguration config = getConfig();
    private SlackSession session;

    @Override
    public void onEnable(){
        createConfig();
        try {
            session = SlackSessionFactory.getSlackSessionBuilder(config.getString("slack-bot-token"))
                    .withAutoreconnectOnDisconnection(true)
                    .withConnectionHeartbeat(10, TimeUnit.SECONDS)
                    .build();
            session.connect();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    private void createConfig() {
        config.options().copyDefaults();
        saveDefaultConfig();
    }
}
