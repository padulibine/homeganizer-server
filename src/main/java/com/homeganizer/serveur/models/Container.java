package com.homeganizer.serveur.models;

import java.util.ArrayList;

public class Container extends Component {

  private ArrayList<Component> objects;

  public Container() {
  }

  public Container(String name, String description, String tag) {
    super(name, description, tag);
    this.objects = new ArrayList<>();
  }

  public Container(String name, String description, String tag, String path) {
    super(name, description, tag, path);
    this.objects = new ArrayList<>();
  }

  public void addObject(Component c) {
    c.setPath(this.path + this.name + "/");
    objects.add(c);
  }

  public void addObjects(ArrayList<Component> childs) {
    for (Component c : childs) {
      this.addObject(c);
    }
  }

  public void rmObject(Component c) {
    this.objects.remove(c);

  }

  public ArrayList<Component> getObjects() {
    return this.objects;
  }
}
