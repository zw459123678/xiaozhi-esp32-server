async def verify_token(config, request):
    if 'token' not in config['manager']:
        return True

    expected_token = config['manager']['token']
    token = request.headers.get('Authorization', '').replace('Bearer ', '')

    if not token or token != expected_token:
        return False
    return True
