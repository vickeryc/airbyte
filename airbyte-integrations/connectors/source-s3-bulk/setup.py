#
# Copyright (c) 2023 Airbyte, Inc., all rights reserved.
#


from setuptools import find_packages, setup
from pathlib import Path

def local_dependency(name: str) -> str:
    """Returns a path to a local package."""
    return f"{name} @ file://{Path.cwd().parent / name}"

MAIN_REQUIREMENTS = [
    "airbyte-cdk>=0.52.0",
    "pyarrow==12.0.1",
    "smart-open[s3]==5.1.0",
    "wcmatch==8.4",
    "dill==0.3.4",
    "pytz",
    "fastavro==1.4.11",
    "python-snappy==0.6.1",
    "unstructured==0.10.19",
    "pdf2image==1.16.3",
    "pdfminer.six==20221105",
    "unstructured[docx]==0.10.19",
    "unstructured.pytesseract>=0.3.12",
    "pytesseract==0.3.10",
    "markdown",
]

MAIN_REQUIREMENTS.append(local_dependency("source-s3"))

TEST_REQUIREMENTS = [
    "requests-mock~=1.9.3",
    "pytest-mock~=3.6.1",
    "pytest~=6.1",
    "pandas==2.0.3",
    "psutil",
    "pytest-order",
    "netifaces~=0.11.0",
    "docker",
    "avro==1.11.0",
]

setup(
    name="source_s3_bulk",
    description="Source implementation for S3.",
    author="Airbyte",
    author_email="contact@airbyte.io",
    packages=find_packages(),
    install_requires=MAIN_REQUIREMENTS,
    package_data={"": ["*.json", "schemas/*.json", "schemas/shared/*.json"]},
    extras_require={
        "tests": TEST_REQUIREMENTS,
    },
)
