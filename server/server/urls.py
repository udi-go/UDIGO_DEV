from django.urls import path, include

urlpatterns = [
    path('place', include('place.urls')),
    path('user', include('user.urls')),
]
