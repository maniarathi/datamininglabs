%%%%%% Roee Ebenstein
%%%%%% 9/13/2013

This file describes the content of this directory.
Can be found at: /home/1/ebenstei/5243/hw1

The structure of this project is :
6 Python files:
PrepareFile that manages the process or preparation
TopicFrequency - that contains all the classes extraction
BasicWordsFrequency - that contains a basic vector representation
BasicWordsFrequencyDictionary - that contains a vector of only dictionary words
FixFileToBeXml - That makes an sgm file become an xml.
README.txt - this file

In addition, there are 3 directories:
output - the files that are created, will be created there
reuters - the original reuters paper
xmls - the XML's created from the SGM will be created there.

To run the application - run from the hw1 directory the command
cd hw1 (if you're not already in it) 
python PrepareFile.py
The default vector that will be created is the BasicWordsFrequencyDictionary

To watch the output - use vi on any desired file in the output directory:
cd output
vi *