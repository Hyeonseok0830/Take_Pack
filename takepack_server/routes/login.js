var express = require('express');
var router = express.Router();

var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);



// var authData = {
//     id:'test',
//     pw:'1111',
//     email:'1'
// }

router.post('/', function (req, res) {

    console.log('Main 접속');
    console.log(req.body);
    var userid = req.body.id;
    var userpw = req.body.pw;

    // if(userid === authData.id&&userpw==authData.pw)
    // {
    //    // session.is_logined=true;
    //    req.session.is_logined = true;
    //    req.session.email = authData.email;
    // }
    // else
    // {
    //     console.log('who?');
    // }
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
module.exports=router;