package com.serviceops.assetdiscovery.service.impl;

import com.serviceops.assetdiscovery.entity.NetworkScan;
import com.serviceops.assetdiscovery.entity.enums.IpRangeType;
import com.serviceops.assetdiscovery.exception.AssetDiscoveryApiException;
import com.serviceops.assetdiscovery.exception.ComponentNotFoundException;
import com.serviceops.assetdiscovery.exception.ResourceAlreadyExistsException;
import com.serviceops.assetdiscovery.repository.CustomRepository;
import com.serviceops.assetdiscovery.repository.PersistToDB;
import com.serviceops.assetdiscovery.rest.CredentialsRest;
import com.serviceops.assetdiscovery.rest.NetworkScanRest;
import com.serviceops.assetdiscovery.service.interfaces.CredentialsService;
import com.serviceops.assetdiscovery.service.interfaces.NetworkScanService;
import com.serviceops.assetdiscovery.utils.LinuxCommandExecutorManager;
import com.serviceops.assetdiscovery.utils.mapper.NetworkScanOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

@Service
public class NetworkScanServiceImpl implements NetworkScanService {
    private static final Logger logger = LoggerFactory.getLogger(NetworkScanServiceImpl.class);
    private final CustomRepository customRepository;
    private final CredentialsService credentialsService;
    private final PersistToDB persistToDB;
    private final NetworkScanOps networkScanOps;

    public NetworkScanServiceImpl(CustomRepository customRepository, CredentialsService credentialsService,
            PersistToDB persistToDB) {
        this.customRepository = customRepository;
        this.credentialsService = credentialsService;
        this.persistToDB = persistToDB;
        networkScanOps = new NetworkScanOps();

    }

    @Override
    public void scan(long id) {
        Optional<NetworkScan> fetchNetworkScan = customRepository.findByColumn("id", id, NetworkScan.class);
        if (fetchNetworkScan.isPresent()) {
            NetworkScan networkScan = fetchNetworkScan.get();
            NetworkScanRest networkScanRest = new NetworkScanRest();
            networkScanRest = networkScanOps.entityToRest(networkScan, networkScanRest);
            if (networkScan.getIpRangeType() == IpRangeType.ENTIRE_NETWORK) {
                performScanOnCredentialEntireNetwork(networkScanRest);
            } else {
                performScanOnCredentialSpecificSetOfIp(networkScanRest);
            }
        } else {
            logger.error("Network scan not found with id ->{} ", id);
            throw new ComponentNotFoundException("NetworkScanJob", "id", id);
        }
    }

    @Override
    public NetworkScanRest save(NetworkScanRest networkScanRest) {
        Optional<NetworkScan> fetchNetworkScan =
                customRepository.findByColumn("id", networkScanRest.getId(), NetworkScan.class);
        if (fetchNetworkScan.isPresent()) {
            logger.info("NetworkScan already exists by id -> {}", fetchNetworkScan.get().getId());
            throw new ResourceAlreadyExistsException("NetworkScan", "id",
                    String.valueOf(networkScanRest.getId()));
        } else {
            NetworkScan networkScan = new NetworkScan();
            networkScanRest.setEnabled(true);
             networkScan = customRepository.save(networkScanOps.restToEntity(networkScan, networkScanRest));
            logger.info("NetworkScan saved by id -> {}", networkScanRest.getId());
            return networkScanOps.entityToRest(networkScan,networkScanRest);
        }

    }

    @Override
    public NetworkScanRest findById(long id) {
        Optional<NetworkScan> fetchNetworkScan = customRepository.findByColumn("id", id, NetworkScan.class);
        if (fetchNetworkScan.isPresent()) {
            NetworkScan networkScan = fetchNetworkScan.get();
            logger.info("NetworkScan found by id -> {}", id);
            return networkScanOps.entityToRest(networkScan, new NetworkScanRest());
        } else {
            logger.error("NetworkScanJob not found by id -> {}", id);
            throw new ComponentNotFoundException("NetworkScan", "id", id);
        }
    }

    @Override
    public List<NetworkScanRest> findAll() {
        List<NetworkScan> list = customRepository.findAll(NetworkScan.class);
        if (list.isEmpty()) {
            return List.of();
        }
        logger.info("network scan find all fetched");
        return list.stream().map(n -> networkScanOps.entityToRest(n, new NetworkScanRest())).toList();
    }

