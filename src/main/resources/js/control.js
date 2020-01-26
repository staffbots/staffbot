//////////////////////////////////////////////////////////////////////////////////////
// Control
//////////////////////////////////////////////////////////////////////////////////////

function leverChange() {
    element('apply_button').disabled = false;
    element('config_name').value = '';
}

function configChange() {
    var value = element('config_name').value;
    var option = document.querySelector('#config_list option[value="'+value+'"]');
    element('load_button').disabled = (option==null);
    element('delete_button').disabled = (option==null);
}