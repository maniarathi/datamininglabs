'''
Created on Sep 6, 2013

@author: Roee Ebenstein

This file is the basic words Frequency creator
Requires 8 GB of memory.
The only reason it does - is because I'm clearing all the words that are longer than 15 chars.
Clearing all the words that are 2 or less chars
removing any digits from the indexing.

Requires - re, nltk, numpy, and stemming (corpora)

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



BasicVectorDocs = { } # The documents that we extracted by now, and their inner ID's
BasicVectorDict = { } # The words that we extracted by now, and their inner ID's
BasicVector = np.diag((1, 1)) # The matrix that represents the data
MaxDocId = 0 # Next document ID to assign
MaxWordId = 0 # Next word ID to assign


# Will extract a text from an XML element
def extractText(element):
    rc = []
    for el in element:
        if el.nodeType == el.TEXT_NODE:
            rc.append(el.data)
    return ''.join(rc)


def AddToBasicVector(Document):
    global BasicVectorDocs
    global BasicVectorDict
    global BasicVector
    global MaxDocId
    global MaxWordId
    
    # Definitions of constants
    MaxNumberOfCharsInWord = 15
    MinNumberOfCharsInWord = 2
    TitleValue = 10 # Title words value, for making it "heavier" than the body words
    
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
    # Clean the text from digits, and non-ascii characters, and remove any of the known stopwords
    Body = re.sub("\d"," ", Body).lower()
    words = re.sub("[^\w]", " ",  Body).split()
    filtered_body = [stem(w) for w in words if not stem(w) in nltk.corpus.stopwords.words('english')]
    
    # Get the Title words, and add them to the relevant vectors and matrices
    # Higher value is assigned for the title
    bFirst = True;
    for word in filtered_title:
        # Validate the word is long enough
        if (word.__len__() <= MinNumberOfCharsInWord or word.__len__() >= MaxNumberOfCharsInWord):
            continue
        # If it already exist (it means we added it before to the vectors and matrices) increase the count of it.
        if (word in BasicVectorDict):
            if (bFirst):
                shape = (MaxDocId,MaxWordId)
                BasicVector.resize(shape)
                bFirst = False
            currWordId = BasicVectorDict[word] 
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
            BasicVectorDict[word] = currWordId
            shape = (MaxDocId,MaxWordId)
            BasicVector.resize(shape)
            BasicVector[currDocId][currWordId] = TitleValue
            
    for word in filtered_body:
        # Validate the word is long enough
        if (word.__len__() <= MinNumberOfCharsInWord or word.__len__() >= MaxNumberOfCharsInWord):
            continue
        # If it already exist (it means we added it before to the vectors and matrices) increase the count of it.
        if (word in BasicVectorDict):
            if (bFirst):
                shape = (MaxDocId,MaxWordId)
                BasicVector.resize(shape)
                bFirst = False
            currWordId = BasicVectorDict[word] 
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
            BasicVectorDict[word] = currWordId
            shape = (MaxDocId,MaxWordId)
            BasicVector.resize(shape)
            BasicVector[currDocId][currWordId] = 1