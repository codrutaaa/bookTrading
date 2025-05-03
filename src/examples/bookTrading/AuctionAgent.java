package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class AuctionAgent extends Agent {
    private Map<String, List<Bid>> auctions = new HashMap<>();
    private Map<String, AID> sellers = new HashMap<>();

    protected void setup() {
        System.out.println("AuctionAgent " + getAID().getName() + " started.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg != null) {
                    String content = msg.getContent();
                    String[] parts = content.split(":");
                    if (parts.length == 2) {
                        String bookTitle = parts[0];
                        int startPrice = Integer.parseInt(parts[1]);

                        auctions.put(bookTitle, new ArrayList<>());
                        sellers.put(bookTitle, msg.getSender());

                        System.out.println("[AuctionAgent] Auction created for book: " + bookTitle);
                        addBehaviour(new AuctionTimer(bookTitle, startPrice));
                    }
                } else {
                    block();
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                if (msg != null) {
                    String content = msg.getContent();
                    String[] parts = content.split(":");
                    if (parts.length == 2) {
                        String bookTitle = parts[0];
                        int bidAmount = Integer.parseInt(parts[1]);
                        auctions.getOrDefault(bookTitle, new ArrayList<>()).add(new Bid(msg.getSender(), bidAmount));
                        System.out.println("[AuctionAgent] Received bid of " + bidAmount + " from " + msg.getSender().getLocalName() + " for " + bookTitle);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private class AuctionTimer extends WakerBehaviour {
        private final String bookTitle;
        private final int startPrice;

        public AuctionTimer(String bookTitle, int startPrice) {
            super(AuctionAgent.this, 10000);
            this.bookTitle = bookTitle;
            this.startPrice = startPrice;
        }

        protected void onWake() {
            List<Bid> bids = auctions.get(bookTitle);
            if (bids == null || bids.isEmpty()) {
                System.out.println("[AuctionAgent] No bids for " + bookTitle);
                return;
            }

            Bid bestBid = Collections.max(bids, Comparator.comparingInt(b -> b.amount));
            System.out.println("[AuctionAgent] Winner is " + bestBid.bidder.getLocalName() + " with bid " + bestBid.amount);

            ACLMessage winMsg = new ACLMessage(ACLMessage.INFORM);
            winMsg.addReceiver(bestBid.bidder);
            winMsg.setContent("You won the auction for " + bookTitle + " at price " + bestBid.amount);
            send(winMsg);

            AID seller = sellers.get(bookTitle);
            if (seller != null) {
                ACLMessage soldMsg = new ACLMessage(ACLMessage.INFORM);
                soldMsg.addReceiver(seller);
                soldMsg.setContent("Book " + bookTitle + " sold to " + bestBid.bidder.getLocalName() + " for " + bestBid.amount);
                send(soldMsg);
            }

            auctions.remove(bookTitle);
            sellers.remove(bookTitle);
        }
    }

    private static class Bid {
        AID bidder;
        int amount;

        Bid(AID bidder, int amount) {
            this.bidder = bidder;
            this.amount = amount;
        }
    }
}
