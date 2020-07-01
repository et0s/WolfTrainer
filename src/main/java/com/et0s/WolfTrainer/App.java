package com.et0s.WolfTrainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.logging.Logger;
import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class App extends JavaPlugin implements Listener
{
    public static HashMap<String, String> wolfRequests = new HashMap<String, String>();
    public static HashMap<String, Wolf> wolfOwners = new HashMap<String, Wolf>();
    private static Logger log;
    
    @Override
    public void onEnable() {
        // Don't log enabling, Spigot does that for you automatically!
        // Commands enabled with following method must have entries in plugin.yml
        log = this.getLogger();
        loadDefaultConfig();
        saveConfig();

        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("wt").setExecutor(this);
    }
    
    public void loadDefaultConfig(){
        if(!getDataFolder().exists()){
            try{
                getDataFolder().mkdir();
            }catch(Exception e){
                log.info("ERROR: " + e.getMessage());
            }
        }
        FileConfiguration config = this.getConfig();
        config.addDefault("Wolf-Stick", "STICK");
        this.getConfig().options().copyDefaults(true);       
    }

    public void tameRequest(Player requestor, Player dstPlayer){
        String reqName = requestor.getName();
        String dstName = dstPlayer.getName();
        if(wolfRequests.containsKey(dstName)){
            if(wolfRequests.get(dstName).equalsIgnoreCase(reqName)){
                requestor.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You already have a pending tame request with " + ChatColor.RED + dstName);
            }else{
                requestor.sendMessage("[WolfTrainer] " + ChatColor.RED + dstName + ChatColor.GRAY + " has too many tame request at this time!" + ChatColor.WHITE + " Try again Later.");
            }
            return;
        }

        requestor.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You have sent a tame request to " + ChatColor.RED + dstName);
        dstPlayer.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You have recieved a tame request from " + ChatColor.RED + reqName + ChatColor.GRAY + ". To Accept, " 
        + ChatColor.WHITE + "type /wt accept." + ChatColor.GRAY + " To Deny, " + ChatColor.WHITE + "type /wt deny");

        wolfRequests.put(dstName, reqName);
    }

    public void acceptTame(Player dstPlayer){
        String dstName = dstPlayer.getName();
        if(!wolfRequests.containsKey(dstName)){
            dstPlayer.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You have no pending " + ChatColor.RED + "tame requests");
            return;
        }
        
        Player requestor = getServer().getPlayer(wolfRequests.get(dstName));
        if(requestor == null){
            dstPlayer.sendMessage("[WolfTrainer]" + ChatColor.GOLD + " Woops! " + ChatColor.GRAY + " Something went wrong.");
            return;
        }

        String reqName = requestor.getName();
        requestor.sendMessage("[WolfTrainer] " + ChatColor.RED + dstName + ChatColor.GRAY + " has accepted your request.");
        Wolf myWolf = wolfOwners.get(reqName);
        myWolf.setTarget(null);
        myWolf.setAngry(false);
        myWolf.setOwner(requestor);
        myWolf.teleport(requestor.getLocation());
        dstPlayer.sendMessage("[WolfTrainer] " + ChatColor.RED + reqName + ChatColor.GRAY + " is now the Wolf Owner");
        wolfRequests.remove(dstName);
    }

    public void denyTame(Player dstPlayer){
        String dstName = dstPlayer.getName();
        if(!wolfRequests.containsKey(dstName)){
            dstPlayer.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You have no pending " + ChatColor.RED + "tame requests");
            return;
        }
        Player requestor = getServer().getPlayer(wolfRequests.get(dstName));
        if(requestor == null){
            dstPlayer.sendMessage("[WolfTrainer]" + ChatColor.GOLD + " Woops! " + ChatColor.GRAY + " Something went wrong.");
            return;
        }
        String reqName = requestor.getName();
        dstPlayer.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You have denied " + ChatColor.RED + reqName);
        requestor.sendMessage("[WolfTrainer] " + ChatColor.RED + dstName + ChatColor.GRAY + " has denied your tame request");
        wolfRequests.remove(dstName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length != 0){
                if(args[0].equalsIgnoreCase("give")){
                    if(args.length == 2){
                        Wolf myWolf = wolfOwners.get(player.getName());
                        if(myWolf == null){
                            player.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You need to select " + ChatColor.RED + ChatColor.BOLD + "YOUR " + ChatColor.RESET + ChatColor.ITALIC + "Tamed Wolf" + ChatColor.RESET + ChatColor.GRAY + " to execute this command.");
                        }else{
                            if(myWolf.getOwner().getUniqueId() == player.getUniqueId()){
                                //execute "give"
                                Player destPlayer = getServer().getPlayer(args[1]);
                                if(destPlayer == null){
                                    player.sendMessage("[WolfTrainer]" + ChatColor.RED + " Player " + args[1] + " can't be found! " + ChatColor.GRAY + "Perhaps they are Offline, or a Typo?");
                                }else if(destPlayer.equals(player)){
                                    player.sendMessage("[WolfTrainer]" +  ChatColor.BOLD + " YOU " + ChatColor.RESET + ChatColor.GRAY + "own this Wolf, silly!");
                                }else{
                                    myWolf.setTarget(null);
                                    myWolf.setAngry(false);
                                    myWolf.setOwner(destPlayer);
                                    myWolf.teleport(destPlayer.getLocation());
                                    player.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You have given a Wolf to " + ChatColor.RED + destPlayer.getName());
                                    destPlayer.sendMessage("[WolfTrainer] " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has just given you a Wolf.");
                                    return true;
                                }
                                return false;
                            }else{
                                player.sendMessage("[WolfTrainer]" + ChatColor.RED + " You are not the " + ChatColor.BOLD + " OWNER " + ChatColor.RESET + ChatColor.RED + ChatColor.RESET + " of this " + ChatColor.ITALIC + "Wolf");
                            }
                        }
                    }else{
                        player.sendMessage("[WolfTrainer] " + ChatColor.RED + "Invalid Argument! " + ChatColor.GRAY + "Try: " + ChatColor.WHITE + "/wt give <name>");
                    }
                }else if(args[0].equalsIgnoreCase("tame")){
                    if(args.length == 1){
                        Wolf myWolf = wolfOwners.get(player.getName());
                        if(myWolf == null){
                            player.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " You need to select " + ChatColor.RED + ChatColor.BOLD + "A " + ChatColor.RESET + ChatColor.ITALIC + "Tamed Wolf" + ChatColor.RESET + ChatColor.GRAY + " to execute this command.");
                        }else{
                            //Execute tame
                            if(myWolf.getOwner().getUniqueId() == player.getUniqueId()){
                                player.sendMessage("[WolfTrainer]" +  ChatColor.BOLD + " YOU " + ChatColor.RESET + ChatColor.GRAY + "already tamed this Wolf, silly!");
                            }else{
                                if(player.isOp()){
                                    myWolf.setTarget(null);
                                    myWolf.setAngry(false);
                                    myWolf.setOwner(player);
                                    myWolf.teleport(player.getLocation());
                                    player.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " This Wolf now belongs to " + ChatColor.RED + player.getName());
                                }else{
                                    if(getServer().getPlayer(myWolf.getOwner().getName()) == null){
                                        player.sendMessage("[WolfTrainer]" + ChatColor.GRAY + " Cannot send tame request. " + ChatColor.RED + myWolf.getOwner().getName() + ChatColor.GRAY + " appears to be Offline");
                                    }else{
                                        tameRequest(player, (Player) myWolf.getOwner());
                                    }
                                }
                            }
                            return true;
                        }
                    }else{
                        player.sendMessage("[WolfTrainer] " + ChatColor.RED + "Invalid Argument! " + ChatColor.GRAY + "Try: " + ChatColor.WHITE + "/wt tame");
                    }
                }else if(args[0].equalsIgnoreCase("accept")){
                    if(args.length == 1){
                        acceptTame(player);
                        return true;
                    }else{
                        player.sendMessage("[WolfTrainer] " + ChatColor.RED + "Invalid Argument! " + ChatColor.GRAY + "Try: " + ChatColor.WHITE + "/wt accept");
                    }
                }else if(args[0].equalsIgnoreCase("deny")){
                    if(args.length == 1){
                        denyTame(player);
                        return true;
                    }else{
                        player.sendMessage("[WolfTrainer] " + ChatColor.RED + "Invalid Argument! " + ChatColor.GRAY + "Try: " + ChatColor.WHITE + "/wt deny");
                    }
                }
            }
            player.sendMessage("[WolfTrainer] " + ChatColor.GREEN + "Need Help?" + ChatColor.GRAY + " Left-click a" + ChatColor.WHITE + ChatColor.ITALIC + " Tamed Wolf " + ChatColor.RESET + ChatColor.GRAY + "with a " + ChatColor.GOLD + this.getConfig().getString("Wolf-Stick"));
            return false;
            
        }else{
            // If the player (or console) uses our command correct, we can return true
            log.info("Wolf Trainer v1.0");
            return true;
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHitWolf(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player){
            Player player = (Player) event.getDamager();
            Entity entity = event.getEntity();
            Material wolfStick = Material.matchMaterial(this.getConfig().getString("Wolf-Stick"));
            if(player.getInventory().getItemInMainHand().getType() == wolfStick
                || player.getInventory().getItemInOffHand().getType() == wolfStick){
                
                if(wolfOwners.containsKey(player.getName()) &&
                    wolfOwners.get(player.getName()).equals(entity)){
                    Wolf wolf = wolfOwners.get(player.getName());
                    event.setCancelled(true);
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.BOLD + "WolfTrainer v1.0");
                    player.sendMessage("Wolf ID=" + wolf.getEntityId() + ":" + ChatColor.RED + " Unselected");
                    player.sendMessage(" ");
                    wolfOwners.remove(player.getName());
                    return;
                }
            
                if(entity instanceof Wolf){
                    Wolf wolf = (Wolf) entity;

                    if(wolf.isTamed()){
                        wolfOwners.put(player.getName(), wolf);
                        event.setCancelled(true);
                        if(wolf.getOwner().getUniqueId() == player.getUniqueId()){
                            player.sendMessage(" ");
                            player.sendMessage(ChatColor.BOLD + "WolfTrainer v1.0");
                            player.sendMessage("Wolf ID=" + wolf.getEntityId() + ":" + ChatColor.GREEN + " Selected");
                            player.sendMessage("Wolf Owner: " + ChatColor.GOLD + wolf.getOwner().getName());
                            player.sendMessage(ChatColor.GRAY + "" + ChatColor.UNDERLINE + "Commands:");
                            player.sendMessage(ChatColor.GRAY + "/wt give " + ChatColor.UNDERLINE + "name" + ChatColor.RESET + ChatColor.GRAY + "  -- Give the Ownership to " + ChatColor.UNDERLINE + "name");
                            player.sendMessage(" ");
                        }else{
                            player.sendMessage(" ");
                            player.sendMessage(ChatColor.BOLD + "WolfTrainer v1.0");
                            player.sendMessage("Wolf ID=" + wolf.getEntityId() + ":" + ChatColor.GREEN + " Selected");
                            player.sendMessage("Wolf Owner: " + ChatColor.GOLD + wolf.getOwner().getName());
                            player.sendMessage(ChatColor.GRAY + "" + ChatColor.UNDERLINE + "Commands:");
                            player.sendMessage(ChatColor.GRAY + "/wt tame   -- Request to be the Owner of this Wolf  ");
                            player.sendMessage(" ");
                        }
                    }
                }
            }
        }
    }
}