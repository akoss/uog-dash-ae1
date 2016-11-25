import java.util.concurrent.LinkedTransferQueue; 
import java.util.concurrent.ConcurrentHashMap; 
import java.util.Map;
import java.util.Collections;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.io.*; 

public class AuctionHouseImpl implements AuctionHouse, Serializable {

    public ConcurrentHashMap<Integer,AuctionImpl> auctions; 
    public ConcurrentHashMap<Integer,Auction> auctionStubs; 
    private MessageQueue q;

    private int currentkey; 

    public AuctionHouseImpl() throws java.rmi.RemoteException {
        super();

        auctions = new ConcurrentHashMap<Integer,AuctionImpl>(); 
        auctionStubs = new ConcurrentHashMap<Integer,Auction>(); 
        currentkey = 0; 
        q = new MessageQueue();
    }

    public String longpoll(String uid) throws java.rmi.RemoteException {
        try {
            return q.take(uid);
        }
        catch(InterruptedException e) {
            return null; 
        }
    }

    public int createAuction(String name, int runsFor, double minimumValue, String uploader) throws java.rmi.RemoteException {
        AuctionImpl auctionimpl = new AuctionImpl(currentkey++, name, runsFor, minimumValue, uploader, this.q);
        Auction auct = (Auction) UnicastRemoteObject.exportObject(auctionimpl, 0);

        auctions.put(auctionimpl.id(), auctionimpl); 
        auctionStubs.put(auctionimpl.id(), auct); 

        return auct.id();
    }

    public Map<Integer,Auction> getAuctions() throws java.rmi.RemoteException {
        return Collections.unmodifiableMap(auctionStubs);
    }

    public Auction getAuction(int id) throws java.rmi.RemoteException {
       return auctionStubs.get(id);
    }

    public void republish() {
        for(Map.Entry<Integer, AuctionImpl> entry : auctions.entrySet()) {
            try {
                auctionStubs.put(entry.getKey(),((Auction) UnicastRemoteObject.exportObject(entry.getValue(), 0)));
                if(!entry.getValue().isFinished()){
                    entry.getValue().setupTimer();    
                }
            }
            catch(Exception e){
                System.err.println("Error during republish: " + e);
            }
            
        }
    }
}