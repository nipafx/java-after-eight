rm -rf cds/
mkdir cds

./stats-time.sh java -Xlog:class+load:file=cds/off.log \
	-cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main

printf "\n--- JDK CDS ---\n\n"
printf ">> building archive...\n"
java -XX:+UnlockDiagnosticVMOptions -Xshare:dump \
	-XX:SharedArchiveFile=cds/jdk.jsa
printf "\n>> using archive...\n"
./stats-time.sh java -Xlog:class+load:file=cds/jdk.log \
	-XX:+UnlockDiagnosticVMOptions -Xshare:on \
	-XX:SharedArchiveFile=cds/jdk.jsa \
	-cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main


printf "\n--- APP CDS ---\n\n"
printf ">> building archive...\n"
java -XX:+UnlockDiagnosticVMOptions \
	-XX:DumpLoadedClassList=cds/classes.lst \
	-cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main
java -XX:+UnlockDiagnosticVMOptions -Xshare:dump \
	-XX:SharedClassListFile=cds/classes.lst -XX:SharedArchiveFile=cds/app.jsa \
	-cp jars/genealogy.jar:jars/genealogists.jar
printf "\n>> using archive...\n"
./stats-time.sh java -Xlog:class+load:file=cds/app.log \
	-XX:+UnlockDiagnosticVMOptions -Xshare:on \
	-XX:SharedArchiveFile=cds/app.jsa \
	-cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main
