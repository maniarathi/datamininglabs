#! /usr/local/bin/python
import sys
import urllib2
import nltk
import time
import re
from itertools import dropwhile
from collections import Counter
from nltk.tokenize import RegexpTokenizer
from nltk.corpus import stopwords
from nltk.corpus import wordnet
from nltk.stem.wordnet import WordNetLemmatizer as WNL

# Global variable that iterates through the document ids to create output
doccounter = 0

# Given an input of an array of words, the function does the following to narrow down the set
# 1. Lemmatizing (e.g. the words are rooted so that play, played, and playing all become play)
# 2. Dictionary - the words are placed into a Counter object which also counts the number of times the same word appears in the original array
# 3. Frequency filter - words that occur less than 100 times in the entire set of documents are removed.
# 4. Stopwords are removed - these words do not distinguish one article from another, so they are not useful.
# 5. non-English words are removed - this step removes any misspelled words.
# 6. Single lettered words are removed - steps 4 and 5 often missed "words" like 'f' and 'b' so this step was included.
# The remaining words are returned as Counter object.
def processwords(words):
        # Lemmatize the words
        print 'Lemmatizing...'
        lmtzr = WNL()
        lemmatized = [lmtzr.lemmatize(w) for w in words ]
        print len(lemmatized)
        # Create a dictionary of the words and the counts
        # Place words in a Counter collection object (this removes duplicates and counts the occurences of a word)
        print 'Mapping words to counts...'
        word_dict = Counter(lemmatized)
        print len(word_dict)
        # Drop out words that occur less than 100 times in the entire set of webpages
        print 'Removing words that appear less than 100 times...'
	for key, count in dropwhile(lambda key_count: key_count[1] >= 100, word_dict.most_common()):
                del word_dict[key]
	print len(word_dict)
        # Filter the words of stopwords (too common), non-English words, and single-letter words
        print 'Filtering out stopwords, non-English words, and single-lettered words...'
        for w in list(word_dict):
                if w in stopwords.words('english'):
                        del word_dict[w]
                elif not wordnet.synsets(w):
                        del word_dict[w]
                elif len(w)==1:
                        del word_dict[w]
        print len(word_dict)
        return word_dict

# Given the string of an entire document, the contents of the body is extracted by searching for the <BODY> tags.
# This function returns an array of the alphabet words contained in between the BODY tags.
def getbodytext(string):
        allwords = '';
        for item in string.split("</BODY>"):
                if "<BODY>" in item:
                        allwords+=item[item.find("<BODY>")+len("</BODY>")-1:]+' '
        print 'Tokenizing...'
        tokenizer = RegexpTokenizer('[a-zA-Z]+')
        words = tokenizer.tokenize(allwords.lower())
        return words

# Given the string of an entire document and an output file, the contents between the tags <PLACES> and <TOPICS> are found and outputed to file.
# This function also removes "words" that are single letter long since they contain no useful information.
# In addition, sometimes the <D> is misclassified as a word so the above filter will remove these tags from the output.
def getplaceandtopictext(item, out):
        out.write('<')
        for i in item.split("</PLACES>"):
                if "<PLACES>" in i:
                        title = i[i.find("<PLACES>")+len("</PLACES>")-1:]
                        tokenizer = RegexpTokenizer('[a-zA-Z]+')
                        words = tokenizer.tokenize(title.lower())
                        for w in words:
                                if wordnet.synsets(w) and len(w)!=1:
                                        out.write(w)
                                        out.write(' ')
        for i in item.split("</TOPICS>"):
                if "<TOPICS>" in i:
                        topics = i[i.find("<TOPICS>")+len("</TOPICS>")-1:]
                        tokenizer = RegexpTokenizer('[a-zA-Z]+')
                        words = tokenizer.tokenize(topics.lower())
                        for w in words:
                                if wordnet.synsets(w) and len(w)!=1:
                                        out.write(w)
                                        out.write(' ')
        out.write('>')

# Given an array of words, the same array is returned with all the duplicate words taken out.
def removeduplicates(arg):
        words = list(set(arg))
        return words

# Creates term vectors given the entire text of a file, the titular words as an array, and the output file to which to write.
def createtermvectors(arg, words, outputfile):
        # Determine a new document by the presence of the <BODY> tag
        for item in arg.split("</BODY>"):
                if "<BODY>" in item:
                        body = item[item.find("<BODY>")+len("</BODY>")-1:]
                        findfrequencies(body, words, outputfile)

