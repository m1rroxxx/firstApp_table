package ru.mirox.table.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.mirox.table.database.DataBaseHandler;
import ru.mirox.table.utils.Response;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Objects;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    System.out.println("/login   PUT");

    try {

      Gson gson = new Gson();
      DataBaseHandler dataBaseHandler = new DataBaseHandler();

      JsonObject request = gson.fromJson(req.getReader().readLine(), JsonObject.class);

      String email = request.get("email").getAsString();
      String password = request.get("password").getAsString();

      Response response = new Response();
      if (dataBaseHandler.checkUserOnExistsByEmail(email)) {

        String[] hashPasswordAndSalt = dataBaseHandler.getHashPassword(email).split(":");
        String hashPasswordFromDataBase = hashPasswordAndSalt[0];
        String salt = hashPasswordAndSalt[1];

        String hashPassword = Encoder.hashPassword(password, Base64.getDecoder().decode(salt)).split(":")[0];

        if (Objects.equals(hashPasswordFromDataBase, hashPassword)) {

          WebSession webSession = new WebSession();
          webSession.createSession();

          Cookie cookie = new Cookie("SESSION_ID", webSession.getSession_id());
          resp.addCookie(cookie);

          long userId = dataBaseHandler.getUserIdByEmail(email);
          dataBaseHandler.addNewSession(webSession, userId);

          response.setType("success");
          response.setMessage("");
          response.setObject(dataBaseHandler.getUser(webSession.getSession_id()));
        } else {
          response.setType("error");
          response.setMessage("invalid password");
        }
      } else {
        response.setType("error");
        response.setMessage("user not exists");
      }
      resp.getWriter().write(gson.toJson(response));
    } catch (SQLException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("/login   GET");

    System.out.println(req.getRemoteAddr());

    Gson gson = new Gson();
    DataBaseHandler dataBaseHandler = new DataBaseHandler();

    String session_id = null;
    if (req.getCookies() != null) session_id = req.getCookies()[0].getValue();

    try {

      Response response = new Response();

      if (session_id != null) {

        WebSession webSession = dataBaseHandler.checkSession(session_id);
        if (webSession != null) {
          if (System.currentTimeMillis() / 1000L - webSession.getLast_used_time() > webSession.getMax_inactive_interval()) {
            dataBaseHandler.deleteSession(session_id);

            Cookie cookie = new Cookie("SESSION_ID", "");
            cookie.setMaxAge(0);
            resp.addCookie(cookie);

            response.setType("error");
            response.setMessage("session has been deleted");
          } else {
            dataBaseHandler.updateSession(session_id);
            response.setType("success");
            response.setMessage("session has been updated");
            response.setObject(dataBaseHandler.getUser(session_id));
          }
        } else {
          response.setType("error");
          response.setMessage("session not exists");
        }
        resp.getWriter().write(gson.toJson(response));

      } else {
        response.setType("success");
        response.setMessage("the user is not logged in");
        resp.getWriter().write(gson.toJson(response));
      }
    } catch (SQLException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("/api/login   DELETE");

    Gson gson = new Gson();
    DataBaseHandler dataBaseHandler = new DataBaseHandler();
    Response response = new Response();

    String session_id = req.getCookies()[0].getValue();

    try {
      dataBaseHandler.deleteSession(session_id);

      Cookie cookie = new Cookie("SESSION_ID", "");
      cookie.setMaxAge(0);
      resp.addCookie(cookie);

      response.setType("success");
      response.setMessage("session has been deleted");
      resp.getWriter().write(gson.toJson(response));

    } catch (SQLException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("/api/login   POST");

    try {

      Gson gson = new Gson();
      DataBaseHandler dataBaseHandler = new DataBaseHandler();
      Response response = new Response();

      JsonObject request = gson.fromJson(req.getReader().readLine(), JsonObject.class);

      System.out.println(request);

      String newName = request.get("newName").getAsString();
      String newLastName = request.get("newLastName").getAsString();
      String newEmail = request.get("newEmail").getAsString();
      String email = request.get("email").getAsString();

      String session_id = req.getCookies()[0].getValue();

      System.out.println(!dataBaseHandler.checkUserOnExistsByEmail(newEmail) && !Objects.equals(email, newEmail));

      boolean emailExists = false;

      if(!Objects.equals(newEmail, email)) {
        emailExists = dataBaseHandler.checkUserOnExistsByEmail(newEmail);
      }

      if(!emailExists) {

        dataBaseHandler.editUserData(newName, newLastName, newEmail, email);

        response.setType("success");
        response.setMessage("the user's data has been changed");
        response.setObject(dataBaseHandler.getUser(session_id));
        resp.getWriter().write(gson.toJson(response));

      }else {
        response.setType("error");
        response.setMessage("user already exists");
        resp.getWriter().write(gson.toJson(response));
      }

    } catch (SQLException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }
}
