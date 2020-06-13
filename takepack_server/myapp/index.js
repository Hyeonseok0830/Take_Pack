// const express = require('express');
// const router = express.Router();
var express = require('express');
var app = express();
var bodyParser = require('body-parser');

// var session = require('express-session');
// var FileStore = require('session-file-store')(session)

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));




// app.use(session({
//   secret: '!@#$%^&*',
//   store: new MySQLStore(dbOptions),
//   resave: false,
//   saveUninitialized: false
// }));



var loginRouter = require('./routes/login');
var joinRouter = require('./routes/join');
var listRouter = require('./routes/list');
var additemRouer = require('./routes/add_item');
var delitemRouer = require('./routes/del_item');
var markerRouter = require('./routes/marker');
var addmarkerRouter = require('./routes/add_marker');

app.use('/login',loginRouter);
app.use('/join',joinRouter);
app.use('/list',listRouter);
app.use('/add_item',additemRouer);
app.use('/del_item',delitemRouer);
app.use('/marker',markerRouter);
app.use('/add_marker',addmarkerRouter);

app.listen(8888, '192.168.219.101', function () {
    console.log('서버 실행 중...');
});
// var connection = mysql.createConnection({
//     host: "localhost",
//     user: "root",
//     database: "takepack",
//     password: "knu2020!",
//     port: 3306
// });

// 








// app.get('/', (req, res) => {

//     console.log('접속완료');
//     //res.json(users)
// });

// // app.set('port', process.env.PORT || 3000);

// // app.get('/', function(req, res){
// //   res.send('Root');
// // });

// // app.listen(app.get('port'), function () {
// //   console.log('Express server listening on port ' + app.get('port'));
// // });
