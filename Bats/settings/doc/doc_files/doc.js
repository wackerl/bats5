var smallTransparentGif = "";
function fixupIEPNG(strImageID, transparentGif) 
{
    smallTransparentGif = transparentGif;
    if (windowsInternetExplorer)
    {
        var img = document.getElementById(strImageID);
        if (img)
        {
            var src = img.src;
            img.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + src + "', sizingMethod='scale')";
            img.src = transparentGif;
            img.attachEvent("onpropertychange", imgPropertyChanged);
        }
    }
}

var windowsInternetExplorer = false;
function detectBrowser()
{
    windowsInternetExplorer = false;
    var appVersion = navigator.appVersion;
    if ((appVersion.indexOf("MSIE") != -1) &&
        (appVersion.indexOf("Macintosh") == -1))
    {
        windowsInternetExplorer = true;
    }
}

var inImgPropertyChanged = false;
function imgPropertyChanged()
{
    if ((window.event.propertyName == "src") && (! inImgPropertyChanged))
    {
        inImgPropertyChanged = true;
        var el = window.event.srcElement;
        if (el.src != smallTransparentGif)
        {
            el.filters.item(0).src = el.src;
            el.src = smallTransparentGif;
        }
        inImgPropertyChanged = false;
    }
}

function onPageLoad()
{
    detectBrowser();
    fixupIEPNG("id1", "doc_files/transparent.gif");
    fixupIEPNG("id2", "doc_files/transparent.gif");
    fixupIEPNG("id3", "doc_files/transparent.gif");
    fixupIEPNG("id4", "doc_files/transparent.gif");
    fixupIEPNG("id5", "doc_files/transparent.gif");
    fixupIEPNG("id6", "doc_files/transparent.gif");
    fixupIEPNG("id7", "doc_files/transparent.gif");
    fixupIEPNG("id8", "doc_files/transparent.gif");
    fixupIEPNG("id9", "doc_files/transparent.gif");
    fixupIEPNG("id10", "doc_files/transparent.gif");
    fixupIEPNG("id11", "doc_files/transparent.gif");
    fixupIEPNG("id12", "doc_files/transparent.gif");
    fixupIEPNG("id13", "doc_files/transparent.gif");
    fixupIEPNG("id14", "doc_files/transparent.gif");
    fixupIEPNG("id15", "doc_files/transparent.gif");
    fixupIEPNG("id16", "doc_files/transparent.gif");
    fixupIEPNG("id17", "doc_files/transparent.gif");
    fixupIEPNG("id18", "doc_files/transparent.gif");
    fixupIEPNG("id19", "doc_files/transparent.gif");
    fixupIEPNG("id20", "doc_files/transparent.gif");
    fixupIEPNG("id21", "doc_files/transparent.gif");
    fixupIEPNG("id22", "doc_files/transparent.gif");
    fixupIEPNG("id23", "doc_files/transparent.gif");
    fixupIEPNG("id24", "doc_files/transparent.gif");
    fixupIEPNG("id25", "doc_files/transparent.gif");
    fixupIEPNG("id26", "doc_files/transparent.gif");
    fixupIEPNG("id27", "doc_files/transparent.gif");
    fixupIEPNG("id28", "doc_files/transparent.gif");
    fixupIEPNG("id29", "doc_files/transparent.gif");
    fixupIEPNG("id30", "doc_files/transparent.gif");
    fixupIEPNG("id31", "doc_files/transparent.gif");
    fixupIEPNG("id32", "doc_files/transparent.gif");
    fixupIEPNG("id33", "doc_files/transparent.gif");
    fixupIEPNG("id34", "doc_files/transparent.gif");
    fixupIEPNG("id35", "doc_files/transparent.gif");
    fixupIEPNG("id36", "doc_files/transparent.gif");
    fixupIEPNG("id37", "doc_files/transparent.gif");
    fixupIEPNG("id38", "doc_files/transparent.gif");
    fixupIEPNG("id39", "doc_files/transparent.gif");
    fixupIEPNG("id40", "doc_files/transparent.gif");
    fixupIEPNG("id41", "doc_files/transparent.gif");
    fixupIEPNG("id42", "doc_files/transparent.gif");
    fixupIEPNG("id43", "doc_files/transparent.gif");
    fixupIEPNG("id44", "doc_files/transparent.gif");
    fixupIEPNG("id45", "doc_files/transparent.gif");
    fixupIEPNG("id46", "doc_files/transparent.gif");
    fixupIEPNG("id47", "doc_files/transparent.gif");
    fixupIEPNG("id48", "doc_files/transparent.gif");
    fixupIEPNG("id49", "doc_files/transparent.gif");
    fixupIEPNG("id50", "doc_files/transparent.gif");
    fixupIEPNG("id51", "doc_files/transparent.gif");
    return true;
}

