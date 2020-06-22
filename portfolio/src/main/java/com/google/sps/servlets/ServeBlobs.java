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
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns (image) files stored in the Blobstore. */
@WebServlet("/serve-image")
@SuppressWarnings("serial")
public class ServeBlobs extends HttpServlet {
  private static BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  /**
   * {@inheritDoc}
   *
   * <p>This Method handles GET requests in order to serve files uploaded to Blobstore based on the
   * blob key placed in the URL query string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobKey blobKey = new BlobKey(request.getParameter("blob-key"));
    blobstoreService.serve(blobKey, response);
  }
}
