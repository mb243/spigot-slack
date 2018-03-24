package US.bittiez.slackspigot;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.bukkit.ChatColor;
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

import java.util.List;
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

            SlackMessagePostedListener messagePostedListener = new SlackMessagePostedListener() {
                @Override
                public void onEvent(SlackMessagePosted event, SlackSession session) {
                    SlackChannel channelOnWhichMessageWasPosted = event.getChannel();
                    String messageContent = event.getMessageContent();
                    SlackUser messageSender = event.getSender();

                    List<String> channels = config.getStringList("incoming-channels");
                        for (String chan : channels) {
                        if (channelOnWhichMessageWasPosted.getName() == chan) {
                            String formattedMsg = config.getString("incoming-chat-format")
                                    .replace("[DISPLAYNAME]", messageSender.getUserName())
                                    .replace("[MSG]", messageContent)
                                    .replace("[CHANNEL]", channelOnWhichMessageWasPosted.getName());
                            formattedMsg = ChatColor.translateAlternateColorCodes('&', formattedMsg);
                            getServer().broadcastMessage(formattedMsg);
                            break;
                        }
                    }
                }
            };
            //add it to the session
            session.addMessagePostedListener(messagePostedListener);

            enabled = true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        if(enabled) {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(this, this);
        } else {
            getLogger().log(Level.INFO, "Could not load slack-spigot.");
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
        if(e.getPlayer().hasPermission("spigotslack.silent"))
            return;
        String formattedMsg = config.getString("player-quit-format").replace("[PLAYER]", e.getPlayer().getName());
        sendMessageToSlack(chatChannel, formattedMsg);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String formatterMsg;
        if(e.getPlayer().hasPermission("spigotslack.silent"))
            return;
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
        msg = ChatColor.stripColor(msg);
        session.sendMessage(chan, msg);
    }

    private void createConfig() {
        config.options().copyDefaults();
        saveDefaultConfig();
    }
}
