
var animation;

function showNextFrame(totalFrames, frame) {
  $('#frame-' + ((frame > 0) ? frame - 1 : totalFrames)).hide();
  $('#frame-' + frame).show();
  setTimeout(function(){showNextFrame(totalFrames, (frame < totalFrames) ? frame + 1 : 0);}, 150);
}

function render (response) {
  $("#output").html(response.data);
  $(".loader").hide();
  clearTimeout(animation);
  animation = showNextFrame(response.frames - 1, 0);
}

function convertImageUrl() {
  $("#output").empty();
  $(".loader").show();
  $.post(context + "/convertImage", {url: $('#url').val()}, render);
}

$(function() {
  $("#upload").click(convertImageUrl);
});
