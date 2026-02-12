import pickle
import json

# Load embeddings
with open('embeddings.pkl', 'rb') as f:
    data = pickle.load(f)

# Save as formats Java can read
np.save('embeddings.npy', data['embeddings'])

with open('knowledge_metadata.json', 'w') as f:
    json.dump(data['knowledge_base'], f)