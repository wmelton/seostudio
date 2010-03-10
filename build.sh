mkdir -p bin
export CLASSPATH=lib/dom4j-1.6.1.jar:lib/htmlcleaner2_1.jar
javac src/seostudio/*.java -d bin
cd bin
jar cfm ../seostudio.jar ../manifest.mf *
