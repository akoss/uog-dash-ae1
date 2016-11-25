import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap; 
import java.util.Calendar;
import java.util.*; 

public class AuctionImpl implements Auction, Serializable {

    private class AuctionFinisher extends TimerTask  {
        AuctionImpl auction;

        public AuctionFinisher(AuctionImpl auction) {
            this.auction = auction;
        }

        @Override
        public void run() {
            auction.finish();
        }
    }

    public AuctionImpl(int id, String name, int runsFor, double minimumValue, String uploader, MessageQueue q) {
        super();
        this.id = id;
        this.name = name; 
        this.runsFor = runsFor; 
        this.minimumValue = minimumValue; 
        this.uploader = uploader; 
        this.winnerBid = null; 
        this.isFinished = false;
        this.q = q;
        bids = new ConcurrentHashMap<String,Bid>(); 

        Calendar calendar = Calendar.getInstance(); 
        calendar.add(Calendar.SECOND, runsFor);
        this.runsUntil = calendar.getTime();
        this.setupTimer();
    }

    private int id; 
    private String name;
    private int runsFor; 
    private double minimumValue; 
    private String uploader; 
    private Bid winnerBid; 
    private boolean isFinished; 
    private Date runsUntil;
    private MessageQueue q;

    private ConcurrentHashMap<String,Bid> bids; 

    public int id() throws java.rmi.RemoteException {
        return this.id;
    }

    public String name() throws java.rmi.RemoteException {
        return this.name;
    }

    public int runsFor() throws java.rmi.RemoteException {
        return this.runsFor;
    }

    public double minimumValue() {
        return this.minimumValue;
    }

    public boolean acceptsBids() throws java.rmi.RemoteException {
        return !isFinished && (this.secondsRemaining() >= 0);
    }

    public String uploader() throws java.rmi.RemoteException {
        return this.uploader;
    }

    public double currentBid(String bidder) throws java.rmi.RemoteException {
        return bids.containsKey(bidder) ? bids.get(bidder).getBid() : 0;
    }

    public long secondsRemaining() {
        return (this.runsUntil.getTime() - Calendar.getInstance().getTime().getTime()) / 1000;
    }

    public void finish() {
        if(this.isFinished) return;

        List<Map.Entry<String,Bid>> entries = new ArrayList<Map.Entry<String,Bid>>(bids.entrySet());

        if(entries.size() > 0) {
            this.winnerBid = Collections.max(entries, new Comparator<Map.Entry<String,Bid>>() {
                public int compare(Map.Entry<String,Bid> a, Map.Entry<String,Bid> b) {
                    return b.getValue().compareTo(a.getValue());
                }
            }).getValue();

            if(this.winnerBid.getBid() < this.minimumValue) {
                this.winnerBid = null; 
            }

            if(q != null) {
                Iterator<Map.Entry<String,Bid>>  it = this.bids.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String,Bid> pair = (Map.Entry<String,Bid>)it.next();
                    q.put(pair.getKey(), "Bidding ended / " + ((this.winnerBid.getBidder() == pair.getKey()) ? "You won!\n" : "Someone else won\n") + getWinnerBidString());
                    it.remove();
                }
            }
        }

        isFinished = true; 
        q.put(uploader, "Bidding ended for your auction / " + getWinnerBidString());
        System.out.println(this.getWinnerBidString());
    }

    public String getWinnerBidString() {
        if(this.winnerBid == null) return "#" + this.id + " / no winning bid";
        return "#" + this.id + " / " + this.winnerBid.toString();
    }

    public String winnerBidder() {
        if(this.winnerBid == null) return null;
        return this.winnerBid.getBidder();
    }

    public double winnerBidAmount() {
        if(this.winnerBid == null) return 0;
        return this.winnerBid.getBid();
    }

    public Date winnerTimestamp() {
        if(this.winnerBid == null) return null;
        return this.winnerBid.getTimestamp();
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean bid(String bidder, double amount) throws java.rmi.RemoteException {
        if(!this.acceptsBids() || this.currentBid(bidder) >= amount) return false; 

        bids.put(bidder, new Bid(bidder, amount, Calendar.getInstance().getTime()));
        return true; 
    }

    public void setupTimer() {
        new Timer().schedule(new AuctionFinisher(this), runsUntil);
    }
}

