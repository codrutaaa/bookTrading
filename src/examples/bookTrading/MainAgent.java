package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.HashMap;
import java.util.Map;

public class MainAgent extends Agent {
    private Map<String, String> activeAuctions = new HashMap<>(); // Map<bookTitle, auctionAgentName>

    protected void setup() {
        System.out.println("[MainAgent] " + getLocalName() + " started.");

        // Listen for commands to create auctions, list auctions, etc.
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    String content = msg.getContent();
                    String[] parts = content.split(":");
                    String command = parts[0];

                    switch (command.toLowerCase()) {
                        case "create-auction":
                            if (parts.length >= 3) {
                                String bookTitle = parts[1];
                                String startPrice = parts[2];
                                createAuctionAgent(bookTitle, startPrice);
                            }
                            break;

                        case "list-auctions":
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("Active auctions: " + activeAuctions.keySet());
                            send(reply);
                            break;

                        default:
                            System.out.println("[MainAgent] Unknown command: " + command);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void createAuctionAgent(String bookTitle, String startPrice) {
        try {
            String agentName = "auction_" + bookTitle.replaceAll("\\s+", "_").toLowerCase();
            AgentContainer container = getContainerController();
            AgentController auctionAgent = container.createNewAgent(
                    agentName,
                    "examples.bookTrading.AuctionAgent",
                    new Object[0]
            );
            auctionAgent.start();

            // Wait a moment for the AuctionAgent to be ready
            doWait(1000); // 1 second

            // Send auction details to the new agent
            ACLMessage setupMsg = new ACLMessage(ACLMessage.INFORM);
            setupMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            setupMsg.setContent(bookTitle + ":" + startPrice);
            send(setupMsg);

            activeAuctions.put(bookTitle, agentName);
            System.out.println("[MainAgent] Created auction for book: " + bookTitle);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
