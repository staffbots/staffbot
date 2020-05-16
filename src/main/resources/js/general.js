//////////////////////////////////////////////////////////////////////////////////////
// Base
//////////////////////////////////////////////////////////////////////////////////////

$(document).ready(function(){
    $('select[name=language_select]').ImageSelect();
});

var updateDelay = 10000;

function setUpdateDelay(){
    updateDelay = Number(element('update_delay').value);
}

//Возвращает (первый) элемент с указанным именем
function element(name){
    return $('[name="' + name + '"]')[0];
}

// Показ продолжительности исполенения процесса
function updateTime(){
    var starttime = element('start_time').value;
    if (starttime > 0) {
        var currenttime = new Date().getTime();
        var dt = Math.round((currenttime - starttime) / 1000);
        var sec = dt%60;                        sec = (sec > 9) ? sec : '0' + sec;
        var min = Math.trunc(dt/60)%60;         min = (min > 9) ? min : '0' + min;
        var hou = Math.trunc(dt/(60*60))%24;    hou = (hou > 9) ? hou : '0' + hou;
        var day = Math.trunc(dt/(60*60*24));
        var str = '-';
        element('process_time').innerHTML =
        ((day > 0) ? (day + ' ' + str + ' ') : '') + hou + ':'+ min + ':'+ sec;
    }
    setTimeout(updateTime, 1000);
}

// Обновление списка текущих заданий по таймеру
function updateTasks(){
    $.get(
        '/status?get=tasklist',
        function(data) {
            $('#task_list').html(data);
        }
    ).fail(function() {
                 location.reload();
             });
    setTimeout(updateTasks, updateDelay);
}

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////

// Отработка нажатия checbox связанного с полем ввода
function dateClick(dateCheckbox, dateName){
    var date = element(dateName);
    date.disabled = !element(dateCheckbox).checked;
    if (date.disabled)
        date.value = '';
}

//////////////////////////////////////////////////////////////////////////////////////
// Status
//////////////////////////////////////////////////////////////////////////////////////

// Обновление списка текущих заданий по таймеру
function updateStatus(){
    $.get(
        '/status?get=processstatus',
        function(data) {
            $('#process_status').html(data);
        }
    ).fail(function() {
        location.reload();
    });
    setTimeout(updateStatus, updateDelay);
}


// Обновление значения
function updateValue(name){
    if (name == '') return;
    $.get(
        '/status?get=' + name,
        function(data) {
            $('#' + name + '_value').html(data);
        }).fail(function() {
            location.reload();
        });

    setTimeout(updateValue, updateDelay, name);
}

//////////////////////////////////////////////////////////////////////////////////////
// System
//////////////////////////////////////////////////////////////////////////////////////

// Отработка нажатия radiobox
function system_radioboxClick() {
    var radios = document.getElementsByName('dbclean_auto_cleaning');
    var disabled;
    for (var i = 0, length = radios.length; i < length; i++){
        if (radios[i].checked){
            disabled = (radios[i].value == 'off');
            break;
        }
    }
    element('dbclean_auto_value').disabled = disabled;
    element('dbclean_auto_measure').disabled = disabled;
    if (disabled)
        element('dbclean_auto_start').value = "";
    element('dbclean_auto_start').disabled = disabled;
}

