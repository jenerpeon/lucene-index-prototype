package uni.lars;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uni on 8/9/16.
 */
public class Measure {
    private Map<Long, Long> takes;
    private long stamp;
    private String label;

    private boolean lock = false;


    public Measure(String label){
        takes = new HashMap<Long, Long>();
        this.label = label;
    }

    public void start(){
        this.stamp = System.currentTimeMillis();
    }

    public String end(){
        takes.put(stamp, System.currentTimeMillis());
        System.out.println("measured in milliseconds: "+label+" took: "+results());
        return results();
    }

    private String results(){
        long sum = 0;
        for(long take: takes.keySet()){
            sum += takes.get(take)-take;
        }
        return new Long(sum).toString();
    }

    public void hold(){
        takes.put(stamp, System.currentTimeMillis());
        lock = true;
    }
    public void unhold(){
        if(lock) {
            stamp= System.currentTimeMillis();
            lock = false;
        }else
            System.out.println("is not on hold. Ignored...");
    }

    public boolean isHold(){
        return lock;
    }
}
