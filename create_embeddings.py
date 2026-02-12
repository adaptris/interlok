from sentence_transformers import SentenceTransformer
import json
import numpy as np
import pickle

# Load your knowledge base
with open('knowledge_base.json', 'r') as f:
    knowledge_base = json.load(f)

# Use a Hugging Face embedding model
model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')

# Create embeddings for each knowledge chunk
texts = [item['content'] for item in knowledge_base]
embeddings = model.encode(texts, show_progress_bar=True)

# Save embeddings and metadata
data = {
    'embeddings': embeddings,
    'knowledge_base': knowledge_base
}

with open('embeddings.pkl', 'wb') as f:
    pickle.dump(data, f)

print(f"Created {len(embeddings)} embeddings")