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
public class Landmark {

  private final String name;

  private final float latitude;
  
  private final float longitude;

  /** 
   * @param text The string of text for an individual comment.
   * @param blobKey The associated image blob key for an individual comment.
   */
  public Landmark(String name, float latitude, float longitude) {
    this.name = name;
    this.latitude = latitude; 
    this.longitude = longitude;
  }
}
