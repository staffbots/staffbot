function users_radioboxClick() {
    document.users_form.submit();
}


function system_radioboxClick() {
    var radios = document.getElementsByName('dbclean_radiobox');
    for (var i = 0, length = radios.length; i < length; i++){
        if (radios[i].checked){
            var disabled = (radios[i].value == "off");
            break;
        }
    }
    //alert(enableElements);
    document.getElementsByName('dbclean_auto_value')[0].disabled = disabled;
    document.getElementsByName('dbclean_auto_measure')[0].disabled = disabled;
    document.getElementsByName('dbclean_auto_start')[0].disabled = disabled;
}
