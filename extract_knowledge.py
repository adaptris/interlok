import os
import json

def extract_code_knowledge(source_dir):
    """Extract docs, comments, and logic descriptions"""
    knowledge_base = []

    for root, dirs, files in os.walk(source_dir):
        for file in files:
            if file.endswith('.java'):
                filepath = os.path.join(root, file)
                with open(filepath, 'r') as f:
                    content = f.read()

                # Extract JavaDoc comments
                # Extract class/method descriptions
                # Extract business logic descriptions
                knowledge_base.append({
                    'file': file,
                    'content': content,
                    'type': 'code_documentation'
                })

    return knowledge_base

# Save as JSON
knowledge = extract_code_knowledge('./interlok-core/src/main/java')
with open('knowledge_base.json', 'w') as f:
    json.dump(knowledge, f)