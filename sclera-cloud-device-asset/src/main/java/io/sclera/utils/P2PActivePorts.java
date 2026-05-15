
//package io.sclera.utils;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class P2PActivePorts {
//	
//	@Autowired
//	private Utils utils;
//	
//    private List<Map<String,Integer>> active_port = new ArrayList<>();
//
//    public void addPort(int destination_port,int port){
//        Map<String,Integer> integerMap = new HashMap<>();
//        integerMap.put("destination_port",destination_port);
//        integerMap.put("port",port);
//        active_port.add(integerMap);
//    }
//
//    public void removePort(int port){
//        for(int i=0;i<active_port.size();i++){
//            if(active_port.get(i).get("port")==port){
//                active_port.remove(i);
//            }
//        }
//    }
//
//    public Map<String, Integer> getPort(int port){
//        if(active_port.size()>0){
//            for (Map<String, Integer> stringIntegerMap : active_port) {
//                if (stringIntegerMap.get("port") == port) {
//                	var isProcessRunning = utils.execPipedCmd(new String[] {"bash","-c","lsof -t -i:"+stringIntegerMap.get("destination_port")});
//                	if(isProcessRunning.get("status").equals("true")) {
//                		if(isProcessRunning.get("output").length()<1) {
//                			System.out.println("PROCESS IS RUNNING IN THIS PORT");
//                    		this.removePort(stringIntegerMap.get("port"));
//                    		utils.execPipedCmd(new String[] {"bash","-c","kill -9 $(lsof -t -i:"+stringIntegerMap.get("port")+")"});
//                    		return null;
//                    	} else {
//                    		var cmd1 = utils.execPipedCmd(new String[]{"bash","-c","kill -9 $(lsof -t -i:"+stringIntegerMap.get("destination_port")+")"}).get("status");
//                            var cmd2 = utils.execPipedCmd(new String[]{"bash","-c","kill -9 $(lsof -t -i:"+port+")"}).get("status");
//                            this.removePort(stringIntegerMap.get("port"));
//                    		return null;
//                    	}
//                	}    
//                }
//            }
//            return null;
//        }else {
//            return null;
//        }
//    }
//
//}




















// NIKHIL NEW VERSION


package io.sclera.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class P2PActivePorts {

    @Autowired
    private Utils utils;

    private final ConcurrentHashMap<Integer, Integer> activePorts = new ConcurrentHashMap<Integer, Integer>();

    public void addPort(int destination_port, int port) {
        activePorts.put(port, destination_port);
    }

    public void removePort(int port) {
        activePorts.remove(port);
    }

    public Integer getPort(int port) {
        var currentPort = activePorts.get(port);
        var isProcessRunning = utils.execPipedCmd(new String[]{"bash", "-c", "lsof -t -i:" + currentPort});
        if (isProcessRunning.get("output").length() < 1) {
            this.removePort(port);
            utils.execPipedCmd(new String[]{"bash", "-c", "kill -9 $(lsof -t -i:" + port + ")"});
            return null;
        } else {
            utils.execPipedCmd(new String[]{"bash", "-c", "kill -9 $(lsof -t -i:" + currentPort + ")"}).get("status");
            utils.execPipedCmd(new String[]{"bash", "-c", "kill -9 $(lsof -t -i:" + port + ")"}).get("status");
            this.removePort(port);
            return null;
        }
    }

}




























































