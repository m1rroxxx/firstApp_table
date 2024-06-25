let showRows = document.querySelector(".numberRows button").textContent;
let numberRows = 0;
let activePage = 1;

let searchString = "";
let sortType = "";
let sortColumn = "";

let numberPages = 0;

let columns;
let users;

(async () => {
    await getPage();
    headersFill(columns.filter((el) => JSON.parse(localStorage.getItem("columns")).includes(el)));
    tableFill(users);
    modalTableEdit(columns);
})();

function updateButtons() {

    let buttons = document.querySelectorAll(".pagination-number");

    for (let i = 1; i < buttons.length - 1; i++) {

        if (i !== 1) buttons[i].classList.add("disabled");
        buttons[i].classList.remove("active");

    }

    switch (activePage) {
        case 1:
        case 2:

            document.querySelector(".pagination-number.first").innerHTML = "1";

            if (activePage === 1) {
                document.querySelector(".pagination-number.first").classList.add("active");
            }
            if (activePage === 2) {
                document.querySelector(".pagination-number.second").classList.add("active");
            }

            if (numberPages >= 2) {
                document.querySelector(".pagination-number.second").classList.remove("disabled");
                document.querySelector(".pagination-number.second").innerHTML = "2";
            }
            if (numberPages >= 3) {
                document.querySelector(".pagination-number.center").classList.remove("disabled");
                document.querySelector(".pagination-number.center").innerHTML = "3";
            }
            if (numberPages >= 4) {
                document.querySelector(".pagination-number.pred-last").classList.remove("disabled");
                document.querySelector(".pagination-number.pred-last").innerHTML = "4";
            }
            if (numberPages >= 5) {
                document.querySelector(".pagination-number.last").classList.remove("disabled");
                document.querySelector(".pagination-number.last").innerHTML = numberPages.toString();
            }
            break;

        case numberPages - 1:
        case numberPages:

            document.querySelector(".pagination-number.first").innerHTML = "1";

            if (numberPages === 3) {
                document.querySelector(".pagination-number.second").classList.remove("disabled");
                document.querySelector(".pagination-number.center").classList.remove("disabled");

                document.querySelector(".pagination-number.second").innerHTML = (numberPages - 1).toString();
                document.querySelector(".pagination-number.center").innerHTML = numberPages.toString();
                document.querySelector(".pagination-number.center").classList.add("active");
            }

            if (numberPages === 4) {
                document.querySelector(".pagination-number.second").classList.remove("disabled");
                document.querySelector(".pagination-number.center").classList.remove("disabled");
                document.querySelector(".pagination-number.pred-last").classList.remove("disabled");

                document.querySelector(".pagination-number.second").innerHTML = (numberPages - 2).toString();
                document.querySelector(".pagination-number.center").innerHTML = (numberPages - 1).toString();
                document.querySelector(".pagination-number.pred-last").innerHTML = numberPages.toString();

                if (activePage === numberPages) {
                    document.querySelector(".pagination-number.pred-last").classList.add("active");
                }

                if (activePage === numberPages - 1) {
                    document.querySelector(".pagination-number.center").classList.add("active");
                }
            }

            if (numberPages >= 5) {

                let buttons = document.querySelectorAll(".pagination-number");

                for (let i = 0; i < buttons.length; i++) {
                    buttons[i].classList.remove("disabled");
                }

                document.querySelector(".pagination-number.second").innerHTML = (numberPages - 3).toString();
                document.querySelector(".pagination-number.center").innerHTML = (numberPages - 2).toString();
                document.querySelector(".pagination-number.pred-last").innerHTML = (numberPages - 1).toString();
                document.querySelector(".pagination-number.last").innerHTML = numberPages.toString();

                if (activePage === numberPages) {
                    document.querySelector(".pagination-number.last").classList.add("active");
                }

                if (activePage === numberPages - 1) {
                    document.querySelector(".pagination-number.pred-last").classList.add("active");
                }
            }
            break;

        default:

            let buttons = document.querySelectorAll(".pagination-number");

            for (let i = 0; i < buttons.length; i++) {
                buttons[i].classList.remove("disabled");
            }

            console.log(activePage)

            document.querySelector(".pagination-number.first").innerHTML = "1";
            document.querySelector(".pagination-number.second").innerHTML = (activePage - 1).toString();
            document.querySelector(".pagination-number.center").innerHTML = activePage.toString();
            document.querySelector(".pagination-number.center").classList.add("active");
            document.querySelector(".pagination-number.pred-last").innerHTML = (activePage + 1).toString();
            document.querySelector(".pagination-number.last").innerHTML = numberPages.toString();
            break;
    }
}

async function nextPage() {
    if (activePage + 1 <= numberPages) {
        activePage += 1;
        await getPage();
        tableFill(users);
    }
}

