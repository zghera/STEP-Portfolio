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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that enters new comments into the Datastore. */
@WebServlet("/new-comment")
public class NewCommentServlet extends HttpServlet {
  private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  /**
   * {@inheritDoc}
   *
   * <p>This Method handles POST requests corresponding to a new comment and creates a new Entity
   * for that comment in the Google Cloud Datastore.
   *
   * <p>This POST request originates from the 'new comment' form in server-dev.html and is initially
   * sent to Blobstore for file processing. Once the Blobstore forward the request to this servlet,
   * the name of file submitted in the form can be used to get the corresponding blob key to be
   * stored in the Datastore Blobstore. The POST request also results in a re-direct back to the
   * original server-dev page.
   *
   * <p>TODO(Issue #15): Do verfification on a new comment before adding it to the comments list.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("comment");
    long timestamp = System.currentTimeMillis();
    BlobKey blobKey = getBlobKey(request, "image");

    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("text", newComment);
    taskEntity.setProperty("timestamp", timestamp);
    taskEntity.setProperty("blobKey", blobKey);
    datastore.put(taskEntity);

    response.sendRedirect("/pages/server-dev.html");
  }

  /**
   * Returns a BlobKey object corresponding to the uploaded file.
   *
   * @param request The <code>HttpServletRequest</code> for the POST request.
   * @param formInputElementName The name attribute of the image file input to the form.
   * @return The blob key associated with the uploaded image file. Null is returned if the user did
   *     not select a file or the file is not an image type.
   */
  private BlobKey getBlobKey(HttpServletRequest request, String formInputElementName) {
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Non-image files are not supported.
    if (blobInfo.getContentType().startsWith("image") == false) {
      blobstoreService.delete(blobKey);
      return null;
    }

    return blobKey;
  }
}
