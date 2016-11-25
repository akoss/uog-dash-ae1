import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap; 
import java.util.Date;

public class Bid implements Comparable<Bid>, Serializable {

    public Bid(String bidder, double bid, Date timestamp) {
        super();
        this.bidder = bidder; 
        this.bid = bid; 
        this.timestamp = timestamp;
    }

    private String bidder;
    private double bid; 
    private Date timestamp; 

    public String getBidder() {
        return bidder; 
    }

    public double getBid() {
        return bid; 
    }

    public Date getTimestamp() {
        return timestamp; 
    }

    public int compareTo(Bid compareBid) {
        double compareBidAmount = ((Bid) compareBid).getBid();
        if(compareBidAmount != this.bid) {
            return Double.compare(compareBid.getBid(), this.bid);
        } else {
            return this.timestamp.compareTo(((Bid) compareBid).getTimestamp());
        }
    }

    public String toString() {
        return "Bidder: " + this.getBidder() + " / Bid: " + this.getBid() + " / Timestamp: " + this.getTimestamp().toString();
    }
}

