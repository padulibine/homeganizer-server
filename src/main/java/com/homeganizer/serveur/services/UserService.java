package com.homeganizer.serveur.services;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.homeganizer.serveur.dao.UserRepository;
import com.homeganizer.serveur.models.User;

import org.springframework.stereotype.Service;

@Service
public class UserService {

  UserRepository user_repository = new UserRepository();

  public void signup(User user) throws SQLException {
    user_repository.signup(user);
  }

  public String verifyUser(String username, String pwd) throws SQLException {
    return user_repository.verifyUser(username, pwd);
  }

  public Integer getUserId(String token) throws SQLException {
    return user_repository.getUserId(token);
  }

  public Integer verifyUserCollection(Integer id_user, String collection_name) throws SQLException {
    return user_repository.verifyUserCollection(id_user, collection_name);
  }

  public void putAuthorizationForCollection(Integer id_user, String collection_token) throws SQLException {
    user_repository.putAuthorizationForCollection(id_user, collection_token);
  }

}
