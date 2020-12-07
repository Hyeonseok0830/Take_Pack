var express = require('express');
var router = express.Router();
var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);

router.get('/', function (req, res) {    
    console.log('Main 접속');
    var userid = req.query.id;
    console.log(userid);
    var sql = 'select * from marker where user_id =' + mysql.escape(userid) + 'order by name';
    connection.query(sql, userid, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';
      
        var List = new Array();
        if (err) {
            resultCode = 404;

        } else {
            for (var i = 0; i < result.length; i++) {
                var data = new Object();
                data.name = result[i].name;
                data.item_name = result[i].item;
                data.lat = result[i].lat;
                data.lng = result[i].lng;
                data.state = result[i].state;
                List.push(data);
            }

            var jsonData = JSON.stringify(List);
            var senddata = JSON.parse(jsonData);
            //console.log(senddata);
            item = jsonData;
            resultCode = 200;
            message = '성공적으로 불러 왔습니다.'
        }

        res.status(resultCode).json({

            'code': resultCode,
            'message': message,
            'item': senddata

        });
    })
});

module.exports = router;
