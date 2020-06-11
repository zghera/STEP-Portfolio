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
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
  private static ImagesService imagesService = ImagesServiceFactory.getImagesService();

  /**
   * {@inheritDoc}
   *
   * <p>This Method handles POST requests corresponding to a new comment and creates a new Entity for
   * that comment in the Google Cloud Datastore.
   *
   * <p>This POST request originates from the 'new comment' form in server-dev.html and is initially
   * sent to Blobstore for file processing. Once the Blobstore forward the request to this servlet,
   * the name of file submitted in the form can be used to get the image URL to be stored in
   * Blobstore. The POST request also results in a re-direct back to the original server-dev page.
   *
   * <p>TODO(Issue #15): Do verfification on a new comment before adding it to the comments list.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("comment");
    long timestamp = System.currentTimeMillis();
    String imageUrl = getUploadedFileUrl(request, "image");

    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("text", newComment);
    taskEntity.setProperty("timestamp", timestamp);
    taskEntity.setProperty("imageUrl", timestamp);
    datastore.put(taskEntity);

    response.sendRedirect("/pages/server-dev.html");
  }

  /**
   * Returns a String corresponding to the URL that points to the uploaded file, or null if the user
   * didn't upload a file.
   *
   * @param request The <code>HttpServletRequest</code> for the POST request.
   * @param formInputElementNameThe Name attribute of the image file input to the form.
   * @return The URL that points to the uploaded file.
   */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
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

    // Check the validity of the file here by making sure it's an image file
    if (!"image".equals(blobInfo.getContentType().substring(0, 5))) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Use imagesService to get a URL that points to the uploaded file.
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    String servingUrl = imagesService.getServingUrl(options);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(servingUrl);
      return url.getPath();
    } catch (MalformedURLException e) {
      return servingUrl;
    }
  }
}
