class ConsoleModal {
    constructor() {
        this.createModal();
        this.overrideConsole();
    }

    createModal() {
        // Creazione della modal (schermo intero)
        this.modal = document.createElement("div");
        this.modal.style = `
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.7);
            justify-content: center;
            align-items: center;
            z-index: 9999;
        `;

        // Contenitore principale della modal
        this.modalContent = document.createElement("div");
        this.modalContent.style = `
            background: #222;
            color: #fff;
            padding: 20px;
            border-radius: 0;
            width: 100%;
            height: 100%;
            position: relative;
            display: flex;
            flex-direction: column;
        `;

        // Titolo
        let title = document.createElement("h3");
        title.textContent = "Console Output";
        title.style.marginTop = "0";
        this.modalContent.appendChild(title);

        // Pulsante di chiusura
        this.closeBtn = document.createElement("button");
        this.closeBtn.innerHTML = "&times;";
        this.closeBtn.style = `
            position: absolute;
            top: 10px;
            right: 20px;
            background: red;
            color: white;
            border: none;
            padding: 10px 20px;
            cursor: pointer;
            font-size: 24px;
        `;
        this.closeBtn.onclick = () => this.hide();
        this.modalContent.appendChild(this.closeBtn);

        // Contenitore dei log (pre scrollabile)
        this.logContainer = document.createElement("pre");
        this.logContainer.style = `
            white-space: pre-wrap;
            font-size: 14px;
            line-height: 1.5;
            background: #333;
            padding: 10px;
            border-radius: 5px;
            flex-grow: 1;
            overflow-y: auto;
            max-height: calc(100% - 50px); /* Usa tutto lo spazio disponibile */
            margin: 10px 0;
            font-family: monospace;
            /*text-wrap: revert;*/
        `;
        this.modalContent.appendChild(this.logContainer);

        // Aggiunta della modal al body
        this.modal.appendChild(this.modalContent);
        document.body.appendChild(this.modal);

        // Pulsante per aprire la console
        this.openBtn = document.createElement("button");
        this.openBtn.textContent = "Console";
        this.openBtn.style = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: #007bff;
            color: white;
            border: none;
            padding: 10px 15px;
            cursor: pointer;
            font-size: 16px;
            border-radius: 5px;
        `;
        this.openBtn.onclick = () => this.show();
        //document.body.appendChild(this.openBtn);
    }

    overrideConsole() {
        const originalLog = console.log;
        const originalError = console.error;
        const originalWarn = console.warn;

        const writeToConsole = (type, color, ...args) => {
            let message = args.map(arg => (typeof arg === "object" ? JSON.stringify(arg, null, 2) : arg)).join(" ");

            let logElement = document.createElement("div");
            logElement.innerHTML = `<span style="color:${color}; font-weight:bold;">[${type.toUpperCase()}]</span> ${message}`;
            this.logContainer.appendChild(logElement);

            this.logContainer.scrollTop = this.logContainer.scrollHeight; // Auto-scroll in basso
        };

        console.log = (...args) => {
            writeToConsole("log", "#00bfff", ...args); // Blu
            originalLog.apply(console, args);
        };

        console.error = (...args) => {
            writeToConsole("error", "#ff4136", ...args); // Rosso
            originalError.apply(console, args);
        };

        console.warn = (...args) => {
            writeToConsole("warn", "#ffcc00", ...args); // Giallo
            originalWarn.apply(console, args);
        };
    }

    show() {
        this.modal.style.display = "flex";
    }

    hide() {
        this.modal.style.display = "none";
    }
}

window.SAConsole = new ConsoleModal();