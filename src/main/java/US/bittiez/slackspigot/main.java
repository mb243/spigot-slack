package US.bittiez.slackspigot;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class main extends JavaPlugin implements Listener {
    private FileConfiguration config = getConfig();
    private SlackSession session;
    private SlackChannel chatChannel;

    @Override
    public void onEnable(){
        createConfig();
        Boolean enabled = false;
        try {
            session = SlackSessionFactory.getSlackSessionBuilder(config.getString("slack-bot-token"))
                    .withAutoreconnectOnDisconnection(true)
                    .withConnectionHeartbeat(10, TimeUnit.SECONDS)
                    .build();
            session.connect();

            chatChannel = session.findChannelByName(config.getString("chat-channel"));
            enabled = true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }

        if(enabled) {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(this, this);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (cmd.getName().equalsIgnoreCase("ssreload")) {
            if (sender.hasPermission("spigotslack.reload")) {
                this.reloadConfig();
                config = getConfig();
                sender.sendMessage("Config reloaded!");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        String formattedMsg = config.getString("player-quit-format").replace("[PLAYER]", e.getPlayer().getName());
        sendMessageToSlack(chatChannel, formattedMsg);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String formatterMsg = "";
        if(e.getPlayer().hasPlayedBefore()){
            formatterMsg = config.getString("first-join-format").replace("[PLAYER]", e.getPlayer().getName());
        } else {
            formatterMsg = config.getString("normal-join-format").replace("[PLAYER]", e.getPlayer().getName());
        }

        sendMessageToSlack(chatChannel, formatterMsg);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
        if(e.isCancelled())
            return;
        if(e.getMessage().length() < 1)
            return;
        if(e.getPlayer().hasPermission("spigotslack.ignore"))
            return;

        String formattedMsg = config.getString("chat-format")
                .replace("[PLAYER]", e.getPlayer().getName())
                .replace("[MSG]", e.getMessage());

        sendMessageToSlack(chatChannel, formattedMsg);
    }

    private void sendMessageToSlack(SlackChannel chan, String msg){
        if(msg.length() < 1)
            return;
        session.sendMessage(chan, msg);
    }

    private void createConfig() {
        config.options().copyDefaults();
        saveDefaultConfig();
    }
}
