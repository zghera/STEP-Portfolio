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

/**
 * Fetches the previously entered comments from the server and inserts each
 * comment as a list item of the 'comments' <ul> element.
 *
 * <p>
 *
 * An option to determine the maximum number of comments is also included
 * using a Query String parameter created from the num-comments form. When
 * the page is (re-)loaded, the number of comments displayed is determined
 * from the selection in the previous session. Otherwise, the last  most
 * recently submitted number selection will be used. The number of comments
 * will also never exceed the number of total comments returned from the
 * datastore.
 */
function getCommentsThread() {
  fetch('/comment-data')
      .then(response => response.json())
      .then((commentList) => {
        const commentThread = document.getElementById('comments-thread');
        const urlParams = new URLSearchParams(window.location.search);

        // Determine the number of comments to display
        var numComments = urlParams.get('num-comments');
        const numCommentsStored = parseInt(
            sessionStorage.getItem("numComments"));
        if (numComments == null) {
          numComments = numCommentsStored;
        } else {
          sessionStorage.setItem("numComments", numComments);
        }
        const maxCommentIdx = Math.min(numComments, commentList.length);

        document.getElementById('comments-thread').innerHTML = "";
        for (var cmntIdx = 0; cmntIdx < maxCommentIdx; cmntIdx++) {
          commentThread.appendChild(createListElement(commentList[cmntIdx]));
        }
      })
      .catch(err => {
        console.log('Error: ' + err);
        document.getElementById('comments-thread').
        appendChild(createListElement('Error: Unable to load ' +
                                      'the comments thread.'));
      });
}

/**
 * Creates an <li> element containing 'text'. 
 * 
 * @param {string} text the inner text of the created <li> element.
 * @return {li} The list element created.
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