async function predPage() {
    if (activePage - 1 > 0) {
        activePage -= 1;
        await getPage();
        tableFill(users);
    }
}

async function openPage(event) {
    activePage = parseInt(event.textContent);
    await getPage();
    tableFill(users);
}

function dropDown() {
    document.querySelector(".selectMenu").classList.toggle("active");
}

async function selectNumberRows(event) {
    document.querySelector(".numberRows button").textContent = event.textContent;

    activePage = 1;
    await getPage();
    tableFill(users);
}

document.addEventListener("click", (event) => {
    if (event.target.id !== "select-button") {
        document.querySelector(".selectMenu").classList.remove("active");
    }

    if(!event.target.classList.contains("account-settings-button")) {
        document.querySelector(".select-menu-account").classList.remove("active");
    }
})

document.querySelector(".search input").addEventListener("input", debounce(async (event) => {
    searchString = event.target.value;

    activePage = 1;
    await getPage();
    tableFill(users);
}, 1000));

document.querySelector(".search input").addEventListener("blur", (event) => {

    event.target.classList.remove("blur");

    if (event.target.value !== "") {
        event.target.classList.add("blur");
    }
});

async function sortPage(event) {
    console.log("sort")
    let arrows = document.querySelectorAll(".th svg");

    sortColumn = event.children[0].textContent;
    let arrow = event.children[1];

    for (let i = 0; i < arrows.length; i++) {
        if (arrows[i] !== arrow) {
            arrows[i].classList.remove("down");
            arrows[i].classList.remove("up");
        }
    }

    if (arrow.classList.contains("down")) {

        arrow.classList.remove("down");
        arrow.classList.add("up");

        sortType = "DESC";

    } else if (arrow.classList.contains("up")) {

        arrow.classList.remove("up");
        arrow.classList.add("down");

        sortType = "ASC";

    } else {
        arrow.classList.add("down");
        sortType = "ASC";
    }

    await getPage();
    tableFill(users);
}

async function getPage() {
    let countRows = document.querySelector(".numberRows button").textContent;

    let stringRequest = "/api/users?searchString=" + searchString + "&showRows=" + countRows + "&pageNumber=" + activePage + "&sortColumn=" + sortColumn + "&sortType=" + sortType + "&columns=" + JSON.parse(localStorage.getItem("columns"));

    let response = await fetch(stringRequest, {
        method: "GET"
    }).then(resp => resp.json());

    showRows = countRows;
    numberRows = parseInt(response.info);

    numberPages = Math.ceil(numberRows / showRows);

    columns = Object.keys(response.object[0]);
    users = response.object;

    if(localStorage.getItem("columns") === null) {
        localStorage.setItem("columns", JSON.stringify(columns));
    }

    updateButtons();
}

async function editTable(event) {
    let id = event.id;
    console.log(id);
    console.log(event.checked);

    let filterColumns = JSON.parse(localStorage.getItem("columns"));

    if (event.checked) {
        filterColumns.push(id);
        localStorage.setItem("columns", JSON.stringify(filterColumns));
    } else {
        filterColumns = filterColumns.filter((el) => el !== id);
        localStorage.setItem("columns", JSON.stringify(filterColumns));
    }

    await getPage();
    headersFill(columns.filter((el) => JSON.parse(localStorage.getItem("columns")).includes(el)));
    tableFill(users);
    sortType = "";
    sortColumn = "";

}

function modalTableEdit(headers) {
    document.querySelector('.modal-content.edit-table').innerHTML = headers.map((header) => `
       <div class="switchRow">
       <label class="switch">
            <input id="${header}" type="checkbox" class="check-box edit-table" onclick="editTable(this)">
            <span class="slider"></span>
        </label>
        <p>${header}</p>
       </div>
    `).join('');

    let checkBoxes = document.querySelectorAll('.check-box.edit-table');

    for(let header of JSON.parse(localStorage.getItem("columns"))) {
        for(let checkBox of checkBoxes) {
            if(header === checkBox.id) {
                checkBox.checked = true;
            }
        }
    }
}
function headersFill(headers) {
    document.querySelector('.table thead').innerHTML = headers.map((header) => `
        <th>
            <div onclick="sortPage(this)" class="th">
                    <p>${header}</p>
                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                         xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
                              d="M15 19l-7-7 7-7"></path>
                    </svg>
                </div>
        </th>`).join('');
}

function tableFill(users) {

    document.querySelector('.table tbody').innerHTML = users.map((user) => {

        let resultString = "<tr>";
        let headers = columns.filter((el) => JSON.parse(localStorage.getItem("columns")).includes(el));

        for(let i = 0; i < headers.length; i++) {
            resultString += `<td>${user[headers[i]]}</td>`;
        }

        resultString += "</tr>"

        return resultString;
    }).join('');
}