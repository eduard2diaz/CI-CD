from random import randint

min_number = int(input('Please enter the min number'))
max_number = int(input('Please enter the max number'))

if max_number < min_number:
    print('Invalid input')
else:
    random_number = randint(min_number, max_number)
    print('Random number', random_number)