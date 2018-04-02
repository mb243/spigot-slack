package US.bittiez.slackspigot;

import US.bittiez.slackspigot.events.MessageReceived;
import US.bittiez.slackspigot.events.OutgoingMessage;
import US.bittiez.slackspigot.threads.ConsoleManager;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class main extends JavaPlugin implements Listener {
    private FileConfiguration config = getConfig();
    private SlackSession session;
    private SlackChannel chatChannel;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private ConsoleManager consoleManager;

    private SlackMessagePostedListener messagePostedListener = new SlackMessagePostedListener() {
        @Override
        public void onEvent(SlackMessagePosted event, SlackSession session) { executor.execute(
                new MessageReceived(event, session, config, getServer())
        ); }
    };


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
            if(config.getBoolean("enable-console", false))
                consoleManager = new ConsoleManager(session.findChannelByName(config.getString("console-channel", "abc123nochannelforme")), session, config, executor);
            if(config.getBoolean("send-started-message")){
                executor.execute(new OutgoingMessage(config.getString("started-message", "The server has started!"),
                        config.getString("started-user-name", "Server"),
                        config.getString("started-avatar-url", "http://i65.tinypic.com/14jak2x.png"),
                        session,
                        chatChannel
                        ));
            }
        } else {
            getLogger().log(Level.INFO, "Could not load slack-spigot.");
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (cmd.getName().equalsIgnoreCase("ssreload")) {
            if (sender.hasPermission("spigotslack.reload")) {
                this.reloadConfig();
                config = getConfig();
                consoleManager.setConfig(config);
                sender.sendMessage("[spigot-slack] Config reloaded!");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e){
        if(config.getBoolean("show-player-deaths", true)){
            String formattedMsg = config.getString("player-death-format", "[DEATHMSG]");
            formattedMsg = formattedMsg.replace("[DEATHMSG]", e.getDeathMessage());
            formattedMsg = formattedMsg.replace("[PLAYER]", e.getEntity().getName());
            sendMessageToSlack(chatChannel, formattedMsg, e.getEntity());
        }
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e){
//        String eventName = "";
//        String formattedMsg = config.getString("player-advancement-format", "[ADVANCEMENT]")
//                .replace("[PLAYER]", e.getPlayer().getName())
//                .replace("[ADVANCEMENT]", eventName);
//        sendMessageToSlack(chatChannel, formattedMsg, e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(e.getPlayer().hasPermission("spigotslack.silent"))
            return;
        String formattedMsg = config.getString("player-quit-format").replace("[PLAYER]", e.getPlayer().getName());
        sendMessageToSlack(chatChannel, formattedMsg, e.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String formatterMsg;
        if(e.getPlayer().hasPermission("spigotslack.silent"))
            return;
        if(!e.getPlayer().hasPlayedBefore()){
            formatterMsg = config.getString("first-join-format").replace("[PLAYER]", e.getPlayer().getName());
        } else {
            formatterMsg = config.getString("normal-join-format").replace("[PLAYER]", e.getPlayer().getName());
        }

        sendMessageToSlack(chatChannel, formatterMsg, e.getPlayer());
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

        sendMessageToSlack(chatChannel, formattedMsg, e.getPlayer());
    }

    private void sendMessageToSlack(SlackChannel chan, String formatterMsg, Player player){
        if(formatterMsg.length() < 1)
            return;

        executor.execute(new OutgoingMessage(formatterMsg, player, session, chan));
    }

    private void createConfig() {
        config.options().copyDefaults();
        saveDefaultConfig();
    }
}
