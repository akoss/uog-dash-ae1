import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.io.*; 
import java.util.Scanner;
import java.util.Calendar;
import java.util.Map;

public class AuctionHouseClient implements Serializable {

	private AuctionHouse house; 
	private String uid; 
	private transient Scanner sc; 
	private transient Thread messageProcessor; 

	public AuctionHouseClient(AuctionHouse house, String uid) {
		super();
		this.house = house; 
		if(uid != null) {
			this.uid = uid;
			this.processMessages(); 
		}
		this.sc = new Scanner(System.in);
	}

	public static void main(String[] args) {
		
		String reg_host = "localhost";
		int reg_port = 1099;

		if (args.length == 1) {
			reg_port = Integer.parseInt(args[0]);
		} else if (args.length == 2) {
			reg_host = args[0];
			reg_port = Integer.parseInt(args[1]);
		}
		AuctionHouse house = null;

		try {
			house = (AuctionHouse) Naming.lookup("rmi://" + reg_host + ":" + reg_port + "/AuctionHouseService");
		}
		catch (MalformedURLException murle) {
			System.out.println();
			System.out.println("MalformedURLException");
			System.out.println(murle);
		}
		catch (RemoteException re) {
			System.out.println();
			System.out.println("RemoteException");
			System.out.println(re);
		}
		catch (NotBoundException nbe) {
			System.out.println();
			System.out.println("NotBoundException");
			System.out.println(nbe);
		}

		AuctionHouseClient client = new AuctionHouseClient(house, null); 
		client.askForUid();

		boolean keepRunning = true;
		while(keepRunning) {
			keepRunning = client.menu();
		}
		client.stop();
	}

	private String ask(String question) {
		if(question != null) {
			System.out.println(question);
		}
		System.out.print(" > ");
		return sc.nextLine();
	}

	private void processMessages() {
		messageProcessor = new Thread(new Runnable() {
			 public void run() {
			 	boolean stop = false;
				while(!stop && !messageProcessor.isInterrupted()) {
					try {
						System.out.println("\n-- System Message --\n" + house.longpoll(uid).toString() + "\n--------------------\n");  
					}
					catch(RemoteException e) {
						System.err.println("\nError in longpoll. Connection lost.");
						stop = true;
						System.exit(1);
					}
				}
			 }
		});  
		messageProcessor.start();
	}

	public void askForUid() {
		while(this.uid == null) {
			String uid = ask("User ID?");
			if(!uid.equals("")) {
				this.uid = uid;
				this.processMessages(); 
			}
		}
	}

	private void stop() {
		if(messageProcessor != null) {
			messageProcessor.interrupt();	
		}
		System.exit(0);
	}

	public static String auctionToStringWithDate(Auction auction, String uid) throws RemoteException {
		return ("Auction ID: " + auction.id())
		+ ("\nName: " + auction.name())
		+ ("\nRuns for: " + auction.runsFor())
		+ ("\nTime left: " + auction.secondsRemaining())
		+ ("\nCurrently accepts bids: " + (auction.acceptsBids() ? "yes" : "no"))
		+ ("\nUploaded by: " + auction.uploader())
		+ (uid != null ? ("\nMy current bid: " + auction.currentBid(uid)) : "")
		+ (auction.winnerBidder() != null ? ("\nWinning bid: " + auction.getWinnerBidString()) : "");
	}

	private boolean menu() {
		System.out.println("\n\n1 - Get Auctions");
		System.out.println("2 - View Auction by ID");
		System.out.println("3 - Add Auction");
		System.out.println("");
		System.out.println("8 - Save State Now");
		System.out.println("9 - System Status");
		System.out.println("0 - Exit");

		boolean toReturn = true;

		switch(ask(null)) {
			case "1": 
				System.out.println("\n\nAuctions:");
				try {
					Map<Integer,Auction> map = house.getAuctions(); 
					for ( Integer key : map.keySet() ) {
						System.out.println( " - " + key );
					}

					if(ask("\n\nIterate over? (Y/N)").toLowerCase().equals("y")) {
						for(Map.Entry<Integer, Auction> entry : map.entrySet()) {
							Auction auction = entry.getValue();
							System.out.println("\n\n" + auctionToStringWithDate(auction, uid));
						}
					}
				}
				catch(RemoteException e) {
					System.out.println("Unable to view auctions: " + e);
				}
			break; 

			case "2": 
				String id = ask("\nAuction ID:");
				try {
					Auction auction = null;
					if(!id.equals("")) {
						auction = house.getAuction(Integer.parseInt(id)); 	
					} 

					if(auction == null) {
						System.out.println("\nAuction ID doesn't seem to exist"); 
					} else {
						System.out.println(auctionToStringWithDate(auction, uid));

						if(ask("\nBid? (Y/N)").toLowerCase().equals("y")) {
							String bidAmount = ask("\nBid amount: "); 
							if(!auction.bid(uid, Double.parseDouble(bidAmount))) System.out.println("\nBid hasn't been saved\n");
						}	
					}
				}
				catch(RemoteException e) {
					System.out.println("Unable to display this auction: " + e);
				}
				
			break;

			case "3": 
				System.out.println("");
				String name = ask("Item Name: ");
				int runsFor = Integer.parseInt(ask("Runs For (in seconds): "));
				double minimumValue = Double.parseDouble(ask("Minimum Value: "));

				try {
					System.out.println("\nAdded item: " + house.createAuction(name, runsFor, minimumValue, uid));
				}
				catch(RemoteException e) {
					System.out.println("Unable to create new auction");
				}
				break;
			case "8":
				break;
			case "0":
				toReturn = false;
				break;
			default: 
				System.out.println("Invalid Selection\n");
			break; 
		}

		return toReturn;
	}
}

