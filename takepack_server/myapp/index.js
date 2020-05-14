var express = require('express');
var mysql = require('mysql');
var bodyParser = require('body-parser');
var dbconfig = require('./config/database.js');
var connection = mysql.createConnection(dbconfig);

var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.listen(3000, '192.168.219.121', function () {
    console.log('서버 실행 중...');
});

var connection = mysql.createConnection({
    host: "localhost",
    user: "root",
    database: "takepack",
    password: "knu2020!",
    port: 3306
});

app.post('/user/join', function (req, res) {
    console.log(req.body);
    var userid = req.body.id;
    var userName = req.body.name;
    var userPwd = req.body.pw;
    var userEmail = req.body.email;

    // 삽입을 수행하는 sql문.
    var sql = 'INSERT INTO user (id, name, pw, email) VALUES (?, ?, ?, ?)';
    var params = [userid, userName, userPwd, userEmail];

    // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';

        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = '회원가입에 성공했습니다.';
        }

        res.json({
            'code': resultCode,
            'message': message
        });
    });
});


app.post('/user/login', function (req, res) {

    console.log('listactivity 접속');
    console.log(req.body);
    var userid = req.body.id;
    var userpw = req.body.pw;
    //  console.log(userid);
    var sql = 'select * from user where id =' + mysql.escape(userid);

    connection.query(sql, userid, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';
        var sendid = "";
        if (err) {
            console.log(err);
        } else {
            if (result.length === 0) {
                resultCode = 204;
                message = '존재하지 않는 계정입니다!';
                sendid = ""
                console.log(message);
            } else if (userpw !== result[0].pw) {
                resultCode = 204;
                console.log(message);
                message = '비밀번호가 틀렸습니다!';
                sendid = result[0].id;
            } else {
                resultCode = 200;
                message = '로그인 성공! ' + result[0].name + '님 환영합니다!';
                sendid = result[0].id;
                console.log(message);
                //  console.log(sendid);
            }
        }

        res.json({

            'code': resultCode,
            'message': message,
            'id': sendid

        });
    })
});
app.post('/user/list', function (req, res) {

    console.log('listactivity 접속');
    console.log(req.body);
    var userid = req.body.id;

    console.log(userid);
    var sql = 'select * from item where user_id =' + mysql.escape(userid);

    connection.query(sql, userid, function (err, result) {
        var send = '';
        var resultCode = '404'
        if (err) {
            console.log(err);
            resultCode = '404';
        } else {
            for (var i = 0; i < result.length; i++) {
                send += result[i].item_name + '#';
            }
            resultCode = '200';
        }
        console.log(send + '전송 했음');
        res.json({
            'code': resultCode,
            'item': send
        });
    })
});


app.post('/user/add_item', function (req, res) {

    console.log('아이템추가버튼 클릭');
    console.log(req.body);

    var useritem = req.body.item;
    var userid = req.body.id;
    // 삽입을 수행하는 sql문.
    var sql = 'INSERT INTO item (item_name,user_id) VALUES (?, ?)';
    var params = [useritem, userid];

    // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';

        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = useritem + '을 추가 하였습니다.';
        }
        console.log(message);
        res.json({
            'code': resultCode,
            'message': message
        });
    });
});

app.post('/user/del_item', function (req, res) {

    console.log('아이템제거버튼 클릭');
    console.log(req.body);

    var useritem = req.body.item;
    var userid = req.body.id;
    console.log(useritem);
    // 삭제를 수행하는 sql문.
    var sql = 'DELETE FROM item  WHERE item_name = ? AND user_id = ? ;'
    var params = [useritem, userid];

    // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';

        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = useritem + '를 삭제 하였습니다.';
        }
        console.log(message);
        res.json({
            'code': resultCode,
            'message': message
        });
    });
});

app.post('/user/marker', function (req, res) {

    console.log('Main 접속');
    var userid = req.body.id;
    console.log(userid);

    var sql = 'select * from marker where user_id =' + mysql.escape(userid) + 'order by name';

    connection.query(sql, userid, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';
        var item = '';
        var List = new Array();
        if (err) {
            console.log(err);
        } else {
            for (var i = 0; i < result.length; i++) {
                var data = new Object();
                data.name = result[i].name;
                data.item_name = result[i].item;
                data.lat = result[i].lat;
                data.lng = result[i].lng;
                List.push(data);
            }

            var jsonData = JSON.stringify(List);
            var senddata = JSON.parse(jsonData);
            console.log(senddata);
            item = jsonData;
            resultCode = 200;
            message = '성공적으로 불러 왔습니다.'
        }

        res.json({

            'code': resultCode,
            'message': message,
            'item': senddata

        });
    })
});


app.post('/user/add_marker', function (req, res) {

    console.log('장소추가버튼 클릭');
    console.log(req.body);
    var userid = req.body.id;
    var add_name = req.body.name;
    var item_list = req.body.item_list;
    var add_lat = req.body.lat;
    var add_lng = req.body.lng;
    var insert_count = req.body.count;
    var strArray = item_list.split(',');
    var resultCode = 404;
    var message = '에러가 발생했습니다';
    for (var i = 0; i < insert_count; i++) {
        // 삽입을 수행하는 sql문.
        var sql = 'INSERT INTO marker (name, item, lat, lng, user_id) VALUES (?, ?, ?, ?, ?)';
        var rowVlaues = [add_name, strArray[i], add_lat, add_lng, userid];
        // var valueString = rowVlaues.join(",");
        // values.push('['+valueString+']');
        //console.log('야!' + values);

        // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
        connection.query(sql, rowVlaues, function (err, result) {
            if (err) {
                resultCode = 404;
                message = '에러가 발생했습니다';
                console.log(err);
            } else {
                resultCode = 200;
                message = strArray[i] + '을 추가 하였습니다.';
                console.log(message);
                console.log(resultCode);
            }
            console.log(resultCode);

        });
        console.log(resultCode);
    }
    console.log(resultCode);
    res.json({
        'code': resultCode,
        'message': message
    });

});

app.get('/', (req, res) => {

    console.log('접속완료');
    //res.json(users)
});

// app.set('port', process.env.PORT || 3000);

// app.get('/', function(req, res){
//   res.send('Root');
// });

// app.listen(app.get('port'), function () {
//   console.log('Express server listening on port ' + app.get('port'));
// });
