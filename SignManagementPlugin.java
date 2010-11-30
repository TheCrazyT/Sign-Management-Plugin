import java.util.logging.Logger;
public class SignManagementPlugin extends Plugin {
    static final SignManagementPluginListener listener = new SignManagementPluginListener();
    private Logger log;
    private String name = "Sign Management Plugin";
    private String version = "1.0";

    public void enable() {
    }
    
    public void disable() {
    	SignManagementPluginListener.ConfServer.server.stop(0);
    }

    public void initialize() {
        etc.getLoader().addListener(
            PluginLoader.Hook.BLOCK_CREATED,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(
            PluginLoader.Hook.LOGIN,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(
            PluginLoader.Hook.BLOCK_BROKEN,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(
            PluginLoader.Hook.COMMAND,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(
            PluginLoader.Hook.SERVERCOMMAND,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(
            PluginLoader.Hook.BLOCK_DESTROYED,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.COMPLEX_BLOCK_CHANGE,
            listener,
            this,
            PluginListener.Priority.MEDIUM);
        log = Logger.getLogger("Minecraft");
        log.info(name + " " + version + " initialized.");
        log.info("");
    }
}