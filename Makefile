JC=javac
JFLAGS=
RM=rm -f

.PHONY: clean install
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

install:
	install -d /usr/local/network-ps
	install -m 0755 pscan /usr/local/network-ps/pscan
	install -m 0755 pscan.jar /usr/local/network-ps/pscan.jar
	install -m 0444 application-x-java.xpm /usr/local/network-ps/application-x-java.xpm
	desktop-file-install pscan.desktop
