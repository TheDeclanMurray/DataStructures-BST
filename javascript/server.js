let express = require('express')

let server = express();
server.use(express.static('app'))


server.listen(3000, function(){
    console.log('Server listening on port 3000');
})