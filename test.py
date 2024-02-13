
from flask import Flask, jsonify
import logging

app = Flask(__name__)

# Configure logging
logging.basicConfig(format='%(asctime)s %(levelname)-8s %(filename)s %(message)s',
    datefmt='%Y-%m-%dT%H:%M:%S',
    level=logging.DEBUG, filename="app.log")

# Define routes
@app.route("/")
def home():
    app.logger.info("Application Started")
    return jsonify(message="Welcome to the Flask App!")

@app.route("/login")
def api1():
    try:
        app.logger.info("User Logged in")
        return jsonify(message="User Logged in")
    except Exception as e:
        app.logger.error(f"Error in Login: {str(e)}")
        return jsonify(error="Internal Server Error"), 500

@app.route("/list1")
def list():
    try:
        app.logger.info("Entered into API")
        app.logger.info("Defined a list")
        list = [1, 2, 3]
        app.logger.info("Accessing a element in list")
        value = list[2]  # Fixed the index to access a valid element
        return jsonify(message=value)
    except Exception as e:
        app.logger.error(f"Error in list: {str(e)}")
        return jsonify(error="Internal Server Error"), 500

@app.route("/list2")
def api3():
    try:
        app.logger.info("Entered into API")
        app.logger.info("Defined a list")
        list = [1, 2, 3]
        app.logger.info("Appending a element in list")
        list.append(4)
        app.logger.info("Finding length of the list")
        length = len(list)  # Use len() to get the length of the list
        return jsonify(message=length)
    except Exception as e:
        app.logger.error(f"Error in list: {str(e)}")
        return jsonify(error="Internal Server Error"), 500

@app.route("/list3")
def api4():
    try:
        app.logger.info("Entered into API")
        app.logger.info("Defined a list")
        x = [1, 2, 3]
        app.logger.info("Appending a element in list")
        x.append(4)
        app.logger.info("Returning the list")
        return jsonify(message=x)  # Return the modified list
    except Exception as e:
        app.logger.error(f"Error in list: {str(e)}")
        return jsonify(error="Internal Server Error"), 500

@app.route("/logout")
def api5():
    try:
        app.logger.info("User Logout")
        return jsonify(message="This is API5")
    except Exception as e:
        app.logger.error(f"Error in API5: {str(e)}")
        return jsonify(error="Internal Server Error"), 500

# Run the app
if __name__ == "__main__":
    app.run(debug=True)
