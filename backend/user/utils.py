import jwt

from .models import User
from server.settings import SECRET_KEY

from django.http import JsonResponse


def login_decorator(func):
    def wrapper(self, request, *args, **kwargs):
        try:
            auth_token = request.headers.get('Authorization')
            payload = jwt.decode(auth_token, SECRET_KEY, algorithm='HS256')
            user = User.objects.get(id=payload["user_id"])
            request.user = user
            return func(self, request, *args, **kwargs)
        except User.DoesNotExist:
            return JsonResponse({'message': 'INVALID_USER'}, status=400)
        except TypeError:
            return JsonResponse({'message': 'INVALID_VALUE'}, status=400)
        except KeyError:
            return JsonResponse({'message': 'INVALID_KEY'}, status=400)

    return wrapper