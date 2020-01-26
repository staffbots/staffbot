//////////////////////////////////////////////////////////////////////////////////////
// Control
//////////////////////////////////////////////////////////////////////////////////////
//includeJQuery();
    date_onClick('status_fromdate_on', 'status_fromdate');
    date_onClick('status_todate_on', 'status_todate');
    updateTime();
    updateTasks();
    update_process_status();

$.each(datasets, function(key, val) {
    $('#' + key + '_checkbox').click(status_checkbox_onClick);
	$('#' + key + '_cell').css('background', val.color);
});

status_checkbox_onClick();

function status_checkbox_onClick(){
			var data = [];

			$.each(datasets, function(key, val) {
				if ($('#' + key + '_checkbox').is(':checked'))
					data.push(datasets[key]);
			});


		var plot = $.plot('#placeholder', data, {
			grid: {
				hoverable: true,
				clickable: true
			},
			xaxis: {
				mode: 'time',
				timezone: 'browser',
				timeformat: '%H:%M:%S'
			}
		});

		$('<div id="tooltip"></div>').css({
			position: 'absolute',
			display: 'none',
			border: '1px solid #fdd',
			padding: '2px',
			'background-color': '#fee',
			opacity: 0.80
		}).appendTo('body');

		$('#placeholder').bind('plothover', function (event, pos, item) {

    		$('#status_data_time').text(moment(new Date(pos.x.toFixed(0) * 1000)).format('DD.MM.YYYY HH:mm:ss'));
			$('#status_data_value').text(pos.y.toFixed(3));

			if (item) {
				var x = item.datapoint[0].toFixed(0),
					y = item.datapoint[1].toFixed(item.series.precision),
                    d = moment(new Date(x * 1000)).format('DD.MM.YYYY HH:mm:ss');
				$('#tooltip').html(item.series.label + ': <b>' + y + '</b><br>' + d)
					.css({top: item.pageY+5, left: item.pageX+5})
					.fadeIn(200);
			} else {
				$('#tooltip').hide();
			}
		});

		$('#placeholder').bind('plothovercleanup', function (event, pos, item) {
				$('#tooltip').hide();
		});

    };

function includeJQuery() {
    var url = '/resource?js/jquery/';
    include(url + 'jquery.canvaswrapper.js');
    include(url + 'jquery.colorhelpers.js');
    include(url + 'jquery.event.drag.js');
    include(url + 'jquery.mousewheel.js');
    var url = '/resource?js/jquery/flot/';
    include(url + 'jquery.flot.js');
    include(url + 'jquery.flot.saturated.js');
    include(url + 'jquery.flot.browser.js');
    include(url + 'jquery.flot.drawSeries.js');
    include(url + 'jquery.flot.errorbars.js');
    include(url + 'jquery.flot.uiConstants.js');
    include(url + 'jquery.flot.logaxis.js');
    include(url + 'jquery.flot.symbol.js');
    include(url + 'jquery.flot.flatdata.js');
    include(url + 'jquery.flot.navigate.js');
    include(url + 'jquery.flot.fillbetween.js');
    include(url + 'jquery.flot.stack.js');
    include(url + 'jquery.flot.touchNavigate.js');
    include(url + 'jquery.flot.hover.js');
    include(url + 'jquery.flot.touch.js');
    include(url + 'jquery.flot.time.js');
    include(url + 'jquery.flot.axislabels.js');
    include(url + 'jquery.flot.selection.js');
    include(url + 'jquery.flot.composeImages.js');
    include(url + 'jquery.flot.legend.js');
    include(url + 'jquery.flot.spline.js');
    console.log('Скрипты jquery загружены');
}
