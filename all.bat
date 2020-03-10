
cd /d %~dp0

#set SIM=0.9
#set SIZE=50


#for /l %%i in (2,1,45) do (
#    echo %%i
#    echo SIM
#    java -jar ccinvec_tfidf.jar -d ..\bcb_reduced\%%i -l java -oc result20190807sim09\%%i.csv
    
#)

java -jar ccinvec.jar -d E:\data_for_ICvolti\ant -d2 E:\data_for_ICvolti\ant2 -l java -oc test.csv
#java -jar ccinvec_tfidf.jar -d ..\bcb_reduced\31 -l java -oc result20190807sim09\31.csv
#java -jar ccinvec_tfidf.jar -d ..\bcb_reduced\32 -l java -oc result20190807sim09\32.csv
#java -jar ccinvec_tfidf.jar -d ..\bcb_reduced\35 -l java -oc result20190807sim09\35.csv
#java -jar ccinvec_tfidf.jar -d ..\bcb_reduced\40 -l java -oc result20190807sim09\40.csv
pause



