
var animation;

function showNextFrame(totalFrames, frame) {
  $('#frame-' + ((frame > 0) ? frame - 1 : totalFrames)).hide();
  $('#frame-' + frame).show();
  animation = setTimeout(function(){showNextFrame(totalFrames, (frame < totalFrames) ? frame + 1 : 0);}, 150);
}

function render (response) {
  clearTimeout(animation);
  $("#output").html(response.data);
  $(".loader").hide();
  $("#share").show();
  animation = showNextFrame(response.frames - 1, 0);
}

function convertImageUrl() {
  $("#output").empty();
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
});
