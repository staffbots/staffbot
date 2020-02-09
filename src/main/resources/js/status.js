//////////////////////////////////////////////////////////////////////////////////////
// Control
//////////////////////////////////////////////////////////////////////////////////////

dateClick('plot_fromdate_checkbox', 'plot_fromdate');
dateClick('plot_todate_checkbox', 'plot_todate');
updateTime();
updateTasks();
updateStatus();

$.each(datasets, function(key, val) {
    $('#' + key + '_checkbox').click(checkboxClick);
	$('#' + key + '_cell').css('background', val.color);
});

checkboxClick();

function checkboxClick(){
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

    		$('#data_time').text(moment(new Date(pos.x.toFixed(0) * 1000)).format('DD.MM.YYYY HH:mm:ss'));
			$('#data_value').text(pos.y.toFixed(3));

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
