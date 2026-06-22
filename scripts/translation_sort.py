import os

TranslationRoot = "../src/main/resources/assets/ic2/lang"

def sort_translation_inline(file_path) -> int:
    with open(file_path, "r", encoding="utf-8") as f:
        lines = f.readlines()
    
    # Remove empty lines and strip whitespace
    lines = [line.strip() for line in lines if line.strip()]
    
    # Sort lines alphabetically
    sorted_lines = sorted(lines)
    
    # Write sorted lines back to the file
    with open(file_path, "w", encoding="utf-8") as f:
        for line in sorted_lines:
            f.write(line + "\n")
    
    return len(sorted_lines)

def main():
    total_lines = 0
    for root, dirs, files in os.walk(TranslationRoot):
        for file in files:
            if file.endswith(".json"):
                file_path = os.path.join(root, file)
                lines_sorted = sort_translation_inline(file_path)
                total_lines += lines_sorted
                print(f"Sorted {lines_sorted} lines in {file_path}")
    
    print(f"Total lines sorted: {total_lines}")