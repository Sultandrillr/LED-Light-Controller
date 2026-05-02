import json
from db import get_connection, ok, error

def handler(event, context):
    method = event['httpMethod']
    room_id = event['pathParameters']['id']

    if not room_id:
        return error(400, "Missing room id")
    if method == 'GET':
        return get_occupancy(room_id)
    if method == 'POST':
        return update_occupancy(room_id, json.loads(event['body']))
    else:
        return error(405, "Unsupported method")

def get_occupancy(room_id):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute('SELECT people_count FROM occupancy WHERE room_id = %s', (room_id,))
            result = cursor.fetchone()
            if not result:
                return error(404, 'Room not found')
            result['room_id'] = int(room_id)
            result['people_count'] = int(result['people_count'])
            return ok(result)
    finally:
        conn.close()

def update_occupancy(room_id, data):
    if 'people_count' not in data:
        return error(400, "Missing people_count")
    
    people_count = data['people_count']
    if people_count < 0:
        return error(400, "Number of people can not be negative")

    allowed = ['people_count']
    fields = {k: v for k, v in data.items() if k in allowed}
    if not fields:
        return error(400, "No valid fields to provided")
    
    set_clause = ', '.join([f"{k} = %s" for k in fields.keys()])
    values = list(fields.values()) + [room_id]

    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(f'UPDATE occupancy SET {set_clause}, last_updated = NOW() WHERE room_id = %s', values)
            conn.commit()
            return ok({'message': 'ok'})
    except Exception as e:
        print(e)
        return error(500, 'Internal server error')
    finally:
        conn.close()


   
