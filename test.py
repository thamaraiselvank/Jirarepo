``python
def divide_numbers(numerator, denominator):
    try:
        # Attempt to perform the division
        result = numerator / denominator
        return result
    except ZeroDivisionError:
        # Handle the ZeroDivisionError exception
        print("Error: Cannot divide by zero.")
        return None