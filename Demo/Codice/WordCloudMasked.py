import nltk
nltk.download('punkt')
nltk.download('stopwords')

import numpy as np
import pandas as pd
from nltk.tokenize import word_tokenize
from nltk.tokenize import TweetTokenizer
import re
from collections import Counter
import itertools
import string
from nltk import wordpunct_tokenize
from nltk.stem.lancaster import LancasterStemmer
import matplotlib.pyplot as plt
#%matplotlib inline
from PIL import Image
import numpy as np
from wordcloud import WordCloud

#import dataset
csv = "C://Users//lucar//Desktop//DAnalytics//Testi//CSVusertext.xlsx"
df = pd.read_excel(csv, encoding = "utf-8")
tweets = df["text"]
    #espressione regolare

     
emoticons_str = r"""
        (?:
            [:=;] # Eyes
            [oO\-]? # Nose (optional)
            [D\)\]\(\]/\\OpP] # Mouth
        )"""
     
regex_str = [
        emoticons_str,
        r'<[^>]+>', # HTML tags
        r'(?:@[\w_]+)', # @-mentions
        r"(?:\#+[\w_]+[\w\'_\-]*[\w_]+)", # hash-tags
        r'http[s]?://(?:[a-z]|[0-9]|[$-_@.&amp;+]|[!*\(\),]|(?:%[0-9a-f][0-9a-f]))+', # URLs
     
        r'(?:(?:\d+,?)+(?:\.?\d+)?)', # numbers
        r"(?:[a-z][a-z'\-_]+[a-z])", # words with - and '
        r'(?:[\w_]+)', # other words
        r'(?:\S)' # anything else
    ]
        
tokens_re = re.compile(r'('+'|'.join(regex_str)+')', re.VERBOSE | re.IGNORECASE)
emoticon_re = re.compile(r'^'+emoticons_str+'$', re.VERBOSE | re.IGNORECASE)
     
def tokenize(s):
        return tokens_re.findall(s)
     
def preprocess(s, lowercase=False):
        tokens = tokenize(s)
        if lowercase:
            tokens = [token if emoticon_re.search(token) else token.lower() for token in tokens]
        return tokens

    #Tokenization
tweets.apply(preprocess)  
tokening = TweetTokenizer()
tokening = TweetTokenizer(strip_handles=True, reduce_len=True)
tweets_tokenized = tweets.apply(tokening.tokenize)
    #print(tweets_tokenized)

    #Counter
    #sentences = (list(itertools.chain(tweets_tokenized)))
    #flat_list = [item for sublist in sentences for item in sublist]
    #c = Counter(flat_list)
    #print(c.most_common(10))

    # Function to get the counter
def get_counter(df):
      sentences = (list(itertools.chain(df)))
      flat_list = [item for sublist in sentences for item in sublist]
      c = Counter(flat_list)
      return c

from nltk.corpus import stopwords
stop =stopwords.words('C://Users//lucar//Desktop//DAnalytics//Testi//stopwords-it.txt')
tweets_tokenized_stop = tweets_tokenized.apply(lambda x: [item for item in x if item not in stop])
sentences = (list(itertools.chain(tweets_tokenized_stop)))
flat_list = [item for sublist in sentences for item in sublist]
    #c = Counter(flat_list)
    #print(c.most_common(10))

    #punteggiatura
punctuation = string.punctuation
tweets_tokenized_stop_punct = tweets_tokenized.apply(lambda x: [item for item in x if item not in punctuation])
    #tweets_tokenized_stop_punct2 = tweets_tokenized_stop_punct.apply(lambda x: [item for item in x if item not in stop])
    #C=get_counter(tweets_tokenized_stop_punct2)
    #print(C.most_common(10))

stop =set(stop)
    #adding some of the stopwords after observing the tweets
stop.add("...")
stop.add("€")
stop.add("x")
stop.add("J")
stop.add("K")
stop.add("…")
stop.add("\x81")
stop.add("\x89")
stop.add("ĚĄ")
stop.add("ă")
stop.add("\x9d")
stop.add("âÂĺ")
stop.add("Ě")
stop.add("˘")
stop.add("Â")
stop.add("âÂ")
stop.add("Ň")
stop.add("http")
stop.add("https")
stop.add("co")
stop.add("000")
stop.add("Ň")
stop.add("Ň")
stop.add("Ň")

stop = list(stop)
tweets_tokenized_stop_punct2 = tweets_tokenized_stop_punct.apply(lambda x: [item for item in x if item not in stop])
    #C=get_counter(tweets_tokenized_stop_punct2)
    #print(C.most_common(10))

    #Stemming
lancaster_stemmer = LancasterStemmer()
tweets_tokenized_new_stem = tweets_tokenized_stop_punct.apply(lambda x: [lancaster_stemmer.stem(item) for item in x])
    #C=get_counter(tweets_tokenized_new_stem)
    #print(C.most_common(10))

    #plotting
sentences = (list(itertools.chain(tweets_tokenized_new_stem)))
flat_list = [item for sublist in sentences for item in sublist]
if flat_list == []:
        flat_list.append("Citazioni")  
fig = plt.figure(figsize=(8,7))
gdf_mask = np.array(Image.open("C://Users//lucar//Desktop//gdf.jpg"))
wordcloud = WordCloud(background_color="white", max_words=2000, mask=gdf_mask,contour_color='black').generate(" ".join(flat_list))
    #print(wordcloud.words_)
plt.imshow(wordcloud,interpolation='bilinear')
plt.axis("off")
plt.savefig('C://Users//lucar//Desktop//Totale_WC.jpg')
plt.close()