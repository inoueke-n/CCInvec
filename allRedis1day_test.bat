cd /d %~dp0

java  -Xms512M -Xmx1024M -jar ccinvec.jar  inputRedis1day_test > Redis1day1_test.csv


REM KEYにアクセストークンを記入
set KEY=RlTjf1ufK8Bn1LbdMXKTnUFYEY2pnjDcCdJH6l3sr4r
REM 引数
set MASSAGE=finish

REM curlで通知用のAPIを叩く
curl -X POST -H "Authorization: Bearer %KEY%" -F "message=%MASSAGE%" https://notify-api.line.me/api/notify


pause



