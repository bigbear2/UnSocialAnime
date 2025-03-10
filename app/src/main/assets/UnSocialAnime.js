const USNAME = "🐲 SAHELPER";
const ULMENU = document.querySelector("body > div.fix_nav_bar.BoxShadow1.destroy_box > div > div.right > ul");
const ISMOBILE = (ULMENU == null);

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


function createFixedButton() {

    if (ISMOBILE) {
        let button = document.createElement("button");
        button.innerText = "📢";
        button.style.position = "fixed";
        button.style.top = "50px";
        button.style.right = "10px";
        button.style.padding = "10px 15px";
        button.style.backgroundColor = "#007bff";
        button.style.color = "white";
        button.style.border = "none";
        button.style.borderRadius = "5px";
        button.style.cursor = "pointer";
        button.style.zIndex = "9999";
        button.style.boxShadow = "2px 2px 5px rgba(0,0,0,0.3)";
        button.onclick = function () {
            getContentNotifiche(false);
        };
        document.body.appendChild(button);

    }else{
        let html = `<li class="menu-separator"></li><li><a target="u_blank" href="javascript:void(0)" onclick="getContentNotifiche(false)">Test Notifiche</a></li>`;
        ULMENU.innerHTML += html;
    }

    /*  html = `<li><a class="ripplelink" href="javascript:void(0)" onclick="getContentNotifiche(false)">
                        <span class="gry sicon-view-carousel"></span> <span id="trasp">Test Notifiche</span>
                </a></li>`;
        try {
            document.querySelector("body > div.mfp-wrap.mfp-close-btn-in.mfp-auto-cursor.mfp-ready > div > div.mfp-content > div > div > ul").innerHTML += html;
        }catch (e) {
            console.error("USNAME", e);
        }*/
}

function sendToAndroid(notifica, isJson = false) {
    if (typeof Android !== "undefined") {
        Android.sendToAndroid(notifica, isJson);
    } else {
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
    console.debug(USNAME, notifiche);

    let json_notifiche = JSON.stringify(notifiche);
    sendToAndroid(json_notifiche, true);
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
            sendToAndroid("Hai " + notifica + " Notifiche Non Lette");
            getContentNotifiche(true);
        }
    });
}

createFixedButton();
getNumeroNotifiche();
setInterval(function(){
    getNumeroNotifiche();
}, 10000);