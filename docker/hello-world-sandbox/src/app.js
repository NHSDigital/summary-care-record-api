var ApiMocker = require('apimocker');
var options = {};

ApiMocker.createServer(options)
    .setConfigFile('src/config.json')
    .start();
