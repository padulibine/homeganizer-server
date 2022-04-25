package com.homeganizer.serveur.controllers;

import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homeganizer.serveur.models.User;
import com.homeganizer.serveur.services.UserService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
public class UserController {

    @Autowired
    public UserService user_service;

    @PostMapping("users/signup")
    public ResponseEntity<String> signup(@RequestParam("user") String username, @RequestParam("password") String pwd)
            throws SQLException {

        String token = "Bearer " + getJWTToken(username);
        User user = new User();
        user.setUser(username);
        user.setToken(token);
        user.setPwd(pwd);
        user_service.signup(user);

        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("users/signin")
    public ResponseEntity<String> signin(@RequestParam("user") String username, @RequestParam("password") String pwd) {

        try {
            String token = user_service.verifyUser(username, pwd);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    public String getJWTToken(String username) {
        String secretKey = "mySecretKey";
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_USER");

        String token = Jwts
                .builder()
                .setId("softtekJWT")
                .setSubject(username)
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS512,
                        secretKey.getBytes())
                .compact();

        return token;
    }
}