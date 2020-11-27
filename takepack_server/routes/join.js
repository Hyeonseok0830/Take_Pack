var express = require('express');
var router = express.Router();

var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);

router.post('/', function (req, res) {
   // console.log(req.body);
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
            resultCode=404;

        } else {
            resultCode = 200;
            message = '회원가입에 성공했습니다.';
        }

        res.status(resultCode).json({
            'code': resultCode,
            'message': message
        });
    });
});

module.exports = router;