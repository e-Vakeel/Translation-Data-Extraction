/* Created By: Aditya Sharma */
/* Last Modified By: Aditya Sharma */
/* Last Modified On: 18/08/2024 */


# NOTE: Download tesseract and poppler binaries and set correct paths

import pytesseract
from pdf2image import convert_from_path
import concurrent.futures
from PIL import Image
import os

pytesseract.pytesseract.tesseract_cmd = r'F:\Applications\Tesseract\tesseract.exe'


# Function to perform OCR on an image
def ocr_core(image):
    text = pytesseract.image_to_string(image, lang='hin+eng', config='--psm 6')
    return text


# Function to split an image vertically into two parts
def split_image(image):
    width, height = image.size
    left_half = image.crop((0, 0, width // 2, height))
    right_half = image.crop((width // 2, 0, width, height))
    return left_half, right_half


# Function to process a PDF
def process_pdf(pdf_path, output_file):
    images = convert_from_path(pdf_path, poppler_path=r"F:\Applications\Poppler\poppler-24.07.0\Library\bin")

    with open(output_file, 'w', encoding='utf-8') as f:
        for i, image in enumerate(images):
            print(f"Processing page {i + 1}")

            # Split the image into two vertical halves
            left_half, right_half = split_image(image)

            # Perform OCR on each half
            left_text = ocr_core(left_half)
            right_text = ocr_core(right_half)

            f.write("\n")
            f.write(left_text)
            f.write('\n')
            f.write(right_text)


if __name__ == "__main__":
    pdf_path = ['A.pdf', 'D.pdf', 'H.pdf', 'M.pdf', 'P.pdf', 'S.pdf', 'W.pdf']
    output_file = ['A.txt', 'D.txt', 'H.txt', 'M.txt', 'P.txt', 'S.txt', 'W.txt']


    def process_pdf_parallel(pdf, output):
        process_pdf(pdf, output)
        print(f"OCR complete. Text saved to {output}")


    with concurrent.futures.ThreadPoolExecutor() as executor:
        executor.map(process_pdf_parallel, pdf_path, output_file)
