// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List; 
import java.util.Arrays; 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/comment-data")
public class DataServlet extends HttpServlet {
  private static List<String> comments = new List<String>;                                                     

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Convert the array of answers to JSON
    String jsonAnswers = convertToJson(comments);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(jsonAnswers);
  }

  private String convertToJson(List<String> jsonAnswers) {
    Gson gson = new Gson();
    String json = gson.toJson(jsonAnswers);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String newComment = request.getParameter("new-comment");

    // Add comment to the comments list
    comments.add(newComment);

    // Redirect back to the server HTML page.
    response.sendRedirect("/server-dev.html");
  }
}
