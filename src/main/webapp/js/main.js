// Switch thumbnail- to listview and back again
$(document).ready(function () {
  $('#list').click(function (event) {
    event.preventDefault();

    // Change button state
    $(this).addClass('active');
    $('#grid').removeClass('active');

    $('#search-results .search-result-item').removeClass('grid-group-item').addClass('list-group-item');
  });
  $('#grid').click(function (event) {
    event.preventDefault();

    // Change button state
    $(this).addClass('active');
    $('#list').removeClass('active');

    $('#search-results .search-result-item').removeClass('list-group-item').addClass('grid-group-item');
  });
});