"""
This module provides utility functions to clean up Java source code by removing
non-Javadoc comments and excessive empty lines, while preserving Javadoc comments
and the general formatting.
"""

import os

def remove_comments_keep_javadoc(code):
    """
    Removes single-line (//) and multi-line (/* */) comments from Java code,
    but preserves Javadoc (/** */) comments and string/character literals.

    Args:
        code (str): The Java source code to process.

    Returns:
        str: The processed Java source code with only Javadoc comments remaining.
    """
    result = []
    i = 0
    n = len(code)

    in_single = False
    in_multi = False
    in_javadoc = False
    in_string = False
    in_char = False

    while i < n:
        c = code[i]
        next_c = code[i + 1] if i + 1 < n else ''
        next_next = code[i + 2] if i + 2 < n else ''

        if in_single:
            if c == '\n':
                in_single = False
                result.append('\n')
            i += 1
            continue

        if in_multi:
            if c == '*' and next_c == '/':
                in_multi = False
                i += 2
            else:
                i += 1
            continue

        if in_javadoc:
            result.append(c)
            if c == '*' and next_c == '/':
                result.append(next_c)
                in_javadoc = False
                i += 2
            else:
                i += 1
            continue

        if in_string:
            result.append(c)
            if c == '\\' and i + 1 < n:
                result.append(code[i + 1])
                i += 2
                continue
            elif c == '"':
                in_string = False
            i += 1
            continue

        if in_char:
            result.append(c)
            if c == '\\' and i + 1 < n:
                result.append(code[i + 1])
                i += 2
                continue
            elif c == "'":
                in_char = False
            i += 1
            continue

        if c == '"':
            in_string = True
            result.append(c)
            i += 1
            continue

        if c == "'":
            in_char = True
            result.append(c)
            i += 1
            continue

        if c == '/' and next_c == '/':
            in_single = True
            i += 2
            continue

        # Javadoc
        if c == '/' and next_c == '*' and next_next == '*':
            in_javadoc = True
            result.append('/**')
            i += 3
            continue

        # Standard multi-line comment
        if c == '/' and next_c == '*':
            in_multi = True
            i += 2
            continue

        result.append(c)
        i += 1

    return ''.join(result)


def remove_extra_empty_lines(code):
    """
    Reduces multiple consecutive empty lines in the code to a single empty line.

    Args:
        code (str): The code to process.

    Returns:
        str: The code with excessive empty lines removed.
    """
    lines = code.splitlines()
    new_lines = []
    empty_count = 0

    for line in lines:
        if line.strip() == "":
            empty_count += 1
            if empty_count <= 1:  # Keep at most one empty line
                new_lines.append("")
        else:
            empty_count = 0
            new_lines.append(line)

    return "\n".join(new_lines)


def clean_java_keep_format(project_path):
    """
    Recursively scans the project path for .java files and applies cleaning:
    removes non-Javadoc comments and extra empty lines.

    Args:
        project_path (str): The root directory of the project to clean.
    """
    for root, dirs, files in os.walk(project_path):
        # Skip common non-source directories
        if any(x in root for x in [".git", ".idea", "build", "gradle"]):
            continue

        for file in files:
            if file.endswith(".java"):
                path = os.path.join(root, file)
                rel = os.path.relpath(path, project_path)

                print(f"Cleaning (keep format): {rel}")

                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        code = f.read()

                    code = remove_comments_keep_javadoc(code)
                    code = remove_extra_empty_lines(code)

                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(code)

                except Exception as e:
                    print(f"Error: {rel} -> {e}")

    print("\nDone! Clean + readable + Javadoc kept.")


if __name__ == "__main__":
    # Get the directory where the script is located and start cleaning from there.
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    clean_java_keep_format(PROJECT_ROOT)
