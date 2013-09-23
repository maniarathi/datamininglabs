'''
Created on Sep 4, 2013

@author: Roee Ebenstein

This file is an XML creator from the SGM Reuters format.
'''
import os
import re
import fnmatch
import string
import HTMLParser

# This will take build xml's from sgm's. 
# The input is the path for the SGM's, and the path for the XML's
def SgmToXml(SgmPath, XmlPath):
    # File extensions to read/create
    inner_ext = 'xml'
    reuters_ext = 'sgm'
    
    # Builds filters for the extensions
    output_extension = ['*.' + inner_ext] # format of output
    output_extension = r'|'.join([fnmatch.translate(x) for x in output_extension])
    
    input_extension = ['*.' + reuters_ext] # for dirs and files
    input_extension = r'|'.join([fnmatch.translate(x) for x in input_extension])
    
    # This will take all the files from the given directory, and will create a valid xml from them xml
    
    # First, delete all xmls from the target directory:
    print 'deleting old, already processed XMLs'
    for (dirpath, dirnames, filenames) in os.walk(XmlPath):
        if (len(filenames) > 0):
            files = [f for f in filenames if re.match(output_extension, f)]
            for filename in files:
                print 'removed ' + filename
                os.remove(dirpath + filename)
    
    # Second, Create the xmls from the SGM's
    print 'Creating XML\'s from reuters input files (sgm\'s)'
    for (dirpath, dirnames, filenames) in os.walk(SgmPath):
        if (len(filenames) > 0):
            files = [f for f in filenames if re.match(input_extension, f)]
            for filename in files:
                
                # Reads the xml file
                print 'Reads ' + dirpath + filename
                with open(dirpath+filename, "r") as xmlFixer:
                    text = xmlFixer.read()
                    
                #Builds the xml to write
                with open(XmlPath+filename +'.xml', "w") as xmlFixer:
                    
                    # Separate the first line from the others (first line is a special line:
                    # <!DOCTYPE lewis SYSTEM "lewis.dtd">
                    # HAS NOT CLOSING ITEM
                    PlaceToAdd = string.find(text,'\n')
                    FirstSection = text[0:PlaceToAdd+1]
                    SecondSection = text[PlaceToAdd+1:]
                    
                    # Clean all the non ascii characters (since this is an escaped character document,
                    # in this phase we don't lose any information
                    FirstSection = filter(lambda x: x in string.printable, FirstSection)
                    SecondSection = filter(lambda x: x in string.printable, SecondSection)
                    
                    # Return the document to be a UTF document.
                    # Since there are two characters that can mix the translation up:
                    # "<" - might be considered as an opening element
                    # "&" - might be considered as a start of unescaped character
                    # We make sure they'll remain in the final output (being replaced to a very unlikely string....
                    pars = HTMLParser.HTMLParser()
                    FirstSection = pars.unescape(FirstSection) # XML header will not contain those characters...
                    
                    SecondSection = SecondSection.replace('&amp;','THIS_WAS_AMP_TEXT_ROEE')
                    SecondSection = SecondSection.replace('&lt;','THIS_WAS_LT_TEXT_ROEE')
                    SecondSection = pars.unescape(SecondSection)
                    SecondSection = SecondSection.replace('THIS_WAS_LT_TEXT_ROEE','&lt;')
                    SecondSection = SecondSection.replace('THIS_WAS_AMP_TEXT_ROEE', '&amp;')
                    
                    # Fix secondString to be a legal unicode 
                    # Removes all characters that are in an illegal UTF representation
                    RE_XML_ILLEGAL = u'([\u0000-\u0008\u000b-\u000c\u000e-\u001f\ufffe-\uffff])' + \
                    u'|' + \
                    u'([%s-%s][^%s-%s])|([^%s-%s][%s-%s])|([%s-%s]$)|(^[%s-%s])' % \
                    (unichr(0xd800),unichr(0xdbff),unichr(0xdc00),unichr(0xdfff),
                     unichr(0xd800),unichr(0xdbff),unichr(0xdc00),unichr(0xdfff),
                     unichr(0xd800),unichr(0xdbff),unichr(0xdc00),unichr(0xdfff))
                    SecondSection = re.sub(RE_XML_ILLEGAL, "?", SecondSection)
                    
                    # Clears more illegal characters... (Somehow the test files provided had to get all of these...)
                    ranges = [(0, 8), (0xb, 0x1f), (0x7f, 0x84), (0x86, 0x9f), (0xd800, 0xdfff), (0xfdd0, 0xfddf), (0xfffe, 0xffff)]
                    nukemap = dict.fromkeys(r for start, end in ranges for r in range(start, end+1))
                    SecondSection = SecondSection.translate(nukemap)

                    # Write the XML to a file
                    # Since it is an illegal XML (multiple children) - I make it legal, by adding a root node.
                    xmlFixer.seek(0) 
                    #xmlFixer.write(FirstSection + u'<reports>\n'+SecondSection + u'\n</reports>') 
                    xmlFixer.write(u'<reports>\n'+SecondSection + u'\n</reports>')
                    print 'Wrote ' + dirpath + filename+'.xml'