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
        Logger.info("NBP files not found. Trying to download from www.nbp.pl");
        Logger.info("NBP files have been downloaded successfully.");
        System.out.println("ZONK");
        controllers.Application.initializeData();
    }
    public void onStop(Application app){
        Logger.info("Application shutdown...");
    }
}