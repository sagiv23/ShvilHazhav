"""
This module provides utility functions to clean up Java source code by removing
non-Javadoc comments and excessive empty lines, while preserving Javadoc comments
and minifying single-line methods (like getters/setters).
"""

import os
import re

def minify_javadoc_block(text):
    """
    Collapses multiple spaces within a Javadoc block and removes lines
    that only contain the '*' character (empty Javadoc lines).
    """
    lines = text.splitlines()
    processed_lines = []
    for line in lines:
        stripped = line.strip()

        # Keep the opening and closing markers as is
        if stripped == "/**" or stripped == "*/" or stripped == "**/":
            processed_lines.append(line.rstrip())
            continue

        # Match standard Javadoc line: (indent) * (content)
        match = re.match(r'^(\s*)\*(\s*)(.*)', line)
        if match:
            indent, spaces, content = match.groups()
            # Collapse multiple spaces in content
            content = re.sub(r' +', ' ', content).strip()

            if not content:
                # This is an "empty" Javadoc line like " * ", skip it
                continue

            # Reconstruct the line with a single space after the asterisk
            processed_lines.append(f"{indent}* {content}")
        else:
            # Handle lines like the start line if it has content: /** Content
            if stripped.startswith("/**"):
                content = stripped[3:].strip()
                if content:
                    content = re.sub(r' +', ' ', content)
                    # Find indent of /**
                    match_indent = re.match(r'^(\s*)', line)
                    indent = match_indent.group(1) if match_indent else ""
                    processed_lines.append(f"{indent}/** {content}")
                else:
                    processed_lines.append(line.rstrip())
            else:
                processed_lines.append(line.rstrip())

    return "\n".join(processed_lines)

def minify_single_line_methods(code):
    """
    Finds methods that contain only one line of code in their body and
    reformats them to be on a single line.
    Example:
    public String getName() {
        return name;
    }
    becomes:
    public String getName() { return name; }
    """
    # Regex to find method signatures followed by a single line body
    # This is a simplified regex and might not catch all cases, but should work for most getters/setters
    pattern = r'(\s*(?:public|protected|private|static|\s)+[\w<>[\]]+\s+\w+\s*\(.*?\)\s*\{)\s*\n\s*(.*?);\s*\n\s*\}'

    def replacer(match):
        signature = match.group(1)
        body = match.group(2)
        return f"{signature} {body}; }}"

    return re.sub(pattern, replacer, code)

def remove_comments_keep_javadoc(code):
    """
    Removes single-line (//) and multi-line (/* */) comments from Java code,
    but preserves Javadoc (/** */) comments and string/character literals.
    Also minifies Javadoc comments.
    """
    result = []
    i = 0
    n = len(code)

    in_single = False
    in_multi = False
    in_javadoc = False
    in_string = False
    in_char = False
    javadoc_buffer = []

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
            javadoc_buffer.append(c)
            if c == '*' and next_c == '/':
                javadoc_buffer.append(next_c)
                javadoc_text = "".join(javadoc_buffer)
                result.append(minify_javadoc_block(javadoc_text))
                javadoc_buffer = []
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

        if c == '/' and next_c == '*' and next_next == '*':
            in_javadoc = True
            javadoc_buffer = ['/**']
            i += 3
            continue

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
    """
    lines = code.splitlines()
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

    return "\n".join(new_lines)


def clean_java_keep_format(project_path):
    """
    Recursively scans and cleans .java files.
    """
    for root, dirs, files in os.walk(project_path):
        if any(x in root for x in [".git", ".idea", "build", "gradle"]):
            continue

        for file in files:
            if file.endswith(".java"):
                path = os.path.join(root, file)
                rel = os.path.relpath(path, project_path)

                print(f"Cleaning: {rel}")

                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        code = f.read()

                    code = remove_comments_keep_javadoc(code)
                    code = remove_extra_empty_lines(code)
                    code = minify_single_line_methods(code)

                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(code)

                except Exception as e:
                    print(f"Error: {rel} -> {e}")

    print("\nDone! Minified Javadocs, single-line methods, and cleaned code.")


if __name__ == "__main__":
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    clean_java_keep_format(PROJECT_ROOT)
