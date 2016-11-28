JC=javac
JFLAGS=
RM=rm -f

.PHONY: clean
.SUFFIXES: .class .java

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES=PortScanner.class Utils.class
OUT_FILE=pscan.jar

all: $(OUT_FILE)

$(OUT_FILE): $(CLASSES)
	jar cvmf MANIFEST $(OUT_FILE) *.class
	chmod a+x $(OUT_FILE)

PortScanner.class: PortScanner.java
Utils.class: Utils.java

clean:
	$(RM) *.class *.jar
