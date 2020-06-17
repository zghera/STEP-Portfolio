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

/**
 * Class representing a comment created on server-dev.html.
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Comment {

  /** The strings of text for the comment */
  private final String text;

  /** The image urls for the comment (null if no image for a comment) */
  private final String imageUrl;

  /** 
   * @param text The string of text for an individual comment.
   * @param imageUrl The image URL for an individual comment (null if no image).
   */
  public Comment(String text, String imageUrl) {
    this.text = text;
    this.imageUrl = imageUrl; 
  }
}
