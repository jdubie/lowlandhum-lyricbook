// ------------------------------------------------------------------------------
// Removing Squarespace
// ------------------------------------------------------------------------------

function unsquarespace() {
  // wipe css
  for (var i in document.styleSheets) {
      var sheet = document.styleSheets[i];
      if (sheet && sheet.href && sheet.href.match("squarespace")) {
          sheet.disabled = true
      }
  }

  // content
  var lhNodes = Array.prototype.slice.call(document.getElementsByClassName('lh'));
  while (document.body.hasChildNodes()) {
    document.body.removeChild(document.body.lastChild);
  }
  for (var i = 0; i < lhNodes.length; i++) {
    document.body.appendChild(lhNodes[i]);
  }
}

// ------------------------------------------------------------------------------
// Updating song
// ------------------------------------------------------------------------------

// state
var curSong = -2;

function displaySong(songNumber) {
  console.log('displaySong', songNumber)
  var main = document.getElementsByClassName('main')[0];
  var articles = document.getElementsByClassName('articles')[0];

  var newNode = document.getElementsByClassName('a-' + songNumber)[0];
  if (newNode) {

    // update state
    curSong = songNumber;
    if (songNumber < 0) {
      textInput.value = "#";
    } else {
      textInput.value = curSong;
    }
    console.log('displaySong', 'updating')

    // clear old node
    for (var i = 0; i < main.children.length; i++) {
      articles.appendChild(main.children[i]);
    }

    // set new node
    main.appendChild(newNode);
  }
}

// ------------------------------------------------------------------------------
// Navigation text input
// ------------------------------------------------------------------------------

var textInput = document.getElementsByClassName('navigation')[0].querySelectorAll("[name=songNumber]")[0]

textInput.onblur = function (e) {
  console.log("blur", curSong)
  e.target.value = curSong;
}

textInput.onfocus = function (e) {
  console.log("focus")
  e.target.value = null;
}

textInput.onkeyup = function (e) {
  console.log('onkeyup', e.target.value)

  var nextValue = parseInt(e.target.value);
  displaySong(nextValue);
}

// ------------------------------------------------------------------------------
// Swiping
// http://stackoverflow.com/questions/2264072/detect-a-finger-swipe-through-javascript-on-the-iphone-and-android
// ------------------------------------------------------------------------------

var xDown = null;
var yDown = null;

function handleTouchStart(evt) {
    xDown = evt.touches[0].clientX;
    yDown = evt.touches[0].clientY;
};

function handleTouchMove(evt) {
    if ( ! xDown || ! yDown ) {
        return;
    }

    var xUp = evt.touches[0].clientX;
    var yUp = evt.touches[0].clientY;

    var xDiff = xDown - xUp;
    var yDiff = yDown - yUp;

    if ( Math.abs( xDiff ) > Math.abs( yDiff ) ) {
        if ( xDiff > 0 ) {
          /* left swipe */
          displaySong(curSong + 1)
        } else {
          /* right swipe */
          displaySong(curSong - 1)
        }
    } else {
        if ( yDiff > 0 ) {
            /* up swipe */
        } else {
            /* down swipe */
        }
    }
    /* reset values */
    xDown = null;
    yDown = null;
};

// ------------------------------------------------------------------------------
// Entrypoint
// ------------------------------------------------------------------------------

// start on first page
displaySong(curSong);

unsquarespace();
document.addEventListener("DOMContentLoaded", function(event) {
  unsquarespace();
});

document.addEventListener('touchstart', handleTouchStart, false);
document.addEventListener('touchmove', handleTouchMove, false);