# Given the body of a document, the titular words, and the output file, the number of times each of the titular words appears in the body is counted.
# The count is outputted to file.  The output is NOT binary; it is a count of the words.
def findfrequencies(body, words, outputfile):
        global doccounter
        outputfile.write('\nDocument ')
        outputfile.write(`doccounter`)
        outputfile.write(': ')
        doccounter+=1
        # Tokenize the body and lemmatize the words
        tokenizer = RegexpTokenizer('[a-zA-Z]+')
        sepwords = tokenizer.tokenize(body)
        lmtzr = WNL()
        lemmatized = [lmtzr.lemmatize(w) for w in sepwords ]
        # Find frequencies
        content = ' '.join(lemmatized)
        wordcount = dict((x,0) for x in list(words))
        for w in re.findall(r"\w+", content):
                if w in wordcount:
                        wordcount[w] += 1
        # Output counts to file
        for w in wordcount:
                outputfile.write(`wordcount[w]`)
                outputfile.write(' ')

# Creates a transaction vector given the entire text of a file, the titular words as an array, and the output file to which to write.
def createtransactionvector(arg, words, out):
        global doccounter
        # Determine a new document by the presence of the <REUTER> tag
        for item in arg.split("</REUTERS>"):
                if "<REUTERS" in item:
                        out.write('\nDocument ')
                        out.write(`doccounter`)
                        out.write(': ')
                        doccounter+=1
                        document = item[item.find("<REUTERS")+len("</REUTERS>")-1:]
                        # Get the titular words of the body
                        findterms(item, words, out)
                        # Get the title and topic words
                        getplaceandtopictext(document, out) 

# Given the text of an entire document (item), find the body, lemmatize and remove duplicates.
# Then determines which of the titular words (passed in as an array words) appear in the body and outputs to file (out).
def findterms(item, words, out):
        body = item[item.find("<BODY>")+len("</BODY>")-1:]
        # Tokenize the body and lemmatize the words
        tokenizer = RegexpTokenizer('[a-zA-Z]+')
        sepwords = tokenizer.tokenize(body)
        lmtzr = WNL()
        lemmatized = [lmtzr.lemmatize(w) for w in sepwords ]
        # Remove duplicates
        uniquewords = removeduplicates(lemmatized)
        # Find matches with the list of titular words and write to file
        intersection = list(set(uniquewords) & set(list(words)))
        out.write('<')
        for w in intersection:
                out.write(w)
                out.write(' ')
        out.write('>')

def main(argv):
        # Check if current file contains the files from the internet to process
        # If it exists then continue; otherwise, download the files
        for count in range(0,22):
                # Construct the URL of the file to read
                url = 'http://www.cse.ohio-state.edu/~srini/674/public/reuters/'
                file = 'reut2-0'
                if count < 10:
                        file+='0'
                        file+=`count`
                        file+='.sgm'
                else:
                        file+=`count`
                        file+='.sgm'
                # Check if file exists, otherwise download
                try:
                        with open(file): pass
                except:
                        print 'Fetching file '+url+file+' from website...'
                        aResp = urllib2.urlopen(url+file, timeout=3)
                        localfile = open(file, 'w')
                        localfile.write(aResp.read())
                        localfile.close()

	# Aggregate list of words from all the documents
	words=[]
	start = time.time()
	for count in range(0,22):
                # Construct the URL of the file to read
                file = 'reut2-0'
                if count < 10:
                        file+='0'
                        file+=`count`
                        file+='.sgm'
                else:
                        file+=`count`
                        file+='.sgm'
                # Read the file and get all the words
                try:
                        doc = open(file, 'r')
                        text = doc.read()
                        print 'Processing file '+file
                        for w in getbodytext(text):
                                words.append(w)
                        doc.close()
                except IOError:
                        print "Can't open "+file+" for reading."
                        #sys.exit(0)
	print len(words)

	# Filter the words
	filtered_words = processwords(words)
	
	# Create term vector
	output = open('out.txt', 'w')
        global doccounter
        doccounter = 1
	for w in list(filtered_words):
                output.write(w+' '),
	for count in range(0,22):
                # Construct the name of the file to read
                file = 'reut2-0'
                if count < 10:
                        file+='0'
                        file+=`count`
                        file+='.sgm'
                else:
                        file+=`count`
                        file+='.sgm'
                # Read the file
                try:
                        doc = open(file, 'r')
                        text = doc.read()
                        doc.close()
                        print 'Creating term vector for file '+file
                        createtermvectors(text, filtered_words, output)
                except IOError:
                        print "Can't open "+file+" for reading."

        # Create transaction vector
        doccounter = 1
        for count in range(0,22):
                # Construct the name of the file to read
                file = 'reut2-0'
                if count < 10:
                        file+='0'
                        file+=`count`
                        file+='.sgm'
                else:
                        file+=`count`
                        file+='.sgm'
                # Read the file
                try:
                        doc = open(file, 'r')
                        text = doc.read()
                        doc.close()
                        print 'Creating transaction vector for file '+file
                        createtransactionvector(text, filtered_words, output)
                        
                except IOError:
                       print "Can't open "+file+" for reading." 

        output.close()
        # Print stats
        print "Total processing time: "
	print time.time()-start

if __name__ == "__main__":
	main(sys.argv[1:])
