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

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the list of comments created on server-dev.html.
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class CommentsThread {

  /** List of the strings of text in each comment */
  private final List<String> texts = new ArrayList<>();

  /** List of the image urls each comment (null if no image for a comment) */
  private final List<String> imageUrls = new ArrayList<>();

  /** 
   * Adds a new comment with associated text and image. 
   *
   * @param text The string of text for an individual comment.
   * @param imageUrl The image URL for an individual comment (null if no image).
   * @return void. The parameters are added to their respective private Lists.
   */
  public void addNewComment(String text, String imageUrl) {
    texts.add(text);
    imageUrls.add(imageUrl);
  }
}
