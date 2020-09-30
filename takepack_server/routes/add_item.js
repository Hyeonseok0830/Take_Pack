var express = require('express');
var router = express.Router();

var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);


router.post('/', function (req, res) {

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

module.exports = router;