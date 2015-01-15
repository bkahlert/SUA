function debounce(func, wait, immediate) {
	var timeout;
	return function() {
		var context = this, args = arguments;
		var later = function() {
			timeout = null;
			if (!immediate) func.apply(context, args);
		};
		var callNow = immediate && !timeout;
		clearTimeout(timeout);
		timeout = setTimeout(later, wait);
		if (callNow) func.apply(context, args);
	};
};

var setCodeMarkup = function(id, markup) {
  var c = $($("[id]").filter(function() { return $(this).attr('id') == id}).parent().find('.codeable'));
  if(markup) c.addClass('coded');
  else c.removeClass('coded');
  
  var markupContainer = c.find('.markup');  
  if(markupContainer.length == 0) markupContainer = $('<span class="markup"></span>').prependTo(c);

  if(markup) markupContainer.html(markup);
  else markupContainer.remove();
}

function getCodeableIds() {
	var ids = [];
	$(".codeable").each(function() {
		ids.push($(this).attr('id'));
	});
	return ids;
}

window.custominit = true;