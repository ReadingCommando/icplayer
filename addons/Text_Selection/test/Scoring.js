TestCase('[Text Selection] scoring tests', {
    setUp : function() {
        this.presenter = AddonText_Selection_create();
        this.presenter.$view = $('<div><div class="text_selection"></div></div>');

        this.presenter.$view.find('.text_selection')
            .append($('<span number="4" class="selected">a</span>'))
            .append($('<span number="5" class="selected">b</span>'))
            .append($('<span number="6" class="selected">c</span>'));

        this.presenter.configuration = {
            isActivity: true
        };
    },

    'test getErrorCount noerrors' : function() {
        this.presenter.markers = { markedWrong: [1, 2, 3] };
        assertEquals(0, this.presenter.getErrorCount());
    },

    'test getErrorCount one error' : function() {
        this.presenter.markers = { markedWrong: [1, 2, 4] };
        assertEquals(1, this.presenter.getErrorCount());
    },

    'test getMaxScore equals zero' : function() {
        this.presenter.markers = { markedCorrect: [] };
        assertEquals(0, this.presenter.getMaxScore());
    },

    'test getMaxScore equals one' : function() {
        this.presenter.markers = { markedCorrect: [1] };
        assertEquals(1, this.presenter.getMaxScore());
    },

    'test getScore equals zero' : function() {
        this.presenter.markers = { markedCorrect: [1, 2, 3] };
        assertEquals(0, this.presenter.getScore());
    },

    'test getScore equals one' : function() {
        this.presenter.markers = { markedCorrect: [1, 2, 4] };
        assertEquals(1, this.presenter.getScore());
    }
});