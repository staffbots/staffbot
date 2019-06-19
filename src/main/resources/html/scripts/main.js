//////////////////////////////////////////////////////////////////////////////////////
// Staffbot
//////////////////////////////////////////////////////////////////////////////////////

//Возвращает (первый) элемент с указанным именем
function element(name){
    return $("[name='" + name + "']")[0];
}

// Отработка нажатия checbox связанного с полем ввода
function date_onClick(checkname, fieldname){
    var fromdate = element(fieldname);
    fromdate.disabled = !element(checkname).checked;
    if (fromdate.disabled)
        fromdate.value = "";
}

//////////////////////////////////////////////////////////////////////////////////////
// Control
//////////////////////////////////////////////////////////////////////////////////////

// Показ продолжительности исполенения процесса
function show_control_time(){
    if (!element('control_stop').disabled) {
        var starttime = document.getElementsByName('control_start_time')[0].value;
        var currenttime = new Date().getTime();
        var dt = Math.round((currenttime - starttime) / 1000);
        var sec = dt%60;                        sec = (sec > 9) ? sec : '0' + sec;
        var min = Math.trunc(dt/60)%60;         min = (min > 9) ? min : '0' + min;
        var hou = Math.trunc(dt/(60*60))%60;    hou = (hou > 9) ? hou : '0' + hou;
        var day = Math.trunc(dt/(60*60*24))%24;
        var str = '-';
        element('control_process_time').innerHTML =
        ((day > 0) ? (day + ' ' + str + ' ') : '') + hou + ':'+ min + ':'+ sec;
        setTimeout(show_control_time, 500);
    }
}

// Обновление списка текущих заданий по таймеру
function update_control_tasklist(){
    $.get(
        '/control?get=tasklist',
        function(data) {
            $('#control_tasklist').html(data);
        }
    );
    setTimeout(update_control_tasklist, 1000);
}

//////////////////////////////////////////////////////////////////////////////////////
// Status
//////////////////////////////////////////////////////////////////////////////////////

// Обновление значения
function update_status_value(name){
    $.get(
        '/status?get=' + name,
        function(data) {
            $('#' + name + '_value').html(data);
        }
    );
    setTimeout(update_status_value(name), 10000);
}


//////////////////////////////////////////////////////////////////////////////////////
// Users
//////////////////////////////////////////////////////////////////////////////////////

// Отработка нажатия radiobox
function users_radioboxClick() {
    var radios = document.getElementsByName('users_radiobox');
    var newUserMode = false;
    var oldUserMode = true;
    for (var i = 0, length = radios.length; i < length; i++){
        if (radios[i].checked){
            newUserMode = (radios[i].value == 'new');
            oldUserMode = (radios[i].value == 'old');
            break;
        }
    }
    element('users_new_login').value = '';
    if (element('users_select_login').length == 0){
        radios[newUserMode ? 0 : 1].checked = true;
        newUserMode = true;
        oldUserMode = false;
        element('users_new_login').value = 'newLogin';
    }
    radios[newUserMode ? 0 : 1].checked = true;
    element('users_new_login').disabled = oldUserMode;
    element('users_select_login').disabled = newUserMode;
    element('users_delete').disabled = newUserMode;
    //alert(Boolean(!newUserMode));
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
    element('dbclean_auto_start').disabled = disabled;
}

