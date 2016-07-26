mvn clean package -DskipTests
java -jar ./mindmaps-engine/target/mindmaps-engine-*-jar-with-dependencies.jar ${1+"$@"}
