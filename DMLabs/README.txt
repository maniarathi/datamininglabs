README Lab 1 CSE 5243
Arathi Mani
9/11/2013

Program description: This Python program fetches ~22,000 documents from 22 links on the web and preprocesses the data to output term vectors for each document and a transaction vector for each document.

Contents: This directory contains lab1.py, the script to create the output vectors, this README, and a PDF called report1.pdf that details the approach to writing lab1.py.

How to run: This program is written in Python so if Python is not installed (you can check by typing 'python' in the command line), you will have to install it. After successfully installing Python, run the program by typing 'python lab1.py' This will begin execution of the program.

Note: The program first checks to see whether the 22 files have already been downloaded from the internet. If they have been and they are successfully located in the same directory as the program, then no internet access is needed. However, if these files are NOT in the same directory as lab1.py, then the program will download them. If the internet connection is poor and the program is not able to connect in 3 seconds, then the file will be skipped, leading to erronous vectors.

Output: The output of this program is a file called 'out.txt' which will reside in the same directory where the script resides. The format of the output is as follows: a list of the words that were used to create the term vectors, each document followed by the number of times the above words are counted in the body of the article, and each document followed by any matching words from the earlier list of words that were used to create term vectors and the words in the places and topic tags.
The output can be opened in your favorite text editor.

EXAMPLE:
Suppose the body of the document is "Mary had a little lamb, little lamb, little lamb. Mary had a little lamb and its fleece was white as snow."
And suppose the topic of the document is "song"
And suppose the places of the document is "usa"
And suppose the titular words were {little, big, dog, lamb, red, white, blue}
And suppose the document number was 23.
Then the output would be:
little big dog lamb red white blue
Document 23: 4 0 0 4 0 1 0
Document 23: <little lamb white><usa song>

Time: The output file is created in about 2.5 minutes and the size of the file is about 80MB.