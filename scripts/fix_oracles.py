import os

# script for fixing paths in oracles
# if you accidentally put ./ in front of the path when generating
# oracles, run this with the correct directory in which the oracles are
# it will replace /./ with / in all the files
# which will match with the paths when the oracle is being evaluated.

directory = "../test-smell-oracle"

if not os.path.exists(directory):
    print(f"Directory {directory} does not exist.")
    exit(1)

count = 0
for filename in os.listdir(directory):
    if filename.endswith(".csv"):
        filepath = os.path.join(directory, filename)
        
        with open(filepath, 'r') as f:
            content = f.read()
            
        new_content = content.replace('/./', '/')
        
        if content != new_content:
            with open(filepath, 'w') as f:
                f.write(new_content)
            count += 1

print(f"Successfully processed and fixed paths in {count} CSV files in {directory}.")
