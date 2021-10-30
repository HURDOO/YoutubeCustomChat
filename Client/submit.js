let chatinput = document.getElementById("chatinput");

chatinput.addEventListener("submit", function(e) {
    e.preventDefault();

    //$("chatbox").append(genmsg($("newchatinput").attr("value")));
    document.getElementById("chatbox").insertAdjacentHTML('beforeend',genmsg(document.getElementById("text").value));
    //console.log('submitted');
    document.getElementById('chatbox').scrollTo(0,document.getElementById('chatbox').scrollHeight);
    document.getElementById('text').value = '';
});

function genmsg(content) {
    return '<div id="chat1" class="chatitem">\n<span class="name" style="color: purple;">가나다123abc</span>\n<span class="msg">' + content + '</span>\n</div>';
}