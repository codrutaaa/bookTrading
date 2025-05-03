package examples.bookTrading;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UserAgent extends Agent {

    protected void setup() {
        System.out.println("[UserAgent] " + getLocalName() + " started and waiting for notifications...");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg != null) {
                    System.out.println("[UserAgent] Notification received: " + msg.getContent());
                } else {
                    block();
                }
            }
        });
    }
}
