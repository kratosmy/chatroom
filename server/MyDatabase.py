import mysql.connector

class MyDatabase: 
	conn = None
	cursor = None

	def __init__(self): 
		self.connect()
		return

	def connect(self):
		self.conn = mysql.connector.connect(
			host = "localhost", 
			user = "dbuser",
			password = "password",
			database = "iems5722")
		self.cursor =self.conn.cursor(dictionary =True) 
		return