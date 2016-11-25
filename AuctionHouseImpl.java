import java.util.concurrent.LinkedTransferQueue; 
import java.util.concurrent.ConcurrentHashMap; 
import java.util.Map;
import java.util.Collections;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;

public class AuctionHouseImpl implements AuctionHouse, Serializable {

    private ConcurrentHashMap<Integer,Auction> auctions; 
    private MessageQueue q;

    private int currentkey; 

    public AuctionHouseImpl() throws java.rmi.RemoteException {
        super();
        q = new MessageQueue();
        auctions = new ConcurrentHashMap<Integer,Auction>(); 
        currentkey = 0; 

        this.createAuction("teszt",150,120, "userke");
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

        auctions.put(auct.id(), auct); 

        return auct.id();
    }

    public Map<Integer,Auction> getAuctions() throws java.rmi.RemoteException {
        return Collections.unmodifiableMap(auctions);
    }

    public Auction getAuction(int id) throws java.rmi.RemoteException {
        return auctions.get(id); 
    }
}