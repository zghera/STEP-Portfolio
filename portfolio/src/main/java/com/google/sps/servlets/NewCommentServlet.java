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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that enters new comments into the Datastore. */
@WebServlet("/new-comment")
public class NewCommentServlet extends HttpServlet {

  /**
  * This Method handles POST requests corresponding to a new comment and creates a new Entity
  * for that comment in the Google Cloud Datastore.
  * <p>
  * The POST request also results in a re-direct back to the original server-dev page.
  *
  * @param  request  The <code>HttpServletRequest</code> for the POST request.
  * @param  response The <code>HttpServletResponse</code> for the POST request.
  * @return None. A Entity of the Comment kind is created and upserted to the Datastore.
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("comment");
    long timestamp = System.currentTimeMillis();

    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("text", newComment);
    taskEntity.setProperty("timestamp", timestamp);
    System.out.println(newComment);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    response.sendRedirect("/pages/server-dev.html");
  }
}