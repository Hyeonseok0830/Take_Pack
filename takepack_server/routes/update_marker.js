var express = require('express');
var router = express.Router();
var mysql      = require('mysql');
var dbconfig = require('../config/database.js');
var connection = mysql.createConnection(dbconfig);

router.get('/',(req,res)=>{
    var user_id = req.query.id;
    var place_name=req.query.place_name;
    var state = req.query.state;
    var sql;
    var params;
    if(place_name===""&&state==""){
        sql  = 'update marker set state = ? where user_id = ?';
        params = [null,user_id];
    }
    else{
        sql = 'update marker set state = ? where user_id = ? and name = ?';
        params = [state,user_id,place_name];
    }
    connection.query(sql,params,(err,result)=>{
        var resultCode = 404;
        var message = '에러가 발생했습니다';
        if(err)
        {
            resultCode = 404;
            message='에러가 발생했습니다';
        }
        else{
            resultCode = 200;
            message='마커 상태 갱신';
        }
        res.json({
            'code': resultCode,
            'msg': message
        });
    });    

});
module.exports = router;
