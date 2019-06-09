
function show_control_time(){
    if (!document.getElementsByName('control_stop')[0].disabled) {
        var starttime = document.getElementsByName('control_start_time')[0].value;
        var currenttime = new Date().getTime();
        var dt = Math.round((currenttime - starttime) / 1000);
        var sec = dt%60;                        sec = (sec > 9) ? sec : '0' + sec;
        var min = Math.trunc(dt/60)%60;         min = (min > 9) ? min : '0' + min;
        var hou = Math.trunc(dt/(60*60))%60;    hou = (hou > 9) ? hou : '0' + hou;
        var day = Math.trunc(dt/(60*60*24))%24;
        var str = 'days';
//        var str = 'дней';
//        var ost = day%10;
//        if ((ost == 1)&&(ost != 11)) str = 'день';
//        if ((ost in [2, 3, 4])&&!((ost in [12, 13, 14]))) str = 'дня';
        //document.getElementsByName('control_time')[0].innerHTML = new Date(time).toTimeString();
        document.getElementsByName('control_process_time')[0].innerHTML =
        ((day > 0) ? (day + ' ' + str + ' ') : '') + hou + ':'+ min + ':'+ sec;
        setTimeout(show_control_time, 500);
    }
}


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
    document.getElementsByName('users_new_login')[0].value = '';
    if (document.getElementsByName('users_select_login')[0].length == 0){
        radios[newUserMode ? 0 : 1].checked = true;
        newUserMode = true;
        oldUserMode = false;
        document.getElementsByName('users_new_login')[0].value = 'newLogin';
    }
    radios[newUserMode ? 0 : 1].checked = true;
    document.getElementsByName('users_new_login')[0].disabled = oldUserMode;
    document.getElementsByName('users_select_login')[0].disabled = newUserMode;
    document.getElementsByName('users_delete')[0].disabled = newUserMode;
    //alert(Boolean(!newUserMode));
}


function system_radioboxClick() {
    var radios = document.getElementsByName('dbclean_auto_cleaning');
    var disabled;
    for (var i = 0, length = radios.length; i < length; i++){
        if (radios[i].checked){
            disabled = (radios[i].value == 'off');
            break;
        }
    }
    document.getElementsByName('dbclean_auto_value')[0].disabled = disabled;
    document.getElementsByName('dbclean_auto_measure')[0].disabled = disabled;
    document.getElementsByName('dbclean_auto_start')[0].disabled = disabled;
}

function journal_todate_onClick(){
    var todate = document.getElementsByName('journal_todate')[0];
    todate.disabled = !document.getElementsByName('journal_todate_on')[0].checked;
    if (todate.disabled)
        todate.value = "";
}

function journal_fromdate_onClick(){
    var todate = document.getElementsByName('journal_fromdate')[0];
    todate.disabled = !document.getElementsByName('journal_fromdate_on')[0].checked;
    if (todate.disabled)
        todate.value = "";
}