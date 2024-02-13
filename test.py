def divide_numbers(numerator, denominator):
    # This line may cause a ZeroDivisionError
    result = numerator / denominator
    return result

# Example usage:
numerator_value = 10
denominator_value = 0

# The following line will raise a ZeroDivisionError
result = divide_numbers(numerator_value, denominator_value)

# This line will not be executed due to the previous error
print("Result:", result)
