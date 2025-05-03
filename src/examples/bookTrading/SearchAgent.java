package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SearchAgent extends Agent {
    private String targetBookTitle;
    private boolean notified = false;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("[SearchAgent] Looking for auctions with: " + targetBookTitle);

            addBehaviour(new TickerBehaviour(this, 5000) {
                protected void onTick() {
                    if (notified) {
                        myAgent.doDelete();
                        return;
                    }

                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("book-selling");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        for (DFAgentDescription dfd : result) {
                            String sellerName = dfd.getName().getLocalName();
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(new AID("user", AID.ISLOCALNAME));
                            msg.setContent("Found auction at " + sellerName + " for book: " + targetBookTitle);
                            send(msg);
                            notified = true;
                            System.out.println("[SearchAgent] Notified user and exiting.");
                            break;
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                }
            });
        } else {
            System.out.println("[SearchAgent] No book title provided. Terminating agent.");
            doDelete();
        }
    }
}
