'''
Created on Sep 4, 2013

@author: Roee Ebenstein

This is the main method.
This will build the required Vectors
'''
#from xml.dom import minidom
import fnmatch
#import os
#import re
from FixFileToBeXml import SgmToXml
#from BasicWordsFrequency import AddToBasicVector
#from BasicWordsFrequencyDictionary import AddToEnglishDictBasicVector, DumpWordOutput
#from TopicFrequency import AddToTopicVector, DumpTopicOutput

# Parameters
inner_ext = 'xml'
SgmsPath = './reuters/'
XmlsPath = './xmls/'
OutputPath = './output/'

# A filter for the xmls (used to choose only the XML's from a specific directory...)
# Those picked XML's will contain the articles on which we will build the vectors
# (This syntax is for supporting multiple extensions) 
output_extension = ['*.' + inner_ext] # format of output
output_extension = r'|'.join([fnmatch.translate(x) for x in output_extension])

# First, Create the XML's 
print 'Changing SGM\'s to XML\'s'
SgmToXml(SgmsPath,XmlsPath)
    
# Second, scna the XML's for the data,
# Create a vector for each data that is required.
#print 'Starting to analyze data'
#for (dirpath, dirnames, filenames) in os.walk(XmlsPath):
    #if (len(filenames) > 0):
        #files = [f for f in filenames if re.match(output_extension, f)]
        #for filename in files:
            #print 'processing ' + dirpath + filename
            
            # Pic the node on which we want to execute the Vectors
            #xmldoc = minidom.parse(dirpath + filename)
            #NewsReport = xmldoc.getElementsByTagName("REUTERS")
            #for article in NewsReport:
                #AddToBasicVector(article)
                #AddToEnglishDictBasicVector(article)
                #AddToTopicVector(article)

#print 'Starting to dump content'
#DumpWordOutput(OutputPath)
#DumpTopicOutput(OutputPath)

print 'done processing'