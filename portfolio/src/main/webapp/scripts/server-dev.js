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
 * The number of comments displayed is determined by 
 * getNumCommentstoDisplay().
 */
function getCommentsThread() {
  fetch('/comment-data')
      .then(response => response.json())
      .then((commentsList) => {
        numComments = getNumCommentstoDisplay(commentsList.length);
        document.getElementById('num-comments').value = numComments;

        const commentsThread = document.getElementById('comments-thread');
        document.getElementById('comments-thread').innerHTML = "";
        for (let cmntIdx = 0; cmntIdx < numComments; cmntIdx++) {
          commentsThread.appendChild(createListElement(commentsList[cmntIdx]));
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
 * Determines the number of comments that should be displayed in the comments
 * thread based on the the current user selection, the last user selection 
 * cached in the session, and the total number of comments stored in the 
 * database.
 * 
 * An option to determine the maximum number of comments is implemented
 * using a Query String parameter created from the num-comments form. When
 * the page is (re-)loaded, the number of comments displayed is determined
 * using the cached value corresponding to the selection in the previous 
 * session. Otherwise, the most recently submitted number selection will be
 * used. The number of comments will also never exceed the number of total
 * comments returned from the datastore.
 * 
 * @param {number} numCommentsDatabase The number of comments stored in the 
 *    Cloud Datastore.
 * @return {number} The number of comments to be displayed in the comments 
 *    thread.
 */
function getNumCommentstoDisplay(numCommentsDatabase) {
  const urlParams = new URLSearchParams(window.location.search);
  let numCommentsSelected = urlParams.get('num-comments');
  const numCommentsCached = parseInt(
      sessionStorage.getItem('numCommentsCached'));
      
  if (numCommentsSelected == null) {
    numCommentsSelected = numCommentsCached;
  } else {
    sessionStorage.setItem('numCommentsCached', numCommentsSelected);
  }
  return Math.min(numCommentsSelected, numCommentsDatabase);
}

/**
 * Creates an <li> element containing 'text'. 
 * 
 * @param {string} text The inner text of the created <li> element.
 * @return {HTMLLIElement} The list element created.
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