    @Override
    public NetworkScanRest updateById(long id, NetworkScanRest networkScanRest) {
        Optional<NetworkScan> fetchNetworkScan = customRepository.findByColumn("id", id, NetworkScan.class);
        if (fetchNetworkScan.isEmpty()) {
            logger.error("networkScan not exist with id ->{}", id);
            throw new ComponentNotFoundException("NetworkScanJob", "id", id);
        } else {
            NetworkScan networkScan = fetchNetworkScan.get();
            networkScanRest.setUpdateById(
                    Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
            networkScan = networkScanOps.restToEntity(networkScan, networkScanRest);
            customRepository.save(networkScan);
            logger.info("network scan updated -> {}", networkScan.getId());
            return networkScanOps.entityToRest(networkScan, networkScanRest);
        }
    }

    @Override
    public boolean deleteById(long id) {
        boolean isDeleted = customRepository.deleteById(NetworkScan.class, id, "id");
        if(isDeleted){
            logger.info("network scan delete with id -> {}", id);
            return true;
        }else{
            logger.info("network scan not delete with id -> {}", id);
            return false;
        }

    }

    private void performScanOnCredentialEntireNetwork(NetworkScanRest networkScanRest) {
        String networkAddress = getNetworkAddress(networkScanRest.getIpRangeStart());
        List<Long> credentialsIds = networkScanRest.getCredentialId();
        List<CredentialsRest> credentialsList =
                credentialsIds.stream().map(credentialsService::findById).toList();
        int thirdOctet = Integer.parseInt(networkScanRest.getIpRangeStart().split("\\.")[2]);
        int fourthOctet = Integer.parseInt(networkScanRest.getIpRangeStart().split("\\.")[3]);
        for (int i = thirdOctet; i <= 255; i++) {
            for (int j = fourthOctet; j <= 255; j++) {
                String ipAddress = networkAddress + i + "." + j;
                if (pingIp(ipAddress)) {
                    for (CredentialsRest credential : credentialsList) {
                        logger.debug("trying to connect using ->{}", credential.getUsername());
                        if (fetch(ipAddress, credential.getUsername(), credential.getPassword())) {
                            logger.debug("fetch completed using ->{}", credential.getUsername());
                            break;
                        }
                    }
                } else {
                    logger.error("Could not ping ->{}", ipAddress);
                }
            }
            fourthOctet = 0;
        }
    }

    private void performScanOnCredentialSpecificSetOfIp(NetworkScanRest networkScanRest) {
        List<String> ipAddressList = networkScanRest.getIpList();
        List<Long> credentialsIds = networkScanRest.getCredentialId();
        List<CredentialsRest> credentialsList =
                credentialsIds.stream().map(credentialsService::findById).toList();
        for (String ip : ipAddressList) {
            for (CredentialsRest credential : credentialsList) {
                logger.debug("trying to connect using ->{}", credential.getUsername());
                if (fetch(ip, credential.getUsername(), credential.getPassword())) {
                    logger.debug("fetch completed using ->{}", credential.getUsername());
                    persistToDB.saveToDB();
                    break;
                }
            }
        }

    }

    private boolean fetch(String ipAddress, String username, String password) {
        try {

            logger.debug("Scanning -> {} ", ipAddress);
            new LinuxCommandExecutorManager(ipAddress, username, password, 22).fetch();
            return true;
        } catch (AssetDiscoveryApiException e) {
            logger.error("Network Scanner Error for host-> {}", ipAddress);
            return false;
        }

    }

    private boolean pingIp(String ipAddress) {

        InetAddress inet = null;
        try {
            inet = InetAddress.getByName(ipAddress);
            return inet.isReachable(1000);
        } catch (UnknownHostException e) {
            logger.warn("unknown host ->{} with message ->{}", inet, e.getMessage());
        } catch (IOException e) {
            logger.warn("fail to ping ->{}'", e.getMessage());
        }

        return false;
    }


    private String getNetworkAddress(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        if (octets.length != 4) {
            return null;
        }
        return octets[0] + "." + octets[1] + ".";
    }

}
