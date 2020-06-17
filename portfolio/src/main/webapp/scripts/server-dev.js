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
      .then((commentsThread) => {
        numCommentsToDisplay = getNumCommentstoDisplay(commentsThread.length);
        document.getElementById('num-comments').value = numCommentsToDisplay;

        const commentsThreadContainer = document.
            getElementById('comments-thread-container');
        commentsThreadContainer.innerHTML = '';
        for (let cmntIdx = 0; cmntIdx < numCommentsToDisplay; cmntIdx++) {
          commentsThreadContainer.appendChild(createListElement(
                                          commentsThread[cmntIdx].text,
                                          commentsThread[cmntIdx].imageUrl));
        }
      })
      .catch(err => {
        console.log('Error in getCommentsThread: ' + err);
        document.getElementById('comments-thread-container').
        // TODO(Issue #19): Create error page/notice
        appendChild(createListElement('Error: Unable to load ' +
                                      'the comments thread.', null));
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
 * @param {number} numComments The number of comments stored in the Cloud
 *    Datastore.
 * @return {number} The number of comments to be displayed in the comments 
 *    thread.
 */
function getNumCommentstoDisplay(numComments) {
  const urlParams = new URLSearchParams(window.location.search);
  let newNumCommentsToDisplay = urlParams.get('num-comments');
  const currNumCommentsToDisplay = parseInt(
      sessionStorage.getItem('currNumCommentsToDisplay'));
  
  if (newNumCommentsToDisplay == null) {
    if (isNaN(currNumCommentsToDisplay)) {
      const defaultNumComments = document.getElementById('num-comments').value;
      newNumCommentsToDisplay = defaultNumComments;
      sessionStorage.setItem('currNumCommentsToDisplay', defaultNumComments);   
    } else {
      newNumCommentsToDisplay = currNumCommentsToDisplay;
    }
  } else {
    sessionStorage.setItem('currNumCommentsToDisplay', newNumCommentsToDisplay);
  }
  return Math.min(newNumCommentsToDisplay, numComments);
}

/**
 * Creates an <li> element containing the comment text and image.
 *
 * If there the imageUrl is null, no image element is included in 
 * parent <li> element. 
 * 
 * @param {string} text the interior text of the created <li> element.
 * @param {?string} imageUrl the URL for the interior image of the created
 *    <li> element. If it is null, no image element is included in parent 
 *    <li> element.
 * @return {HTMLLIElement} The list element created.
 */
function createListElement(text, imageUrl) {
  const liElement = document.createElement('li');
  const textElement = document.createElement('p');
  textElement.innerText = text;
  liElement.appendChild(textElement);

  if (imageUrl != null) {
    const imageElement = document.createElement('img');
    imageElement.src = imageUrl;
    liElement.appendChild(imageElement);
  }
  return liElement;
}

/**
 * Fetches the URL that points to Blobstore and assigns the value of the
 * commment submission form's action element to this URL. 
 * 
 * The Blobstore URL is generated and posted to the page /blobstore-upload-url
 * in plain HTML. On page load, this URL is fetched so that the form data is 
 * initially sent to Blobstore in order to process any files included in the 
 * request. This POST request is then forwarded to the NewComment servlet 
 * to enter form data into the Cload Datastore.
 */
function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => response.text())
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('new-comment-form');
        messageForm.action = imageUploadUrl;
      })
      .catch(err => {
        console.log('Error in fetchBlobstoreUrl: ' + err);
        document.getElementById('comments-thread-container').
        // TODO(Issue #19): Create error page/notice
        appendChild(createListElement('Error: Unable to fetch ' +
                              'image upload url from Blobstore.', null));
      });
}

/**
 * Wrapper function that calls functions that need to hapen upon loading 
 * server-dev.html. This method prevents inline scripting on the body 
 * html element. 
 */
function loadPage() {
  fetchBlobstoreUrl();
  getCommentsThread();
}
window.onload = loadPage;