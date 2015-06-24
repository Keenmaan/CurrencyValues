import play.Application;
import play.GlobalSettings;
import play.Logger;

/**
 * Created by keen on 4/10/15.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app){
        Logger.info("Application is starting.");
        Logger.info("Looking for NBP files.");
        controllers.Initialization.initializeData();
    }
    public void onStop(Application app){
        Logger.info("Application shutdown...");
    }
}