import java.util.Date;

public interface Auction extends java.rmi.Remote {  
    public int id() throws java.rmi.RemoteException;
    public String name() throws java.rmi.RemoteException;
    public int runsFor() throws java.rmi.RemoteException;
    public String uploader() throws java.rmi.RemoteException;  

    public boolean acceptsBids() throws java.rmi.RemoteException; 
    public long secondsRemaining() throws java.rmi.RemoteException;
    public boolean isFinished() throws java.rmi.RemoteException;

    public double currentBid(String bidder) throws java.rmi.RemoteException;

    public boolean bid(String bidder, double amount) throws java.rmi.RemoteException; 

    public String getWinnerBidString() throws java.rmi.RemoteException; 
    public String winnerBidder() throws java.rmi.RemoteException; 
    public double winnerBidAmount() throws java.rmi.RemoteException; 
    public Date winnerTimestamp() throws java.rmi.RemoteException; 
}

