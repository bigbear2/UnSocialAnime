const MAX_SUGGESTIONS_LINES = 4 * 42; // 42px è l'altezza di una riga

function initTextArea() {
    console.log(USNAME, "initTextArea");

    let style = `
    .suggestions {
        display: none;
        position: fixed;
        top: 50px;
        left: 0;
        width: 100%;
        border: 1px solid #ccc;
        background-color: #fff;
        z-index: 1000;
        overflow-y: scroll;
        max-height: ${MAX_SUGGESTIONS_LINES}px;
    }
    .suggestion-item {
        padding: 10px;
        cursor: pointer;
    }
    .suggestion-item:hover {
        background-color: #f0f0f0;
    }
    .highlighted {
        background-color: #e0e0e0;
    }`;

    let styleSheet = document.createElement("style");
    styleSheet.type = "text/css";
    styleSheet.innerText = style;
    document.head.appendChild(styleSheet);

    let textAreas = document.querySelectorAll('textarea');
    textAreas.forEach((textarea, idx) => {
        initSuggestions(textarea, idx);
    });
}

function getFollowers() {
    fetch("https://socialanime.it/u/7365/seguiti")
        .then(response => response.json())
        .then(result => {
        console.log(USNAME, result);
    })
        .catch(error => console.error(USNAME, error));
}

function getNames() {
    let userComments = Array.from(document.querySelectorAll(".layout_comment > div > div > a > b"))
        .map(el => el.textContent.trim());

    let userPost = Array.from(document.querySelectorAll(".user_name"))
        .map(el => el.textContent.trim());

    // Unisce gli array e rimuove i duplicati
    let uniqueUsers = [...new Set([...userComments, ...userPost])];

    console.log(USNAME, uniqueUsers);
    return uniqueUsers;
}

function getPosition(el) {
    let rect = el.getBoundingClientRect();
    return {
        top: rect.top,
        left: rect.left,
        width: rect.width,
        height: rect.height
    };
}

function initSuggestions(textarea, idx) {
    const parent = textarea.parentElement;
    let id = "suggestions-" + idx;
    let suggestionsContainer = document.createElement('div');
    suggestionsContainer.id = id;
    suggestionsContainer.className = 'suggestions';
    parent.appendChild(suggestionsContainer);

    const names = getNames();
    let currentFocus = -1;

    textarea.addEventListener("blur", function() {
        hideSuggestions();
    });

    textarea.addEventListener("focus", function() {
        let $this = $(textarea);
        let offset = $this.offset().top;
        let windowHeight = $(window).height();
        let elementHeight = $this.outerHeight();
        let scrollTo = offset - (windowHeight / 2) + (elementHeight / 2);
        $("html, body").animate({ scrollTop: scrollTo }, 500);
    });

    textarea.addEventListener('input', function() {
        console.log(USNAME, "TEXTAREA INPUT");
        const cursorPos = this.selectionStart;
        const textBeforeCursor = this.value.substring(0, cursorPos);
        const atPosition = textBeforeCursor.lastIndexOf('@');

        if (atPosition !== -1) {
            const searchText = textBeforeCursor.substring(atPosition + 1).toLowerCase();
            const filteredNames = names.filter(name => name.toLowerCase().startsWith(searchText));

            if (filteredNames.length > 0) {
                showSuggestions(filteredNames);
            } else {
                hideSuggestions();
            }
        } else {
            hideSuggestions();
        }
    });

    textarea.addEventListener('keydown', function(e) {
        if (suggestionsContainer.style.display === 'block') {
            let items = suggestionsContainer.querySelectorAll('.suggestion-item');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                currentFocus = (currentFocus + 1) % items.length;
                setActiveSuggestion(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                currentFocus = (currentFocus - 1 + items.length) % items.length;
                setActiveSuggestion(items);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (currentFocus > -1) {
                    insertName(items[currentFocus].textContent);
                }
            }
        }
    });

    function showSuggestions(names) {
        let position = getPosition(textarea);
        suggestionsContainer.style.top = (position.top + textarea.offsetHeight + 5) + 'px';
        suggestionsContainer.style.left = position.left + 'px';
        suggestionsContainer.style.width = position.width + 'px';
        suggestionsContainer.innerHTML = names.map(name => `<div class="suggestion-item">${name}</div>`).join('');
        suggestionsContainer.style.display = 'block';
        currentFocus = -1;
    }

    function hideSuggestions() {
        suggestionsContainer.style.display = 'none';
    }

    function setActiveSuggestion(items) {
        items.forEach(item => item.classList.remove('highlighted'));
        if (currentFocus > -1) {
            items[currentFocus].classList.add('highlighted');
        }
    }

    function insertName(name) {
        let cursorPos = textarea.selectionStart;
        let textBeforeCursor = textarea.value.substring(0, cursorPos);
        let atPosition = textBeforeCursor.lastIndexOf('@');

        let newText = textarea.value.substring(0, atPosition) + '@' + name + ' ' + textarea.value.substring(cursorPos);
        textarea.value = newText;
        hideSuggestions();
        textarea.focus();
        textarea.setSelectionRange(atPosition + name.length + 2, atPosition + name.length + 2);
    }

    document.querySelectorAll('.suggestion-item').forEach(item => {
        item.addEventListener('click', function(e) {
            insertName(e.target.textContent);
        });
    });
}

setTimeout(function() {
    try {
        initTextArea();
    } catch (e) {

    }
}, 1000);