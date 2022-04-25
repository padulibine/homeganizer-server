package com.homeganizer.serveur.controllers;

import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.homeganizer.serveur.services.ComponentService;
import com.homeganizer.serveur.services.UserService;
import com.homeganizer.serveur.models.Component;
import com.homeganizer.serveur.models.Container;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@RestController
public class ComponentController {

  private static final Logger log = LoggerFactory.getLogger(ComponentController.class);

  @Autowired
  public ComponentService component_service;

  @Autowired
  public UserService user_service;

  @GetMapping(value = "/")
  public String getHome(Model model) {
    return "Bienvenue sur homeganizer";

  }

  @GetMapping(value = "/collections")
  public ArrayList<String> getAllCollections(
      @RequestHeader("Authorization") String token) throws URISyntaxException,
      SQLException {
    Integer id_user = user_service.getUserId(token);
    return component_service.getAllCollections(id_user);
  }

  @PostMapping(value = "/collections/{collection_name}")
  public ResponseEntity<String> createCollection(
      @RequestHeader("Authorization") String token,
      @PathVariable String collection_name) throws URISyntaxException,
      SQLException {
    try {
      UserController user_controller = new UserController();
      Integer id_user = user_service.getUserId(token);
      String collection_token = user_controller.getJWTToken(collection_name);
      component_service.createCollection(collection_name, collection_token, id_user);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/collections/{collection_name}")
  public ResponseEntity<String> getCollectionToken(
      @RequestHeader("Authorization") String token,
      @PathVariable String collection_name) throws URISyntaxException,
      SQLException {
    try {
      Integer id_user = user_service.getUserId(token);
      user_service.verifyUserCollection(id_user, collection_name);
      return new ResponseEntity<>(component_service.getCollectionToken(collection_name), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(value = "/collections/{collection_token}")
  public ResponseEntity putAuthorizationForCollection(
      @RequestHeader("Authorization") String token,
      @PathVariable String collection_token) throws URISyntaxException,
      SQLException {
    try {
      Integer id_user = user_service.getUserId(token);
      user_service.putAuthorizationForCollection(id_user, collection_token);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping(value = "/components")
  public ArrayList<Component> getAllComponentsByPath() throws URISyntaxException, SQLException {
    return component_service.getAllComponents();
  }

  @GetMapping(value = "/collections/{collection_name}/components")
  public ResponseEntity<ArrayList<Component>> getAllComponentsByCollection(
      @RequestHeader("Authorization") String token,
      @PathVariable String collection_name) throws URISyntaxException, SQLException {
    try {
      Integer id_user = user_service.getUserId(token);
      Integer id_collection = user_service.verifyUserCollection(id_user, collection_name);
      return new ResponseEntity<>(component_service.getAllComponents(id_collection), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping(value = "/collections/{collection_name}/components/{name}")
  public ResponseEntity<ArrayList<Component>> getAllComponentsByPathByCollection(
      @RequestHeader("Authorization") String token,
      @PathVariable String collection_name,
      @PathVariable String name) throws URISyntaxException, SQLException {
    try {
      Integer id_user = user_service.getUserId(token);
      Integer id_collection = user_service.verifyUserCollection(id_user, collection_name);
      return new ResponseEntity<>(component_service.getAllComponentsByPath(name, id_collection), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping(value = "/components/{name}")
  public ArrayList<Component> getAllComponentsByPath(
      @PathVariable String name) throws URISyntaxException, SQLException {
    return component_service.getAllComponentsByPath(name, 1);
  }

  @PostMapping(value = "/collections/{collection_name}/components/{name}")
  public ResponseEntity addComponent(@RequestHeader("Authorization") String token,
      @PathVariable String collection_name, @PathVariable String name, @RequestParam(name = "desc") String desc,
      @RequestParam(name = "parent", defaultValue = "") String parent,
      @RequestParam(name = "container") Boolean container,
      @RequestParam(name = "tag", defaultValue = "") String tag) throws SQLException {
    try {
      Integer id_user = user_service.getUserId(token);
      Integer id_collection = user_service.verifyUserCollection(id_user, collection_name);
      component_service.addComponent(name, desc, parent, container, tag, id_collection);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @DeleteMapping(value = "/collections/{collection_name}/components/{name}")
  public ResponseEntity deleteComponent(@RequestHeader("Authorization") String token,
      @PathVariable String collection_name, @PathVariable String name) throws Exception {
    try {
      Integer id_user = user_service.getUserId(token);
      Integer id_collection = user_service.verifyUserCollection(id_user, collection_name);
      component_service.deleteComponent(name, id_collection);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @PutMapping(value = "/components/{name}")
  public void updateComponent(@PathVariable String name, @RequestParam(name = "desc") String desc,
      @RequestParam(name = "path") String path) {
    component_service.updateComponent(name, desc, path);
  }

  @GetMapping(value = "/collections/{collection_name}/components/tags")
  public ResponseEntity<ArrayList<String>> getAllTags(@RequestHeader("Authorization") String token,
      @PathVariable String collection_name) throws SQLException {

    try {
      Integer id_user = user_service.getUserId(token);
      Integer id_collection = user_service.verifyUserCollection(id_user, collection_name);
      return new ResponseEntity<>(component_service.getAllTags(id_collection), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping(value = "/components/tags/{tag}")
  public ResponseEntity<ArrayList<Component>> getAllComponentsbyTag(@RequestHeader("Authorization") String token,
      @PathVariable String collection_name, @PathVariable String tag) throws SQLException {
    try {
      Integer id_user = user_service.getUserId(token);
      Integer id_collection = user_service.verifyUserCollection(id_user, collection_name);
      return new ResponseEntity<>(component_service.getAllComponentsbyTag(tag, id_collection), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }
}