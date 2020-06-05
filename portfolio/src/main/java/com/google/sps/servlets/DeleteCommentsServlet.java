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
import com.google.appengine.api.datastore.getKey;
import com.google.appengine.api.datastore.Key;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes all comments from the Datastore. */
@WebServlet("/delete-comments")
public class DeleteCommentsServlet extends HttpServlet {
  private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * This Method handles POST requests corresponding to deleting all Comment kind Entities from
   * the Google Cloud Datastore.
   *
   * <p>
   * 
   * The POST request also results in a re-direct back to the original server-dev page.
   *
   * @param request The <code>HttpServletRequest</code> for the POST request.
   * @param response The <code>HttpServletResponse</code> for the POST request.
   * @return None. All entities in the 'Comments' kind are deleted.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment");
    PreparedQuery results = datastore.prepare(query);

    List<Key> entityKeys = new ArrayList();
    for (Entity commentEntity : results.asIterable()) {
      entityKeys.append(commentEntity.getKey())
    }
    for (Key key : entityKeys) {
      datastore.delete(key)
    }

    response.sendRedirect("/pages/server-dev.html");
  }
}
