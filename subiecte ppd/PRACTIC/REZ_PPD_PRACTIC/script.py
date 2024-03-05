import sys
import random
import string

def get_random_string(length):
    # choose from all lowercase letter
    letters = string.ascii_lowercase
    result_str = ''.join(random.choice(letters) for i in range(length))
    result_str = result_str.capitalize()
    return result_str

def generate_random_input_for_department(noMessages, filename):
    with open(filename, "w") as file:
        for _ in range(noMessages):
            title = get_random_string(8)
            author = get_random_string(10)
            file.write(title + ',' + author + '\n')


filename = sys.argv[1]
noMessages = int(sys.argv[2])
generate_random_input_for_department(noMessages=noMessages, filename=filename)