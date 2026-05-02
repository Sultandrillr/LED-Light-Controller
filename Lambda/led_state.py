import json
import os
from db import get_connection, ok, error


def handler(event, context):
    method = event['httpMethod']
    room_id = event['pathParameters']['id']

    if not room_id:
        return error(400, 'Missing room id')
    if method == 'GET':
        return get_led_state(room_id)
    elif method == 'POST':
        return update_led_state(room_id,json.loads(event['body']))
    else:
        return error(405, 'Method not allowed')

def get_led_state(room_id):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute('SELECT is_on, hue, saturation, value FROM led_state WHERE room_id = %s', (room_id,))
            result = cursor.fetchone()
            if not result:
                return error(404, 'Room not found')
            result['room_id'] = int(room_id)
            result['is_on'] = bool(result['is_on'])
            result['hue'] = int(result['hue'])
            result['saturation'] = int(result['saturation'])
            result['value'] = int(result['value'])
            return ok(result)
    finally:
        conn.close()

def update_led_state(room_id, data):
    if 'hue' in data and not(0 <= data['hue'] <= 360):
        return error(400, 'Hue must be between 0 and 360')
    if 'saturation' in data and not(0 <= data['saturation'] <= 255):
        return error(400, 'Saturation must be between 0 and 100')
    if 'value' in data and not(0 <= data['value'] <= 255):
        return error(400, 'Value must be between 0 and 100')
    if 'is_on' in data and not isinstance(data['is_on'], bool):
        return error(400, 'is_on must 1 or 0')
     
    allowed = ['is_on', 'hue', 'saturation', 'value']
    fields = {k: v for k, v in data.items() if k in allowed}
    if not fields:
        return error(400, 'No valid fields')
    
    set_clause = ', '.join([f'{k} = %s' for k in fields.keys()])
    values = list(fields.values()) + [room_id]
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(f'UPDATE led_state SET {set_clause} WHERE room_id = %s', values)
            conn.commit()
            return ok({'message': 'ok'})
    except Exception as e:
        print(e)
        return error(500, 'Internal server error')
    finally:
        conn.close()

    
