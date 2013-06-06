import nltk

path = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/716514 - Eric  Neumayer/" \
       "2001_The_human_development_index_and_sustainability_a_constructive_proposal.txt"
with open(path, 'r') as f:
    sample = f.read()
     
sentences = nltk.sent_tokenize(sample)
tokenized_sentences = [nltk.word_tokenize(sentence) for sentence in sentences]
tagged_sentences = [nltk.pos_tag(sentence) for sentence in tokenized_sentences]
chunked_sentences = nltk.batch_ne_chunk(tagged_sentences, binary=True)
 
def extract_entity_names(t):
    entity_names = []
    
    if hasattr(t, 'node') and t.node:
        if t.node == 'NE':
            entity_names.append(' '.join([child[0] for child in t]))
        else:
            for child in t:
                entity_names.extend(extract_entity_names(child))
                
    return entity_names
 
entity_names = []
for tree in chunked_sentences:
    # Print results per sentence
    # print extract_entity_names(tree)
    
    entity_names.extend(extract_entity_names(tree))
 
# Print all entity names
#print entity_names
 
# Print unique entity names
print set(entity_names)