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

package com.google.sps.data;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Class representing a comment created on server-dev.html.
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Comment {

  /** The message text for the comment. */
  private final String text;

  /** The keys corresponding to the image stored in the Blobstore for the
      comment (null if no image for a comment). */
  private final BlobKey blobKey;

  /** A landmark instance corresponding the the image given by blobKey.  */
  private final Landmark landmark;

  /** 
   * @param text The string of text for an individual comment.
   * @param blobKey The associated image blob key for an individual comment.
   * @param landmark The associated landmark instance for an individual comment.
   */
  public Comment(String text, BlobKey blobKey, Landmark landmark) {
    this.text = text;
    this.blobKey = blobKey; 
    this.landmark = landmark;
  }
}
