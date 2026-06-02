"""
This module provides utility functions to clean up XML files by removing
comments and ALL empty lines.
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
    Removes all empty lines from the content.
    """
    lines = content.splitlines()
    new_lines = [line for line in lines if line.strip() != ""]
    return "\n".join(new_lines)

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
                        original_content = f.read()

                    # Apply cleanups
                    content = remove_xml_comments(original_content)
                    content = remove_extra_empty_lines(content)

                    # Only write back if the content has changed
                    if content != original_content:
                        with open(path, 'w', encoding='utf-8') as f:
                            f.write(content)

                except Exception as e:
                    print(f"Error processing {rel}: {e}")

    print("\nDone! Cleaned XML comments and all empty lines.")

if __name__ == "__main__":
    # Use the directory where the script is located as the project root
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    clean_xml_files(PROJECT_ROOT)
