'''
Created on Sep 7, 2013

@author: Roee Ebenstein

This file is the basic words Frequency creator
Trying to save memory - using american EnglishDictionary to use only words that are in the EnglishDictionary, after stemming.
the idea is that you can use that EnglishDictionary, and build a EnglishDictionary of words that are important for you.

REQUIRED INSTALLATIONS:
=======================
http://nltk.org/install.html
http://johnlaudun.org/20130126-nltk-stopwords/
https://pypi.python.org/pypi/stemming/1.0#downloads

'''
import re
import nltk
import numpy as np
from stemming.porter2 import stem
import enchant
import os


BasicVectorDocs = { } # The documents that we extracted by now, and their inner ID's
BasicVectorEnglishDict = { } # The words that we extracted by now, and their inner ID's
BasicVector = np.diag((1, 1)) # The matrix that represents the data
MaxDocId = 0 # Next document ID to assign
MaxWordId = 0 # Next word ID to assign

wordDumpName = "DictionaryWordDump.txt"
matrixDumpName = "DictionaryMatrixDump.csv"
wordDumpDocIdName = "DictionaryWordDocDump.txt"

# Will extract a text from an XML element
def extractText(element):
    rc = []
    for el in element:
        if el.nodeType == el.TEXT_NODE:
            rc.append(el.data)
    return ''.join(rc)


def AddToEnglishDictBasicVector(Document):
    global BasicVectorDocs
    global BasicVectorEnglishDict
    global BasicVector
    global MaxDocId
    global MaxWordId
    
    # Definitions of constants
    MaxNumberOfCharsInWord = 15 # Min
    MinNumberOfCharsInWord = 2 # Max
    TitleValue = 10 # Title words value, for making it "heavier" than the body words
    
    # English dictionaried words
    EnglishDict = enchant.Dict("en_US")
    
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
        shape = (MaxDocId,MaxWordId)
        BasicVector.resize(shape)
    
    # Get the title
    TitleNode = Document.getElementsByTagName("TITLE")
    Title = ''
    i = 0
    while (i < TitleNode.length):
        Title = Title + TitleNode[i].firstChild.nodeValue
        i = i + 1 
    # Clean the text from digits, and non-ascii characters, and remove any of the known stopwords
    Title = re.sub("\d"," ", Title).lower()
    words = re.sub("[^\w]", " ",  Title).split()
    filtered_title = [stem(w) for w in words if not stem(w) in nltk.corpus.stopwords.words('english')]
    
    # Get the body
    BodyNode = Document.getElementsByTagName("BODY")
    Body = ''
    i = 0
    while (i < BodyNode.length):
        Body = Body + BodyNode[i].firstChild.nodeValue
        i = i + 1 
    # Clean the text from digits, and non-ascii characters, and remove any of the known stopwordss
    Body = re.sub("\d"," ", Body).lower()
    words = re.sub("[^\w]", " ",  Body).split()
    filtered_body = [stem(w) for w in words if not stem(w) in nltk.corpus.stopwords.words('english')]
    
    # Get the Title words, and add them to the relevant vectors and matrices
    # Higher value is assigned for the title
    bFirst = True;
    for word in filtered_title:
        # Validate the word is long enough, and that it is in the English dictionary
        if (word.__len__() <= MinNumberOfCharsInWord or word.__len__() >= MaxNumberOfCharsInWord or not EnglishDict.check(word)):
            continue
        # If it already exist (it means we added it before to the vectors and matrices) increase the count of it.
        if (word in BasicVectorEnglishDict):
            if (bFirst):
                shape = (MaxDocId,MaxWordId)
                BasicVector.resize(shape)
                bFirst = False
            currWordId = BasicVectorEnglishDict[word] 
            BasicVector[currDocId][currWordId] = BasicVector[currDocId][currWordId] + TitleValue
        # Otherwise, add it to the vectors, and matrices
        else:
            if (bFirst):
                bFirst = False
            try:
                MaxWordId
            except NameError:
                currWordId = 0
                MaxWordId = 1
            else:
                currWordId = MaxWordId
            MaxWordId = MaxWordId + 1
            BasicVectorEnglishDict[word] = currWordId
            shape = (MaxDocId,MaxWordId)
            BasicVector.resize(shape)
            BasicVector[currDocId][currWordId] = TitleValue
            
    for word in filtered_body:
        # Validate the word is long enough, and that it is in the English dictionary
        if (word.__len__() <= MinNumberOfCharsInWord or word.__len__() >= MaxNumberOfCharsInWord or not EnglishDict.check(word)):
            continue
        # If it already exist (it means we added it before to the vectors and matrices) increase the count of it.
        if (word in BasicVectorEnglishDict):
            if (bFirst):
                shape = (MaxDocId,MaxWordId)
                BasicVector.resize(shape)
                bFirst = False
            currWordId = BasicVectorEnglishDict[word] 
            BasicVector[currDocId][currWordId] = BasicVector[currDocId][currWordId] + 1
        # Otherwise, add it to the vectors, and matrices 
        else:
            if (bFirst):
                bFirst = False
            try:
                MaxWordId
            except NameError:
                currWordId = 0
                MaxWordId = 1
            else:
                currWordId = MaxWordId
            MaxWordId = MaxWordId + 1
            BasicVectorEnglishDict[word] = currWordId
            shape = (MaxDocId,MaxWordId)
            BasicVector.resize(shape)
            BasicVector[currDocId][currWordId] = 1
            
def DumpWordOutput(folderPath):
    
    if (not os.path.exists(folderPath)):
        os.mkdir(folderPath)
    
    with open(folderPath + wordDumpName, "wb") as wordFile:
        for id in range(0, MaxWordId):
            for word, wordID in BasicVectorEnglishDict.iteritems():
                if (id == wordID):
                    wordFile.write(word + "\n")

    with open(folderPath + matrixDumpName, "wb") as matrixFile:
        for docID in range(0, MaxDocId):
            for wordID in range(0, MaxWordId):
                matrixFile.write('"' + str(BasicVector[docID][wordID]) + '",')
            matrixFile.write("\n")
            
    with open(folderPath + wordDumpDocIdName, "wb") as matrixFile:
        for docID in range(0, MaxDocId):
            matrixFile.write('"' + str(BasicVectorDocs[docID]) + '",')
        matrixFile.write("\n")