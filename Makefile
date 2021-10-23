CC=
FLAGS=
OBJECTS=Driver.class FileSystem.class Functions.class Util.class InternalFile.class PeekableScanner.class Symbol.class
FILES=Driver.java FileSystem.java Functions.java Util.java InternalFile.java PeekableScanner.java Symbol.java
TARGET=VSFS.jar

all: $(TARGET)

$(TARGET): $(OBJECTS)
	jar -cf $(TARGET) $(OBJECTS) 

$(OBJECTS): $(FILES)
	javac $(FILES)

clean:
	rm -f $(OBJECTS) $(TARGET)
