import subprocess
import sys
from collections import defaultdict

def main():
    script_path = "./test-groups-assg4.sh"
    if len(sys.argv) > 1:
        script_path = sys.argv[1]

    print(f"Running script {script_path} and compiling results...")
    
    # Run the bash script and capture output
    process = subprocess.Popen([script_path], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    
    # metrics[smell_name] = {'TP': 0, 'FP': 0, 'FN': 0, 'TN': 0, 'Samples': 0}
    metrics = defaultdict(lambda: {'TP': 0, 'FP': 0, 'FN': 0, 'TN': 0, 'Samples': 0})
    
    in_table = False
    
    for line in process.stdout:
        print(line, end='') # Echo output so user can see progress
        line_stripped = line.strip()
        
        if "STATICNOSE PER-SMELL METRICS REPORT" in line_stripped:
            in_table = False
            continue
            
        if line_stripped.startswith("Smell Name"):
            in_table = True
            continue
            
        if in_table and line_stripped.startswith("---"):
            continue
            
        if in_table and line_stripped.startswith("==="):
            in_table = False
            continue
            
        if in_table and "|" in line_stripped:
            # Parse row
            parts = [p.strip() for p in line_stripped.split("|")]
            if len(parts) >= 6:
                smell_name = parts[0]
                samples = int(parts[1])
                tp = int(parts[2])
                fp = int(parts[3])
                fn = int(parts[4])
                tn = int(parts[5])
                
                metrics[smell_name]['Samples'] += samples
                metrics[smell_name]['TP'] += tp
                metrics[smell_name]['FP'] += fp
                metrics[smell_name]['FN'] += fn
                metrics[smell_name]['TN'] += tn

    process.wait()
    
    if process.returncode != 0:
        print(f"Warning: The script {script_path} exited with code {process.returncode}")

    # Print compiled results
    print("\n\n")
    print("=========================================================================================")
    print("                   COMPILED STATICNOSE PER-SMELL METRICS REPORT")
    print("=========================================================================================")
    print(f"  {'Smell Name':<24} | {'Samples':>7} | {'TP':>4} | {'FP':>4} | {'FN':>4} | {'TN':>4} | {'Precision':>9} | {'Recall':>8} | {'F1':>4}")
    print("-" * 89)
    
    total_tp = total_fp = total_fn = total_tn = total_samples = 0
    
    for smell, counts in sorted(metrics.items()):
        tp = counts['TP']
        fp = counts['FP']
        fn = counts['FN']
        tn = counts['TN']
        samples = counts['Samples']
        
        total_tp += tp
        total_fp += fp
        total_fn += fn
        total_tn += tn
        total_samples += samples
        
        precision = tp / (tp + fp) if (tp + fp) > 0 else 0.0
        recall = tp / (tp + fn) if (tp + fn) > 0 else 0.0
        f1 = 2 * (precision * recall) / (precision + recall) if (precision + recall) > 0 else 0.0
        
        print(f"  {smell:<24} | {samples:>7} | {tp:>4} | {fp:>4} | {fn:>4} | {tn:>4} | {precision:>8.2%} | {recall:>7.2%} | {f1:.2f}")

    print("=========================================================================================")
    
    if (total_tp + total_fp + total_fn + total_tn) > 0:
        print("\n========================================")
        print("   COMPILED GLOBAL METRICS REPORT     ")
        print("========================================")
        print(f"  Total TP: {total_tp:<4}   Total FP: {total_fp:<4}")
        print(f"  Total FN: {total_fn:<4}   Total TN: {total_tn:<4}")
        print("-" * 40)
        
        global_precision = total_tp / (total_tp + total_fp) if (total_tp + total_fp) > 0 else 0.0
        global_recall = total_tp / (total_tp + total_fn) if (total_tp + total_fn) > 0 else 0.0
        global_f1 = 2 * (global_precision * global_recall) / (global_precision + global_recall) if (global_precision + global_recall) > 0 else 0.0
        
        print(f"  Overall Precision : {global_precision:.2%}")
        print(f"  Overall Recall    : {global_recall:.2%}")
        print(f"  Overall F1-Score  : {global_f1:.2f}")
        print("----------------------------------------\n")

if __name__ == "__main__":
    main()
