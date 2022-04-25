package com.homeganizer.serveur.dao;

import java.sql.Statement;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.homeganizer.serveur.models.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepository {

  private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

  private static Connection getConnection() throws URISyntaxException, SQLException {
    try {
      URI dbUri = new URI(System.getenv("DATABASE_URL"));

      String username = dbUri.getUserInfo().split(":")[0];
      String password = dbUri.getUserInfo().split(":")[1];
      String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
      return DriverManager.getConnection(dbUrl, username, password);
    } catch (Exception e) {
      log.error(e.toString());
      return null;
    }

  }

  public void signup(User user) throws SQLException {
    String query = "INSERT INTO USERS (username, password, token) VALUES (?,?,?)";
    try (Connection conn = UserRepository.getConnection()) {

      PreparedStatement ps = conn.prepareStatement(query);

      ps.setString(1, user.getUser());
      ps.setString(2, user.getPwd());
      ps.setString(3, user.getToken());

      log.error(ps.toString());

      ps.executeUpdate();
    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public String verifyUser(String username, String pwd) throws SQLException {
    String query = "SELECT password,token from USERS where username='" + username + "'";
    try (Connection conn = UserRepository.getConnection()) {

      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      log.info(query);
      log.info(rs.toString());
      while (rs.next()) {
        log.info(pwd + "==" + rs.getString(1));
        if (rs.getString(1).equals(pwd)) {
          return rs.getString(2);
        }
      }
      log.error("Username/password incorrect");
      throw new Exception();

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public Integer getUserId(String token) throws SQLException {
    String query = "SELECT id_user from USERS where token='" + token + "'";
    try (Connection conn = UserRepository.getConnection()) {

      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);

      if (rs.next()) {
        return rs.getInt(1);
      }
      throw new Exception("token introuvable");

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public Integer verifyUserCollection(Integer id_user, String collection_name) throws SQLException {
    String query = "select uc.id_collection from users_collections uc inner join collections c on c.id_collection=uc.id_collection where uc.id_user="
        + id_user + " and c.name='" + collection_name + "'";
    try (Connection conn = UserRepository.getConnection()) {

      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);

      if (rs.next()) {
        return rs.getInt(1);
      }
      throw new Exception("unauthorized");

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

  public void putAuthorizationForCollection(Integer id_user, String collection_token) throws SQLException {
    String query_select = "SELECT id_collection from collections where token='" + collection_token + "'";
    String query_insert = "INSERT INTO users_collections (id_user,id_collection) values(?,?)";
    try (Connection conn = UserRepository.getConnection()) {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query_select);
      if (rs.next()) {
        Integer id_collection = rs.getInt(1);
        PreparedStatement ps = conn.prepareStatement(query_insert);
        ps.setInt(1, id_user);
        ps.setInt(2, id_collection);
        ps.executeUpdate();
      } else {
        throw new Exception("token not found");
      }

    } catch (Exception e) {
      log.error(e.toString());
      throw new SQLException();
    }
  }

}
