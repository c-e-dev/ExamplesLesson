package customLogger;

import customLogger.customLogger.CustomerLogger;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main {

    public static void main(String[] args)  throws Exception {
        CustomerLogger log = new CustomerLogger();
        log.init();
    }

}
