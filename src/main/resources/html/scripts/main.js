function users_radioboxClick() {
    document.users_form.submit();
}


function system_radioboxClick() {
    var radios = document.getElementsByName('dbclean_auto_cleaning');
    for (var i = 0, length = radios.length; i < length; i++){
        if (radios[i].checked){
            var disabled = (radios[i].value == "off");
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