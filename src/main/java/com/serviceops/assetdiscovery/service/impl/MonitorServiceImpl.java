package com.serviceops.assetdiscovery.service.impl;
import com.serviceops.assetdiscovery.entity.ComputerSystem;
import com.serviceops.assetdiscovery.entity.Monitor;
import com.serviceops.assetdiscovery.exception.ResourceNotFoundException;
import com.serviceops.assetdiscovery.repository.CustomRepository;
import com.serviceops.assetdiscovery.rest.MonitorRest;
import com.serviceops.assetdiscovery.service.interfaces.MonitorService;
import com.serviceops.assetdiscovery.utils.LinuxCommandExecutorManager;
import com.serviceops.assetdiscovery.utils.mapper.MonitorOps;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class MonitorServiceImpl implements MonitorService{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CustomRepository customRepository;
    public MonitorServiceImpl(CustomRepository customRepository){
        this.customRepository = customRepository;
        setCommands();
    }

    @Override
    @Transactional
    public void save(Long id) {
        String[][] parsedResults = parseResults();
        List<Monitor> monitors = customRepository.findAllByColumnName(Monitor.class,"refId",id);
        logger.info(String.valueOf(parsedResults.length));
        if(!monitors.isEmpty()){
            if(monitors.size()==parsedResults.length){
                for(Monitor monitor: monitors){
                  for(String[] updateMonitor : parsedResults){
                      monitor.setDescription(updateMonitor[1]);
                      monitor.setManufacturer(updateMonitor[2]);
                      customRepository.save(monitor);
                  }
                }
            }else{
                for(Monitor monitor: monitors){
                    customRepository.deleteById(Monitor.class,monitor.getId(),"id");
                }
                for(String[] updateMonitor : parsedResults){
                    Monitor monitor = new Monitor();
                    monitor.setRefId(id);
                    monitor.setDescription(updateMonitor[1]);
                    monitor.setManufacturer(updateMonitor[2]);
                    customRepository.save(monitor);
                }
            }
        }else{
            for(String[] updateMonitor : parsedResults){
                Monitor monitor = new Monitor();
                monitor.setRefId(id);
                monitor.setDescription(updateMonitor[1]);
                monitor.setManufacturer(updateMonitor[2]);
                customRepository.save(monitor);
            }
        }

        }

    @Override
    public void update(MonitorRest monitorRest){
        MonitorOps monitorOps = new MonitorOps(new Monitor(),monitorRest);
        logger.info("Updated added field in monitor -> {}",monitorRest.getId());
        customRepository.save(monitorOps.restToEntity(monitorRest));
    }

    @Transactional
    @Override
    public void deleteById(Long id){
        logger.info("Deleting Monitor with id  -> {}",id);
        customRepository.deleteById(Monitor.class,id,"id");
    }

    @Override
    public List<MonitorRest> get(Long id){
        List<Monitor> monitorsList = customRepository.findAllByColumnName(Monitor.class,"refId",id);
        if(!monitorsList.isEmpty()){
            List<MonitorRest> monitorRestList = new ArrayList<>();
            for(Monitor monitor : monitorsList) {
                MonitorOps monitorOps = new MonitorOps(monitor,new MonitorRest());
                monitorRestList.add(monitorOps.entityToRest(monitor));
                logger.info("Retrieving Monitor of refId -> {}",id);
            }
            return monitorRestList;
        }else{
            logger.info("Monitor Component of refId ->{} does not exist",id);
            throw new ResourceNotFoundException("Monitors","refId",String.valueOf(id));
        }

    }

    private void setCommands() {
        LinkedHashMap<String, String[]> commands = new LinkedHashMap<>();
        //command for getting number of monitors by counting number of decription
        commands.put("sudo lshw -c display | grep description | wc -l",new String[]{});

        //command for getting description of monitor
        commands.put("sudo lshw -c display | grep description",new String[]{});

   //     commands.put("sudo lshw -c display | grep description | sed 's/^.*description: *//' | sed 's/ *$//'",new String[]{});

        //command for getting manufacturer of monitor
        commands.put("sudo lshw -c display | grep vendor",new String[]{});
        //commands.put("sudo lshw -c display | grep vendor | sed 's/^.*vendor: *//' | sed 's/ *$//'",new String[]{});

        //command for getting screen height of monitor
        commands.put("sudo xdpyinfo |grep dimensions | grep -oP '\\(\\K[0-9]+'",new String[]{});

        //command for getting screen width of monitor
        commands.put("sudo xdpyinfo | grep dimensions | grep -oP '\\(\\K[^)]+' | cut -d'x' -f2 | grep -oE '[0-9]+'",new String[]{});

        // Adding all the commands to the Main HasMap where the class Asset is the key for all the commands
        LinuxCommandExecutorManager.add(Monitor.class, commands);
    }
    private String[][] parseResults(){
        Map<String, String[]> commandResults = LinuxCommandExecutorManager.get(Monitor.class);
        String[] numberOfMonitors = commandResults.get("sudo lshw -c display | grep description | wc -l");
        if(numberOfMonitors.length==0){
            return new String[][]{};
        }
        else{
        Pattern pattern = Pattern.compile("(?<=\\s|^)\\d+(?=\\s|$)");

        int numberOfMonitor = 0;
        for(int i=0;i<numberOfMonitors.length;i++) {
            Matcher matcher = pattern.matcher(numberOfMonitors[0]);
            if (matcher.find()) {
                String number = matcher.group();
                numberOfMonitor = Integer.parseInt(number);
            }
        }
        String[][] parsedResult = new String[numberOfMonitor][commandResults.size()];
        int j=0;
        int count=1;
        for(Map.Entry<String,String[]> commandResult : commandResults.entrySet()){
            if(j==0){
                j++;
                continue;
            }
            String[] result = commandResult.getValue();
           for(int i=0;i<result.length;i++){
               String results = result[i];
               switch(count) {
                   case 1:
                       if (results.contains("description:")) {
                           parsedResult[i][j] = results.substring(results.indexOf("description:") + "description:".length());
                           break;
                       }
                   case 2:
                       if (results.contains("vendor")) {
                           parsedResult[i][j] = results.substring(results.indexOf("vendor:") + "vendor:".length());
                           break;
                       }
                   case 3:
                       if (results.contains("unable to open")) {
                           parsedResult[i][j] = "Not Found";
                           break;
                       } else {
                           parsedResult[i][j] = result[i];
                       }
                   case 4:
                       if (results.contains("unable to open")) {
                           parsedResult[i][j] = "Not Found";
                           break;
                       } else {
                           parsedResult[i][j] = result[i];
                       }
                   case 5: continue;
               }
               count++;
               j++;
           }
           }
            return parsedResult;
        }

    }
//    public void saves(Long id){
//        List<String> parse = getParseResult();
//        for(String i:parse){
//            System.out.println(i);
//        }
//        Monitor monitor = new Monitor();
//        monitor.setRefId(id);
//        monitor.setDescription(parse.get(0));
//        monitor.setManufacturer(parse.get(1));
//        customRepository.save(monitor);
//    }

    //    private List<String> getParseResult() {
//        Map<String, String[]> commandResults = LinuxCommandExecutorManager.get(Monitor.class);
//        List<String> parsedResults = new ArrayList<>();
//        int count =0;
//        for (Map.Entry<String, String[]> commandResult : commandResults.entrySet()) {
//            String[] results = commandResult.getValue();
//            for(String i :results) {
//                switch (count) {
//                    case 1:
//                        if (i.contains("description:")) {
//                            parsedResults.add(i.substring(i.indexOf("description:") + "description:".length()));
//                            break;
//                        }
//                    case 2:
//                        if (i.contains("vendor")) {
//                            parsedResults.add(i.substring(i.indexOf("vendor:") + "vendor:".length()));
//                            break;
//                        }
//                    case 3:
//                        if (i.contains("unable to open")) {
//                            parsedResults.add("Not Found");
//                            break;
//                        } else {
//                            parsedResults.add(i);
//                        }
//                    case 4:
//                        if (i.contains("unable to open")) {
//                            parsedResults.add("Not Found");
//                            break;
//                        } else {
//                            parsedResults.add(i);
//                        }
//                    case 5: continue;
//                }
//                count++;
//            }
//        }
//        return parsedResults;
//    }


}