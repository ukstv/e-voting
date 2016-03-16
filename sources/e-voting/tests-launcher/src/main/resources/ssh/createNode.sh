cd /home/ubuntu/e-voting/build/;
mkdir -p $1;
cd $1;
cp ../libs/client.jar ./;
cp ../../src/e-voting/sources/e-voting/client/src/main/resources/client.properties ./;
mkdir -p ./conf;
cp ../../src/e-voting/sources/e-voting/conf/nxt-default.properties ./conf/;
mkdir -p ./gui-public;
cp -r ../../src/e-voting/sources/e-voting/gui-public/* ./gui-public;
cp -r ./gui-public/app/default-server-properties.js ./gui-public/app/server-properties.js;
cd -p ./gui-public;
npm install;
npm install -g bower;
bower update;