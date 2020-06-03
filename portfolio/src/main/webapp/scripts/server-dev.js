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
 */
function getCommentsThread() {
  fetch('/comment-data')
      .then(response => response.json())
      .then((commentList) => {
        const commentThread = document.getElementById('comments-thread');
        commentList.forEach((comment) => {
          commentThread.appendChild(createListElement(comment));
        })
        .catch(err => {
          console.log('Error: ' + err);
          document.getElementById('comments-thread').
              appendChild(createListElement('Error: Unable to load ' +
                                            'the comments thread.'));
        });
  });
}

/** Creates an <li> element containing 'text'. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
