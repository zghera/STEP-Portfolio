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

        const numCommentsToDispStored = parseInt(sessionStorage.getItem("numCommentsToDisp"));
        console.log(numCommentsToDispStored)
        var numCommentsToDisp = document.getElementById("num-comments").value;
        if (numCommentsToDisp != numCommentsToDispStored) {
          numCommentsToDisp = numCommentsToDispNew;
        }
        sessionStorage.setItem("numCommentsToDisp", numCommentsToDisp);
        console.log(numCommentsToDisp)
        console.log('-------')
        const maxCommentIdx = Math.min(numCommentsToDisp, commentList.length);

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
 * @param {string} text the inner text of the created <li> element.
 * @return {li} The list element created.
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
