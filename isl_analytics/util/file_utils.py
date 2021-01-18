import os
from typing import List


def get_project_directory() -> str:
    """
    Returns project directory
            Returns:
                    project_dir (str): string containing the directory of the project
    """
    project_dir = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..", "..")
    return project_dir


def get_filepath(files: List[str]) -> str:
    return os.path.join(get_project_directory(), *files)


def get_raw_data_filepath(files: List[str]) -> str:
    return get_filepath(["data", "raw"] + files)
