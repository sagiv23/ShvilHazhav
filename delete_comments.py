import os

"""
Utility script to programmatically remove all comments (single-line and multi-line)
and excessive empty lines from Java source files in the project.
This is typically used to prepare code for final production release or size-constrained exports.
"""

def remove_comments_safe(code):
    """
    State-machine based comment removal that safely ignores comment-like characters inside strings or chars.

    :param code: Raw source code string.
    :return: Cleaned code string without comments.
    """
    result = []
    i = 0
    n = len(code)

    in_single_line_comment = False
    in_multi_line_comment = False
    in_string = False
    in_char = False

    while i < n:
        c = code[i]
        next_c = code[i + 1] if i + 1 < n else ''

        # Handle Single-line comments (//)
        if in_single_line_comment:
            if c == '\n':
                in_single_line_comment = False
                result.append(c)
            i += 1
            continue

        # Handle Multi-line comments (/* */)
        if in_multi_line_comment:
            if c == '*' and next_c == '/':
                in_multi_line_comment = False
                i += 2
            else:
                i += 1
            continue

        # Handle characters inside Strings ("...")
        if in_string:
            result.append(c)
            if c == '\\': # Escape sequence
                if i + 1 < n:
                    result.append(code[i + 1])
                    i += 2
                    continue
            elif c == '"':
                in_string = False
            i += 1
            continue

        # Handle characters inside Chars ('...')
        if in_char:
            result.append(c)
            if c == '\\': # Escape sequence
                if i + 1 < n:
                    result.append(code[i + 1])
                    i += 2
                    continue
            elif c == "'":
                in_char = False
            i += 1
            continue

        # Detect start of String
        if c == '"':
            in_string = True
            result.append(c)
            i += 1
            continue

        # Detect start of Char
        if c == "'":
            in_char = True
            result.append(c)
            i += 1
            continue

        # Detect start of Single-line comment
        if c == '/' and next_c == '/':
            in_single_line_comment = True
            i += 2
            continue

        # Detect start of Multi-line comment
        if c == '/' and next_c == '*':
            in_multi_line_comment = True
            i += 2
            continue

        # Standard character
        result.append(c)
        i += 1

    return ''.join(result)


def remove_empty_lines(code):
    """
    Reduces consecutive empty lines to a single newline to maintain readability.

    :param code: Source code string.
    :return: Formatted code string.
    """
    lines = code.splitlines()
    cleaned_lines = []
    previous_empty = False

    for line in lines:
        stripped = line.strip()
        if stripped == "":
            if not previous_empty:
                cleaned_lines.append("")
                previous_empty = True
        else:
            cleaned_lines.append(line)
            previous_empty = False

    return "\n".join(cleaned_lines)


def clean_java_files(project_path):
    """
    Iterates through the project directory and applies comment and line cleanup to all Java files.
    """
    for root, dirs, files in os.walk(project_path):
        # Skip system and build directories
        if any(hidden in root for hidden in [".git", ".idea", "build", "gradle"]):
            continue

        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                relative_path = os.path.relpath(file_path, project_path)

                print(f"Cleaning comments from: {relative_path}")

                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        code_content = f.read()

                    # Execute cleanup steps
                    cleaned_code = remove_comments_safe(code_content)
                    cleaned_code = remove_empty_lines(cleaned_code)

                    # Overwrite the original file with cleaned code
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(cleaned_code)

                except Exception as e:
                    print(f"Error processing {relative_path}: {e}")

    print("\nCleanup finished! All Java files are now comment-free and formatted.")


if __name__ == "__main__":
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    clean_java_files(PROJECT_ROOT)
