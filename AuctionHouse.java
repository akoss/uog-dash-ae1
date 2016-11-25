import java.util.Map;

public interface AuctionHouse extends java.rmi.Remote {	

	public String longpoll(String uid) throws java.rmi.RemoteException;

	public int createAuction(String name, int runsFor, double minimumValue, String uploader) throws java.rmi.RemoteException; 

	public Map<Integer,Auction> getAuctions() throws java.rmi.RemoteException;

	public Auction getAuction(int id) throws java.rmi.RemoteException;

	public String status() throws java.rmi.RemoteException;
}