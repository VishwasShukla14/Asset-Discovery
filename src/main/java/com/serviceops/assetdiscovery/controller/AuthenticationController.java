package com.serviceops.assetdiscovery.controller;

import com.serviceops.assetdiscovery.rest.AuthenticationResponse;
import com.serviceops.assetdiscovery.rest.UsersRest;
import com.serviceops.assetdiscovery.service.interfaces.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final UsersService authenticationService;

    public AuthenticationController(UsersService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UsersRest request) {
        logger.debug("register request for email -> {}", request.getEmail());
        return new ResponseEntity<>(authenticationService.register(request), HttpStatus.CREATED);
    }

    @PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody UsersRest request) {
        logger.debug("login request for email -> {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

}
