'''
Created on Sep 6, 2013

@author: Roee Ebenstein

This file is the topics classification result

Requires - re, nltk, numpy, and stemming (corpora)

REQUIRED INSTALLATIONS:
=======================
http://nltk.org/install.html
http://johnlaudun.org/20130126-nltk-stopwords/
https://pypi.python.org/pypi/stemming/1.0#downloads
'''
import os
import re
import nltk
import numpy as np
from stemming.porter2 import stem

wordDumpName = "TopicDump.txt"
wordDumpDocIdName = "TopicDumpDocIds.txt"
matrixDumpName = "TopicMatrixDump.csv"

BasicVectorDocs = { } # The documents that we extracted by now, and their inner ID's
BasicVectorDict = { } # The words that we extracted by now, and their inner ID's
BasicVector = np.diag((1, 1)) # The matrix that represents the data
MaxDocId = 0 # Next document ID to assign
MaxTopicId = 0 # Next word ID to assign

# Will extract a text from an XML element
def extractText(element):
    rc = []
    for el in element:
        if el.nodeType == el.TEXT_NODE:
            rc.append(el.data)
    return ''.join(rc)


def AddToTopicVector(Document):
    global BasicVectorDocs
    global BasicVectorDict
    global BasicVector
    global MaxDocId
    global MaxTopicId
    
    # Get the document main ID, and assign the local ID for it
    paperId = Document.getAttributeNode("NEWID").nodeValue
    try:
        MaxDocId
    except NameError:
        currDocId = 0
        MaxDocId = 1
    else:
        currDocId = MaxDocId
    BasicVectorDocs[currDocId] = paperId
    MaxDocId = MaxDocId + 1
    if (MaxDocId > 2):
        shape = (MaxDocId,MaxTopicId)
        BasicVector.resize(shape)
    
    # Get the body
    topicList = Document.getElementsByTagName("TOPICS")[0].getElementsByTagName("D")
    
    #For every topic
    for i in range(0, topicList.length):
        
        #Extract and clean the topic
        topicValue = topicList[i].firstChild.nodeValue
        topicValue = re.sub("\d"," ", topicValue).lower()
        topics = re.sub("[^\w]", " ",  topicValue).split()
        
        for topic in topics:
            bFirst = True;
            if (topic in BasicVectorDict):
                currTopicId = BasicVectorDict[topic] 
                BasicVector[currDocId][currTopicId] = BasicVector[currDocId][currTopicId]
            # Otherwise, add it to the vectors, and matrices
            else:
                if (bFirst):
                    bFirst = False
                try:
                    MaxTopicId
                except NameError:
                    currTopicId = 0
                    MaxTopicId = 1
                else:
                    currTopicId = MaxTopicId
                MaxTopicId = MaxTopicId + 1
                BasicVectorDict[topic] = currTopicId
                shape = (MaxDocId,MaxTopicId)
                BasicVector.resize(shape)
                BasicVector[currDocId][currTopicId] = 1

def DumpTopicOutput(folderPath):
    
    if (not os.path.exists(folderPath)):
        os.mkdir(folderPath)
    
    with open(folderPath + wordDumpName, "wb") as wordFile:
        for id in range(0, MaxTopicId):
            for word, wordID in BasicVectorDict.iteritems():
                if (id == wordID):
                    wordFile.write(word + "\n")

    with open(folderPath + matrixDumpName, "wb") as matrixFile:
        for docID in range(0, MaxDocId):
            for wordID in range(0, MaxTopicId):
                matrixFile.write('"' + str(BasicVector[docID][wordID]) + '",')
            matrixFile.write("\n")
            
    with open(folderPath + wordDumpDocIdName, "wb") as matrixFile:
        for docID in range(0, MaxDocId):
            matrixFile.write('"' + str(BasicVectorDocs[docID]) + '",')
        matrixFile.write("\n")