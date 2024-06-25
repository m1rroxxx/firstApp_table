function toggleRegistration() {
    document.querySelector(".registration.modal-background").classList.toggle("active");
}

async function sendRegistrationForm() {

    let name = document.getElementById("name");
    let lastName = document.getElementById("last-name");
    let email = document.getElementById("email");
    let password = document.getElementById("password");
    let repeatPassword = document.getElementById("repeat-password");

    let tooltip = document.querySelector(".registration.tooltip p");

    if(!inputIsEmpty([name, lastName, email, password, repeatPassword])) {
        if(isEmailValid(email)) {
            if(isPasswordValid(password, repeatPassword)) {

                let response = await fetch("/api/registration", {
                    method: "POST",
                    body: JSON.stringify({
                        name: name.value,
                        lastName: lastName.value,
                        email: email.value,
                        password: password.value
                    })
                }).then(resp => resp.json());

                console.log(response.message)
                console.log(response)

                if(response.message === "user already exists") {
                    tooltip.innerHTML = "пользователь с такой почтой уже зарегистрирован";
                }

                if(response.type === "success") {
                    document.getElementById("login-email").value = email.value;
                    document.getElementById("login-password").value = password.value;
                    toggleRegistration();
                    await sendLoginForm();
                }

            }else {
                tooltip.innerHTML = "пароли не совпадают или ваш пароль слишком короткий";
            }
        }else {
            tooltip.innerHTML = "введите правильную почту";
        }
    } else {
        tooltip.innerHTML = "заполните все поля";
    }

}

function inputIsEmpty(arrayEl) {
    let isEmpty = false;
    for(let i = 0; i < arrayEl.length; i++) {
        if(arrayEl[i].value === "") {
            arrayEl[i].classList.add("error");
            isEmpty = true;
        }else {
            arrayEl[i].classList.remove("error");
        }
    }
    return isEmpty;
}

function isEmailValid(email) {
    const EMAIL_REGEXP = /^(([^<>()[\].,;:\s@"]+(\.[^<>()[\].,;:\s@"]+)*)|(".+"))@(([^<>()[\].,;:\s@"]+\.)+[^<>()[\].,;:\s@"]{2,})$/iu;

    if(EMAIL_REGEXP.test(email.value)) {
        email.classList.remove("error");
        return true;
    }else {
        email.classList.add("error");
        return false;
    }
}

function isPasswordValid(password, repeatPassword) {
    if(password.value.length > 6 && password.value === repeatPassword.value) {
        password.classList.remove("error");
        repeatPassword.classList.remove("error");
        return true;
    }else {
        password.classList.add("error");
        repeatPassword.classList.add("error");
        return false;
    }

}

function toggleLogin() {
    document.querySelector(".login.modal-background").classList.toggle("active");
}

function closeLogin() {
    document.querySelector(".login.modal-background").classList.remove("active");
}

async function sendLoginForm() {
    let email = document.getElementById("login-email");
    let password = document.getElementById("login-password");

    let tooltip = document.querySelector(".login.tooltip p");

    if(!inputIsEmpty([email, password])) {

        let response = await fetch("/api/login", {
            method: "PUT",
            body: JSON.stringify({
                email: email.value,
                password: password.value
            })
        }).then(resp => resp.json());

        console.log(response);

        if(response.message === "invalid password") {
            password.classList.add("error");
            tooltip.innerHTML = "неверный пароль";
        }
        if(response.message === "user not exists") {
            email.classList.add("error");
            tooltip.innerHTML = "пользователь с такой почтой не зарегистрирован";
        }

        if(response.type === "success") {
            closeLogin();
            isAuthorized = true;
            user = response.object;
            editPage();
        }

    }else {
        tooltip.innerHTML = "заполните все поля";
    }
}

function toggleEditTable() {
    document.querySelector(".edit-table.modal-background").classList.toggle("active");
}

function toggleEditUser() {
    document.querySelector(".edit.modal-background").classList.toggle("active");
}

function editDataFill() {
    document.getElementById("edit-name").value = user.name;
    document.getElementById("edit-lastName").value = user.lastName;
    document.getElementById("edit-email").value = user.email;
}

async function sendEditForm() {

    let newName = document.getElementById("edit-name");
    let newLastName = document.getElementById("edit-lastName");
    let newEmail = document.getElementById("edit-email");
    let email = user.email;

    let tooltip = document.querySelector(".edit.tooltip p");

    if(!inputIsEmpty([newName, newLastName, newEmail])){

        tooltip.innerHTML = "";

        let response = await fetch("/api/login", {
            method: "POST",
            body: JSON.stringify({
                newName: newName.value,
                newLastName: newLastName.value,
                newEmail: newEmail.value,
                email: email
            })
        }).then(resp => resp.json());

        if(response.type === "success") {
            user = response.object;
            toggleEditUser();
        }
        console.log(response.message)

        if(response.type === "error") {
            if(response.message === "user already exists"){
                tooltip.innerHTML = "пользователь с такой электронной почтной уже зарегестрирован";
            }
        }

    }else {
        tooltip.innerHTML = "заполните все поля";
    }
}