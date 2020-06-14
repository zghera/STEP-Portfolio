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
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.type.LatLng;
import com.google.appengine.api.datastore.GeoPt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
   * <p> If there is no image uploaded or there is no landmark in the image, the landmark name 
   * and geo point uploaded to the Datastore will be null.
   *
   * <p>TODO(Issue #15): Do verfification on a new comment before adding it to the comments list.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("comment");
    long timestamp = System.currentTimeMillis();
    BlobKey blobKey = getBlobKey(request, "image");
    String landmarkName = null;
    GeoPt landmarkGeoPt = null;
    
    if (blobKey != null) {
      byte[] blobBytes = getBlobBytes(blobKey);
      List<EntityAnnotation> landmarkInfoList = getLandmarkInfo(blobBytes);
 
      if (landmarkInfoList.size() > 0) {
        EntityAnnotation landmarkInfo = landmarkInfoList.get(0);
        landmarkName = landmarkInfo.getDescription();
        LatLng landmarkLatLng = landmarkInfo.getLocationsList().listIterator().next().getLatLng();
        landmarkGeoPt = new GeoPt((float) landmarkLatLng.getLatitude(), 
                                  (float) landmarkLatLng.getLongitude());
      }
    }

    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("text", newComment);
    taskEntity.setProperty("timestamp", timestamp);
    taskEntity.setProperty("blobKey", blobKey);
    taskEntity.setProperty("landmarkName", landmarkName);
    taskEntity.setProperty("landmarkGeoPt", landmarkGeoPt);
    
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

    // Check the validity of the file here by making sure it's an image file
    if (!"image".equals(blobInfo.getContentType().substring(0, 5))) {
      blobstoreService.delete(blobKey);
      return null;
    }

    return blobKey;
  }

  /**
   * Blobstore stores files as binary data. This function retrieves the binary data stored at the
   * BlobKey parameter.
   *
   * @param blobKey The key associated with the image whose binary data is retrieved.
   * @return An byte array containing the binary data of the image associated with the blobKey.
   * @throws IOException - If an output error occurs when writing bytes from the temp blobstore 
   *                       buffer <code>b</code> to the output byte array <code>outputBytes</code>.
   */
  private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
      byte[] b =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(b);

      // if we read fewer bytes than we requested, then we reached the end
      if (b.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }

  /**
   * Uses the Google Cloud Vision API to generate location information for a landmark detected in
   * the image represented by the binary data stored in imgBytes.
   *
   * @param imgBytes Binary data for the image that is being inspected for landmark detection.
   * @return An landmark annotation object containing information such as name and coordinates for
   *     the landmark detected in the image. Null if there are no landmarks detected or other errors
   *     occur when obtaining the landmark information.
   * @throws IOException - If an input or output error occurs when creating the 
   *                       <code>ImageAnnotatorClient</code> object.
   */
  private List<EntityAnnotation> getLandmarkInfo(byte[] imgBytes) throws IOException {
    ByteString byteString = ByteString.copyFrom(imgBytes);
    Image image = Image.newBuilder().setContent(byteString).build();

    Feature feature = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
    List<AnnotateImageRequest> requests = new ArrayList<>();
    requests.add(request);

    ImageAnnotatorClient client = ImageAnnotatorClient.create();
    BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
    client.close();

    AnnotateImageResponse imageResponse = batchResponse.getResponsesList().get(0);
    if (imageResponse.hasError()) {
      System.err.println("Error: " + imageResponse.getError().getMessage());
      return null;
    }

    return imageResponse.getLandmarkAnnotationsList();
  }
}
