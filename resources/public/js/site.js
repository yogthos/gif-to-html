var totalFrames;
var frame;
var t;

function showNextFrame() {
  document.getElementById('frame-' + ((frame > 0) ? frame - 1 : totalFrames)).style.display = 'none';
  document.getElementById('frame-' + frame).style.display = 'block';
  frame = (frame < totalFrames) ? frame + 1 : 0;
  t = setTimeout(showNextFrame, 150);
}

function render (response) {
  t = 0;
  frame = 0;
  totalFrames = response.frames - 1;
  $("#output").html(response.data);
  $(".loader").hide();
  showNextFrame();
}

function convertImageUrl() {
  $("#output").empty();
  $(".loader").show();
  $.post(context + "/convertImage", {url: $('#url').val()}, render);
}

$(function() {
  $("#upload").click(convertImageUrl);
});
