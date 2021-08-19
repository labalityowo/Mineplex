$(document).ready(function() {
    /**
     * Remove leading and trailing whitespace in username span
     */
    $('#log').find("> .log-line > .remove-whitespace > span").filter(function() {
        return $(this).text() === ': ';
    }).prev().each(function() {
        $(this).text($(this).text().trim());
    });

    /**
     * Remove newlines and whitespace in tags with the '.remove-whitespace' class.
     */
    $('.remove-whitespace').contents().filter(function() {
        return this.nodeType = Node.TEXT_NODE && /\S/.test(this.nodeValue) === false;
    }).remove();
});
