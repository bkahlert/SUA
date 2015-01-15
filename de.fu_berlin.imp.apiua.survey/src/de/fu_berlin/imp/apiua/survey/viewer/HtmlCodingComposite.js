function setCodeMarkup(fragment, markup) {
  var c = $($("[data-fragment=" + fragment + "]").parent().find('.codeable'));
  if(markup) c.addClass('coded');
  else c.removeClass('coded');
  
  var markupContainer = c.find('.markup');  
  if(markupContainer.length == 0) markupContainer = $('<span class="markup"></span>').prependTo(c);

  if(markup) markupContainer.html(markup);
  else markupContainer.remove();
}

window.custominit = true;