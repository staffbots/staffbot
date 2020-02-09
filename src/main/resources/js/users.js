//////////////////////////////////////////////////////////////////////////////////////
// Users
//////////////////////////////////////////////////////////////////////////////////////

// Отработка нажатия radiobox
function radioboxClick() {
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
    newlogin = element('new_login');
    selectlogin = element('select_login');
    newlogin.value = '';
    if (selectlogin.length == 0){
        radios[newUserMode ? 0 : 1].checked = true;
        newUserMode = true;
        oldUserMode = false;
        newlogin.value = 'newLogin';
    }
    radios[newUserMode ? 0 : 1].checked = true;
    newlogin.disabled = oldUserMode;
    selectlogin.disabled = newUserMode;
    element('delete_button').disabled = newUserMode;
    rolelist();
}

function rolelist(){
    selectlogin = element('select_login');
    login = selectlogin.disabled ? '' : selectlogin.value;
    $.get(
        '/users?get=role_list&login_name=' + login,
        function(data) {
            $('#role_list').html(data);
        }
    );
}