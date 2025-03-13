import client.PreLoginUIState;
import ui.Repl;
import web.ServerFacade;

public class Main {
    public static void main(String[] args) {
        var serverFacade = new ServerFacade(getUrl(args));
        var firstState = new PreLoginUIState(serverFacade);
        new Repl().run(firstState);
    }

    private static String getUrl(String[] cliArgs) {
        if(cliArgs.length == 1) {
            return cliArgs[0];
        }
        if(cliArgs.length == 2) {
            return "http://" + cliArgs[0] + ":" + cliArgs[1];
        }
        if(cliArgs.length == 0) {
            return "http://localhost:8080";
        }
        throw new IllegalArgumentException("Invalid number of arguments");
    }
}