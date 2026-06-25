import os
import csv
import argparse

def process_csv(file_path, prefix_to_strip):
    updated_rows = []
    with open(file_path, 'r', newline='') as f:
        reader = csv.reader(f)
        header = next(reader, None)
        if header:
            updated_rows.append(header)
            
        for row in reader:
            if not row: continue
            
            filepath = row[0].replace('\\', '/')
            
            if filepath.startswith(prefix_to_strip):
                filepath = filepath[len(prefix_to_strip):]
                # remove any leading slash left over
                if filepath.startswith('/'):
                    filepath = filepath[1:]
            
            row[0] = filepath
            updated_rows.append(row)
            
    with open(file_path, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(updated_rows)

def main():
    parser = argparse.ArgumentParser(description="Convert absolute paths in oracle CSVs to relative paths.")
    parser.add_argument("--oracles_dir", required=True, help="Directory containing the oracle CSVs")
    parser.add_argument("--strip_prefix", required=True, help="Prefix to strip from the paths")
    
    args = parser.parse_args()
    
    prefix = args.strip_prefix.replace('\\', '/')
    count = 0
    
    if os.path.isfile(args.oracles_dir):
        process_csv(args.oracles_dir, prefix)
        count += 1
    elif os.path.isdir(args.oracles_dir):
        for filename in os.listdir(args.oracles_dir):
            if filename.endswith(".csv"):
                file_path = os.path.join(args.oracles_dir, filename)
                process_csv(file_path, prefix)
                count += 1
    else:
        print(f"Path not found: {args.oracles_dir}")
        exit(1)
            
    print(f"Successfully converted absolute to relative file paths for {count} oracle files.")

if __name__ == "__main__":
    main()
