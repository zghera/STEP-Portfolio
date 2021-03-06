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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.sps.data.Landmark;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comments stored in the Datastore. */
@WebServlet("/comment-data")
@SuppressWarnings("serial")
public class ListCommentsServlet extends HttpServlet {
  private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * {@inheritDoc}
   *
   * <p>This Method handles GET requests in order to display all of the comments that are stored in
   * the Comments kind of the Google Cloud Datastore.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    List<Comment> commentsThread = new ArrayList<>();
    for (Entity commentEntity : results.asIterable()) {
      Landmark landmark = null;
      if (commentEntity.getProperty("landmarkName") != null) {
        landmark =
            new Landmark(
                (String) commentEntity.getProperty("landmarkName"),
                ((GeoPt) commentEntity.getProperty("landmarkGeoPt")).getLatitude(),
                ((GeoPt) commentEntity.getProperty("landmarkGeoPt")).getLongitude());
      }
      commentsThread.add(
          new Comment(
              (String) commentEntity.getProperty("text"),
              (BlobKey) commentEntity.getProperty("blobKey"),
              landmark));
    }

    String jsonComments = convertToJson(commentsThread);
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  /**
   * Converts a list of Comment objects to a JSON string.
   *
   * @param commentsThread The List of Comment objects that should be converted to a JSON string.
   * @return The JSON string corresponding to the list of comments.
   */
  private String convertToJson(List<Comment> commentsThread) {
    Gson gson = new Gson();
    String json = gson.toJson(commentsThread);
    return json;
  }
}
