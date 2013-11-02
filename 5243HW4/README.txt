////////////////////////////////////////////////////////////
/////////               Roee Ebenstein            //////////
////////////////////////////////////////////////////////////
/////////                 10.31.2013              //////////
////////////////////////////////////////////////////////////
// HW3

The main file will execute Questions 1, 2 and 3.

For making it a bit easier on you - on this directory there a script named "run"
you can execute it (from this directory by):
	./run [numOfGaussian, default is 3]
	
This is for the file you gave us, if you want to have a better control over it-execute the java code directly:

For running the application execute:
	java -classpath bin Hw3EmKmeans data/1dgauss_au12.txt

If you want to change the number of means (for Q1, and Q3) run the following command (8 demonstrates number of means)
	java -classpath bin Hw3EmKmeans data/1dgauss_au12.txt 8


All the answers are implemented within Hw3EmKmeans.java files,
while the additional classes help to manage it (as handed in HW2)