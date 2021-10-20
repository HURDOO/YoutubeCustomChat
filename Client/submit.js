let chatinput = document.getElementById("chatinput");

chatinput.addEventListener("submit", function(e) {
    e.preventDefault();

    //$("chatbox").append(genmsg($("newchatinput").attr("value")));
    document.getElementById("chatbox").insertAdjacentHTML('beforeend',genmsg(document.getElementById("newchatinput").value));
    //console.log('submitted');
});

function genmsg(content = String) {
    return '<div id="chat1" class="chatitem">\n<span class="name" style="color: purple;">가나다123abc</span>\n<span class="msg">' + content + '</span>\n</div>';
}