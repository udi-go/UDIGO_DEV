import jwt
import requests

from django.views import View
from django.http import JsonResponse, HttpResponse

from .models import User
from server.settings import SECRET_KEY
from server_settings import app_key


class KakaoLoginView(View):
    def post(self, request):
        social_type = request.POST.get('type')
        token = request.POST.get('token')

        if not token:
            return JsonResponse({"MESSAGE": "INVALID_TOKEN"}, status=400)

        if social_type == 'kakao':
            headers = {'Authorization': f"Bearer {token}"}
            url = "https://kapi.kakao.com/v2/user/me"
            response = requests.get(url, headers=headers)
            response = response.json()

            # 토큰 유효성 체크 > 자동 리프레쉬? > -401 check
            # https://developers.kakao.com/docs/latest/ko/reference/rest-api-reference#response-code
            if 'code' in response and response['code'] == -401:
                return JsonResponse({"MESSAGE": "EXPIRED_KAKAO_TOKEN"}, status=401)

            # jwt 토큰 발급
            if User.objects.filter(kakao_id=response['id']).exists():
                user = User.objects.get(kakao_id=response['id'])
            else:
                user = User.objects.create(
                    kakao_id=response['id'],
                    nickname=response['properties']['nickname'],
                    social_type=0
                )
            access_token = jwt.encode({'user_id': user.id}, SECRET_KEY, algorithm='HS256')
            return JsonResponse({'access_token': access_token.decode('utf-8')}, status=200)

        elif social_type == 'google':
            pass
        elif social_type == 'naver':
            pass
        else:
            return JsonResponse({'message': 'INVALID_SOCIAL_TYPE'}, status=400)

