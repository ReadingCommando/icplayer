function AddonCount_and_Graph_create() {
    var presenter = function () {};

    presenter.seriesColors = [];
    presenter.errorMode = false;
    presenter.xAxisSeriesDescriptions = [];

    presenter.selected = [];
    presenter.seriesColorsArray = [];

    var isActive = false;
    var isStarted = false;

    function returnErrorObject(errorCode) {
        return { isValid: false, errorCode: errorCode };
    }

    presenter.ERROR_MESSAGES = {
        AXIS_Y_MAXIMUM_VALUE_NOT_NUMERIC: "Y axis maximum value is not numeric",
        ANSWER_NOT_NUMERIC: "Answer \"%answer%\" is not numeric",
        C01: "Wrong color format"
    };

    presenter.showErrorMessage = function (message, substitutions) {
        var errorContainer;
        if (typeof(substitutions) == 'undefined') {
            errorContainer = '<p>' + message + '</p>';
        } else {
            var messageSubst = message;
            for (var key in substitutions) {
                messageSubst = messageSubst.replace('%' + key + '%', substitutions[key]);
            }
            errorContainer = '<p>' + messageSubst + '</p>';
        }
        presenter.$view.html(errorContainer);
    };

    presenter.setPlayerController = function (controller) {
        presenter.eventBus = controller.getEventBus();
    };

    presenter.sendEvent = function (isCorrect, column, change) {
        presenter.eventBus.sendEvent('ValueChanged', {
            'source': presenter.configuration.ID,
            'item': column + (change === "noChange" ? "" : " " + change),
            'value': '',
            'score': isCorrect ? '1' : '0'
        });
    };

    presenter.sendEventAllOk = function () {
        presenter.eventBus.sendEvent('ValueChanged', {
            'source': presenter.configuration.ID,
            'item': 'all',
            'value': '',
            'score': ''
        });
    };

    presenter.setShowErrorsMode = function () {
        if (!isStarted) return false;

        turnOffEventListeners();
        presenter.errorMode = true;

//        var goodColor = "#78AB46";
//        var badColor = "red";
//
//        var row, column;
//        for (row=0; row<presenter.configuration.axisYMaximumValue; row++) {
//            for (column=0; column<presenter.configuration.columnsCount; column++) {
//                if (row < presenter.selected[column]) {                  // if (current <= selected)
//                    if (row < presenter.configuration.answers[column]) { // if (current <= answer)
//                        presenter.plotCountGraph.series[row].seriesColors[column] = goodColor;
//                    } else {
//                        presenter.plotCountGraph.series[row].seriesColors[column] = badColor;
//                    }
//                }
//            }
//        }
//        presenter.plotCountGraph.replot();

        var column;
        for (column=0; column<presenter.configuration.columnsCount; column++) {
            var className = "";

            if (presenter.selected[column] === parseInt(presenter.configuration.answers[column], 10)) {
                className = "ok";
            } else if (presenter.selected[column] > parseInt(presenter.configuration.answers[column], 10)) {
                className = "down";
            } else {
                className = "up";
            }

            var columnClass = ".jqplot-point-" + column;
            var number = presenter.selected[column] > 0 ? presenter.selected[column] - 1 : presenter.selected[column];
            var rowClass = ".jqplot-series-" + number;
            var selector = "div" + columnClass + rowClass;
            $(selector).addClass(className);
        }

        return false;
    };

    presenter.setWorkMode = function () {
        if (!isStarted) return false;

        turnOnEventListeners();
        presenter.errorMode = false;
        cleanGraph(false);

        var row, column;
        for (column=0; column<presenter.configuration.columnsCount; column++) {
            for (row=0; row<presenter.configuration.axisYMaximumValue; row++) {
                presenter.plotCountGraph.series[row].seriesColors[column] = "white";
            }
            for (row=0; row<presenter.selected[column]; row++) {
                presenter.plotCountGraph.series[row].seriesColors[column] = presenter.seriesColors[column];
            }
        }
        presenter.plotCountGraph.replot();

        return false;
    };

    function cleanGraph(resetSelected) {
        var i, j;
        for (i=0; i<presenter.configuration.axisYMaximumValue; i++) {
            for (j=0; j<presenter.configuration.columnsCount; j++) {
                presenter.plotCountGraph.series[i].seriesColors[j] = "white";
            }
        }
        presenter.plotCountGraph.replot();

        if (resetSelected) {
            initializeSelectedArray();
        }
    }

    presenter.reset = function () {
        isStarted = false;
        cleanGraph(true);
        if (!isActive) turnOnEventListeners();
    };

    presenter.getMaxScore = function () {
        return presenter.configuration.columnsCount;
    };

    presenter.getScore = function () {
        var score = 0;
        for (var i=0; i<presenter.configuration.columnsCount; i++) {
            if (parseInt(presenter.configuration.answers[i],10) === presenter.selected[i]) score++;
        }
        return score;
    };

    presenter.getErrorCount = function () {
        var errorCount = 0;
        for (var i=0; i<presenter.configuration.columnsCount; i++) {
            if (parseInt(presenter.configuration.answers[i],10) !== presenter.selected[i]) errorCount++;
        }
        return errorCount;
    };

    presenter.getState = function() {
        if (!isStarted) return false;

        var cols = [];
        for (var i=0; i<presenter.configuration.axisYMaximumValue; i++) {
            for (var j=0; j<presenter.configuration.columnsCount; j++) {
                cols.push(presenter.plotCountGraph.series[i].seriesColors[j]);
            }
        }

        return JSON.stringify({
            selected: presenter.selected,
            isVisible: presenter.configuration.isVisible,
            colors: cols
        });
    };

    presenter.setState = function (stateString) {
        if (!stateString) return;
        var k=0;
        var state = JSON.parse(stateString);

        presenter.setVisibility(state.isVisible);
        presenter.selected = state.selected;
        presenter.configuration.isVisible = state.isVisible;
        isStarted = true;

        for (var i=0; i<presenter.configuration.axisYMaximumValue; i++) {
            for (var j=0; j<presenter.configuration.columnsCount; j++) {
                presenter.plotCountGraph.series[i].seriesColors[j] = state.colors[k++];
            }
        }

        presenter.plotCountGraph.replot();
    };

    presenter.setVisibility = function (isVisible) {
        presenter.$view.css("visibility", isVisible ? "visible" : "hidden");
    };

    presenter.hide = function () {
        presenter.setVisibility(false);
        presenter.configuration.isVisible = false;
    };

    presenter.show = function () {
        presenter.setVisibility(true);
        presenter.configuration.isVisible = true;
    };

    presenter.executeCommand = function (name, params) {
        var commands = {
            'show': presenter.show,
            'hide': presenter.hide,
            'getValue': presenter.getValue
        };

        return Commands.dispatch(commands, name, params, presenter);
    };

    function createChartArray() {
        var dataArray = [];
        var rowArray = [];
        var i;

        for (i=0; i<presenter.configuration.columnsCount; i++) {
            rowArray.push(1);
        }

        for (i=0; i<presenter.configuration.axisYMaximumValue; i++) {
            dataArray[i] = rowArray;
        }

        return dataArray;
    }

    function createSeriesColors() {
        var dataArray = [];
        var rowArray = [];
        var i;

        for (i = 0; i < presenter.configuration.columnsCount; i++) {
            rowArray.push("#fff");
        }
        for (i = 0; i < presenter.configuration.axisYMaximumValue; i++) {
            dataArray.push({seriesColors: rowArray.slice(0)});
        }

        return dataArray;
    }

    function getStringChange(previous, current) {
        if (previous < current) {
            return "increase";
        } else if (previous > current) {
            return "decrease";
        } else {
            return "noChange";
        }
    }

    function updateGraphColors(column, value) {
        var i;
        // cleaning column
        for (i=0; i<presenter.configuration.axisYMaximumValue; i++) {
            presenter.plotCountGraph.series[i].seriesColors[column] = "white";
        }
        // coloring column
        var previous = presenter.selected[column];
        var to = value > previous ? previous + 1 : previous - 1;

        for (i=0; i<to; i++) {
            presenter.plotCountGraph.series[i].seriesColors[column] = presenter.seriesColors[column];
        }
        var change = getStringChange(presenter.selected[column], to);

        presenter.selected[column] = to;
        if (presenter.selected[column] >= presenter.configuration.answers[column]) {
            var isCorrect = presenter.configuration.answers[column] ===  presenter.selected[column];
            presenter.sendEvent(isCorrect, column, change);
        }

        if (presenter.getScore() === presenter.getMaxScore()) presenter.sendEventAllOk();
    }

    function getStringTicks(from, to, step) {
        var result = [];

        for (var i=from; i<=to; i += step) {
            result.push(i.toString());
        }

        return result;
    }

    function getEmptyLabels() {
        var result = [];

        for (var i=0; i<presenter.configuration.columnsCount; i++) {
            result.push("");
        }

        return result;
    }

    function getChartOptions() {
        return {
            height: parseInt(presenter.configuration.height, 10),
            stackSeries: true,
            seriesDefaults: {
                color: "#FFF",
                renderer: $.jqplot.BarRenderer,
                rendererOptions: {
                    barMargin: 30, // Put a 30 pixel margin between bars.
                    highlightMouseDown: true,
                    barWidth: presenter.configuration.barsWidth,
                    varyBarColor: true
                },
                pointLabels:{
                    show: true,
                    labels: getEmptyLabels()
                }
            },
            series: presenter.seriesColorsArray,
            axesDefaults: {
                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
                tickOptions: {
                    fontSize: '13px'
                }
            },
            axes: {
                xaxis: {
                    renderer: $.jqplot.CategoryAxisRenderer,
                    label: presenter.configuration.axisXDescription,
                    ticks: presenter.xAxisSeriesDescriptions,
                    tickOptions: {
                        showGridline: false // whether to draw a gridline (across the whole grid) at this tick
                    }
                },
                yaxis: {
                    min: 0,
                    max: presenter.configuration.axisYMaximumValue,
                    ticks: presenter.configuration.yValues[0] === "" ? getStringTicks(0, presenter.configuration.axisYMaximumValue, 1) : presenter.configuration.yValues,
                    label: presenter.configuration.axisYDescription
                }
            },
            grid: {
                backgroundColor: presenter.configuration.backgroundColor,
                gridLineColor: presenter.configuration.gridLineColor,
                borderColor: 'black',
                borderWidth: presenter.configuration.border
            }
        };
    }

    presenter.run = function(view, model) {
        presenter.initialize(view, model, false);
    };

    presenter.createPreview = function(view, model) {
        presenter.initialize(view, model, true);
    };

    // Border always will be in <0, 5>
    function parseBorder(border) {
        border = border || 0;

        if (border > 5) return 5;
        if (border < 0) return 0;

        return border;
    }

    function parseColor(color) {

        if (color[0] === '#' && (color.length !== 4 || color.length !== 7)) {
            return returnErrorObject("C01");
        }

        return {
            value: color,
            isValid: true
        }
    }

    function parseYTicks(ticks) {
        if (ticks[ticks.length-1] === "*") {
            //console.log("nie ma", presenter.configuration.axisYMaximumValue);
            var len = presenter.tmpMaxYValue == undefined ? 10 : presenter.tmpMaxYValue;
            var inc = parseInt(ticks.split('*')[0], 10);
            ticks = "";
            for (var i=0; i<=len; i += inc) {
                ticks += i + ';';
            }
            // remove last character
            ticks = ticks.slice(0, -1);
        }

        ticks = ticks.split(';').map(function(n) {
            return n.toString();
        });

        return {
            value: ticks,
            isValid: true
        }
    }

    function parseXAxisData(data) {
        var len = data.length;
        var answers = [];
        var colors = [];
        var desc = [];
        var descImg = [];
        var imgString = "<img class='countGraph_image' src='[SRC]'>";

        for (var i=0; i<len; i++) {
            if (isNaN(data[i]["Answer"])) {
                return {
                    isValid: false,
                    errorCode: "ANSWER_NOT_NUMERIC"
                }
            }
            answers.push(data[i]["Answer"]);

            colors.push(data[i]["Color"]);

            // TODO zwrucić w configuracji
            presenter.seriesColors.push(data[i]["Color"]);
            desc.push(data[i]["Description"]);
            descImg.push(data[i]["Description image"]);

            // TODO zwrucić w configuracji
            if (data[i]["Description"] === "" && data[i]["Description image"] !== "") {
                presenter.xAxisSeriesDescriptions.push(imgString.replace("[SRC]", data[i]["Description image"]));
            } else {
                presenter.xAxisSeriesDescriptions.push(data[i]["Description"]);
            }
        }

        return {
            isValid: true,
            answers: answers,
            numberOfColumns: len
        }
    }

    function parseBarsWidth(barsWidth) {
        barsWidth = barsWidth || 5;
        barsWidth = parseInt(barsWidth, 10);

        return barsWidth < 5 ? 5 : barsWidth;
    }

    presenter.validateModel = function(model) {
        //console.log(model);

        var parsedXAxisData = parseXAxisData(model["X axis data"]);
        if (!parsedXAxisData.isValid) {
            return returnErrorObject(parsedXAxisData.errorCode);
        }

        // Y-axis maximum value
        var axisYMaximumValue = ModelValidationUtils.validateFloat(model['Y axis maximum value']);
        if (!axisYMaximumValue.isValid) {
            return returnErrorObject("AXIS_Y_MAXIMUM_VALUE_NOT_NUMERIC");
        }

        presenter.tmpMaxYValue = axisYMaximumValue.value;
        //console.log("presenter.tmpMaxYValue", presenter.tmpMaxYValue);

        var parsedYTicks = parseYTicks(model["Y axis values"]);
        if (!parsedYTicks.isValid) {
            return returnErrorObject("AXIS_Y_MAXIMUM_VALUE_NOT_NUMERIC");
        }

        var parsedBGColor = parseColor(model["Background color"]);
        if (!parsedBGColor.isValid) {
            return returnErrorObject(parsedBGColor.errorCode);
        }

        var parsedGridLineColor = parseColor(model["Grid line color"]);
        if (!parsedGridLineColor.isValid) {
            return returnErrorObject(parsedGridLineColor.errorCode);
        }

        var parsedBarsWidth = parseBarsWidth(model['Bars width']);

        return {
            axisXDescription: model['X axis description'],
            axisYDescription: model['Y axis description'],
            axisYMaximumValue: axisYMaximumValue.parsedValue,

            answers: parsedXAxisData.answers,
            barsWidth: parsedBarsWidth,
            columnsCount: parsedXAxisData.numberOfColumns,
            width: parseInt(model["Width"], 10),
            height: parseInt(model["Height"], 10),
            backgroundColor: parsedBGColor.value,
            gridLineColor: parsedGridLineColor.value,
            border: parseBorder(parseInt(model["Border"], 10)),
            yValues: parsedYTicks.value,

            isVisible: ModelValidationUtils.validateBoolean(model["Is Visible"]),
            ID: model.ID,
            isValid: true
        };
    };

    function initializeSelectedArray() {
        for (var i = 0; i < presenter.configuration.columnsCount; i++) {
            presenter.selected[i] = 0;
        }
    }

    function turnOnEventListeners() {
        var $graph = presenter.$view.find('#' + presenter.plotID);
        $graph.on('jqplotDataClick', function(e, seriesIndex, pointIndex, data) {
            e.stopPropagation();
            updateGraphColors(pointIndex, seriesIndex + 1);
            presenter.plotCountGraph.replot();
            isStarted = true;
        });
        isActive = true;
    }

    function turnOffEventListeners() {
        var $graph = presenter.$view.find('#' + presenter.plotID);
        $graph.off('jqplotDataClick');
        isActive = false;
    }

    presenter.initialize = function (view, model, isPreview) {
        presenter.$view = $(view);
        presenter.configuration = presenter.validateModel(model);

        if (!presenter.configuration.isValid) {
            presenter.showErrorMessage(presenter.ERROR_MESSAGES[presenter.configuration.errorCode]);
            return;
        }

        presenter.setVisibility(presenter.configuration.isVisible);

        initializeSelectedArray();
        presenter.seriesColorsArray = createSeriesColors();

        presenter.plotID = presenter.configuration.ID + "plot";
        presenter.$view.find("div").attr('id', presenter.plotID);
        presenter.plotCountGraph = $.jqplot(presenter.plotID, createChartArray(), getChartOptions());

        presenter.$view.on("click", function(e) {
            e.stopPropagation();
            e.preventDefault();
        });

        if (!isPreview) {
            turnOnEventListeners();
        }
    };

    return presenter;
}