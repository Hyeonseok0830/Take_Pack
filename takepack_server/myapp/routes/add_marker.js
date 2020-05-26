var express = require('express');
var router = express.Router();

var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);

router.post('/', function (req, res) {

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
              //  console.log(message);
         //       console.log(resultCode);
            }
       

        });        
        
    }
 
    res.json({
        'code': resultCode,
        'message': message
    });
    console.log(resultCode + "  : " + message);

});

module.exports = router;