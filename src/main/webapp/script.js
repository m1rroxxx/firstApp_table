
let isAuthorized = false;
let user = null;

// localStorage.setItem("columns", "[0, 1, 1, 1]");

checkOnValidSession().then(r => {

    console.log(isAuthorized);

    editPage();

    document.addEventListener("submit", (event)=> {
        event.preventDefault();
    })
});

async function checkOnValidSession() {

    let response = await fetch("/api/login", {
        method: "GET"
    }).then(resp => resp.json());

    if(response.message === "session has been updated") {
        isAuthorized = true;
        user = response.object;
    }
}

function editPage() {
    if(isAuthorized){
        document.querySelector(".noAuthorized").classList.remove("active");
        document.querySelector(".authorized").classList.add("active");
        editDataFill();
    }else {
        document.querySelector(".authorized").classList.remove("active");
        document.querySelector(".noAuthorized").classList.add("active");
    }
}

function debounce(func, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            func(...args);
        }, wait);
    }
}

function toggleSettings() {
    document.querySelector(".select-menu-account").classList.toggle("active");
}

async function exitAccount() {

    let response = await fetch("/api/login", {
        method: "DELETE"
    }).then(resp => resp.json());

    console.log(response.message);

    isAuthorized = false;
    user = null;
    editPage();
}