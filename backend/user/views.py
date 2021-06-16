from django.views import View
from django.http import JsonResponse

from .models import User
from .utils import OauthKakao
from django.forms.models import model_to_dict


SOCIAL_TYPE = ['kakao', 'naver', 'google']


class LoginView(View):
    def post(self, request):
        try:
            token = request.headers.get('Authorization')
            social_type, token = token.split(' ')
            social_type = social_type.lower()

            if not token:
                return JsonResponse({"MESSAGE": "INVALID_TOKEN"}, status=400)

            user_data = {'social_type': SOCIAL_TYPE.index(social_type)}

            if social_type == 'kakao':
                kakao = OauthKakao()
                valid_result = kakao.get_access_token_info(access_token=token)
                if valid_result['code'] == 200:
                    user_info = kakao.get_user_info(access_token=token)
                    user_data['id'] = user_info['id']
                    user_data['nickname'] = user_info['properties']['nickname']
                else:
                    return JsonResponse({'message': valid_result['message']}, status=400)

            elif social_type == 'google':
                pass

            elif social_type == 'naver':
                pass

            else:
                return JsonResponse({'message': 'INVALID_SOCIAL_TYPE'}, status=400)

            # sign in
            user = User.objects.filter(social_type=user_data['social_type'], social_id=user_data['id'])
            if user.exists():
                user = user.values()[0]
                del user['created_at']
                del user['social_id']
                user['social_type'] = SOCIAL_TYPE[user['social_type']]
                return JsonResponse(user, status=200)

            # sign up
            else:
                user = User.objects.create(
                    social_type=user_data['social_type'],
                    social_id=user_data['id'],
                    nickname=user_data['nickname']
                )
                user = model_to_dict(user, fields=[field.name for field in user._meta.fields])
                user['social_type'] = SOCIAL_TYPE[user['social_type']]
                del user['social_id']
                return JsonResponse(user, status=201)
        except ValueError:
            return JsonResponse({'message': 'INVALID_TOKEN'}, status=400)
