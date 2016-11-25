import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;


public class AuctionHouseServer {
	static int port = 1099;

	public AuctionHouseServer() {

	 try {
		AuctionHouseImpl houseimpl = new AuctionHouseImpl();
		AuctionHouse house = (AuctionHouse) UnicastRemoteObject.exportObject(houseimpl, 0);
		Naming.rebind("rmi://localhost:" + port + "/AuctionHouseService", house);
	 } 
	 catch (Exception e) {
	   System.out.println("Server Error: " + e);
	 }
   }

   public static void main(String args[]) {
	if (args.length == 1)
		port = Integer.parseInt(args[0]);
	
	new AuctionHouseServer();
   }
}