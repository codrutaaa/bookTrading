package examples.bookTrading;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {
    public static void main(String[] args) {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Show JADE GUI

        AgentContainer mainContainer = runtime.createMainContainer(profile);

        try {
            // Start MainAgent only
            AgentController mainAgent = mainContainer.createNewAgent(
                    "main-agent",
                    "examples.bookTrading.MainAgent",
                    new Object[0]
            );
            mainAgent.start();

            AgentController sellerAgent = mainContainer.createNewAgent(
                    "seller1",
                    "examples.bookTrading.BookSellerAgent",
                    new Object[0]
            );
            sellerAgent.start();

            AgentController buyerAgent = mainContainer.createNewAgent(
                    "buyer1",
                    "examples.bookTrading.BookBuyerAgent",
                    new Object[] {"Java programming", "oneshot"}
            );
            buyerAgent.start();

            AgentController searchAgent = mainContainer.createNewAgent(
                    "search1",
                    "examples.bookTrading.SearchAgent",
                    new Object[] { "Java programming" }
            );
            searchAgent.start();

            AgentController userAgent = mainContainer.createNewAgent(
                    "user",
                    "examples.bookTrading.UserAgent",
                    new Object[0]
            );
            userAgent.start();



            System.out.println("[DEBUG] MainAgent started successfully.");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
