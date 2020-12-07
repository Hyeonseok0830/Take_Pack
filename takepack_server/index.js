// const express = require('express');
// const router = express.Router();
var express = require('express');
var app = express();
var bodyParser = require('body-parser');

// var session = require('express-session');
// var FileStore = require('session-file-store')(session)

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));



var loginRouter = require('./routes/login');
var joinRouter = require('./routes/join');
var listRouter = require('./routes/list');
var additemRouer = require('./routes/add_item');
var delitemRouer = require('./routes/del_item');
var markerRouter = require('./routes/marker');
var addmarkerRouter = require('./routes/add_marker');
var updatemarkerRouter = require('./routes/update_marker');


app.use('/login',loginRouter);
app.use('/join',joinRouter);
app.use('/list',listRouter);
app.use('/add_item',additemRouer);
app.use('/del_item',delitemRouer);
app.use('/marker',markerRouter);
app.use('/add_marker',addmarkerRouter);
app.use('/update_marker',updatemarkerRouter);

app.listen(3000, '192.168.0.2', function () {
    console.log('서버 실행 중...');
});