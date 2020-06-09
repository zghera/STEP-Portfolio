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
 * An option to determine the maximum number of comments is also included
 * using a Query String parameter created from the num-comments form. When
 * the page is (re-)loaded, the number of comments displayed is determined
 * from the selection in the previous session. Otherwise, the most recently
 * submitted number selection will be used. The number of comments will
 * also never exceed the number of total comments returned from the datastore.
 */
function getCommentsThread() {
  fetch('/comment-data')
      .then(response => response.json())
      .then((commentList) => {
        const commentThread = document.getElementById('comments-thread');
        const urlParams = new URLSearchParams(window.location.search);

        // Determine the number of comments to display.
        let numComments = urlParams.get('num-comments');
        const numCommentsStored = parseInt(
            sessionStorage.getItem('numComments'));
        if (numComments == null) {
          numComments = numCommentsStored;
        } else {
          sessionStorage.setItem('numComments', numComments);
        }
        const maxCommentIdx = Math.min(numComments, commentList.length);
        document.getElementById('num-comments').value = numComments;

        document.getElementById('comments-thread').innerHTML = '';
        for (let cmntIdx = 0; cmntIdx < maxCommentIdx; cmntIdx++) {
          commentThread.appendChild(createListElement(commentList[cmntIdx]));
        }
      })
      .catch(err => {
        console.log('Error in getCommentsThread: ' + err);
        document.getElementById('comments-thread').
        // TODO(Issue #19): Create error page/notice
        appendChild(createListElement('Error: Unable to load ' +
                                      'the comments thread.'));
      });
}

/**
 * Creates an <li> element containing 'text'. 
 * 
 * @param {string} text the inner text of the created <li> element.
 * @return {HTMLLIElement} The list element created.
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
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
        document.getElementById('comments-thread').
        // TODO(Issue #19): Create error page/notice
        appendChild(createListElement('Error: Unable to fetch ' +
                              'image upload url from Blobstore.'));
      });
}

/**
 * Wrapper function that calls functions that need to hapen upon loading 
 * server-dev.html. This method prevents inline scripting on the body 
 * html element. 
 */
function loadPage() {
  fetchBlobstoreUrl();
  getCommentsThread()
}
window.onload = loadPage;