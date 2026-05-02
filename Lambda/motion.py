import json
from db import get_connection, ok, error

def handler(event, context):
    method  = event['httpMethod']
    room_id = event['pathParameters']['id']
    
    if not room_id:
        return error(400, 'Missing room id')
    if method == 'GET':
        return get_motion_state(room_id)
    elif method == 'POST':
        return update_motion_state(room_id,json.loads(event['body']))
    else:
        return error(405, 'Method not allowed')
    
def get_motion_state(room_id):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute('SELECT motion_sensing_enabled FROM motion_settings WHERE room_id = %s', (room_id,))
            result = cursor.fetchone()
            if not result:
                return error(404, 'Room not found')
            result['room_id'] = int(room_id)
            result['motion_sensing_enabled'] = bool(result['motion_sensing_enabled'])
            return ok(result)
    finally:
        conn.close()

def update_motion_state(room_id, data):
    if 'motion_sensing_enabled' not in data:
        return error(400, 'Missing motion_sensing_enabled field')
    motion_bool = data['motion_sensing_enabled']
    if not isinstance(motion_bool, bool):
        return error(400, 'motion_sensing_enabled must be a boolean')

    allowed = ['motion_sensing_enabled']
    fields = {k: v for k, v in data.items() if k in allowed}
    if not fields:
        return error(400, 'No valid fields provided')

    set_clause = ', '.join([f"{k} = %s" for k in fields.keys()])
    values = list(fields.values()) + [room_id]

    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(f'UPDATE motion_settings SET {set_clause}, last_updated = NOW() WHERE room_id = %s', values)
            conn.commit()
            return ok({'message': 'ok'})
    except Exception as e:
        print(e)
        return error(500, 'Internal server error')
    finally:
        conn.close()