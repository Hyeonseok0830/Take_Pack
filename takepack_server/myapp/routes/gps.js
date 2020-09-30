var express = require('express');
var router = express.Router();

var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);

router.get('/', function (req, res) {
    
    console.log('Main 접속');
    var test1 = req.query.test;
    console.log(test1);
    
    
    
});
router.post('/', function (req, res) {

    console.log('아이템추가버튼 클릭');
    console.log(req.body);

    var lat = req.body.lat;
    var lng = req.body.lng;
    var title = req.body.title;
    var snip = req.body.snip;

    // 삽입을 수행하는 sql문.
    var sql = 'INSERT INTO mydb.gps (lat,lng,title,snip) VALUES (?,?,?,?)';
    var params = [lat, lng, title, snip];

    // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
    connection.query(sql, params, function (err, result) {
        console.log("gps");
        var resultCode = 404;
        var message = '에러가 발생했습니다';

        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = title + '을 추가 하였습니다.';
        }
        console.log(message);
        res.json({
            'code': resultCode,
            'message': message
        });
    });
});

module.exports = router;