import os
from docx import Document
from docx.shared import Pt, RGBColor

"""
Utility script to export all Java source files from the project into a single Microsoft Word document.
This is often used for academic submissions or code reviews.

Dependencies:
- python-docx: install via 'pip install python-docx'
"""

def export_java_to_word(project_path, output_docx):
    """
    Crawls the project directory and appends the content of every .java file to a Word document.
    """
    doc = Document()
    doc.add_heading('Shvil Hazhav - Java Source Code', 0)

    # ניתוב לתיקיית המקור של ה-Java כדי שהנתיבים יהיו קצרים ויחסיים לחבילה (Package)
    java_base_path = os.path.join(project_path, "app", "src", "main", "java", "com", "example", "sagivproject")

    # בדיקה שהתיקייה קיימת, אם לא - נשתמש בנתיב הפרויקט הכללי
    search_path = java_base_path if os.path.exists(java_base_path) else project_path

    # Walk through the project directory structure
    for root, dirs, files in os.walk(search_path):
        # Skip hidden directories like .git or .idea
        if any(hidden in root for hidden in [".git", ".idea", "build", "gradle"]):
            continue

        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                # חישוב נתיב יחסי מהחבילה הראשית
                relative_path = os.path.relpath(file_path, search_path)
                # החלפת לוכסנים לפורמט ווינדוס והוספת הסימן מהדוגמה
                display_path = "▶ " + relative_path.replace('/', '\\')

                print(f"Processing: {display_path}")

                # הוספת כותרת ועיצובה (Arial, 12, #09890E, ללא Bold)
                heading = doc.add_heading('', level=1)
                run = heading.add_run(display_path)
                run.font.name = 'Arial'
                run.font.size = Pt(12)
                run.font.bold = False  # ביטול ההדגשה (Bold)
                run.font.color.rgb = RGBColor(0x09, 0x89, 0x0E)

                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        code_content = f.read()

                    # Add the code content using a monospaced font
                    paragraph = doc.add_paragraph()
                    code_run = paragraph.add_run(code_content)
                    code_run.font.name = 'Courier New'
                    code_run.font.size = Pt(9)

                except Exception as e:
                    doc.add_paragraph(f"Error reading file {display_path}: {e}")

                doc.add_page_break()

    doc.save(output_docx)
    print(f"\nOperation complete! Source code saved to: {output_docx}")

if __name__ == "__main__":
    # Define the project root relative to this script's location
    PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
    OUTPUT_FILE = "ShvilHazhav_SourceCode_Export.docx"

    export_java_to_word(PROJECT_ROOT, OUTPUT_FILE)