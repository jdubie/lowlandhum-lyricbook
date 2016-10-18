/* reset squarespace css */
for (var i in document.styleSheets) {
    var sheet = document.styleSheets[i];
    if (sheet && sheet.href && sheet.href.match("squarespace")) {
        sheet.disabled = true
    }
}

function displaySong(songNumber) {
  console.log('displaySong', songNumber)
  var main = document.getElementsByClassName('main')[0];
  var articles = document.getElementsByClassName('articles')[0];

  var newNode = document.getElementsByClassName('a-' + songNumber)[0];
  if (newNode) {
    console.log('displaySong', 'updating')

    // clear old node
    //
    for (var i = 0; i < main.children.length; i++) {
      articles.appendChild(main.children[i]);
    }

    // set new node
    main.appendChild(newNode);
  }
}

var curSong = null;

var textInput = document.getElementsByClassName('navigation')[0].querySelectorAll("[name=songNumber]")[0]

textInput.onblur = function (e) {
  //e.target.value = curSong;
}

textInput.onfocus = function (e) {
  //e.target.value = null;
}

textInput.onkeyup = function (e) {
  console.log('onkeyup', e.target.value)

  var nextValue = e.target.value;
  var curSong = nextValue;
  displaySong(nextValue);
}

// start on first page
displaySong(1);