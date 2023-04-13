package com.serviceops.assetdiscovery.controller;

import com.serviceops.assetdiscovery.rest.PhysicalDiskRest;
import com.serviceops.assetdiscovery.service.interfaces.PhysicalDiskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{refId}/physicalDisk")
public class PhysicalDiskController {
    private final PhysicalDiskService physicalDiskService;
    private final Logger logger = LoggerFactory.getLogger(PhysicalDiskController.class);
    public PhysicalDiskController(PhysicalDiskService physicalDiskService) {
        this.physicalDiskService = physicalDiskService;
    }

    @GetMapping
    public void get(@PathVariable("refId") Long id) {
        logger.info("Saving physical disk with id: " + id);
        physicalDiskService.findByRefId(id);
    }

    @DeleteMapping
    public void delete(@PathVariable("refId") Long id) {
        logger.info("Deleting physical disk with id: " + id);
        physicalDiskService.delete(id);
    }

    @PutMapping
    public void update(@PathVariable("refId") Long id, @RequestBody PhysicalDiskRest physicalDiskRest){
        logger.info("Updating physical disk with id: " + id);
        physicalDiskService.update(id,physicalDiskRest);
    }
}
