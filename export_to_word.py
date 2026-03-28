import os
from docx import Document
from docx.shared import Pt

"""
Utility script to export all Java source files from the project into a single Microsoft Word document.
This is often used for academic submissions or code reviews.

Dependencies:
- python-docx: install via 'pip install python-docx'
"""

def export_java_to_word(project_path, output_docx):
    """
    Crawls the project directory and appends the content of every .java file to a Word document.

    :param project_path: The root directory of the project to scan.
    :param output_docx: The name/path of the resulting .docx file.
    """
    doc = Document()
    doc.add_heading('Shvil Hazhav - Java Source Code', 0)

    # Walk through the project directory structure
    for root, dirs, files in os.walk(project_path):
        # Skip hidden directories like .git or .idea
        if any(hidden in root for hidden in [".git", ".idea", "build", "gradle"]):
            continue

        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                relative_path = os.path.relpath(file_path, project_path)

                print(f"Processing: {relative_path}")

                # Add a heading for each file with its relative path
                doc.add_heading(relative_path, level=1)

                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        code_content = f.read()

                    # Add the code content using a monospaced font
                    paragraph = doc.add_paragraph()
                    run = paragraph.add_run(code_content)
                    run.font.name = 'Courier New'
                    run.font.size = Pt(9)

                except Exception as e:
                    doc.add_paragraph(f"Error reading file {relative_path}: {e}")

                doc.add_page_break()

    doc.save(output_docx)
    print(f"\nOperation complete! Source code saved to: {output_docx}")

if __name__ == "__main__":
    # Define the project root relative to this script's location
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    OUTPUT_FILE = "ShvilHazhav_SourceCode_Export.docx"

    export_java_to_word(PROJECT_ROOT, OUTPUT_FILE)
