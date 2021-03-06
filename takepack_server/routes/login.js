var express = require('express');
var router = express.Router();

var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);


router.post('/', function (req, res) {

    console.log('Main 접속');
    console.log(req.body);
    var userid = req.body.id;
    var userpw = req.body.pw;

    var sql = 'select * from user where id =' + mysql.escape(userid);

    connection.query(sql, userid, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';
        var sendid = "";
        if (err) {
            resultCode=404;
            
            
        } else {
            if (result.length === 0) {
                resultCode = 202;
                message = '존재하지 않는 계정입니다!';
                sendid = ""
            } else if (userpw !== result[0].pw) {
                resultCode = 202;            
                message = '비밀번호가 틀렸습니다!';
                sendid = result[0].id;
            } else {
                resultCode = 200;
                message = '로그인 성공! ' + result[0].name + '님 환영합니다!';
                sendid = result[0].id;            
            }
        }

        res.status(resultCode).json({           
            'code': resultCode,
            'message': message,
            'id': sendid

        });
    })
});
module.exports=router;