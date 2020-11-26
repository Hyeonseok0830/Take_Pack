var express = require('express');
var router = express.Router();

var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);


router.post('/', function (req, res) {

    console.log('listactivity 접속');
    console.log(req.body);
    var userid = req.body.id;

    console.log(userid);
    var sql = 'select * from item where user_id =' + mysql.escape(userid);

    connection.query(sql, userid, function (err, result) {
        var send = '#';
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
module.exports = router;