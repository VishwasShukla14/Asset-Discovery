package com.serviceops.assetdiscovery.controller;

import com.serviceops.assetdiscovery.rest.BiosRest;
import com.serviceops.assetdiscovery.service.impl.BiosServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class BiosController {

    private final BiosServiceImpl biosService;
    Logger logger = LoggerFactory.getLogger(BiosController.class);

    public BiosController(BiosServiceImpl biosService) {
        this.biosService = biosService;
    }

    @GetMapping("/bios/{redId}")
    public List<BiosRest> getBios(@PathVariable("redId") long refId){

        List<BiosRest> biosRests = new ArrayList<>();

        biosRests.add(biosService.findByRefId(refId));

        logger.debug("Fetching Bios with Asset id -> {}",refId);

        return biosRests;
    }

    @DeleteMapping("/bios/{refId}")
    public void deleteBios(@PathVariable("refId") long refId){

        logger.debug("Deleting Bios with Asset id -> {}",refId);

        biosService.deleteByRefId(refId);

    }

    @PutMapping("/bios/{refId}")
    public void updateBios(@PathVariable("refId") long refId,@RequestBody BiosRest biosRest){

        logger.debug("Updating Bios with Asset id -> {}",refId);

        biosService.update(biosRest);

    }


}