Html 2 Pdf
=============

This is a pdf creation plugin for Phonegap 3.3.0 / Cordova 3.3.1 supporting Android (>=2.3.3) and iOS(>=6.0).
It creates a pdf from the given html and stores it on the device.

There is one method:

* create(html, filePath, successCallback, errorCallback)

Installation
======
You may use phonegap CLI as follows:

<pre>
➜ phonegap local plugin add https://github.com/moderna/cordova-plugin-html2pdf.git
[phonegap] adding the plugin: https://github.com/moderna/cordova-plugin-html2pdf.git
[phonegap] successfully added the plugin
</pre>

Usage
====
```javascript
document.addEventListener('deviceready', onDeviceReady);
function onDeviceReady()
{
        var success = function(status) {
            alert('Message: ' + status);
        }

        var error = function(status) {
            alert('Error: ' + status);
        }

        window.html2pdf.create(
            "<html><head></head><body><h1>Some</h1><p>html content.</p></body></html>",
            "~/Documents/test.pdf", // on iOS (android is "work in progress")
            success,
            error
        );
}
```