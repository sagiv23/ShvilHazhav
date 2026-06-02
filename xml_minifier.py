"""
This module provides utility functions to clean up XML files by removing
comments and excessive empty lines, while preserving the basic structure.
It recursively scans the project for .xml files and applies these cleanups.
"""

import os
import re

def remove_xml_comments(content):
    """
    Removes <!-- comment --> blocks from XML content.
    """
    # Using regex to remove comments. re.DOTALL allows matching across multiple lines.
    return re.sub(r'<!--.*?-->', '', content, flags=re.DOTALL)

def remove_extra_empty_lines(content):
    """
    Reduces multiple consecutive empty lines to a single empty line,
    and removes leading/trailing empty lines.
    """
    lines = content.splitlines()
    new_lines = []
    empty_count = 0

    for line in lines:
        if line.strip() == "":
            empty_count += 1
            if empty_count <= 1:
                new_lines.append("")
        else:
            empty_count = 0
            new_lines.append(line)

    # Join lines and strip to remove leading/trailing blank lines
    return "\n".join(new_lines).strip()

def clean_xml_files(project_path):
    """
    Recursively scans and cleans .xml files in the given project path.
    """
    for root, dirs, files in os.walk(project_path):
        # Skip common directories that shouldn't be touched
        if any(x in root for x in [".git", ".idea", "build", "gradle", ".gradle"]):
            continue

        for file in files:
            if file.endswith(".xml"):
                path = os.path.join(root, file)
                rel = os.path.relpath(path, project_path)

                print(f"Cleaning XML: {rel}")

                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        content = f.read()

                    # Apply cleanups
                    original_content = content
                    content = remove_xml_comments(content)
                    content = remove_extra_empty_lines(content)

                    # Only write back if changes were made
                    if content != original_content.strip():
                        with open(path, 'w', encoding='utf-8') as f:
                            f.write(content)
                            # Ensure file ends with a newline if it's not empty
                            if content:
                                f.write('\n')

                except Exception as e:
                    print(f"Error processing {rel}: {e}")

    print("\nDone! Cleaned XML comments and empty lines.")

if __name__ == "__main__":
    # Use the directory where the script is located as the project root
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    clean_xml_files(PROJECT_ROOT)
