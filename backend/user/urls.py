from django.urls import path

from .views import KakaoLoginView

urlpatterns = [
    path('/kakao', KakaoLoginView.as_view()),
]
