/* reset squarespace css */
for (var i in document.styleSheets) {
    var sheet = document.styleSheets[i];
    if (sheet && sheet.href && sheet.href.match("squarespace")) {
        sheet.disabled = true
    }
}

function displaySong(songNumber) {
  var main = document.getElementsByClassName('main')[0];

  // clear old node
  while (main.hasChildNodes()) {
      main.removeChild(main.lastChild);
  }

  // set new node
  var newNode = document.getElementsByClassName('a-' + songNumber)[0];
  main.appendChild(newNode);
}

// listen for navigation from text input
document.getElementsByClassName('navigation')[0].onsubmit = function (e) {
  e.preventDefault();
  displaySong(e.target.querySelectorAll("[name=songNumber]")[0].value);
}

// start on first page
displaySong(1);