package ru.mirox.table;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.mirox.table.database.DataBaseHandler;
import ru.mirox.table.utils.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@WebServlet("/api/users")
public class UserServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("/api/users   GET");

    Gson gson = new Gson();
    DataBaseHandler dataBaseHandler = new DataBaseHandler();

    String searchString = req.getParameter("searchString");
    int limit = Integer.parseInt(req.getParameter("showRows"));
    int pageNumber = Integer.parseInt(req.getParameter("pageNumber"));

    if(pageNumber > 0) pageNumber -= 1;

    String sortColumn = req.getParameter("sortColumn");
    String sortType = req.getParameter("sortType");

    String[] columns = req.getParameter("columns").split(",");
    HashSet<String> setColumns = new HashSet<>(List.of(columns));


    if(Objects.equals(sortType, "ASC")) {
      sortType = "ASC";
    }

    if(Objects.equals(sortType, "DESC")) {
      sortType = "DESC";
    }
    try {
      Response response = new Response();
      response.setType("success");
      response.setMessage("array users in object");
      response.setInfo(Integer.toString(dataBaseHandler.getNumberRows(searchString)));
      response.setObject(dataBaseHandler.getUsers(searchString, limit, pageNumber, sortColumn, sortType, setColumns));
      resp.getWriter().write(gson.toJson(response));
    } catch (SQLException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }
}
