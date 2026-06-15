import os
import statistics
import re

def get_metrics(base_dir):
    loc_per_file = []
    size_per_file = []
    classes_per_group = []
    methods_per_class = []

    class_regex = re.compile(r'\b(?:class|interface|enum)\s+(\w+)')
    
    # Basic method regex: matches 'name(args) {'
    # capturing the name
    method_regex = re.compile(r'\b(?:public|private|protected|static|final|abstract|synchronized)?\s*(?:[\w\<\>\[\]]+\s+)+(\w+)\s*\([^)]*\)\s*(?:throws\s+[^{;]+)?\s*\{')
    
    control_structures = {'if', 'for', 'while', 'switch', 'catch', 'try', 'synchronized', 'return', 'else', 'do'}

    for group_folder in os.listdir(base_dir):
        group_path = os.path.join(base_dir, group_folder)
        if not os.path.isdir(group_path):
            continue
            
        group_classes_count = 0
        
        for root, dirs, files in os.walk(group_path):
            for file in files:
                if file.endswith('.java'):
                    filepath = os.path.join(root, file)
                    
                    size_kb = os.path.getsize(filepath) / 1024.0
                    size_per_file.append(size_kb)
                    
                    try:
                        with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                            content = f.read()
                            
                            # LOC
                            lines = content.split('\n')
                            loc_per_file.append(len(lines))
                            
                            classes_in_file = len(class_regex.findall(content))
                            group_classes_count += classes_in_file
                            
                            method_matches = method_regex.findall(content)
                            valid_methods = [m for m in method_matches if m not in control_structures]
                            methods_count = len(valid_methods)
                            
                            if classes_in_file > 0:
                                for _ in range(classes_in_file):
                                    methods_per_class.append(methods_count / classes_in_file)
                            elif methods_count > 0:
                                methods_per_class.append(methods_count)
                                
                    except Exception as e:
                        pass
                        
        if group_classes_count > 0:
            classes_per_group.append(group_classes_count)

    def print_stats(name, data):
        if not data:
            print(f"{name}: No data")
            return
        print(f"{name}:")
        print(f"  Min   : {min(data):.2f}")
        print(f"  Median: {statistics.median(data):.2f}")
        print(f"  Max   : {max(data):.2f}")

    print("=== Metrics for ASSG4 ===")
    print_stats("Lines of Code (per file)", loc_per_file)
    print_stats("Size in KB (per file)", size_per_file)
    print_stats("Classes (per group/submission)", classes_per_group)
    print_stats("Methods (per class)", methods_per_class)

if __name__ == "__main__":
    get_metrics("../datasets/student-submissions/assg6")
