import os
import csv

# This script was used to move all the oracle data from the old class name based oracle
# files to the new absolute file path based oracle files.

source_dir = "oracle/student-submissions/assg6"
target_dir = "assg6-oracles"

if not os.path.exists(source_dir) or not os.path.exists(target_dir):
    print("Directories not found")
    exit(1)

count = 0
for filename in os.listdir(source_dir):
    if not filename.endswith(".csv"):
        continue
        
    source_path = os.path.join(source_dir, filename)
    target_path = os.path.join(target_dir, filename)
    
    if not os.path.exists(target_path):
        print(f"Skipping {filename}: Not found in {target_dir}")
        continue
        
    class_to_smells = {}
    with open(source_path, 'r', newline='') as sf:
        reader = csv.reader(sf)
        header = next(reader, None)
        for row in reader:
            if len(row) >= 2:
                class_to_smells[row[0].strip()] = row[1].strip()
                
    updated_rows = []
    with open(target_path, 'r', newline='') as tf:
        reader = csv.reader(tf)
        header = next(reader, None)
        if header:
            updated_rows.append(header)
            
        for row in reader:
            if len(row) > 0:
                filepath = row[0]
                basename = filepath.split('/')[-1]
                classname = basename.replace('.java', '')
                
                smells = class_to_smells.get(classname, "[]")
                
                if len(row) >= 2:
                    row[1] = smells
                else:
                    row.append(smells)
            updated_rows.append(row)
            
    with open(target_path, 'w', newline='') as tf:
        writer = csv.writer(tf)
        writer.writerows(updated_rows)
    count += 1

print(f"Successfully transferred oracle data for {count} files.")
