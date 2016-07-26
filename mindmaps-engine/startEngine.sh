mvn clean package -DskipTests
java -jar ./target/mindmaps-engine-*-jar-with-dependencies.jar ${1+"$@"}
