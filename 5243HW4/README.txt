The main file will execute one clustering algorithm, with one distance measurement

For making it a bit easier on you - on this directory there a script named "run"
you can execute it (from this directory by):
	./run algorithm distance [K]

algorithm can be kmeans or hir
distance can be euc or man
K is relevant only for kmeans
	
This is for the file you gave us, if you want to have a better control over it-execute the java code directly:

For running the application execute:
	java -classpath bin Lab3Loader kmeans euc 4
	java -classpath bin Lab3Loader kmeans man 8
	java -classpath bin Lab3Loader hir euc
	java -classpath bin Lab3Loader hir man

	
The hierarchy algorithm will run for K=4,8,16,32,64, and 128 since it's practically free after training.