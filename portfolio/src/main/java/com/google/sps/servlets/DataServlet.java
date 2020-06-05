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
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/comment-data")
public class DataServlet extends HttpServlet {
  private static List<String> comments = new ArrayList<>();                                                     

  /**
  * This Method handles GET requests in order to display all of the comments that are stored 
  * in the Comments kind of the Google Cloud Datastore.
  * <p>
  *
  * @param  request  The <code>HttpServletRequest</code> for the GET request.
  * @param  response The <code>HttpServletResponse</code> for the GET request.
  * @return None. The Servlet writes to the /comment-data page which JavaScript then fetches 
  *         in order to serve the comments to the UI. 
  */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String jsonComments = convertToJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  /**
  * Converts a list of strings to a JSON string.
  * <p>
  *
  * @param  comments  The List of String comments that should be converted to a JSON string.
  * @return <code>String</code> The JSON string corresponding to the list of comments.
  */
  private String convertToJson(List<String> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

  /**
  * This Method handles POST requests corresponding to a new comment and appends that comment
  * as new String to the static class variable <code>comments</code>.
  * <p>
  * The POST request also results in a re-direct back to the original server-dev page.
  * TODO(Issue #15): Do verfification on a new comment before adding it to the comments list.
  *
  * @param  request  The <code>HttpServletRequest</code> for the POST request.
  * @param  response The <code>HttpServletResponse</code> for the POST request.
  * @return None. A new String corresponding to the new comment is appended to the 
  *         <code>comments</code> list. 
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("new-comment");
    comments.add(newComment);
    response.sendRedirect("/pages/server-dev.html");
  }
}
