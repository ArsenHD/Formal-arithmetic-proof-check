all: build

build: out
	javac -cp src -d out src/Main.java

out:
	mkdir -p out

run:
	java -cp out Main < res/test.txt

pack:
	zip TaskE.zip -r src res Makefile README.txt

clean:
	rm -rf out
