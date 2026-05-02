import json
from db import get_connection, ok, created, error
import pymysql

def rooms_handler(event, context):
    method = event['httpMethod']

    if method == 'GET':
        return get_rooms()
    elif method == 'POST':
        return create_room(event)
    else:
        return error(405, 'Unsupported http method!')

def get_rooms():
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute("SELECT * FROM rooms")
            result = cursor.fetchall()
            ''' Datetime needs to be a string for json to read it'''
            for room in result:
                room['created_at'] = str(room['created_at'])
            return ok(result)
    finally:
        conn.close()

def create_room(event):
    payload = json.loads(event['body'])
    name = payload['name']
    if not name:
        return error(400, 'Room name is required')
    with get_connection() as conn:
        with conn.cursor() as cursor:
            cursor.execute("INSERT INTO rooms (name) VALUES (%s)", (name,))
            room_id = cursor.lastrowid
            cursor.execute('INSERT INTO led_state (room_id, is_on, hue, saturation, value) VALUES (%s, 0, 0, 0, 255)', (room_id,))
            cursor.execute('INSERT INTO motion_settings (room_id, motion_sensing_enabled) VALUES (%s, 1)', (room_id,))
            cursor.execute('INSERT INTO occupancy (room_id, people_count) VALUES (%s, 0)', (room_id,))
            conn.commit()
            return created({
                'id': room_id,
                'name': name
            })
