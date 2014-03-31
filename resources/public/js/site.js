
var animation;

function showNextFrame(totalFrames, frame, delay) {
  $('#frame-' + ((frame > 0) ? frame - 1 : totalFrames)).hide();
  $('#frame-' + frame).show();  
  animation = setTimeout(function(){showNextFrame(totalFrames, (frame < totalFrames) ? frame + 1 : 0, delay);}, delay);
}

function render (response) {
  clearTimeout(animation);
  $(".loader").hide();
  if (response.error) {
    $("#error").html(response.error);
  }
  else {
    $("#output").html(response.data);
    $("#share").show();    
    animation = showNextFrame(response.frames - 1, 0, response.delay*10);
  }
}

function convertImageUrl() {
  $("#output").empty();
  $("#error").empty();
  $(".loader").show();
  $("#share").hide();
  $.post(context + "/convertImage", {url: $('#url').val()}, render);
}

function shareLink() {
  window.prompt("Share the link: ", "http://gif-to-html.herokuapp.com/?" + $('#url').val());
}

$(function() {
  $("#upload").click(convertImageUrl);
  $("#share").click(shareLink);
  if (autorun) convertImageUrl();
});
