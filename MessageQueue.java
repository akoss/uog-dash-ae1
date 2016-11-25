import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue; 
import java.util.Map;

public class MessageQueue implements Serializable {

    private ConcurrentHashMap<String, LinkedTransferQueue<String>> map;

    public MessageQueue() {
        super();
        map = new ConcurrentHashMap<String, LinkedTransferQueue<String>>(); 
    }

    public void put(String whom, String message) {
        if(!map.containsKey(whom)) {
            map.put(whom, new LinkedTransferQueue<String>()); 
        }
        map.get(whom).put(message);
    }

    public String take(String whom) throws InterruptedException {
        if(!map.containsKey(whom)) {
            map.put(whom, new LinkedTransferQueue<String>()); 
        }
        return map.get(whom).take();
    }

    public int numberOfConnections() {
        int i = 0;
        for(Map.Entry<String, LinkedTransferQueue<String>> entry : map.entrySet()) {
            i += entry.getValue().getWaitingConsumerCount();
        }
        return i; 
    }
}