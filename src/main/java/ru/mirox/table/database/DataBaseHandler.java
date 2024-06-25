package ru.mirox.table.database;

import ru.mirox.table.authentication.Encoder;
import ru.mirox.table.authentication.WebSession;
import ru.mirox.table.utils.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.*;

public class DataBaseHandler {

  public Connection dataBaseConnection() throws IOException, ClassNotFoundException, SQLException {

    Properties properties = new Properties();
    properties.load(Files.newInputStream(Paths.get("C:\\Users\\Nikita\\IdeaProjects\\table\\src\\main\\resources\\database.properties")));

    String url = properties.get("url").toString();
    String dataBaseName = properties.get("dataBaseName").toString();
    String user = properties.get("user").toString();
    String password = properties.get("password").toString();

    Class.forName("com.mysql.jdbc.Driver");

    return DriverManager.getConnection(url + dataBaseName, user, password);
  }

  public void addNewUser(String name, String lastName, String email, String password) throws SQLException, IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
    Connection connection = dataBaseConnection();

    String sql = "INSERT INTO users(name, lastName, email, password, salt) VALUES (?,?,?,?,?)";

    String[] hashPasswordAndSalt = Encoder.hashPassword(password, Encoder.generateSalt()).split(":");
    String hashPassword = hashPasswordAndSalt[0];
    String salt = hashPasswordAndSalt[1];

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, name);
    preparedStatement.setString(2, lastName);
    preparedStatement.setString(3, email);
    preparedStatement.setString(4, hashPassword);
    preparedStatement.setString(5, salt);
    preparedStatement.executeUpdate();

    connection.close();
  }

  public boolean checkUserOnExistsByEmail(String email) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "SELECT * FROM users WHERE email=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, email);
    ResultSet resultSet = preparedStatement.executeQuery();

    if (resultSet.next()) {
      connection.close();
      return true;
    } else {
      connection.close();
      return false;
    }
  }

  public String getHashPassword(String email) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "SELECT users.password, users.salt FROM users WHERE email=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, email);
    ResultSet resultSet = preparedStatement.executeQuery();

    resultSet.next();

    String hashPassword = resultSet.getString(1);
    String salt = resultSet.getString(2);

    connection.close();

    return hashPassword + ":" + salt;

  }

  public void addNewSession(WebSession webSession, long user_id) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "INSERT INTO sessions (session_id, user_id, creation_time, last_used_time, max_inactive_interval) VALUES (?,?,?,?,?)";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, webSession.getSession_id());
    preparedStatement.setLong(2, user_id);
    preparedStatement.setLong(3, webSession.getCreation_time());
    preparedStatement.setLong(4, webSession.getLast_used_time());
    preparedStatement.setLong(5, webSession.getMax_inactive_interval());
    preparedStatement.executeUpdate();

    connection.close();
  }

  public long getUserIdByEmail(String email) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "SELECT users.id FROM users WHERE email=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, email);
    ResultSet resultSet = preparedStatement.executeQuery();

    resultSet.next();

    long userId = resultSet.getLong(1);
    connection.close();
    return userId;
  }

  public WebSession checkSession(String session_id) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "SELECT * FROM sessions WHERE session_id=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, session_id);
    ResultSet resultSet = preparedStatement.executeQuery();

    if (resultSet.next()) {
      WebSession webSession = new WebSession();
      webSession.setSession_id(session_id);
      webSession.setCreation_time(resultSet.getLong("creation_time"));
      webSession.setLast_used_time(resultSet.getLong("last_used_time"));
      webSession.setMax_inactive_interval(resultSet.getLong("max_inactive_interval"));
      connection.close();
      return webSession;
    } else {
      connection.close();
      return null;
    }
  }

  public void updateSession(String session_id) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "UPDATE sessions SET last_used_time=? WHERE session_id=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setLong(1, System.currentTimeMillis() / 1000L);
    preparedStatement.setString(2, session_id);
    preparedStatement.executeUpdate();

    connection.close();
  }

  public void deleteSession(String session_id) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "DELETE FROM sessions WHERE session_id=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, session_id);
    preparedStatement.execute();
    connection.close();
  }

  public List<User> getUsers(String searchString, int limit, int pageNumber, String sortColumn, String sortType, HashSet<String> columns) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = null;
    PreparedStatement preparedStatement = null;

    if (Objects.equals(sortColumn, "") && Objects.equals(sortType, "")) {
      sql = "SELECT * FROM users WHERE name LIKE ? OR lastName LIKE ? OR email LIKE ? LIMIT ? OFFSET ?";
    } else {
      sql = "SELECT * FROM users WHERE name LIKE ? OR lastName LIKE ? OR email LIKE ? ORDER BY " + sortColumn + " " + sortType + " LIMIT ? OFFSET ?";
    }

    System.out.println(sql);

    preparedStatement = connection.prepareStatement(sql);
    if (columns.contains("name")) {
      preparedStatement.setString(1, "%" + searchString + "%"); //searchName
    } else {
      preparedStatement.setString(1, "");
    }

    if (columns.contains("lastName")) {

      preparedStatement.setString(2, "%" + searchString + "%"); //searchLastName
    } else {
      preparedStatement.setString(2, "");
    }
    if (columns.contains("email")) {
      preparedStatement.setString(3, "%" + searchString + "%"); //searchEmail
    } else {
      preparedStatement.setString(3, "");
    }
    preparedStatement.setInt(4, limit);
    preparedStatement.setInt(5, limit * pageNumber);

    ResultSet resultSet = preparedStatement.executeQuery();

    List<User> users = new ArrayList<>();
    while (resultSet.next()) {
      User user = new User();
      user.setId(resultSet.getLong("id"));
      user.setName(resultSet.getString("name"));
      user.setLastName(resultSet.getString("lastName"));
      user.setEmail(resultSet.getString("email"));
      users.add(user);
    }
    connection.close();
    return users;
  }

  public int getNumberRows(String searchString) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "SELECT COUNT(*) FROM users WHERE name LIKE ? OR lastName LIKE ? OR email LIKE ?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, "%" + searchString + "%");
    preparedStatement.setString(2, "%" + searchString + "%");
    preparedStatement.setString(3, "%" + searchString + "%");

    ResultSet resultSet = preparedStatement.executeQuery();
    resultSet.next();

    int count = resultSet.getInt(1);
    connection.close();

    return count;

  }

  public void editUserData(String newName, String newLastName, String newEmail, String email) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "UPDATE users SET name=?, lastName=?, email=? WHERE email=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);

    preparedStatement.setString(1, newName);
    preparedStatement.setString(2, newLastName);
    preparedStatement.setString(3, newEmail);
    preparedStatement.setString(4, email);

    System.out.println(sql);

    preparedStatement.execute();
    connection.close();
  }

  public User getUser(String session_id) throws SQLException, IOException, ClassNotFoundException {
    Connection connection = dataBaseConnection();

    String sql = "SELECT * FROM users JOIN sessions ON users.id = sessions.user_id WHERE session_id=?";

    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, session_id);

    ResultSet resultSet = preparedStatement.executeQuery();
    resultSet.next();

    User user = new User();

    user.setId(resultSet.getLong("id"));
    user.setName(resultSet.getString("name"));
    user.setLastName(resultSet.getString("lastName"));
    user.setEmail(resultSet.getString("email"));

    connection.close();

    return user;
  }
}
