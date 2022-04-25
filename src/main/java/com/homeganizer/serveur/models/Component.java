package com.homeganizer.serveur.models;

import java.util.ArrayList;

public class Component {

  String name;
  String description;
  String path;
  String tag;

  public Component() {
  }

  public Component(String name, String description, String tag) {
    this.name = name;
    this.description = description;
    this.path = "/";
    this.tag = tag;

  }

  public Component(String name, String description, String tag, String path) {
    this.name = name;
    this.description = description;
    this.path = path;
    this.tag = tag;
  }

  public String getPath() {
    return this.path;
  }

  protected void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTag() {
    return this.tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }


}
