const USNAME = "🐲 SAHELPER";
const ULMENU = document.querySelector("body > div.fix_nav_bar.BoxShadow1.destroy_box > div > div.right > ul");
const UL_MENU_MOBILE_SELECTOR = "body > div.mfp-wrap.mfp-close-btn-in.mfp-auto-cursor.mfp-ready > div > div.mfp-content > div > div > ul";
const BTN_MENU_MOBILE = $("#menu_mobile > div:nth-child(1) > div:nth-child(1) > a");
const ISMOBILE = (ULMENU == null);
let intervalNotifiche = null;
function hashCode(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = (hash << 5) - hash + str.charCodeAt(i);
        hash |= 0; // Convert to 32-bit integer
    }
    return hash;
}

function downloadTextFile(filename, content) {
    let blob = new Blob([content], { type: "text/plain" });
    let link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

function addMenuItem(id,text,onclick) {
    if (ISMOBILE) {

        const MENU_MOBILE_ITEM = document.querySelector(UL_MENU_MOBILE_SELECTOR);
        const MENU_MOBILE_ITEM_ANDROID = document.querySelector("#" + id);

        if (MENU_MOBILE_ITEM_ANDROID != null) MENU_MOBILE_ITEM_ANDROID.remove();

        html = `<li id="${id}"><a class="ripplelink" href="javascript:void(0)" onclick="${onclick}">
                        <span class="gry sicon-view-carousel"></span> <span id="trasp">${text}</span>
                        </a></li>`;

        MENU_MOBILE_ITEM.innerHTML += html;

    }else{
        let html = `<li id="${id}">
                        <a target="u_blank" href="javascript:void(0)" onclick="${onclick}">${text}</a>
                    </li>`;
        ULMENU.innerHTML += html;
    }
}

function addAndroidMenu() {

    if (ISMOBILE) {
        /* $("<button>", {
            text: "📢",
            css: {
                position: "fixed", top: "50px", right: "10px",
                padding: "10px 15px", backgroundColor: "#007bff", color: "white",
                border: "none", borderRadius: "5px", cursor: "pointer", zIndex: "9999",
                boxShadow: "2px 2px 5px rgba(0,0,0,0.3)"
            }, click: function () { getContentNotifiche(false); }
        }).appendTo("body"); */


        BTN_MENU_MOBILE.click(function () {
            setTimeout(function () {
                const MENU_MOBILE_ITEM = document.querySelector(UL_MENU_MOBILE_SELECTOR);

                try {
                    MENU_MOBILE_ITEM.innerHTML += `<li id="android_menu">`;
                    addMenuItem("android_menu","Android Menu","androidMenu()");
                    addMenuItem("console_menu","Console","window.SAConsole.show()");
                }catch (e) {
                    console.error("USNAME", e);
                }
            }, 500);
        });

    }else{
        try {
            MENU_MOBILE_ITEM.innerHTML += `<li id="android_menu">`;
            addMenuItem("android_menu","Android Menu","androidMenu()");
            addMenuItem("console_menu","Console","window.SAConsole.show()");
        }catch (e) {
            console.error("USNAME", e);
        }
    }


}

function androidMenu() {
    if (typeof Android !== "undefined") {
        Android.androidMenu();
    } else {
        console.error(USNAME,"Android interface non disponibile!");
        alert("Android interface non disponibile!");
    }
}

function androidNotification(notifica, isJson = false) {
    if (typeof Android !== "undefined") {
        Android.androidNotification(notifica, isJson);
    } else {
        clearInterval(intervalNotifiche);
        console.error(USNAME,"Android interface non disponibile!");
    }
    console.debug(USNAME, notifica, isJson);
}

function is_page_login() {
    return window.location.href === 'https://socialanime.it/';
}

function analizzaNotifiche(html,soloNonLette = true) {
    let $html = $(html);
    let $elements;
    if (soloNonLette) {
        let $stopDiv = $html.find("div[style='margin-bottom:15px;margin-top:5px;']").eq(1);
        $elements = $html.find(".cont_n.animated.fadeInUp.bordo").filter(function () {
            return $(this).nextAll().filter($stopDiv).length > 0;
        });
    }else{
        $elements = $html.find(".cont_n.animated.fadeInUp.bordo");
    }

    let notifiche = [];
    $elements.each(function () {
        let $elm = $(this);
        let icona = "https://socialanime.it/" + $elm.find("img").attr("src");
        let contenuto = $elm.find(".cont_txt_n > a").html();
        contenuto = contenuto.split("<br>");
        contenuto[0] = contenuto[0].replace('\n            ', ' ');

        let titolo = $(contenuto[0]).text().trim();
        let descrizione = contenuto[1];
        let link = $elm.find(".cont_txt_n > a").attr("href");
        let id = hashCode(titolo + descrizione + link);
        if (id < 0) id *= -1;

        let data = {
            "id": id,
            "titolo": titolo,
            "descrizione": descrizione,
            "link": link,
            "icona": icona,
            "data": $(contenuto[2]).text().trim()
        }
        notifiche.push(data);

    });
    //console.debug(USNAME, notifiche);

    let json_notifiche = JSON.stringify(notifiche);
    androidNotification(json_notifiche, true);
    if (!soloNonLette) {
        downloadTextFile("notifiche.json", json_notifiche);
    }
}

function getContentNotifiche(soloNonLette = true) {
    console.debug(USNAME,"getContentNotifiche");
    let url = "https://socialanime.it/notify.php";
    $.get(url, function(result){
        let contesto = result.trim();
        analizzaNotifiche(contesto,soloNonLette);
    });
}

function getNumeroNotifiche() {
    if (is_page_login()){
        return;
    }

    $.get("https://socialanime.it/ico_notifica.php?notifiy_mobile_mode", function(result){
        let notifica = result.trim();
        if (!isNaN(notifica) && parseInt(notifica) > 0) {
            androidNotification("Hai " + notifica + " Notifiche Non Lette");
            getContentNotifiche(true);
        }
    });
}



addAndroidMenu();
getNumeroNotifiche();
intervalNotifiche = setInterval(function(){
    getNumeroNotifiche();
}, 30000);