import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.io.*; 
import java.util.Map;

public class AuctionHouseServer {
	static int port = 1099;

    private class ShutdownSaver extends Thread  {
        AuctionHouseImpl house;

        public ShutdownSaver(AuctionHouseImpl house) {
            this.house = house;
        }

        @Override
        public void run() {
			try {
				OutputStream outStream = new FileOutputStream("state.ser");
				ObjectOutputStream fileObjectOut = new ObjectOutputStream(outStream);
				fileObjectOut.writeObject(this.house);
				fileObjectOut.close();
				outStream.close();
			}
			catch(FileNotFoundException e) {
				System.out.println("\nFileNotFoundException: " + e);
			}
			catch(IOException e) {
				System.out.println("\nIOException: " + e);
			}
        }
    }

	public AuctionHouseServer() {

		AuctionHouseImpl houseimpl = null;

		try {
			InputStream inStream = new FileInputStream("state.ser");
	        ObjectInputStream fileObjectIn = new ObjectInputStream(inStream);
	        houseimpl = (AuctionHouseImpl) fileObjectIn.readObject();
	        fileObjectIn.close();
	        inStream.close();	

	        houseimpl.republish();
		}
		catch(Exception e) {
			System.err.println("Exception during file load " + e);
			houseimpl = null;
		}

		try {
			if(houseimpl == null) {
				houseimpl = new AuctionHouseImpl();
			}

			AuctionHouse house = (AuctionHouse) UnicastRemoteObject.exportObject(houseimpl, 0);
			Naming.rebind("rmi://localhost:" + port + "/AuctionHouseService", house);
		} 
		catch (Exception e) {
			System.out.println("Server Error: " + e);
		}

		Runtime.getRuntime().addShutdownHook(new ShutdownSaver(houseimpl));
	}

	public static void main(String args[]) {
		if (args.length == 1)
		port = Integer.parseInt(args[0]);

		new AuctionHouseServer();
		}
}