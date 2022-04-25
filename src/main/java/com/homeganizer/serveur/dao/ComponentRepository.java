package com.homeganizer.serveur.dao;

import java.sql.Statement;
import java.lang.Thread.State;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.homeganizer.serveur.models.Component;
import com.homeganizer.serveur.models.Container;
import com.homeganizer.serveur.models.Element;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ComponentRepository {

  private static final Logger log = LoggerFactory.getLogger(ComponentRepository.class);

  private static Connection getConnection() throws URISyntaxException, SQLException {
    try {
      URI dbUri = new URI(System.getenv("DATABASE_URL"));

      String username = dbUri.getUserInfo().split(":")[0];
      String password = dbUri.getUserInfo().split(":")[1];
      String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
      log.info(username);
      log.info(password);
      log.info(dbUrl);
      return DriverManager.getConnection(dbUrl, username, password);
    } catch (Exception e) {
      log.error(e.toString());
      return null;
    }

  }

  public ArrayList<Component> getComponents(String name, Integer id_collection)
      throws URISyntaxException, SQLException {

    ArrayList<Component> components = new ArrayList<Component>();
    String query;
    String path;
    try (Connection conn = ComponentRepository.getConnection()) {
      if (name == "home") {
        path = "/";
      } else {
        String query_path = "SELECT path FROM Components where name='" + name + "'";
        log.debug(query_path);
        Statement st_path = conn.createStatement();
        ResultSet rs_path = st_path.executeQuery(query_path);
        if (rs_path.next()) {
          path = rs_path.getString(1);
        } else {
          throw new Exception("Object not found");
        }
      }

      Statement st = conn.createStatement();

      query = "SELECT * FROM Components where path='" + path + "' and id_collection=" + id_collection;
      ResultSet rs = st.executeQuery(query);
      log.debug(query);
      while (rs.next()) {
        Component current;
        log.info(rs.getString(3) + "/" + rs.getString(4) + "/" + rs.getString(6) + "/" + rs.getString(5));
        if (rs.getBoolean(7)) {
          log.info("Container");
          current = new Container(rs.getString(3), rs.getString(4), rs.getString(6), rs.getString(5));
          ((Container) current).addObjects(getChilds(rs.getInt(1), id_collection));
        } else {
          log.info("Component");
          current = new Element(rs.getString(3), rs.getString(4), rs.getString(6), rs.getString(5));
        }
        components.add(current);
      }
      return components;
    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }

  }

  public void addComponent(Component component, Boolean container, String parent, Integer id_collection)
      throws SQLException {
    String query = "INSERT INTO COMPONENTS (id_parent, name, description, path, tag, is_container, id_collection) VALUES (?,?,?,?,?,?,?)";

    try (Connection conn = ComponentRepository.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(query)) {
      if (parent.isBlank()) {
        preparedStatement.setNull(1, java.sql.Types.INTEGER);
        preparedStatement.setString(4, component.getPath());
      } else {
        String query_parent = "SELECT id_component,path FROM components where name='" + parent + "'";
        // PreparedStatement preparedStatement_parent =
        // conn.prepareStatement(query_parent);
        // preparedStatement_parent.setString(1, parent);
        log.error(query_parent);

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query_parent);
        while (rs.next()) {
          preparedStatement.setInt(1, rs.getInt(1));
          preparedStatement.setString(4, rs.getString(2) + parent + "/");
        }

      }
      preparedStatement.setString(2, component.getName());
      preparedStatement.setString(3, component.getDescription());
      preparedStatement.setString(5, component.getTag());
      preparedStatement.setBoolean(6, container);
      preparedStatement.setInt(7, id_collection);

      log.error(preparedStatement.toString());

      preparedStatement.executeUpdate();

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  private ArrayList<Component> getChilds(Integer id_parent, Integer id_collection) throws SQLException {
    String query_child = "SELECT * from Components where id_parent=" + id_parent + " and id_collection="
        + id_collection;
    log.info(query_child);
    ArrayList<Component> childs = new ArrayList<>();
    try (Connection conn = ComponentRepository.getConnection()) {
      // PreparedStatement preparedStatement_child =
      // conn.prepareStatement(query_child);
      // preparedStatement_child.setInt(1, id_parent);
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query_child);

      // Condition d'arrêt de la fonction recursive, si jamais on a pas d'enfants on
      // renvoie une liste vide
      if (!rs.next()) {
        log.info("No childs");
        return childs;
      } else {
        // Sinon on récupère les enfants et les enfants des enfants
        do {
          Component current;
          if (rs.getBoolean(7)) {
            current = new Container(rs.getString(3), rs.getString(4), rs.getString(6), rs.getString(5));
            ((Container) current).addObjects(getChilds(rs.getInt(1), id_collection));
          } else {
            current = new Element(rs.getString(3), rs.getString(4), rs.getString(6), rs.getString(5));
          }
          childs.add(current);
        } while (rs.next());
        return childs;
      }
    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }

  }

  public void deleteComponent(String name, Integer id_collection) throws Exception {
    String query_select = "SELECT id_component,id_parent,path FROM components where id_collection=" + id_collection
        + " and name='" + name + "'";
    Integer id_parent;
    Integer id_component;
    String path;
    try (Connection conn = ComponentRepository.getConnection()) {
      // PreparedStatement preparedStatement_child =
      // conn.prepareStatement(query_child);
      // preparedStatement_child.setInt(1, id_parent);
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query_select);
      if (rs.next()) {
        id_component = rs.getInt(1);
        id_parent = rs.getInt(2);
        path = rs.getString(3).replace(name + "/", "");
      } else {
        throw new Exception("Object not found");
      }
      String query_update_childs = "UPDATE components SET id_parent = " + id_parent + ", path='" + path
          + "' WHERE id_parent = " + id_component;
      Statement st_update_childs = conn.createStatement();
      st_update_childs.executeUpdate(query_update_childs);

      String query_delete = "DELETE FROM Components WHERE id_component=" + id_component + "and id_collection="
          + id_collection;
      Statement st_delete = conn.createStatement();
      st_delete.executeUpdate(query_delete);

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public ArrayList<String> getAllTags(Integer id_collection) throws SQLException {
    String query_select = "SELECT DISTINCT tag FROM components where id_collection=" + id_collection;
    ArrayList<String> tags = new ArrayList<>();
    try (Connection conn = ComponentRepository.getConnection()) {

      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query_select);

      while (rs.next()) {
        tags.add(rs.getString(1));
      }

      return tags;
    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }

  }

  public ArrayList<Component> getAllComponentsbyTag(String tag, Integer id_collection) throws SQLException {
    ArrayList<Component> components = new ArrayList<Component>();
    String query = "SELECT * from Components WHERE id_collection=" + id_collection + "and tag = '" + tag + "'";
    try (Connection conn = ComponentRepository.getConnection()) {
      Component current;
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      while (rs.next()) {
        current = new Component(rs.getString(3), rs.getString(4), rs.getString(6), rs.getString(5));
        components.add(current);
      }
      return components;
    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public ArrayList<String> getAllCollections(Integer id_user) throws SQLException {
    ArrayList<String> collections = new ArrayList<String>();
    String query = "select c.name from collections c inner join users_collections uc on uc.id_collection = c.id_collection where uc.id_user="
        + id_user;
    try (Connection conn = ComponentRepository.getConnection()) {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);

      while (rs.next()) {
        collections.add(rs.getString(1));
      }
      return collections;

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public String getCollectionToken(String collection_name) throws SQLException {
    String query = "SELECT token from collections where name='" + collection_name + "'";
    try (Connection conn = ComponentRepository.getConnection()) {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);

      if (rs.next()) {
        return rs.getString(1);
      }
      throw new Exception("Collection inexistant");

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public void createCollection(String collection_name, String collection_token, Integer id_user) throws SQLException {
    String query_insert_collection = "INSERT INTO Collections (name, token) VALUES (?,?)";
    String query_right = "INSERT INTO users_collections (id_user, id_collection) VALUES (?,?)";

    try (Connection conn = ComponentRepository.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(query_insert_collection)) {

      preparedStatement.setString(1, collection_name);
      preparedStatement.setString(2, collection_token);
      preparedStatement.executeUpdate();

      String query_select_collection = "SELECT id_collection from Collections where name='" + collection_name + "'";
      Statement s = conn.createStatement();
      ResultSet rs = s.executeQuery(query_select_collection);

      if (rs.next()) {
        PreparedStatement ps2 = conn.prepareStatement(query_right);
        ps2.setInt(1, id_user);
        ps2.setInt(2, rs.getInt(1));
        ps2.executeUpdate();
      } else {
        throw new Exception("probleme lors de creation de la collection");
      }

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }
}
