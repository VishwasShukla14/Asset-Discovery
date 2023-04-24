package com.serviceops.assetdiscovery.service.impl;

import com.serviceops.assetdiscovery.config.SchedulerService;
import com.serviceops.assetdiscovery.entity.Schedulers;
import com.serviceops.assetdiscovery.exception.ComponentNotFoundException;
import com.serviceops.assetdiscovery.repository.CustomRepository;
import com.serviceops.assetdiscovery.rest.SchedulerRest;
import com.serviceops.assetdiscovery.service.interfaces.SchedulersService;
import com.serviceops.assetdiscovery.utils.mapper.SchedulerOps;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchedulerServiceImpl implements SchedulersService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);
    private final CustomRepository customRepository;
    private final SchedulerService schedulerService;
    private final SchedulerOps schedulerOps;

    public SchedulerServiceImpl(CustomRepository customRepository, SchedulerService schedulerService) {
        this.customRepository = customRepository;
        this.schedulerService = schedulerService;
        schedulerOps = new SchedulerOps();
    }

    @Override
    public SchedulerRest save(long networkScanId, SchedulerRest schedulerRest) {
        Optional<Schedulers> fetchScheduler =
                customRepository.findByColumn("networkScanId", networkScanId, Schedulers.class);
        if (fetchScheduler.isPresent()) {
            logger.error("Scheduler already exists with networkScanId -> {} ", networkScanId);
            throw new ComponentNotFoundException("Scheduler", "networkScanId", networkScanId);
        } else {
            Schedulers schedulers = new Schedulers();
            schedulerRest.setNetworkScanRestId(networkScanId);
            schedulers = schedulerOps.restToEntity(schedulers, schedulerRest);
            customRepository.save(schedulers);
            logger.info("Created scheduler with id ->{}", schedulers.getId());
            try {
                schedulerRest.setId(schedulers.getId());
                schedulerService.scheduleJob(schedulerRest);
            } catch (SchedulerException e) {
                logger.error("fail to add scheduler for id ->{}", schedulerRest.getId());
            }

            return schedulerOps.entityToRest(schedulers, schedulerRest);
        }

    }


    public SchedulerRest update(long networkScanId, SchedulerRest schedulerRest) {
        Optional<Schedulers> fetchSchedulers =
                customRepository.findByColumn("networkScanId", networkScanId, Schedulers.class);
        if (fetchSchedulers.isEmpty()) {
            logger.error("Scheduler not exist with networkScanId -> {} ", networkScanId);
            throw new ComponentNotFoundException("Scheduler", "networkScanId", networkScanId);
        } else {
            Schedulers schedulers = fetchSchedulers.get();
            schedulers = schedulerOps.restToEntity(schedulers, schedulerRest);
            customRepository.save(schedulers);
            schedulerRest.setId(schedulers.getId());
            schedulerService.updateTrigger(schedulerRest);
            return schedulerOps.entityToRest(schedulers, schedulerRest);
        }

    }

    @Override
    public SchedulerRest findByNetworkScanId(long networkScanId) {
        Optional<Schedulers> fetchScheduler =
                customRepository.findByColumn("networkScanId", networkScanId, Schedulers.class);
        if (fetchScheduler.isPresent()) {
            SchedulerRest schedulerRest = new SchedulerRest();
            schedulerRest = schedulerOps.entityToRest(fetchScheduler.get(), schedulerRest);
            logger.info("found scheduler with networkScanId -> {}", schedulerRest.getNetworkScanRestId());
            return schedulerRest;
        } else {
            logger.error("Scheduler not exist with networkScanId -> {} ", networkScanId);
            throw new ComponentNotFoundException("Scheduler", "networkScanId", networkScanId);
        }
    }

    @Override
    public List<SchedulerRest> findAll() {
        List<Schedulers> schedulersList = customRepository.findAll(Schedulers.class);
        return schedulersList.stream().map(e -> schedulerOps.entityToRest(e,new SchedulerRest()))
                .toList();
    }

    @Override
    public boolean deleteByNetworkScanId(long networkScanId) {
        Optional<Schedulers> scheduler =
                customRepository.findByColumn("networkScanId", networkScanId, Schedulers.class);
        if (scheduler.isPresent()) {
            schedulerService.deleteTimer(String.valueOf(scheduler.get().getId()));
            logger.info("Scheduler deleted with networkScanId -> {} ", networkScanId);
            return customRepository.deleteById(Schedulers.class, networkScanId, "networkScanId");
        } else {
            logger.info("Scheduler not exist  with networkScanId -> {}", networkScanId);
            return false;
        }
    }
}
