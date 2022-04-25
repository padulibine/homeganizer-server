package com.homeganizer.serveur.services;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.homeganizer.serveur.dao.ComponentRepository;
import com.homeganizer.serveur.models.Component;
import com.homeganizer.serveur.models.Container;
import com.homeganizer.serveur.models.Element;

import org.springframework.stereotype.Service;

@Service
public class ComponentService {

  ArrayList<Component> objects = new ArrayList<>();
  ComponentRepository component_repository = new ComponentRepository();

  public ArrayList<Component> getAllComponentsByPath(String name, Integer id_collection)
      throws URISyntaxException, SQLException {

    return component_repository.getComponents(name, id_collection);
    /*
     * Container maison = new Container("maison", "maison de charly", "charly");
     * Container salon = new Container("salon", "salon", "charly");
     * maison.addObject(salon);
     * Container armoire = new Container("armoire", "armoire du salon", "charly");
     * salon.addObject(armoire);
     * Element e1 = new Element("table", "table à manger", "charly");
     * Element e2 = new Element("couverts", "couteaux & fourchettes", "charly");
     * salon.addObject(e1);
     * armoire.addObject(e2);
     * 
     * objects.add(maison);
     * Element voiture = new Element("voiture", "mercedes", "charly");
     * 
     * objects.add(voiture);
     */

  }

  public ArrayList<Component> getAllComponents() throws URISyntaxException, SQLException {
    return component_repository.getComponents("home", 1);
  }

  public void updateComponent(String name, String desc, String path) {

  }

  public void addComponent(String name, String desc, String parent, Boolean container, String tag,
      Integer id_collection)
      throws SQLException {
    if (container == null) {
      container = false;
    }
    component_repository.addComponent(new Component(name, desc, tag), container, parent, id_collection);
  }

  public void deleteComponent(String name, Integer id_collection) throws Exception {
    component_repository.deleteComponent(name, id_collection);
  }

  public ArrayList<String> getAllTags(Integer id_collection) throws SQLException {
    return component_repository.getAllTags(id_collection);
  }

  public ArrayList<Component> getAllComponentsbyTag(String tag, Integer id_collection) throws SQLException {
    return component_repository.getAllComponentsbyTag(tag, id_collection);
  }

  public ArrayList<String> getAllCollections(Integer id_user) throws SQLException {
    return component_repository.getAllCollections(id_user);
  }

  public ArrayList<Component> getAllComponents(Integer id_collection) throws URISyntaxException, SQLException {
    return component_repository.getComponents("home", id_collection);
  }

  public String getCollectionToken(String collection_name) throws SQLException {
    return component_repository.getCollectionToken(collection_name);
  }

  public void createCollection(String collection_name, String collection_token, Integer id_user) throws SQLException {
    component_repository.createCollection(collection_name, collection_token, id_user);
  }

}
