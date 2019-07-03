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
// Status
//////////////////////////////////////////////////////////////////////////////////////

// Отработка нажатия кнопки (LeverButton)
function control_button_onclick(name){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/control?set="+name);
    xhr.send();
}

//////////////////////////////////////////////////////////////////////////////////////
// Status
//////////////////////////////////////////////////////////////////////////////////////

// Обновление списка текущих заданий по таймеру
function update_process_status(){

    $.get(
        '/status?get=processstatus',
        function(data) {
            $('#process_status').html(data);
        }
    );
    setTimeout(update_process_status, 1000);
}

// Обновление списка текущих заданий по таймеру
function update_task_list(){
    $.get(
        '/status?get=tasklist',
        function(data) {
            $('#task_list').html(data);
        }
    );
    setTimeout(update_task_list, 1000);
}

// Показ продолжительности исполенения процесса
function update_process_time(){
    var starttime = element('start_time').value;
    if (starttime > 0) {
        var currenttime = new Date().getTime();
        var dt = Math.round((currenttime - starttime) / 1000);
        var sec = dt%60;                        sec = (sec > 9) ? sec : '0' + sec;
        var min = Math.trunc(dt/60)%60;         min = (min > 9) ? min : '0' + min;
        var hou = Math.trunc(dt/(60*60))%60;    hou = (hou > 9) ? hou : '0' + hou;
        var day = Math.trunc(dt/(60*60*24))%24;
        var str = '-';
        element('process_time').innerHTML =
        ((day > 0) ? (day + ' ' + str + ' ') : '') + hou + ':'+ min + ':'+ sec;
    }
    setTimeout(update_process_time, 500);
}


// Обновление значения
function update_status_value(name){
    if (name == '') return;
    $.get(
        '/status?get=' + name,
        function(data) {
            $('#' + name + '_value').html(data);
        }
    );
    setTimeout(update_status_value, 1000, name);
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
    if (disabled)
        element('dbclean_auto_start').value = "";
    element('dbclean_auto_start').disabled = disabled;
}

