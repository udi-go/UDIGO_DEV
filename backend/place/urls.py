from django.urls import path
from django.conf.urls.static import static

from server import settings
from .views import Classification, PlaceReviewView, UserReviewView

urlpatterns = [
    path('/upload', Classification.as_view()),
    path('/review', UserReviewView.as_view()),
    path('/<int:place_id>/review', PlaceReviewView.as_view())
]
urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)